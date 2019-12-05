/* /////////////////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//                                 MIT License
//                            Copyright (c) 2019 VIA, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//
// ////////////////////////////////////////////////////////////////////////////////////////////// */

#include <automotiveControl/AutomotiveController.h>
#include "mobile360/adas-core/utils/mathTool.h"
#include "mobile360/SensingComponent/SensingSamples.h"
#include "car/CarContext.h"
#include "car/CarModel.h"
#include "tools/TimeTag.h"
#include "automotiveControl/LatitudePlanner/LatitudePlanner.h"
#include "automotiveControl/LatitudePlanner/LatitudePlannerTask.h"
#include "automotiveControl/LatitudePlanner/PIDController.h"
#include "automotiveControl/LongitudePlanner/LongitudePlanner.h"

#include <android/log.h>
#define  LOG_TAG    "AutomotiveController"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//-----------------------------------------------------------------------------------------------------------------------------------------------------
using namespace std;
using namespace automotive::longitude;
using namespace via::automotive;
using namespace via::camera;
using namespace via::sensing;
using namespace via::tools;

namespace {
//  Speed [ 30 ~ 60 ]  ---- [1.0 - 0.05  ]
template <class T>
inline T interpolate(T vInMinSpeed, T vInMaxSpeed, T speed, T minSpeed, T maxSpeed) {
    T v;
    if (speed <= minSpeed) {
        v = vInMinSpeed;
    }
    else if (speed >= maxSpeed) {
        v = vInMaxSpeed;
    }
    else {
        v = vInMinSpeed + ((vInMaxSpeed - vInMinSpeed) / (maxSpeed - minSpeed)) * (speed - minSpeed);
    }
    return v;
}

template <class T>
inline T limit(T value, T min, T max) {
    if (value > max) {
        return max;
    }
    else if (value < min) {
        return min;
    }
    else {
        return value;
    }
}


class InternalVehicleStatus {
public:
    InternalVehicleStatus() {
        isDriverOverride = false;
        isSteerControllable = false;
        isSensingOnline = false;
        sensingMode_ = via::sensing::SensingComponent::ComponentMode::Idle;
        sensingPrevMode_ = via::sensing::SensingComponent::ComponentMode::Idle;
    }
    bool isDriverOverride;
    bool isSteerControllable;
    bool isSensingOnline;
    bool isInCalibrationRise;
    bool isInCalibrationFall;
    via::sensing::SensingComponent::ComponentMode sensingMode_;
    via::sensing::SensingComponent::ComponentMode sensingPrevMode_;
};


class AutomotiveSensingUint {
public:
    AutomotiveSensingUint() {
        refSensingComponent = NULL;
    }
    via::sensing::SensingComponent *refSensingComponent;
};


class LateralControlData {
public:
    LateralControlData() {
        accSteerTorque_ = 0;
    }
    float accSteerTorque_;
    via::automotive::latitude::PIDControlInput  pidIn_;
    via::automotive::latitude::PIDControlOutput pidOut_;
};


enum class ControllerStatus : unsigned char {
    CTRL_Idle,
    CTRL_Disabled,
    CTRL_SoftDisabled,
    CTRL_Enabled,
    CTRL_Release,
};

}   // namespace {EMPTY}
//-----------------------------------------------------------------------------------------------------------------------------------------------------
class AutomotiveController::ControllerImpl
{
public:
    ControllerImpl();
    ~ControllerImpl();

    bool init(via::car::CarTypes carType, std::string &cfgPath, std::string &calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset);
    void release();
    const EventSlot runAutoControl();
    void toggleCameraCalibration(via::camera::CameraLocationTypes location, float cameraInstalledHeight, float cameraToCenterOffset, bool enable);
    void pushCommand(std::unique_ptr<ControllerCommand> &cmd);

    void registerCameraModule(via::camera::CameraModule *ref);
    void registerCANbusModule(via::canbus::CANbusModule *ref);
    void registerSensingModule_Lane(via::sensing::SensingComponent *ref);
    void registerSensingModule_ForwardVehicle(via::sensing::SensingComponent *ref);


    void stopRecord();
    bool startRecord(std::string path, bool appendFile);
    bool isRecording();

    via::automotive::LatitudePlan *getLatitudePlan();
    std::string getRecordPath();

private:
    void parseCommand();
    void dataSample();
    void updatePlanners();
    void updateEvent();
    void updateController();
    void sendData();

    bool loadConfigurations(std::string &cfgPath);
    InternalVehicleStatus vehicleStatus;

    ControllerStatus mControllerStatus;
    ControllerStatus getControllerStatus();
    void setControllerStatus(ControllerStatus status);

    // data
    std::string ctlConfigPath;
    std::string calibExportPath;
    float cameraInstalledHeight;    // unit : cm
    float cameraToCenterOffset;     // unit : cm

    // Control counter
    std::mutex controllerMutex;
    std::map<unsigned int, shared_ptr<ControllerCommand>> cmdMap;
    CommandListSwitcher cmdSwitcher_;

    // Car parameter
    via::car::CarContext carContext_;
    via::car::CarModel carModel_;

    // Planner
    via::tools::TimeTag onTheFlyCalibTimeTag_;
    via::automotive::LatitudePlanner latitudePlanner_;
    via::automotive::LatitudePlannerTask latitudePlannerTask;
    via::automotive::LatitudePlan latitudePlan;
    LateralControlData latitudeCtlData_;

    ::automotive::longitude::LongitudePlanner longitudePlanner_;
    ::automotive::longitude::LongitudePlan longitudePlan_;

    // Controller
    via::automotive::latitude::PIDController latitudePIDController_;

    // Camera modules
    via::camera::CameraModule *cameraModule;

    // Module references
    via::canbus::CANbusModule *refCANbusModule;
    AutomotiveSensingUint refSensingUnit_Lane;
    AutomotiveSensingUint refSensingUnit_ForwardVehicle;

    // CANbus parameters (Rx)
    via::canbus::CANHealth canHealth;
    via::canbus::CANParams_Speed canSpeed;
    via::canbus::CANParams_SteeringSensor canSteeringSensor;
    via::canbus::CANParams_SteeringControl canSteeringControl;
    via::canbus::CANParams_SafetyFeature canSafetyFeature;
    via::canbus::CANParams_DriverControllers canDriverControllers;
    via::canbus::CANParams_LKASHud canLKASHud;
    via::canbus::CANParams_ACCHud canACCHud;


    // CANbus parameters (prev Rx)
    via::canbus::CANParams_DriverControllers canPrevDriverControllers;
    via::canbus::CANHealth canPrevHealth;
    via::canbus::CANParams_SafetyFeature canPrevSafetyFeature;

    // CANbus parameters (Tx)
    via::canbus::CANParams_LKASHud txLKASHud;
    via::canbus::CANParams_SteeringControl txSteeringControl;
    via::canbus::CANParams_WheelButtons txWheelButtons;

    // recoder
    via::tools::TimeTag recordTimeTag;
    std::mutex recoderMutex;
    bool b_IsRecording;
    std::string recordPath;
    std::ofstream recordStream;
    void record(via::automotive::latitude::PIDControlInput &pidIn ,
                via::automotive::latitude::PIDControlOutput &pidOut,
                via::canbus::CANParams_SteeringSensor &canSteeringSensor,
                float outSteerTorqueValue);

    // event
    via::tools::TimeTag blinkerTimeTag_L;
    via::tools::TimeTag blinkerTimeTag_R;
    std::vector<EventTypes> eventList[(int)EventLevels::LevelCount];
    EventSlot lastEvent;

    void clearEvents() {
        for(int Ei = 0 ; Ei < (int)EventLevels::LevelCount ; Ei++) {
            eventList[Ei].clear();
        }
    }

    void addEvent(EventTypes events) {
        switch(events) {
            case EventTypes::SystemDisable:
            case EventTypes::SystemDisable_Gas:
            case EventTypes::SystemDisable_Brake:
            case EventTypes::SystemDisable_InvalidCalibration:
                eventList[(int)EventLevels::Level_0].push_back(events);
                break;

                // level 1
            case EventTypes::LaneDeparture:
                eventList[(int)EventLevels::Level_1].push_back(events);
                break;

                // level 2
            case EventTypes::LaneDetectFail:
            case EventTypes::CurvatureOverControl:
                eventList[(int)EventLevels::Level_2].push_back(events);
                break;

                // level 3
            case EventTypes::SystemEnable:
            case EventTypes::CalibrationStart:
            case EventTypes::CalibrationFail:
            case EventTypes::CalibrationSuccess:
                eventList[(int)EventLevels::Level_3].push_back(events);
                break;

            case EventTypes::DriverClickAccelBtn:
            case EventTypes::DriverClickDecelBtn:
            case EventTypes::InvalidManualSpeed:
            case EventTypes::SystemNoEnable:
                eventList[(int)EventLevels::Level_3].push_back(events);
                break;
            default:
                LOGE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                LOGE("Event not handled ..... %d", (int)events);
                LOGE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;
        }
    }
};

//-----------------------------------------------------------------------------------------------------------------------------------------------------
//about AutomotiveSensingUint
AutomotiveController::ControllerImpl::ControllerImpl()
{
    setControllerStatus(ControllerStatus::CTRL_Idle);

    cameraInstalledHeight = 150;    // unit : cm
    cameraToCenterOffset = 0;       // unit : cm

    // Camera modules
    cameraModule = NULL;

    // Module references
    refCANbusModule = NULL;

    b_IsRecording = false;
    recordPath ="";
}

AutomotiveController::ControllerImpl::~ControllerImpl()
{
    setControllerStatus(ControllerStatus::CTRL_Idle);
}

bool AutomotiveController::ControllerImpl::loadConfigurations(std::string &cfgPath)
{
    bool ret = true;
    ret &= carContext_.load(cfgPath);
    ret &= carModel_.load(cfgPath);
    ret &= latitudePlanner_.load(cfgPath);
    ret &= latitudePIDController_.load(cfgPath);
    ret &= longitudePlanner_.load(cfgPath);

    return ret;
}

bool AutomotiveController::ControllerImpl::init(via::car::CarTypes carType, std::string &cfgPath, std::string &calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset)
{
    bool ret = false;
    this->carContext_.setCarType(carType);
    this->carModel_.init(carContext_);
    this->cameraInstalledHeight = cameraInstalledHeight;
    this->cameraToCenterOffset = cameraToCenterOffset;
    this->calibExportPath = calibExportPath;

    LOGI("set control config path : %s", ctlConfigPath.c_str());
    ctlConfigPath = cfgPath;
    ret = loadConfigurations(ctlConfigPath);

    return ret;
}

void AutomotiveController::ControllerImpl::release()
{
    latitudePlannerTask.release();
}

bool AutomotiveController::ControllerImpl::isRecording() {
    return b_IsRecording;
}

std::string AutomotiveController::ControllerImpl::getRecordPath() {
    return recordPath;
}

void AutomotiveController::ControllerImpl::stopRecord() {
    std::lock_guard<std::mutex> lock(recoderMutex);
    if(recordStream.is_open()) {
        recordStream.close();
        LOGE("close recordStream");
    }
    if(latitudePlanner_.isRecording()) latitudePlanner_.stopRecord();
    b_IsRecording = false;
}

bool AutomotiveController::ControllerImpl::startRecord(std::string path, bool appendFile) {

    do {
        latitudePlanner_.startRecord(path + ".plan", appendFile);
    } while(false);

    do {

        std::lock_guard<std::mutex> lock(recoderMutex);
        recordPath = path;

        if(appendFile) {
            recordStream.open(recordPath, std::ios::out | std::ios::app);
        }
        else {
            recordStream.open(recordPath, std::ios::out | std::ios::trunc);
        }

        recordTimeTag.updateNow();
        b_IsRecording = recordStream.is_open();

        LOGE("recordPath %s .... %d", recordPath.c_str(), b_IsRecording);
    } while(false);

    return b_IsRecording;
}


void AutomotiveController::ControllerImpl::record(via::automotive::latitude::PIDControlInput &pidIn ,
                                  via::automotive::latitude::PIDControlOutput &pidOut,
                                  via::canbus::CANParams_SteeringSensor &canSteeringSensor,
                                  float outSteerTorqueValue)
{
    std::lock_guard<std::mutex> lock(recoderMutex);
    if(recordStream.is_open()) {
        double timeDiff = recordTimeTag.diffNow() * 0.001;
        std::stringstream value;
        value << timeDiff << ","
              << pidIn.carSpeed << ","
              << pidIn.curSteerAngle << ","
              << pidIn.planDesiredSteerAngle << ","
              << pidIn.steerControllable << ","
              << pidOut.ctlSteerValue << ","
              << pidOut.steeringTorque << ","
              << outSteerTorqueValue << ","
              << pidOut.tErr << ","
              << pidOut.tP << ","
              << pidOut.tI << ","
              << pidOut.tD << ","
              << pidOut.tF << ","
              << canSteeringSensor.steerTorque
              << endl;
        recordStream.write(value.str().c_str(), strlen(value.str().c_str()));
        static int CC = 0;
        if(CC % 100 ==0) {
            recordStream.flush();
        }
        CC++;
    }
}

void AutomotiveController::ControllerImpl::registerCameraModule(via::camera::CameraModule *ref)
{
    stringstream ss;
    if(ref != NULL) {
        switch(ref->getCameraLocation()) {
            case CameraLocationTypes::Location_Front:
                cameraModule = ref;
                break;
            default:
                ss << "Unsupported camera location " << (int) ref->getCameraLocation() << endl;
                throw std::runtime_error(ss.str());
                break;
        }
    }
}

void AutomotiveController::ControllerImpl::registerCANbusModule(via::canbus::CANbusModule *ref)
{
    refCANbusModule = ref;
}

void AutomotiveController::ControllerImpl::registerSensingModule_Lane(via::sensing::SensingComponent *ref)
{
    refSensingUnit_Lane.refSensingComponent = ref;
}

void AutomotiveController::ControllerImpl::registerSensingModule_ForwardVehicle(via::sensing::SensingComponent *ref)
{
    refSensingUnit_ForwardVehicle.refSensingComponent = ref;
}

ControllerStatus AutomotiveController::ControllerImpl::getControllerStatus()
{
    return mControllerStatus;
}

void AutomotiveController::ControllerImpl::setControllerStatus(ControllerStatus status)
{
    mControllerStatus = status;
}


void AutomotiveController::ControllerImpl::parseCommand()
{
    CommandList *cmdList = cmdSwitcher_.getProcList();
    cmdMap.clear();

    for (size_t ci = 0; cmdList != NULL && ci < cmdList->size(); ci++) {
        shared_ptr<ControllerCommand> cmd = cmdList->get(ci);

        if (cmd != nullptr) {
            // keep command into map.
            if(cmdMap.count((unsigned int)cmd->getType())) {
                cmdMap.erase((unsigned int)cmd->getType());
            }
            cmdMap.insert(pair<unsigned int, shared_ptr<ControllerCommand>>((unsigned int)cmd->getType(), cmd));

            ControllerCommand *pcmd = cmd.get();
            LOGI("ctl get command %d", (int)pcmd->getType());
            switch (pcmd->getType()) {
                case ControllerCommand::Types::SetCruiseSpeed: {
                    if(!vehicleStatus.isSensingOnline || vehicleStatus.sensingMode_ != SensingComponent::ComponentMode::Sensing || !longitudePlanner_.isInit()) {
                        pcmd->setStatus((u_int32_t)SetCruiseSpeedCommand::Codes::SYSTEM_NOT_INIT, "system not initialize");
                    }
                    else if (!canSafetyFeature.isEnabled_ACC) {
                        addEvent(EventTypes::SystemNoEnable);
                        pcmd->setStatus((u_int32_t)SetCruiseSpeedCommand::Codes::ACC_NOT_ENABLE, "acc not enable");
                    }
                    else {
                        shared_ptr<SetCruiseSpeedCommand> cmd_SetCruiseSpeed = std::dynamic_pointer_cast<SetCruiseSpeedCommand>(cmd);
                        if (cmd_SetCruiseSpeed->getSpeed() < 30) {
                            addEvent(EventTypes::InvalidManualSpeed);
                            pcmd->setStatus((u_int32_t)SetCruiseSpeedCommand::Codes::SPEED_UNDER_CONTROL, "speed under control.");
                            cmd_SetCruiseSpeed->setSpeed(30);
                        }
                        else if (cmd_SetCruiseSpeed->getSpeed() > 140) {
                            addEvent(EventTypes::InvalidManualSpeed);
                            pcmd->setStatus((u_int32_t)SetCruiseSpeedCommand::Codes::SPEED_OVER_CONTROL, "speed over control.");
                            cmd_SetCruiseSpeed->setSpeed(140);
                        }
                        else {
                            pcmd->setStatus((u_int32_t)SetCruiseSpeedCommand::Codes::SUCCESS, "command success");
                        }
                    }
                } break;

                case ControllerCommand::Types::StopRecord: {
                    this->stopRecord();
                    pcmd->setStatus((u_int32_t)StopRecordCommand::Codes::SUCCESS, "command success");
                } break;

                case ControllerCommand::Types::StartRecord: {
                    string path = ((StartRecordCommand *)pcmd)->getPath();
                    bool append = ((StartRecordCommand *)pcmd)->isAppend();
                    if(this->startRecord(path, append) == true) {
                        pcmd->setStatus((u_int32_t)StartRecordCommand::Codes::SUCCESS, "create record file in apth " + path);
                    }
                    else {
                        pcmd->setStatus((u_int32_t)StartRecordCommand::Codes::FAIL, "fail to record data in path " + path);
                    }
                } break;

                case ControllerCommand::Types::StartCameraExtrinsicCalibration: {
                    shared_ptr<StartCameraExtrinsicCalibrationCommand> calib = std::dynamic_pointer_cast<StartCameraExtrinsicCalibrationCommand>(cmd);
                    this->toggleCameraCalibration(calib->getLocation(), calib->getInstallHeight(), calib->getCameraToCenterOffset(), true);
                } break;

                default:
                    break;
            }
        }
    }
}

void AutomotiveController::ControllerImpl::dataSample()
{
    // get last CANBus data sample
    if (refCANbusModule != NULL) {
        refCANbusModule->Rx_CANHealth(canHealth);
        refCANbusModule->Rx_SpeedParams(canSpeed);
        refCANbusModule->Rx_SteeringSensorParams(canSteeringSensor);
        refCANbusModule->Rx_SteeringControlParams(canSteeringControl);
        refCANbusModule->Rx_SafetyFeature(canSafetyFeature);
        refCANbusModule->Rx_DriverControllers(canDriverControllers);
        refCANbusModule->Rx_ACC_HUD(canACCHud);
        canSpeed.roughSpeed = 90;
    }

    // Update vehicle status
    vehicleStatus.isDriverOverride = (abs(canSteeringSensor.steerTorque) > 800);
    vehicleStatus.isSteerControllable = (!vehicleStatus.isDriverOverride && canHealth.controlsAllowed &&
                                         canSafetyFeature.isEnabled_LKS && (canSteeringSensor.steerStatus == 0));
    if(canSafetyFeature.isGasPressed || canSafetyFeature.isBrakePressed) vehicleStatus.isSteerControllable = false;
    if(canSteeringSensor.steerAngle > 30 || canSteeringSensor.steerAngle < -30) vehicleStatus.isSteerControllable = false;
    if(vehicleStatus.isDriverOverride) vehicleStatus.isSteerControllable = false;

    vehicleStatus.isSensingOnline = (cameraModule != NULL && refSensingUnit_Lane.refSensingComponent != NULL);
    vehicleStatus.sensingPrevMode_ = vehicleStatus.sensingMode_;
    if(vehicleStatus.isSensingOnline) {
        vehicleStatus.sensingMode_ = refSensingUnit_Lane.refSensingComponent->getComponentMode();
    }
    else {
        vehicleStatus.sensingMode_ = via::sensing::SensingComponent::ComponentMode::Idle;
    }


    // check steer-wheel enable calibration or not
    do {
        static int clickCount = 0;
        static double prevClickTime = 0;
        double curTime = getms();
        bool isLKSBtnClick = (canDriverControllers.lksBtnOn_  && (canDriverControllers.lksBtnOn_ != canPrevDriverControllers.lksBtnOn_));
        if (isLKSBtnClick && (curTime - prevClickTime) > 100) {
            prevClickTime = curTime;
            clickCount++;
        }
        if((curTime - prevClickTime) > 3000) {
            clickCount = 0;
        }
        if(clickCount >= 2) {
            clickCount = 0;
            std::unique_ptr<ControllerCommand> cmd (new StartCameraExtrinsicCalibrationCommand(via::camera::CameraLocationTypes::Location_Front, this->cameraInstalledHeight, this->cameraToCenterOffset));
            this->pushCommand(cmd);
        }

    } while(false);

    vehicleStatus.isInCalibrationRise = (vehicleStatus.sensingPrevMode_ != SensingComponent::ComponentMode::OnTheFlyCalibration && vehicleStatus.sensingMode_ == SensingComponent::ComponentMode::OnTheFlyCalibration);
    vehicleStatus.isInCalibrationFall = (vehicleStatus.sensingPrevMode_ == SensingComponent::ComponentMode::OnTheFlyCalibration && vehicleStatus.sensingMode_ != SensingComponent::ComponentMode::OnTheFlyCalibration);
}

void AutomotiveController::ControllerImpl::updatePlanners()
{
    // 2.1. Do planner (Check sensing unit calibration status)
    do {
        // Is system calibration finish ?
        if(vehicleStatus.isSensingOnline && vehicleStatus.isInCalibrationFall) {
            OnTheFlyCalibrationSample *caliSample = (OnTheFlyCalibrationSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Calibration);
            if(caliSample->mStatus == OnTheFlyCalibrationSample::CalibrationStatus::Success) {
                caliSample->mCalibratedCamera.copyTo(*cameraModule);
                refSensingUnit_Lane.refSensingComponent->exportConfig(this->calibExportPath );
                LOGE("Calibration finish ...... export into %s", this->calibExportPath.c_str());
                // update new data to planner
                latitudePlanner_.setCameraModule(cameraModule);
            }
        }

        // Update on-the-fly calibration every 30s
        if (vehicleStatus.isSensingOnline && vehicleStatus.sensingMode_ == SensingComponent::ComponentMode::Sensing) {
            refSensingUnit_Lane.refSensingComponent->toggleOnTheFlyCalibration(true);
            OnTheFlyCalibrationSample *caliSample = (OnTheFlyCalibrationSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Calibration);

            if(latitudePlanner_.isInit() && !caliSample->mCalibratedCamera.isIntrinsicEmpty() && !caliSample->mCalibratedCamera.isExtrinsicEmpty()) {
                // update new data to planner
                if(onTheFlyCalibTimeTag_.diffNow() > (20 * 1000)) {
                    refSensingUnit_Lane.refSensingComponent->lockComponent();

                    if(caliSample->mStatus == OnTheFlyCalibrationSample::CalibrationStatus::Success) {
                        caliSample->mCalibratedCamera.copyTo(*cameraModule);
                        latitudePlanner_.setCameraModule(cameraModule);
                    }
                    refSensingUnit_Lane.refSensingComponent->unlockComponent();
                    onTheFlyCalibTimeTag_.updateNow();
                }
            }
        }
    } while(false);


    // 2.3. Do planner ( latitude control planner )
    do {
        if(vehicleStatus.isSensingOnline && vehicleStatus.sensingMode_ == SensingComponent::ComponentMode::Sensing) {
            if (latitudePlanner_.isInit() == false && cameraModule != NULL) {
                latitudePlanner_.init(&carContext_, cameraModule);
            }

            if(latitudePlannerTask.isInit() == false) {
                latitudePlannerTask.init(&latitudePlanner_, &carModel_);
                latitudePlannerTask.active();
            }

            // update plane
            via::sensing::LaneSample *laneSample = (via::sensing::LaneSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Lane);
            if (latitudePlannerTask.isInit()) {
                latitudePlannerTask.update(&canSpeed, &canSteeringSensor, laneSample);
                latitudePlannerTask.getLastResult(&latitudePlan);
                latitudePlan.isSteerControllable = vehicleStatus.isSteerControllable;
            }
        }
        else {
            latitudePlan.isPlanValid = false;
        }

    } while(false);


    // 2.4. Do planner ( longitude control planner )
    do {
        //longitudePlan_.reset();
        if(vehicleStatus.isSensingOnline && vehicleStatus.sensingMode_ == SensingComponent::ComponentMode::Sensing) {
            if (longitudePlanner_.isInit() == false && cameraModule != NULL) {
                longitudePlanner_.init(&carContext_, cameraModule);
            }

            // update plane
            if (longitudePlanner_.isInit()) {
                shared_ptr<SetCruiseSpeedCommand> cmd_SetCruiseSpeed;
                map<unsigned int, shared_ptr<ControllerCommand>>::iterator iter = cmdMap.find((unsigned int)ControllerCommand::Types::SetCruiseSpeed);
                if(iter != cmdMap.end()) {
                    cmd_SetCruiseSpeed = std::dynamic_pointer_cast<SetCruiseSpeedCommand>(iter->second);
                }

                LongitudePlanner::Input input;
                input.canSpeed_ = &canSpeed;
                input.canSteeringSensor_ = &canSteeringSensor;
                input.canSafetyFeature_ = &canSafetyFeature;
                input.canACCHud = &canACCHud;
                if (cmd_SetCruiseSpeed != nullptr) {
                    input.driverConfigSpeed_ = cmd_SetCruiseSpeed->getSpeed();
                }
                longitudePlanner_.update(input, longitudePlan_);
//                LOGE("ABC  input.driverConfigSpeed_ %f  ..... %d   ..... map size %d ...... plane %f",
//                     input.driverConfigSpeed_,
//                     cmd_SetCruiseSpeed.use_count(),
//                     cmdMap.size(),
//                     longitudePlan_.desiredSpeed_);
            }
        }
        else {
            longitudePlan_.isValid_ = false;
        }
    } while(false);

}

void AutomotiveController::ControllerImpl::updateEvent()
{
    double curTime = via::tools::getms();

    // calibration event
    if(vehicleStatus.isSensingOnline) {
        // calibration finish or fail
        if(vehicleStatus.isInCalibrationFall) {
            OnTheFlyCalibrationSample *caliSample = (OnTheFlyCalibrationSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Calibration);
            if(caliSample->mStatus == OnTheFlyCalibrationSample::CalibrationStatus::Success) {
                addEvent(EventTypes::CalibrationSuccess);
            }
            else {
                addEvent(EventTypes::CalibrationFail);
            }
        }
        else if(vehicleStatus.isInCalibrationRise) {
            addEvent(EventTypes::CalibrationStart);
        }
    }

    // ldws events
    if(vehicleStatus.isSensingOnline && vehicleStatus.sensingMode_ == SensingComponent::ComponentMode::Sensing) {
        via::sensing::LaneSample *laneSample = (via::sensing::LaneSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Lane);
        double radius = laneSample->mLaneModelCtx.curvature;
        if(radius < 1e-6) {
            radius = 1000.0;
        }
        else {
            radius = 1.0 / radius;
        }
        if(canSafetyFeature.isEnabled_LKS && radius < 100.0f) {
            addEvent(EventTypes::CurvatureOverControl);
        }

        const int enableSpeed_LKS = 22;
        const int enableSpeed_LDW = 45;
        if(canDriverControllers.rightBlinkerOn || canDriverControllers.leftBlinkerOn) {
            blinkerTimeTag_L.updateNow();
            blinkerTimeTag_R.updateNow();
        }

        bool warn_LKS = canSafetyFeature.isEnabled_LKS && canSpeed.roughSpeed > enableSpeed_LKS;
        bool warn_LDW = canSpeed.roughSpeed > enableSpeed_LDW;
        switch(laneSample->mLaneStatus) {
            case LaneSample::SampleStatus::NoDetected:
            case LaneSample::SampleStatus::Unknown:
                if(warn_LKS) {
                    addEvent(EventTypes::LaneDetectFail);
                }
                break;
            case LaneSample::SampleStatus::LeftDeparture:
                if(warn_LKS || warn_LDW) {
                    if(blinkerTimeTag_L.diffNow() > 8000) {
                        addEvent(EventTypes::LaneDeparture);
                    }
                }
                break;
            case LaneSample::SampleStatus::RightDeparture:
                if(warn_LKS || warn_LDW) {
                    if(blinkerTimeTag_R.diffNow() > 8000) {
                        addEvent(EventTypes::LaneDeparture);
                    }
                }
                break;
            default:
                break;
        }
    }

    // driver control options
    if (canSafetyFeature.isEnabled_ACC == true && canHealth.controlsAllowed == 1 && cameraModule != NULL) {
        if (canDriverControllers.accelBtnOn_ != canPrevDriverControllers.accelBtnOn_) {
            if(canDriverControllers.accelBtnOn_ == true) addEvent(EventTypes::DriverClickAccelBtn);
        }
        else if (canDriverControllers.decelBtnOn_ != canPrevDriverControllers.decelBtnOn_) {
            if(canDriverControllers.decelBtnOn_ == true) addEvent(EventTypes::DriverClickDecelBtn);
        }
    }

    // control system trigger
    if (canPrevHealth.controlsAllowed != canHealth.controlsAllowed) {
        if (canHealth.controlsAllowed == 1) {   // start control
            if(cameraModule != NULL && !cameraModule->isExtrinsicStable()) {
                addEvent(EventTypes::SystemDisable_InvalidCalibration);
            }
            else {
                addEvent(EventTypes::SystemEnable);
            }
        }
        else {  // stop control
            if(canSafetyFeature.isGasPressed) {
                addEvent(EventTypes::SystemDisable_Gas);
            }
            else if(canSafetyFeature.isBrakePressed) {
                addEvent(EventTypes::SystemDisable_Brake);
            }
            else {
                addEvent(EventTypes::SystemDisable);
            }
        }
    }


    // Refresh events
    EventSlot curEvent;
    for(int Ei = 0 ; Ei < (int)EventLevels::LevelCount ; Ei++) {
        if(eventList[Ei].size() > 0) {
            for(int li = 0 ; li < eventList[Ei].size() ; li++) {
                curEvent.type = eventList[Ei][li];
                curEvent.time = via::tools::getms();
                curEvent.level = (EventLevels)Ei;
            }
        }
    }
    //LOGE("curEvent %d , lastEvent %d !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ", (int)curEvent.type, (int)lastEvent.type);

    // update new event
    if(curEvent.level >= lastEvent.level) {
        lastEvent = curEvent;
    }
    else {
        double timeDiff = via::tools::getms() - lastEvent.time;
        if (timeDiff > 100.0) {
            lastEvent = curEvent;
        }
    }
    //LOGE("lastEvent %d !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ", (int)lastEvent.type);

    // Clear events
    clearEvents();
}

void AutomotiveController::ControllerImpl::updateController()
{
    // CAN : LKAS HUD
    do {
        via::sensing::LaneSample *sample = (via::sensing::LaneSample *)refSensingUnit_Lane.refSensingComponent->getSample(SampleTypes::Lane);
        if (vehicleStatus.isSensingOnline && sample->mLaneStatus != via::sensing::LaneSample::SampleStatus::NoDetected && sample->mLaneStatus != via::sensing::LaneSample::SampleStatus::Unknown) {
            txLKASHud.isLaneDetected = 1;
        } else {
            txLKASHud.isLaneDetected = 0;
        }

        switch(lastEvent.type) {
            case EventTypes::SystemDisable:
            case EventTypes::SystemDisable_Gas:
            case EventTypes::SystemDisable_Brake:
                txLKASHud.steering_requeired = true;
                txLKASHud.beep = 2;
                break;
            case EventTypes::SystemDisable_InvalidCalibration:
                txLKASHud.steering_requeired = true;
                txLKASHud.beep = 1;
                break;
            case EventTypes::LaneDeparture:
            case EventTypes::CurvatureOverControl:
                txLKASHud.steering_requeired = true;
                txLKASHud.beep = 1;
                break;
            case EventTypes::LaneDetectFail:
                txLKASHud.steering_requeired = true;
                txLKASHud.beep = 2;
            default:
                txLKASHud.steering_requeired = false;
                txLKASHud.beep = 0;
                break;

        }
    } while(false);


    // CAN : Steering control  (use PID controller to control the angle of steering wheel)
    do {
        const float STEER_WHEEL_CONTROL_ANGLE_LIMIT = 22.0f;
        const float STEER_TORQUE_LIMIT = 4096.0f;

        // Do PID control
        latitudeCtlData_.pidIn_.carSpeed                      = canSpeed.roughSpeed;
        latitudeCtlData_.pidIn_.curSteerAngle                 = canSteeringSensor.steerAngle;
        latitudeCtlData_.pidIn_.planDesiredSteerAngle         = latitudePlan.planDesiredSteerAngle;
        latitudeCtlData_.pidIn_.steerControllable             = vehicleStatus.isSteerControllable;
        latitudeCtlData_.pidIn_.steerTorqueLimit              = STEER_TORQUE_LIMIT;
        latitudeCtlData_.pidIn_.steerWhellControlAngleLimit   = STEER_WHEEL_CONTROL_ANGLE_LIMIT;
        latitudePIDController_.update(latitudeCtlData_.pidIn_, latitudeCtlData_.pidOut_);
        latitudeCtlData_.pidOut_.steeringTorque = limit<float>(latitudeCtlData_.pidOut_.steeringTorque, -STEER_TORQUE_LIMIT, STEER_TORQUE_LIMIT);

        // guard control torque
        if(vehicleStatus.isDriverOverride)
            latitudeCtlData_.accSteerTorque_ = latitudeCtlData_.accSteerTorque_ + latitudeCtlData_.pidOut_.actuatorFactor * (canSteeringSensor.steerTorque - latitudeCtlData_.accSteerTorque_);
        else
            latitudeCtlData_.accSteerTorque_ = latitudeCtlData_.accSteerTorque_ + latitudeCtlData_.pidOut_.actuatorFactor * (latitudeCtlData_.pidOut_.steeringTorque - latitudeCtlData_.accSteerTorque_);

        if(vehicleStatus.isSteerControllable == false)  latitudeCtlData_.accSteerTorque_ = 0;
        latitudeCtlData_.accSteerTorque_ = limit<float>(latitudeCtlData_.accSteerTorque_, -STEER_TORQUE_LIMIT, STEER_TORQUE_LIMIT);

        // is desired steer over control ?
        if (latitudePlan.planDesiredSteerAngle > STEER_WHEEL_CONTROL_ANGLE_LIMIT) {
            latitudePlan.isSteerOverControl = true;
            txSteeringControl.steerTorqueRequest = 0;
            txSteeringControl.steerTorque = 0;
        }
        else {
            if(vehicleStatus.isSteerControllable == false) {
                latitudePlan.isSteerOverControl = false;
                txSteeringControl.steerTorqueRequest = 0;
                txSteeringControl.steerTorque = 0;
            }
            else {
                latitudePlan.isSteerOverControl = false;
                txSteeringControl.steerTorqueRequest = 1;
                txSteeringControl.steerTorque = (long) latitudeCtlData_.accSteerTorque_;
            }
        }
    } while(false);

    // CAN :  buttons (TODO : for longitude control now, need replace to gas/brake in feature)
    txWheelButtons.reset();
    if(longitudePlan_.isControllable_ && longitudePlan_.isValid_) {
        if(longitudePlan_.accelType_ == 1.0f) {
            txWheelButtons.accumlate_ = true;
        }
        else if(longitudePlan_.accelType_ == -1.0f) {
            txWheelButtons.decelerate_ = true;
        }
    }
}

void AutomotiveController::ControllerImpl::sendData()
{
    if(refCANbusModule != NULL) {
        refCANbusModule->Tx_SteeringControl(txSteeringControl);
        refCANbusModule->Tx_LKAS_HUD(txLKASHud);
        refCANbusModule->Tx_WheelButtons(txWheelButtons);
    }

}

const EventSlot AutomotiveController::ControllerImpl::runAutoControl()
{
    lock_guard<mutex> mLock(controllerMutex);

    do {
        static int CC = 0;
        if(CC == 300) {
            loadConfigurations(ctlConfigPath);
            CC = 0;
        }
        CC++;
    } while(false);


    // 1. parse command
    parseCommand();

    // 2. update data from can. (fake recv from CANBus module)
    dataSample();

    // 3. update planners to calculate lateral/longitudinal control parameters
    updatePlanners();

    // 4. update events
    updateEvent();

    // 5. update controllers
    updateController();

    // 6. send data to CANbus    (fake send to CANBus module)
    sendData();

    // keep current data
    canHealth.copyTo(canPrevHealth);
    canSafetyFeature.copyTo(canPrevSafetyFeature);
    canDriverControllers.copyTo(canPrevDriverControllers);

    // swap command  list
    cmdSwitcher_.lock();
    cmdSwitcher_.swap();
    cmdSwitcher_.unlock();

    // record data
    if(b_IsRecording) {
        record(latitudeCtlData_.pidIn_, latitudeCtlData_.pidOut_, canSteeringSensor, latitudeCtlData_.accSteerTorque_);
    }

    return lastEvent;
}

void AutomotiveController::ControllerImpl::toggleCameraCalibration(via::camera::CameraLocationTypes location, float cameraInstalledHeight, float cameraToCenterOffset, bool enable)
{
    switch(location) {
        case CameraLocationTypes::Location_Front:
            if(enable) {
                if(refSensingUnit_Lane.refSensingComponent->getComponentMode() == SensingComponent::ComponentMode::Sensing) {
                    refSensingUnit_Lane.refSensingComponent->doAutoCalibration(cameraInstalledHeight, cameraToCenterOffset);
                }
                else {
                    LOGE("Sensing component is in calibration mode now ...");
                }
            }
            else {
                // TODO : Cancel calibration
            }
            break;
        default:
            break;
    }
}

void AutomotiveController::ControllerImpl::pushCommand(std::unique_ptr<ControllerCommand> &cmd)
{
    cmdSwitcher_.lock();
    cmdSwitcher_.getRepoList()->push_and_move(cmd, getms());
    cmdSwitcher_.unlock();
}

LatitudePlan *AutomotiveController::ControllerImpl::getLatitudePlan()
{
    return &latitudePlan;
}

//-----------------------------------------------------------------------------------------------------------------------------------------------------
AutomotiveController::AutomotiveController()
{
    pimpl_ = std::move(std::unique_ptr<AutomotiveController::ControllerImpl> (new AutomotiveController::ControllerImpl()));
}

AutomotiveController::~AutomotiveController()
{

}

bool AutomotiveController::init(via::car::CarTypes carType, std::string &cfgPath, std::string &calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset)
{
    return pimpl_->init(carType, cfgPath, calibExportPath, cameraInstalledHeight, cameraToCenterOffset);
}
void AutomotiveController::release()
{
    pimpl_->release();
}

const EventSlot AutomotiveController::runAutoControl()
{
    return pimpl_->runAutoControl();
}

void AutomotiveController::pushCommand(std::unique_ptr<ControllerCommand> &cmd)
{
    pimpl_->pushCommand(cmd);
}

void AutomotiveController::registerCameraModule(via::camera::CameraModule *ref)
{
    pimpl_->registerCameraModule(ref);
}

void AutomotiveController::registerCANbusModule(via::canbus::CANbusModule *ref)
{
    pimpl_->registerCANbusModule(ref);
}

void AutomotiveController::registerSensingModule_Lane(via::sensing::SensingComponent *ref)
{
    pimpl_->registerSensingModule_Lane(ref);
}

void AutomotiveController::registerSensingModule_ForwardVehicle(via::sensing::SensingComponent *ref)
{
    pimpl_->registerSensingModule_ForwardVehicle(ref);
}

bool AutomotiveController::isRecording()
{
    return pimpl_->isRecording();
}

via::automotive::LatitudePlan *AutomotiveController::getLatitudePlan()
{
    return pimpl_->getLatitudePlan();
}

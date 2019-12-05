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

#include <iostream>
#include <sstream>
#include <unistd.h>
#include <string.h>
#include <sched.h>
#include <sys/time.h>
#include <vBus/CANbusModule.h>
#include <vBus/CANDongles.h>
#include "CANbusDefines.h"
#include <iomanip>

#include <android/log.h>
#define  LOG_TAG    "CANbusModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static double getms()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    double ms = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000 ;
    return ms;
}

//----------------------------------------------------------------------------------------
using namespace std;
using namespace via::canbus;
using namespace via::car;
//----------------------------------------------------------------------------------------
// double the FIFO size/
#define RECV_SIZE (0x1000)
#define TIMEOUT 0
//----------------------------------------------------------------------------------------

static int set_realtime_priority(int level) {
    // should match python using chrt
    struct sched_param sa;
    memset(&sa, 0, sizeof(sa));
    sa.sched_priority = level;
    return sched_setscheduler(getpid(), SCHED_FIFO, &sa);
}

static void *thread_CANSend(void *param)
{
    LOGI("Start thread [ CAN Send ]\n");
    CANbusModule *module = (CANbusModule *)param;

    set_realtime_priority(3);
    // run at 100hz
    while (!module->isExit()) {
        if(module->isDongleConnected() == true) {
            //LOGE("[ CAN Recv ] .... doDongleCANSend\n");
            module->doDongleCANSend(module);
        }
        else {
            LOGE("[ CAN Send ] .... dongle disconnected\n");
            usleep(1000 * 1000);
        }
    }
    LOGI("Exit thread [ CAN Send ]\n");
    return NULL;
}

static void *thread_CANRecv(void *param)
{
    LOGI("Start thread [ CAN Recv ]\n");
    CANbusModule *module = (CANbusModule *)param;

    set_realtime_priority(3);
    // run at 200 hz
    while (!module->isExit()) {
        if(module->isDongleConnected() == true) {
            //LOGE("[ CAN Recv ] .... doDongleCANRecv\n");
            module->doDongleCANRecv(module);
        }
        else {
            LOGE("[ CAN Recv ] .... dongle disconnected\n");
            usleep(1000 * 1000);
        }
    }
    LOGI("Exit thread [ CAN Recv ]\n");
    return NULL;
}

static void *thread_CANHealth(void *param)
{
    CANbusModule *module = (CANbusModule *)param;

    int sch = set_realtime_priority(3);
    LOGI("sched after %s\n", strerror(errno));

    // run at 1hz
    while (!module->isExit()) {
        if(module->isDongleConnected() == true) {
            module->doDongleCANHealth(module);
        }
        else {
            LOGE("[ CAN Health ] .... dongle disconnected ..... %d\n", sch);
            usleep(1000 * 1000);
        }
    }
    LOGI("Exit thread [ CAN Health ]\n");
    return NULL;
}

//----------------------------------------------------------------------------------------
CANbusModule::CANbusModule()
{
    flagExit = false;
    pCANDongle = nullptr;
    carType = CarTypes::Unknown;
    frameId_Tx = 0;
    threadHandle_CANSend = 0;
    threadHandle_CANRecv = 0;
    threadHandle_CANHealth = 0;
    b_IsRecording = false;
    recordPath = "";
    recordStartTime = 0;
    gpsLastUpdateTime = 0;
    gpsRefreshTime = 0;

    // reset Tx data
    this->canCtx_Tx.canSteeringControlParams.reset();

    this->canCtx_Tx.canLKASHUDParams.reset();

    this->canCtx_Tx.canACCHudParams.reset();

    // reset Rx data
    this->canCtx_Rx.canSpeedParams.reset();

    this->canCtx_Rx.canSteeringSensorParams.reset();

    this->canCtx_Rx.canSteeringControlParams.reset();

    this->canCtx_Rx.canLKASHUDParams.reset();

    this->canCtx_Rx.canACCHudParams.reset();

    this->canCtx_Rx.canSafetyFeatureParams.reset();

    this->canCtx_Rx.canHealth.reset();

    this->canCtx_Rx.canDriverControllers.reset();

}

CANbusModule::~CANbusModule()
{
}

bool CANbusModule::init(via::car::CarTypes carType, CANDongleTypes dongleType)
{
    LOGI("CANbusModule init()");

    bool ret = false;

    this->carType = carType;
    this->dongleType = dongleType;

    if (this->pCANDongle != nullptr) this->doDongleRelease();

    // Alloc & init new dongle object.
    try {
        this->pCANDongle = std::move(std::unique_ptr<CANDongle>(new CANDongle(this->dongleType)));
        this->doDongleInit();
        ret = true;
    }
    catch (std::runtime_error){
        ret = false;
    }


    return ret;
}

void CANbusModule::exec()
{
    // create threads
    int err;
    if(this->threadHandle_CANHealth == 0) err = pthread_create(&this->threadHandle_CANHealth, NULL, thread_CANHealth, this);
    if(this->threadHandle_CANRecv == 0) err = pthread_create(&this->threadHandle_CANRecv, NULL, thread_CANRecv, this);
    if(this->threadHandle_CANSend == 0) err = pthread_create(&this->threadHandle_CANSend, NULL, thread_CANSend, this);

}

void CANbusModule::release()
{
    int err;
    flagExit = true;
    LOGI("CANbusModule::release()");

    if(threadHandle_CANRecv != 0) err = pthread_join(threadHandle_CANRecv, NULL);
    threadHandle_CANRecv = 0;

    if(threadHandle_CANSend != 0) err = pthread_join(threadHandle_CANSend, NULL);
    threadHandle_CANSend = 0;

    if(threadHandle_CANHealth != 0) err = pthread_join(threadHandle_CANHealth, NULL);
    threadHandle_CANHealth = 0;

    LOGI("CANbusModule::doDongleRelease()");
    this->doDongleRelease();
    LOGI("CANbusModule::release() finish ");
}

bool CANbusModule::isInit()
{
    return (this->carType != CarTypes::Unknown);
}

bool CANbusModule::isExit()
{
    return flagExit;
}

bool CANbusModule::isRecording() {
    return b_IsRecording;
}

std::string CANbusModule::getRecordPath() {
    return recordPath;
}

void CANbusModule::stopRecord() {
    std::lock_guard<std::mutex> lock(recoderMutex);
    if(recordStream.is_open()) {
        recordStream.close();
        LOGE("close recordStream");
    }
    b_IsRecording = false;
}

bool CANbusModule::startRecord(std::string path, bool appendFile) {
    std::lock_guard<std::mutex> lock(recoderMutex);
    recordPath = path;

    if(appendFile) {
        recordStream.open(recordPath, std::ios::out | std::ios::app);
    }
    else {
        recordStream.open(recordPath, std::ios::out | std::ios::trunc);
    }

    recordStartTime = getms();
    b_IsRecording = recordStream.is_open();

    if(b_IsRecording) {
        recordStream << "time,address,bus,content";
        LOGE("write header");
    }
    LOGE("recordPath %s .... %d", recordPath.c_str(), b_IsRecording);
    return b_IsRecording;
}

bool CANbusModule::isDongleConnected()
{
    if (this->pCANDongle == NULL) {
        std::string err = "CAN dongle is NULL";
        throw std::runtime_error(err);
    }
    else {
        return this->pCANDongle->isConnect();
    }
}

bool CANbusModule::connectDongle(ConnectDataBundle *dataBundle)
{
    bool ret = false;
    if (this->pCANDongle == nullptr) {
        std::string err = "CAN dongle is NULL";
        throw std::runtime_error(err);
    }
    else {
        ret = this->pCANDongle->connect(dataBundle);
    }
    return ret;
}

void CANbusModule::doDongleInit()
{
    if (this->pCANDongle == nullptr) {
        std::string err = "CAN dongle is NULL";
        throw std::runtime_error(err);
    }
    else {
        this->pCANDongle->init();
    }
}

void CANbusModule::doDongleRelease()
{
    if (this->pCANDongle != nullptr) {
        this->pCANDongle->release();
        this->pCANDongle = nullptr;
    }
}


void CANbusModule::doDongleCANHealth(CANbusModule *module)
{
    if (this->pCANDongle == nullptr) {
        std::string err = "CAN dongle is NULL";
        LOGE("Health Err : %s", err.c_str());
        throw std::runtime_error(err);
    }
    else {
        CANHealth health;

        //LOGE("Health :  this->pCANDongle->doCANHealth(health, TIMEOUT);");
        this->pCANDongle->doCANHealth(health, TIMEOUT);
        canCtx_Rx.canHealth.controlsAllowed = health.controlsAllowed;
        if(this->pCANDongle->isConnect())
            canCtx_Rx.canHealth.dongleConnected = 1;
        else
            canCtx_Rx.canHealth.dongleConnected = 0;

        LOGE("Health : \n");
        LOGE("    Controls Allowed         : %d\n", health.controlsAllowed);
        LOGE("    Dongle Connected         : %d %d\n", health.dongleConnected, this->pCANDongle->isConnect());
    }

    // sync time of thread
    switch (carType) {
        case CarTypes::HONDA_CRV_2017_BOSCH:
            usleep(1000 * 1000);    // 1Hz
            break;
        default:
            usleep(1000 * 1000);    // 1Hz
            break;
    }
}

void CANbusModule::doDongleCANRecv(CANbusModule *module)
{
    uint32_t data[RECV_SIZE / 4];

    if (this->pCANDongle == nullptr) {
        std::string err = "CAN dongle is NULL";
        throw std::runtime_error(err);
    }
    else {
        do {
            // do recv
            int recvSize = this->pCANDongle->doCANRecv((unsigned char *)data, RECV_SIZE, TIMEOUT);

            // break if length is 0
            if (recvSize <= 0) break;

            // parse received data
            if (module != NULL) {
                switch (carType) {
                    case CarTypes::HONDA_CRV_2017_BOSCH:
                        honda_crv_2017_bosch::CANPaser::parse((uint32_t *)data, recvSize, this->canCtx_Rx);
                        break;
                    default:
                        break;
                }

                if(b_IsRecording) {
                    record((uint32_t *) data, recvSize);
                }
            }
        } while (false);
    }

    // sync time of thread
    switch (carType) {
        case CarTypes::HONDA_CRV_2017_BOSCH:
            usleep(5 * 1000);   // 200Hz
            break;
        default:
            break;
    }
}

void CANbusModule::doDongleCANSend(CANbusModule *module)
{
    struct timeval curTime;
    gettimeofday(&curTime, NULL);

    long long curms = (curTime.tv_sec) * 1000 + (curTime.tv_usec) / 1000 ;
    static long long prevms = 0;
    if(prevms == 0) prevms = curms;

    // Run this thread as specifyHz
    int Hz_Tx = 0;
    long long ms_Tx = 0;

    switch (carType) {
        case CarTypes::HONDA_CRV_2017_BOSCH:
            Hz_Tx = 100;
            ms_Tx = 1000 / Hz_Tx;
            break;
        default:
            break;
    }

    // Run at 100 Hz
    int idx;
    long long diff = curms - prevms;
    if (diff >= ms_Tx) {
        if(diff >10) {
            LOGE("Tx time out!!!!!!!!!!!!!!!!!!!!!!!!!!! diff %lld ", diff);

        }
        unsigned char bus = 2;
        std::vector<CANPackageUnit> unitList;
        do {
            switch (carType) {
                case CarTypes::HONDA_CRV_2017_BOSCH:
                    bus = 2;
                    idx = frameId_Tx % 4;
                    honda_crv_2017_bosch::CANPacker::pack(unitList, bus, idx, this->canCtx_Rx, this->canCtx_Tx.canSteeringControlParams);

                    if ((frameId_Tx % 10) == 0) {
                        idx = (frameId_Tx / 10) % 4;
                        honda_crv_2017_bosch::CANPacker::pack(unitList, bus, idx, this->canCtx_Rx, this->canCtx_Tx.canLKASHUDParams);
                        honda_crv_2017_bosch::CANPacker::pack(unitList, bus, idx, this->canCtx_Rx, this->canCtx_Tx.canACCHudParams);
                    }

                    if(this->canCtx_Tx.canWheelButtons.decelerate_ || this->canCtx_Tx.canWheelButtons.accumlate_) {
                        bus = 0;
                        honda_crv_2017_bosch::CANPacker::pack(unitList, bus, idx, this->canCtx_Rx, this->canCtx_Tx.canWheelButtons);
                    }
                    break;
                default:
                    break;
            }

            // Tx
            this->pCANDongle->doCANSend(unitList, TIMEOUT);
            if(!this->pCANDongle->isConnect()) {

            }

        } while (false);

        frameId_Tx++;

        prevms = curms - (diff - ms_Tx);
    }

    //usleep(100);   // sleep 1ms
    usleep(1000);   // sleep 1ms
}

void CANbusModule::setGPS(double latitude, double longitude)
{
    gpsLatitude = latitude;
    gpsLongitude = longitude;
    gpsRefreshTime = clock();
}
void CANbusModule::record(uint32_t *data, int recvSize)
{
    std::lock_guard<std::mutex> lock(recoderMutex);
    char const hex_chars[16] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    // populate message
    unsigned char frameData[16];

    double timeDiff = (getms() - recordStartTime) * 0.001;
    for (int i = 0; i<(recvSize / 0x10); i++) {
        unsigned int address = 0;
        if (data[i * 4] & 4) {  // extended
            address = data[i * 4] >> 3;
        }
        else { // normal
            address = data[i * 4] >> 21;
        }

        int busTime = data[i * 4 + 1] >> 16;
        int len = data[i * 4 + 1] & 0xF;
        int busID = (data[i * 4 + 1] >> 4) & 0xff;


        //memcpy(frameData, (uint8_t*)&data[i * 4 + 2], len);

        uint8_t* content = (uint8_t*)&data[i * 4 + 2];
        std::stringstream ss;
        for(int hi = 0; hi< len; hi++) {
            uint8_t byte = *(content + hi);
            ss << hex_chars[ ( byte & 0xF0 ) >> 4 ] << hex_chars[ ( byte & 0x0F ) >> 0 ];
        }

        recordStream << timeDiff << ","
                     << address << ","
                     << busID << ","
                     << ss.str();
        //recordStream._M_write((char*)&data[i * 4 + 2], len);
        recordStream << "\n";
    }

    if(gpsRefreshTime != gpsLastUpdateTime) {
        recordStream << timeDiff  << ","
                     << 5233 << ","
                     << 0 << ","
                     << std::setprecision(8) << gpsLatitude << ","
                     << std::setprecision(8) << gpsLongitude << "\n";
        gpsLastUpdateTime = gpsRefreshTime;
    }

}

void CANbusModule::Tx_SteeringControl(CANParams_SteeringControl &param)
{
    this->canCtx_Tx.canSteeringControlParams = param;
//    this->canCtx_Tx.canSteeringControlParams.steerTorque = param.steerTorque;
//    this->canCtx_Tx.canSteeringControlParams.steerTorqueRequest = param.steerTorqueRequest;
}

void CANbusModule::Tx_LKAS_HUD(CANParams_LKASHud &param)
{
    this->canCtx_Tx.canLKASHUDParams = param;
//    this->canCtx_Tx.canLKASHUDParams.isLaneDetected = param.isLaneDetected;
//    this->canCtx_Tx.canLKASHUDParams.laneType = param.laneType;
//    this->canCtx_Tx.canLKASHUDParams.beep = param.beep;
//    this->canCtx_Tx.canLKASHUDParams.steering_requeired = param.steering_requeired;
}

void CANbusModule::Tx_ACC_HUD(CANParams_ACCHud &param)
{
    this->canCtx_Tx.canACCHudParams = param;
//    this->canCtx_Tx.canACCHudParams.accOn = param.accOn;
//    this->canCtx_Tx.canACCHudParams.cruiseSpeed = param.cruiseSpeed;
}

void CANbusModule::Tx_WheelButtons(CANParams_WheelButtons &param)
{
    this->canCtx_Tx.canWheelButtons = param;
//    this->canCtx_Tx.canSteeringControlParams.steerTorque = param.steerTorque;
//    this->canCtx_Tx.canSteeringControlParams.steerTorqueRequest = param.steerTorqueRequest;
}


void CANbusModule::Rx_CANHealth(CANHealth &param)
{
    param = this->canCtx_Rx.canHealth;
//    param.controlsAllowed  = this->canCtx_Rx.canHealth.controlsAllowed;
//    param.dongleConnected   = this->canCtx_Rx.canHealth.dongleConnected;
}

void CANbusModule::Rx_SpeedParams(CANParams_Speed &param)
{
    param = this->canCtx_Rx.canSpeedParams;
//    param.engineSpeed           = this->canCtx_Rx.canSpeedParams.engineSpeed;
//    param.roughSpeed            = this->canCtx_Rx.canSpeedParams.roughSpeed;
//    param.wheelSpeed_FrontLeft  = this->canCtx_Rx.canSpeedParams.wheelSpeed_FrontLeft;
//    param.wheelSpeed_FrontRight = this->canCtx_Rx.canSpeedParams.wheelSpeed_FrontRight;
//    param.wheelSpeed_RearLeft   = this->canCtx_Rx.canSpeedParams.wheelSpeed_RearLeft;
//    param.wheelSpeed_RearRight  = this->canCtx_Rx.canSpeedParams.wheelSpeed_RearRight;
}

void CANbusModule::Rx_SteeringSensorParams(CANParams_SteeringSensor &param)
{
    param = this->canCtx_Rx.canSteeringSensorParams;
//    param.steerAngle            = this->canCtx_Rx.canSteeringSensorParams.steerAngle;
//    param.steerAngleRate        = this->canCtx_Rx.canSteeringSensorParams.steerAngleRate;
//    param.angleOffset           = this->canCtx_Rx.canSteeringSensorParams.angleOffset;
//    param.steerControlActive    = this->canCtx_Rx.canSteeringSensorParams.steerControlActive;
//    param.steerStatus           = this->canCtx_Rx.canSteeringSensorParams.steerStatus;
//    param.steerTorque           = this->canCtx_Rx.canSteeringSensorParams.steerTorque;
}

void CANbusModule::Rx_SteeringControlParams(CANParams_SteeringControl &param)
{
    param = this->canCtx_Rx.canSteeringControlParams;
//    param.steerTorque           = this->canCtx_Rx.canSteeringControlParams.steerTorque;
//    param.steerTorqueRequest    = this->canCtx_Rx.canSteeringControlParams.steerTorqueRequest;
}

void CANbusModule::Rx_LKAS_HUD(CANParams_LKASHud &param)
{
    param = this->canCtx_Rx.canLKASHUDParams;
//    param.beep              = this->canCtx_Rx.canLKASHUDParams.beep;
//    param.isLaneDetected    = this->canCtx_Rx.canLKASHUDParams.isLaneDetected;
//    param.laneType          = this->canCtx_Rx.canLKASHUDParams.laneType;
}

void CANbusModule::Rx_ACC_HUD(CANParams_ACCHud &param)
{
    param = this->canCtx_Rx.canACCHudParams;
//    param.accOn         = this->canCtx_Rx.canACCHudParams.accOn;
//    param.cruiseSpeed   = this->canCtx_Rx.canACCHudParams.cruiseSpeed;
}

void CANbusModule::Rx_SafetyFeature(CANParams_SafetyFeature &param)
{
    param = this->canCtx_Rx.canSafetyFeatureParams;
//    param.isEnabled_ACC         = this->canCtx_Rx.canSafetyFeatureParams.isEnabled_ACC;
//    param.isEnabled_LKS         = this->canCtx_Rx.canSafetyFeatureParams.isEnabled_LKS;
//    param.isBrakePressed        = this->canCtx_Rx.canSafetyFeatureParams.isBrakePressed;
//    param.isGasPressed          = this->canCtx_Rx.canSafetyFeatureParams.isGasPressed;
}

void CANbusModule::Rx_DriverControllers(CANParams_DriverControllers &param)
{
    param = this->canCtx_Rx.canDriverControllers;
//    param.leftBlinkerOn     = this->canCtx_Rx.canDriverControllers.leftBlinkerOn;
//    param.rightBlinkerOn    = this->canCtx_Rx.canDriverControllers.rightBlinkerOn;
//    param.wiperStatus       = this->canCtx_Rx.canDriverControllers.wiperStatus;
}

void CANbusModule::manual_CANHealth(CANHealth &param)
{
    param.copyTo(this->canCtx_Rx.canHealth);
}

void CANbusModule::manual_CANSpeedParams(CANParams_Speed &param)
{
    this->canCtx_Rx.canSpeedParams = param;
}

void CANbusModule::manual_CANSteeringSensorParams(CANParams_SteeringSensor &param)
{
    this->canCtx_Rx.canSteeringSensorParams = param;
}

void CANbusModule::manual_CANSteeringControlParams(CANParams_SteeringControl &param)
{
    this->canCtx_Rx.canSteeringControlParams = param;
}

void CANbusModule::manual_LKAS_HUD(CANParams_LKASHud &param)
{
    this->canCtx_Rx.canLKASHUDParams = param;
}

void CANbusModule::manual_ACC_HUD(CANParams_ACCHud &param)
{
    this->canCtx_Rx.canACCHudParams = param;
}

void CANbusModule::manual_SafetyFeature(CANParams_SafetyFeature &param)
{
    this->canCtx_Rx.canSafetyFeatureParams = param;
}

void CANbusModule::manual_DriverControllers(CANParams_DriverControllers &param)
{
    this->canCtx_Rx.canDriverControllers = param;
}

CANDongleTypes CANbusModule::getDongleType()
{
    return this->dongleType;
}
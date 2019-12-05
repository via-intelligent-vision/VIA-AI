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
#include <ostream>
#include <unistd.h>
#include <vector>
#include <opencv2/core/core.hpp>
#include "automotiveControl/LongitudePlanner/LongitudePlanner.h"

//--------------------------------------------------------------------------------------------------

#include <android/log.h>
#define  LOG_TAG    "LongitudePlanner"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


//--------------------------------------------------------------------------------------------------
using namespace via::camera;
using namespace automotive::longitude;
using namespace std;
using namespace cv;
//--------------------------------------------------------------------------------------------------
#define TAG_Controller          "Controller"
#define TAG_LongitudePlane      "LongitudePlane"
#define TAG_PlaneTimeStep       "PlaneTimeStep"
#define TAG_Speed               "Speed"
//--------------------------------------------------------------------------------------------------
static long long getms()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    double ms = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000 ;
    return (long long)ms;
}

//  Speed [ 30 ~ 60 ]  ---- [1.0 - 0.05  ]
template <class T>
T interpolate(T vInMinSpeed, T vInMaxSpeed, T speed, T minSpeed, T maxSpeed) {
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
//--------------------------------------------------------------------------------------------------
class LongitudePlanner::ConfigImpl
{
public:
    ConfigImpl() {
        planeTimeStep_ = 10;
        speed_ = 0.0f;
    }
    double planeTimeStep_;  //ms
    float speed_;  //ms
};
//--------------------------------------------------------------------------------------------------
class LongitudePlanner::DataImpl {
public:
    DataImpl() {
        refCameraModule_ = NULL;
        isInit_ = false;
        isRecording_ = false;
        recordStartTime_ = 0;
        recordPath_ ="";
        cruiseSpeed_ = 0;
    }

    // core data
    bool isInit_;
    float cruiseSpeed_;

    // Reference Moule
    via::camera::CameraModule *refCameraModule_;

    // car data
    via::car::CarAxleParams carAxleParams_;
    via::car::CarBodyShellParams carBodyShellParams_;
    via::car::CarTireParams carTireParams_;
    via::car::CarSteeringParams carSteeringParams_;
    automotive::longitude::LongitudePlan prevResult_;

    // recoder data
    std::mutex recoderMutex_;
    bool isRecording_;
    double recordStartTime_;
    std::string recordPath_;
    std::ofstream recordStream_;
};

//--------------------------------------------------------------------------------------------------
LongitudePlanner::Input::Input()
{
    canSafetyFeature_ = NULL;
    canSpeed_ = NULL;
    canSteeringSensor_ = NULL;
    canACCHud = NULL;
    driverConfigSpeed_ = 0;
}

//--------------------------------------------------------------------------------------------------
LongitudePlanner::LongitudePlanner() :
        cfg_(new ConfigImpl()),
        data_(new DataImpl())
{
}

LongitudePlanner::~LongitudePlanner()
{

}

void LongitudePlanner::init(via::car::CarContext *carContext, via::camera::CameraModule *cameraModule)
{
    LOGI("LongitudePlanner::init");
    carContext->getCarAxleParams(data_->carAxleParams_);
    carContext->getCarBodyShellParams(data_->carBodyShellParams_);
    carContext->getCarTireParams(data_->carTireParams_);
    carContext->getCarSteeringParams(data_->carSteeringParams_);

    setCameraModule(cameraModule);

    data_->isInit_ = true;
}


bool LongitudePlanner::load(const std::string &xmlPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        ret = storage.open(xmlPath, cv::FileStorage::READ);
        if (!ret) {
            errStream << "Open file error :" << xmlPath;
            throw std::runtime_error(errStream.str());
        }
        ret = this->load(storage);
        storage.release();
    }
    catch (cv::Exception &error) {   //it's a fatal error, throw to abort
        LOGE("[opencv exception] %s", error.what());
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        LOGE("[runtime error] %s", error.what());
    }

    return ret;
}

bool LongitudePlanner::load(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        FileNode nodeController;    // Due to compatibility, <LDWS> <FCWS> <Camera> is accepted.
        if (fs[TAG_Controller].isNamed()) {
            nodeController = fs[TAG_Controller];
        }

        if (!nodeController.isNone()) {
            FileNode nodeLongitudePlane;
            if (nodeController[TAG_LongitudePlane].isNamed()) {
                nodeLongitudePlane = nodeController[TAG_LongitudePlane];
            }

            if (!nodeLongitudePlane.isNone()) {
                if(nodeLongitudePlane[TAG_PlaneTimeStep].isNamed()) nodeLongitudePlane[TAG_PlaneTimeStep] >> cfg_->planeTimeStep_;
                if(nodeLongitudePlane[TAG_Speed].isNamed()) nodeLongitudePlane[TAG_Speed] >> cfg_->speed_;
                ret = true;
            }
            else {
                errStream << "No element <" << TAG_LongitudePlane << "> in this file";
            }
        }
        else {
            errStream << "No element <" << TAG_Controller << "> in this file";
        }
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        throw error;
    }

    static bool dbg = true;
    if(dbg) {
        LOGE(" cfg_->planeTimeStep_       %f", cfg_->planeTimeStep_);
        LOGE("cfg_->speed_ %f", cfg_->speed_);
        dbg = false;
    }

    return ret;
}


void LongitudePlanner::setCameraModule(via::camera::CameraModule *cameraModule)
{
    data_->refCameraModule_ = cameraModule;

    if(data_->refCameraModule_ != NULL) {
        LOGD("setCameraModule , camera type  %d", (int) cameraModule->getCameraType());
    }
}

bool LongitudePlanner::isInit()
{
    return data_->isInit_;
}

bool LongitudePlanner::isRecording() {
    return data_->isRecording_;
}

std::string LongitudePlanner::getRecordPath() {
    return data_->recordPath_;
}

void LongitudePlanner::_stopRecord() {
    if(data_->recordStream_.is_open()) {
        data_->recordStream_.close();
        LOGI("close recordStream");
    }
    data_->isRecording_ = false;
}

void LongitudePlanner::stopRecord() {
    std::lock_guard<std::mutex> lock(data_->recoderMutex_);
    _stopRecord();
}

bool LongitudePlanner::startRecord(std::string path, bool appendFile) {
    std::lock_guard<std::mutex> lock(data_->recoderMutex_);

    // stop record first.
    _stopRecord();

    data_->recordPath_ = path;

    if(appendFile) {
        data_->recordStream_.open(data_->recordPath_, std::ios::out | std::ios::app);
    }
    else {
        data_->recordStream_.open(data_->recordPath_, std::ios::out | std::ios::trunc);
    }

    data_->recordStartTime_ = getms();
    data_->isRecording_ = data_->recordStream_.is_open();

    LOGI("recordPath %s .... %d", data_->recordPath_.c_str(), data_->isRecording_);
    return data_->isRecording_;
}

bool LongitudePlanner::update(LongitudePlanner::Input &input, automotive::longitude::LongitudePlan &result)
{
    bool ret = false;
    static long long prevTime = 0;
    long long curTime = getms();
    //if((curTime - prevTime) < cfg_->planeTimeStep_) return ret;
    if((curTime - prevTime) < cfg_->planeTimeStep_ && input.driverConfigSpeed_ == 0.0f) return ret;
    prevTime = curTime;

   // input.canSafetyFeature_->isEnabled_ACC = true;
    if(input.canSafetyFeature_->isEnabled_ACC) {
        if(input.driverConfigSpeed_ != 0.0f) data_->cruiseSpeed_ = input.driverConfigSpeed_;

        if(data_->cruiseSpeed_ >= 30.0f && data_->cruiseSpeed_ <= 140.0f) {
            if (input.canACCHud->cruiseSpeed > data_->cruiseSpeed_) {
                result.accelType_ = -1.0f;
            } else if (input.canACCHud->cruiseSpeed < data_->cruiseSpeed_) {
                result.accelType_ = 1.0f;
            } else {
                result.accelType_ = 0.0f;
            }
        }
        else {
            result.accelType_ = 0.0f;
        }
        result.desiredSpeed_ = data_->cruiseSpeed_;
        result.isControllable_ = true;
        result.isValid_ = true;
    }
    else {
        data_->cruiseSpeed_ = 0;
        result.isControllable_ = false;
        result.isValid_ = true;
    }

    result.copyTo(data_->prevResult_);
    ret = true;

    return ret;
}

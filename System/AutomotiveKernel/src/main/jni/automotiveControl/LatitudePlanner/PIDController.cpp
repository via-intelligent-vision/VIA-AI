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

#include <sys/time.h>
#include <stdexcept>
#include "PIDController.h"

#include <android/log.h>
#define  LOG_TAG    "PIDControl"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;
using namespace via::automotive::latitude;

#define TAG_Controller              "Controller"
#define TAG_LatPID                  "LatitudePID"
#define TAG_ctlP                    "ctlP"
#define TAG_ctlI                    "ctlI"
#define TAG_ctlD                    "ctlD"
#define TAG_ctlF                    "ctlF"
#define TAG_ActuatorSmootherFilter  "ActuatorSmootherFilter"
#define TAG_Param                   "Param"
#define TAG_Param_InMinSpeed        "Param_InMinSpeed"
#define TAG_Param_InMaxSpeed        "Param_InMaxSpeed"

// ------------------------------------------------------------------------------------
//  ex: Speed [ 30 ~ 60 ]  ---- [1.0 - 0.05  ]
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
static long long getms()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    double ms = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000 ;
    return (long long)ms;
}


// ------------------------------------------------------------------------------------
//about PIDControlInput
//
PIDControlInput::PIDControlInput() {
    carSpeed = 0.0f;
    planDesiredSteerAngle = 0.0f;
    curSteerAngle = 0.0f;
    steerWhellControlAngleLimit = 0.0f;
    steerTorqueLimit = 0.0f;
    steerControllable = false;
}

// ------------------------------------------------------------------------------------
//about PIDControlOutput
//
PIDControlOutput::PIDControlOutput() {
    ctlSteerValue = 0.0f;
    steeringAngle = 0.0f;
    steeringTorque = 0.0f;
    steerControllable = false;
}

// ------------------------------------------------------------------------------------
//about ControlItem
//
ControlItem::ControlItem() {
    param = 0.0f;
    param_InMinSpeed = 0.0f;
    param_InMaxSpeed = 0.0f;
    torque = 0.0f;
}

// ------------------------------------------------------------------------------------
//about ActuatorSmootherFilter
//
ActuatorSmootherFilter::ActuatorSmootherFilter()
{
    param_InMinSpeed = 1.0f;
    param_InMaxSpeed = 1.0f;
    reset();
};

void ActuatorSmootherFilter::reset(float steer) {
    value = steer;
}

void ActuatorSmootherFilter::update(float steer, float speed_kmh, float minSpeed_kmh, float maxSpped_kmh) {
    float gain = interpolate<float>(this->param_InMinSpeed, this->param_InMaxSpeed, speed_kmh, minSpeed_kmh, maxSpped_kmh);
    value = (1.0f - gain) * value + gain *  steer;
   // LOGE("vale %f , gain %f , steer %f", value, gain ,steer);
}

float ActuatorSmootherFilter::getSteerValue() {
    return value;
}
// ------------------------------------------------------------------------------------
//about PIDController
//
PIDController::PIDController()
{
    prevError = 0.0f;
    integralValue = 0.0f;
}

PIDController::~PIDController()
{

}

bool PIDController::load(const std::string &xmlPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        bool ret = storage.open(xmlPath, cv::FileStorage::READ);
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

    static bool dbg = true;
    if(dbg) {
        LOGE("ctl_P inMin %f , inMax %f", ctl_P.param_InMinSpeed, ctl_P.param_InMaxSpeed);
        LOGE("ctl_I inMin %f , inMax %f", ctl_I.param_InMinSpeed, ctl_I.param_InMaxSpeed);
        LOGE("ctl_D inMin %f , inMax %f", ctl_D.param_InMinSpeed, ctl_D.param_InMaxSpeed);
        LOGE("ctl_F inMin %f , inMax %f", ctl_F.param_InMinSpeed, ctl_F.param_InMaxSpeed);
        LOGE("ActuatorSmootherFilter inMin %f , inMax %f", actuatorSmoother.param_InMinSpeed, actuatorSmoother.param_InMaxSpeed);
        dbg = false;
    }

    return ret;
}

bool PIDController::load(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        FileNode nodeController;    // Due to compatibility, <LDWS> <FCWS> <Camera> is accepted. 
        if (fs[TAG_Controller].isNamed()) {
            nodeController = fs[TAG_Controller];
        }

        if (!nodeController.isNone()) {
            FileNode nodeLatPID;
            if (nodeController[TAG_LatPID].isNamed()) {
                nodeLatPID = nodeController[TAG_LatPID];
            }

            if (!nodeLatPID.isNone()) {
                if (nodeLatPID[TAG_ctlP].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ctlP];
                    nodeCtl[TAG_Param] >> this->ctl_P.param;
                    nodeCtl[TAG_Param_InMinSpeed] >> this->ctl_P.param_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->ctl_P.param_InMaxSpeed;
                }

                if (nodeLatPID[TAG_ctlI].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ctlI];
                    nodeCtl[TAG_Param] >> this->ctl_I.param;
                    nodeCtl[TAG_Param_InMinSpeed] >> this->ctl_I.param_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->ctl_I.param_InMaxSpeed;
                }

                if (nodeLatPID[TAG_ctlD].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ctlD];
                    nodeCtl[TAG_Param] >> this->ctl_D.param;
                    nodeCtl[TAG_Param_InMinSpeed] >> this->ctl_D.param_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->ctl_D.param_InMaxSpeed;
                }

                if (nodeLatPID[TAG_ctlF].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ctlF];
                    nodeCtl[TAG_Param] >> this->ctl_F.param;
                    nodeCtl[TAG_Param_InMinSpeed] >> this->ctl_F.param_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->ctl_F.param_InMaxSpeed;
                }

                if (nodeLatPID[TAG_ActuatorSmootherFilter].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ActuatorSmootherFilter];
                    nodeCtl[TAG_Param_InMinSpeed] >> this->actuatorSmoother.param_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->actuatorSmoother.param_InMaxSpeed;
                }

                ret = true;
            }
            else {
                
            }
        }
        else {
            errStream << "No element <" << TAG_Controller << "> in this file";
            throw runtime_error(errStream.str());
        }
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        throw error;
    }

    return ret;
}

bool PIDController::save(const std::string &exportFullPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        bool ret = storage.open(exportFullPath, cv::FileStorage::WRITE);
        if (!ret) {
            errStream << "Open file error :" << exportFullPath;
            throw std::runtime_error(errStream.str());
        }
        ret = this->save(storage);
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

bool PIDController::save(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        if (!fs.isOpened()) {
            errStream << "FileStorage couldn't be empty.";
        }
        else {
            fs << TAG_Controller << "{";

            fs << TAG_LatPID << "{";
            fs << TAG_ctlP << "{";
            fs << TAG_Param << ctl_P.param;
            fs << TAG_Param_InMinSpeed << ctl_P.param_InMinSpeed;
            fs << TAG_Param_InMaxSpeed << ctl_P.param_InMaxSpeed;
            fs << "}"; // end of TAG_ctlP

            fs << TAG_ctlI << "{";
            fs << TAG_Param << ctl_I.param;
            fs << TAG_Param_InMinSpeed << ctl_I.param_InMinSpeed;
            fs << TAG_Param_InMaxSpeed << ctl_I.param_InMaxSpeed;
            fs << "}"; // end of TAG_ctlI

            fs << TAG_ctlD << "{";
            fs << TAG_Param << ctl_D.param;
            fs << TAG_Param_InMinSpeed << ctl_D.param_InMinSpeed;
            fs << TAG_Param_InMaxSpeed << ctl_D.param_InMaxSpeed;
            fs << "}"; // end of TAG_ctlD

            fs << TAG_ctlF << "{";
            fs << TAG_Param << ctl_F.param;
            fs << TAG_Param_InMinSpeed << ctl_F.param_InMinSpeed;
            fs << TAG_Param_InMaxSpeed << ctl_F.param_InMaxSpeed;
            fs << "}"; // end of TAG_ctlF

            fs << "}";  // end of TAG_Controller
            ret = true;
        }
    }
    catch (cv::Exception e) {
        throw;
    }

    return ret;
}

void PIDController::update(PIDControlInput &in, PIDControlOutput &out)
{
    const float unit_Angel_to_Torque = (in.steerTorqueLimit / in.steerWhellControlAngleLimit);

    static int CC = 0;
    bool isLog = false;
    if(CC % 50 == 0) isLog = true;
    CC++;


    if (in.steerControllable) {
        const float interpolate_maxSpeed = 140.0f;
        const float interpolate_minSpeed = 30.0f;


        //actuatorSmoother.update(in.planDesiredSteerAngle, in.carSpeed, interpolate_minSpeed, interpolate_maxSpeed);
        actuatorSmoother.value = actuatorSmoother.value + actuatorSmoother.param_InMinSpeed * (in.planDesiredSteerAngle - actuatorSmoother.value);
        float steerValue = actuatorSmoother.getSteerValue();



        // calc Error
        //float steerErr = in.planDesiredSteerAngle - in.curSteerAngle;
        float steerErr = steerValue - in.curSteerAngle;

        //  calc Feed-forward
        do {
            float v_ms = (in.carSpeed * 1000.0f) / 3600.0f;
            float kF = interpolate<float>(ctl_F.param_InMinSpeed, ctl_F.param_InMaxSpeed, in.carSpeed, interpolate_minSpeed, interpolate_maxSpeed);
            //if(isLog) LOGE("F , inMinSpeed %.2f , inMaxSeed %.2f , cur Speed [30 -- %.2f -- 70] , kF %f", ctl_F.param_InMinSpeed, ctl_F.param_InMaxSpeed, in.carSpeed, kF);

            //ctl_F.torque = kF * in.planDesiredSteerAngle * unit_Angel_to_Torque;
            ctl_F.torque = kF * steerValue * v_ms * v_ms;
        } while (false);


        //  calc P
        do {
            float kP = interpolate<float>(ctl_P.param_InMinSpeed, ctl_P.param_InMaxSpeed, in.carSpeed, interpolate_minSpeed, interpolate_maxSpeed);
            //if(isLog) LOGE("P , inMinSpeed %.2f , inMaxSeed %.2f , cur Speed [30 -- %.2f -- 70] , kP %f", ctl_P.param_InMinSpeed, ctl_P.param_InMaxSpeed, in.carSpeed, kP);

            ctl_P.torque = kP * steerErr * unit_Angel_to_Torque;
        } while (false);

        //  calc I
        do {
            float kI = interpolate<float>(ctl_I.param_InMinSpeed, ctl_I.param_InMaxSpeed, in.carSpeed, interpolate_minSpeed, interpolate_maxSpeed);
            //if(isLog) LOGE("I , inMinSpeed %.2f , inMaxSeed %.2f , cur Speed [30 -- %.2f -- 70] , kI %f ,integralValue %f", ctl_I.param_InMinSpeed, ctl_I.param_InMaxSpeed, in.carSpeed, kI, integralValue);

            //float dmp = interpolate<float>(0.98, 0.96, in.carSpeed, 30, 90);
            //integralValue *= 0.99f;

            integralValue = integralValue + steerErr * kI * 0.025f;
//            if (integralValue > 16) integralValue = 16;
//            if (integralValue < -16) integralValue = -16;
            if (integralValue > 10) integralValue = 10;
            if (integralValue < -10) integralValue = -10;

            ctl_I.torque = integralValue * unit_Angel_to_Torque;
        } while (false);

        //  calc D
        do {
            float kD = interpolate<float>(ctl_D.param_InMinSpeed, ctl_D.param_InMaxSpeed, in.carSpeed, interpolate_minSpeed, interpolate_maxSpeed);
            //if(isLog) LOGE("D , inMinSpeed %.2f , inMaxSeed %.2f , cur Speed [30 -- %.2f -- 70] , kD %f", ctl_D.param_InMinSpeed, ctl_D.param_InMaxSpeed, in.carSpeed, kD);

            //ctl_D.torque = kD * (steerErr - this->prevError) * unit_Angel_to_Torque;
            float v_ms = (in.carSpeed * 1000.0f) / 3600.0f;
            ctl_D.torque = kD * steerErr * v_ms * v_ms;
        } while (false);

        // merge control torque
        float canSteerTorqueValue = ctl_P.torque + ctl_I.torque + ctl_D.torque + ctl_F.torque;
       
        // save current data
        this->prevError = steerErr;


        // output
        out.tErr = steerErr;
        out.tP = ctl_P.torque;
        out.tI = ctl_I.torque;
        out.tD = ctl_D.torque;
        out.tF = ctl_F.torque;
        out.ctlSteerValue = steerValue;
        out.steeringTorque = canSteerTorqueValue;
        out.steeringAngle = canSteerTorqueValue / unit_Angel_to_Torque;
        out.steerControllable = in.steerControllable;
        out.actuatorFactor = actuatorSmoother.param_InMaxSpeed;
    }
    else {
        integralValue *= 0.05f;

        out.tErr = 0;
        out.tP = 0;
        out.tI = 0;
        out.tD = 0;
        out.tF = 0;
        out.steeringTorque = 0;
        out.steeringAngle = 0;
        out.steerControllable = in.steerControllable;

        actuatorSmoother.reset(in.curSteerAngle);
    }

}
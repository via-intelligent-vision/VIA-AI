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

#pragma once

namespace via {
namespace canbus {

enum class CANDongleTypes : unsigned char {
    Unknown                 = 0,
    UserManual              = 1,
    commaai_WhitePanda      = 2,
};

/**
@brief Define CAN speed parameters, unit : km/h
carSpeed is calculated by program, engineSpeed & wheelSpeeds are set by canbus.
*/
class CANParams_Speed {
public:
    CANParams_Speed() {
        reset();
    }

    void reset() {
        roughSpeed = 0;
        engineSpeed = 0;
        wheelSpeed_FrontLeft = 0;
        wheelSpeed_FrontRight = 0;
        wheelSpeed_RearLeft = 0;
        wheelSpeed_RearRight = 0;
    }

    CANParams_Speed &operator=(CANParams_Speed &src) {
        if (this != &src) {
            this->roughSpeed = src.roughSpeed;
            this->engineSpeed = src.engineSpeed;
            this->wheelSpeed_FrontLeft = src.wheelSpeed_FrontLeft;
            this->wheelSpeed_FrontRight = src.wheelSpeed_FrontRight;
            this->wheelSpeed_RearLeft = src.wheelSpeed_RearLeft;
            this->wheelSpeed_RearRight = src.wheelSpeed_RearRight;
        }
        return *this;
    }

    float roughSpeed;
    float engineSpeed;
    float wheelSpeed_FrontLeft;
    float wheelSpeed_FrontRight;
    float wheelSpeed_RearLeft;
    float wheelSpeed_RearRight;
} ;


/**
@brief Define CAN steering parameters,
all parameter are set by canbus.
*/
class CANParams_SteeringSensor {
public:
    CANParams_SteeringSensor() {
        reset();
    }

    void reset() {
        steerAngle = 0;
        steerAngleRate = 0;
        steerStatus = 0;
        steerControlActive = 0;
        steerTorque = 0;
        angleOffset = 0;
    }

    CANParams_SteeringSensor &operator=(const CANParams_SteeringSensor &src) {
        if (this != &src) {
            this->steerAngle = src.steerAngle;
            this->steerAngleRate = src.steerAngleRate;
            this->angleOffset = src.angleOffset;
            this->steerStatus = src.steerStatus;
            this->steerControlActive = src.steerControlActive;
            this->steerTorque = src.steerTorque;
        }
        return *this;
    }

    float steerAngle;       //unit : deg
    float steerAngleRate;   //unit : deg/s
    float angleOffset;  //unit : deg
    unsigned char steerStatus;  // status of steer
    unsigned char steerControlActive;
    short steerTorque;
};


/**
@brief Define CAN steering control parameters,
*/
class CANParams_SteeringControl {
public:
    CANParams_SteeringControl() {
        reset();
    }

    void reset() {
        steerTorqueRequest = 0;
        steerTorque = 0;
    }

    CANParams_SteeringControl &operator=(const CANParams_SteeringControl &src) {
        if (this != &src) {
            this->steerTorqueRequest = src.steerTorqueRequest;
            this->steerTorque = src.steerTorque;
        }
        return *this;
    }

    unsigned char steerTorqueRequest;
    long steerTorque;   //unit : ?
};


/**
@brief Define CAN  wheel buttons parameters,
*/
class CANParams_WheelButtons {
public:
    CANParams_WheelButtons() {
        reset();
    }

    void reset() {
        controller_toggle_ = false;
        cancel_control_ = false;
        accumlate_ = false;
        decelerate_ = false;
    }

    CANParams_WheelButtons &operator=(const CANParams_WheelButtons &src) {
        if (this != &src) {
            this->controller_toggle_ = src.controller_toggle_;
            this->cancel_control_ = src.cancel_control_;
            this->accumlate_ = src.accumlate_;
            this->decelerate_ = src.decelerate_;
        }
        return *this;
    }

    bool controller_toggle_;
    bool cancel_control_;
    bool accumlate_;
    bool decelerate_;
};


/**
@brief Define CAN LKAS HUD parameters,
*/
class CANParams_LKASHud {
public:
    CANParams_LKASHud() {
        reset();
    }

    void reset() {
        isLaneDetected = 0;
        laneType = 0;
        beep = 0;
        steering_requeired = false;
    }

    CANParams_LKASHud &operator=(const CANParams_LKASHud &src) {
        if (this != &src) {
            this->isLaneDetected = src.isLaneDetected;
            this->laneType = src.laneType;
            this->beep = src.beep;
            this->steering_requeired = src.steering_requeired;
        }
        return *this;
    }

    unsigned char isLaneDetected;   //unit : 0/1
    unsigned char laneType;         //unit : depend on vehicle
    unsigned char beep;
    bool steering_requeired;
};

/**
@brief Define CAN ACC HUD parameters,
*/
struct CANParams_ACCHud {
public:
    CANParams_ACCHud() {
        reset();
    }

    void reset() {
        accOn = 0;
        cruiseSpeed = 0;
    }

    CANParams_ACCHud &operator=(const CANParams_ACCHud &src) {
        if (this != &src) {
            this->accOn = src.accOn;
            this->cruiseSpeed = src.cruiseSpeed;
        }
        return *this;
    }

    unsigned char accOn;
    int cruiseSpeed;
};


/**
@brief Define CAN ACC/LKAS enabled parameters,
*/
struct CANParams_SafetyFeature {
public:
    CANParams_SafetyFeature() {
        reset();
    }

    void reset() {
        isControlSystemReady = false;
        isEnabled_ACC = false;
        isEnabled_LKS = false;
        isBrakePressed = false;
        isGasPressed = false;
    }

    CANParams_SafetyFeature &operator=(const CANParams_SafetyFeature &src) {
        if (this != &src) {
            this->isControlSystemReady = src.isControlSystemReady;
            this->isEnabled_ACC = src.isEnabled_ACC;
            this->isEnabled_LKS = src.isEnabled_LKS;
            this->isBrakePressed = src.isBrakePressed;
            this->isGasPressed = src.isGasPressed;
        }
        return *this;
    }

    //CANParams_SafetyFeature &operator=(const CANParams_SafetyFeature &src) {
    void copyTo(CANParams_SafetyFeature &dst) {
        dst.isControlSystemReady = this->isControlSystemReady;
        dst.isEnabled_ACC = this->isEnabled_ACC;
        dst.isEnabled_LKS = this->isEnabled_LKS;
        dst.isBrakePressed = this->isBrakePressed;
        dst.isGasPressed = this->isGasPressed;
    }

    bool isControlSystemReady;
    bool isEnabled_ACC;
    bool isEnabled_LKS;
    bool isBrakePressed;
    bool isGasPressed;
};

/**
@brief Define Driver controllers  parameters,
*/
struct CANParams_DriverControllers {
public:
    CANParams_DriverControllers() {
        reset();
    }

    void reset() {
        rightBlinkerOn = 0;
        leftBlinkerOn = 0;
        wiperStatus = 0;
        accelBtnOn_ = false;
        decelBtnOn_ = false;
        cancelBtnOn_ = false;
        lksBtnOn_ = false;
    }

    CANParams_DriverControllers &operator=(const CANParams_DriverControllers &src) {
        if (this != &src) {
            this->rightBlinkerOn = src.rightBlinkerOn;
            this->leftBlinkerOn = src.leftBlinkerOn;
            this->wiperStatus = src.wiperStatus;
            this->accelBtnOn_ = src.accelBtnOn_;
            this->decelBtnOn_ = src.decelBtnOn_;
            this->cancelBtnOn_ = src.cancelBtnOn_;
            this->lksBtnOn_ = src.lksBtnOn_;
        }
        return *this;
    }


    //CANParams_SafetyFeature &operator=(const CANParams_SafetyFeature &src) {
    void copyTo(CANParams_DriverControllers &dst) {
        dst.rightBlinkerOn = this->rightBlinkerOn;
        dst.leftBlinkerOn = this->leftBlinkerOn;
        dst.wiperStatus = this->wiperStatus;
        dst.accelBtnOn_ = this->accelBtnOn_;
        dst.decelBtnOn_ = this->decelBtnOn_;
        dst.cancelBtnOn_ = this->cancelBtnOn_;
        dst.lksBtnOn_ = this->lksBtnOn_;
    }

    bool accelBtnOn_;
    bool decelBtnOn_;
    bool cancelBtnOn_;
    bool lksBtnOn_;
    unsigned char rightBlinkerOn;
    unsigned char leftBlinkerOn;
    unsigned char wiperStatus;  // 0 is off, 1 ~ n is speed, order increment.
};

/**
@brief Define the health of canbus
*/
class CANHealth {
public:
    CANHealth() {
        reset();
    }

    void reset() {
        controlsAllowed = 0;
        dongleConnected = 0;
    }


    CANHealth &operator=(const CANHealth &src) {
        if (this != &src) {
            this->controlsAllowed = src.controlsAllowed;
            this->dongleConnected = src.dongleConnected;
        }
        return *this;
    }

    //CANHealth &operator=(const CANHealth &src) {
    void copyTo(CANHealth &dst) {
        dst.controlsAllowed = this->controlsAllowed;
        dst.dongleConnected = this->dongleConnected;
//        this->controlsAllowed = src.controlsAllowed;
//        this->dongleConnected = src.dongleConnected;
    }

    unsigned char controlsAllowed;
    unsigned char dongleConnected;

};


struct CANPackageUnit {
    unsigned int mAddress;
    unsigned char mBusSrc;
    int mDataSize;
    unsigned char mData[8];
};

struct CANCtx_Rx
{
    CANParams_Speed canSpeedParams;
    CANParams_SteeringSensor canSteeringSensorParams;
    CANParams_SteeringControl canSteeringControlParams;
    CANParams_LKASHud canLKASHUDParams;
    CANParams_ACCHud canACCHudParams;
    CANHealth canHealth;
    CANParams_SafetyFeature canSafetyFeatureParams;
    CANParams_DriverControllers canDriverControllers;
};

struct CANCtx_Tx
{
    CANParams_SteeringControl canSteeringControlParams;
    CANParams_LKASHud canLKASHUDParams;
    CANParams_ACCHud canACCHudParams;
    CANParams_WheelButtons canWheelButtons;
};


}
}
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
namespace car {

/**
@brief Define camera types.
*/
enum class CarTypes : unsigned int {
    Unknown                       = 0,
    LUXGEN_M7_Turbo             = 1,
    HONDA_CRV_2017_BOSCH      = 2,
};


/**
@brief Define car body shell parameters, unit : cm
*/
class CarBodyShellParams{
public:
    void copyFrom(CarBodyShellParams &src) {
        this->vehicleMass           = src.vehicleMass;
        this->vehicleWidth          = src.vehicleWidth;
        this->vehicleLength         = src.vehicleLength;
        this->vehicleTailLength     = src.vehicleTailLength;
        this->vehicleFrontLength    = src.vehicleFrontLength;
    }
    float vehicleMass;
    float vehicleWidth;
    float vehicleLength;
    float vehicleTailLength;
    float vehicleFrontLength;
};


/**
@brief Define car axle parameters, unit : cm
*/
class CarAxleParams {
public:
    void copyFrom(CarAxleParams &src) {
        this->wheelBase             = src.wheelBase;
        this->frontTrack            = src.frontTrack;
        this->rearTrack             = src.rearTrack;
        this->ratioCenterToFront    = src.ratioCenterToFront;
    }
    float wheelBase;
    float ratioCenterToFront;
    float frontTrack;
    float rearTrack;
};


/**
@brief Define car tire parameters, unit : degree
*/
class CarTireParams {
public:
    void copyFrom(CarTireParams &src) {
        this->tireDegree_LeftFront  = src.tireDegree_LeftFront;
        this->tireDegree_RightFront = src.tireDegree_RightFront;
    }
    float tireDegree_LeftFront;
    float tireDegree_RightFront;
};


/**
@brief Define car speed parameters, unit : km/h
carSpeed is calculated by program, engineSpeed & wheelSpeeds are set by canbus.
*/
class CarSpeedParams {
public:
    void copyFrom(CarSpeedParams &src) {
        this->carSpeed              = src.carSpeed;
        this->engineSpeed           = src.engineSpeed;
        this->wheelSpeed_FrontLeft  = src.wheelSpeed_FrontLeft;
        this->wheelSpeed_FrontRight = src.wheelSpeed_FrontRight;
        this->wheelSpeed_RearLeft   = src.wheelSpeed_RearLeft;
        this->wheelSpeed_RearRight  = src.wheelSpeed_RearRight;
    }
    float carSpeed;
    float engineSpeed;
    float wheelSpeed_FrontLeft;
    float wheelSpeed_FrontRight;
    float wheelSpeed_RearLeft;
    float wheelSpeed_RearRight;
};


/**
@brief Define car steering parameters,
all parameter are set by canbus.
*/
class CarSteeringParams {
public:
    void copyFrom(CarSteeringParams &src) {
        this->steerAngle            = src.steerAngle;
        this->steerMaxAngle         = src.steerMaxAngle;
        this->steeringCtlAngleRate  = src.steeringCtlAngleRate;
        this->steeringRatio         = src.steeringRatio;
    }
    float steerAngle;       //unit : deg
    float steerMaxAngle;
    float steeringCtlAngleRate;   //unit : deg/s
    float steeringRatio;
};


/**
@brief Define car steering control parameters,
*/
class CarSteeringControlParams {
public:
    void copyFrom(CarSteeringControlParams &src) {
        this->steerTorqueRequest                = src.steerTorqueRequest;
        this->steerTorque                       = src.steerTorque;
        this->unit_SteerTorque_to_SteerAngle    = src.unit_SteerTorque_to_SteerAngle;
    }
    unsigned char steerTorqueRequest;
    float steerTorque;   //unit : ?
    float unit_SteerTorque_to_SteerAngle;
};


}
}
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

package com.viatech.vBus;

/**
 *@brief CANbus data (readable, not raw frame data.)
*/
public class CANbusData {

    public static class CANHealth {
        public CANHealth() {
            controls_allowed = false;
            dongleConnected = false;
        }
        public boolean controls_allowed;
        public boolean dongleConnected;
    };

    /**
     @brief Define CAN speed parameters, unit : km/h
     carSpeed is calculated by program, engineSpeed & wheelSpeeds are set by canbus.
     */
    public static class CANParams_Speed {
        public  CANParams_Speed() {
            roughSpeed = 0.0f;
            engineSpeed = 0.0f;
            wheelSpeed_FrontLeft = 0.0f;
            wheelSpeed_FrontRight = 0.0f;
            wheelSpeed_RearLeft = 0.0f;
            wheelSpeed_RearRight = 0.0f;
        }

        public float roughSpeed;
        public float engineSpeed;
        public float wheelSpeed_FrontLeft;
        public float wheelSpeed_FrontRight;
        public float wheelSpeed_RearLeft;
        public float wheelSpeed_RearRight;
    };

    /**
     @brief Define CAN steering parameters,
     all parameter are set by canbus.
     */
    public static class CANParams_SteeringSensor {
        public CANParams_SteeringSensor() {
            steerAngle = 0.0f;
            steerAngleRate = 0.0f;
            steerStatus = 0;
            steerControlActive = false;
        }
        public float steerAngle;       //unit : deg
        public float steerAngleRate;   //unit : deg/s
        public byte steerStatus;  // status of steer
        public boolean steerControlActive;
    };


    /**
     @brief Define CAN steering control parameters,
     */
    public static class CANParams_SteeringControl {
        public CANParams_SteeringControl() {
            steerTorqueRequest = false;
            steerTorque = 0;
        }
        public boolean steerTorqueRequest;
        public long steerTorque;   //unit : ?
    };


    /**
     @brief Define CAN LKAS HUD parameters,
     */
    public static class CANParams_LKASHud {
        public CANParams_LKASHud() {
            isLaneDetected = 0;
            laneType = 0;
            beep = 0;
        }
        public byte isLaneDetected;   //unit : 0/1
        public byte laneType;         //unit : depend on vehicle
        public byte beep;
    };


    /**
     @brief Define CAN ACC HUD parameters,
     */
    public static class CANParams_ACCHud {
        public CANParams_ACCHud() {
            accOn = false;
            cruiseSpeed = 0;
        }
        public boolean accOn;
        public int cruiseSpeed;
    };


    /**
     @brief Define CAN ACC/LKAS enabled parameters,
     */
    public static class CANParams_SafetyFeature {
        public CANParams_SafetyFeature() {
            isControlSystemReady = false;
            isEnabled_ACC = false;
            isEnabled_LKS = false;
            isGasPressed = false;
            isBrakePressed = false;
        }
        public boolean isControlSystemReady;
        public boolean isEnabled_ACC;
        public boolean isEnabled_LKS;
        public boolean isGasPressed;
        public boolean isBrakePressed;
    };


    /**
     @brief Define Driver controllers  parameters,
     */
    public static class CANParams_DriverControllers {
        public CANParams_DriverControllers() {
            rightBlinkerOn = false;
            leftBlinkerOn = false;
            wiperStatus = 0;
        }
        public boolean rightBlinkerOn;
        public boolean leftBlinkerOn;
        public byte wiperStatus;  // 0 is off, 1 ~ n is speed, order increment.
    };

}

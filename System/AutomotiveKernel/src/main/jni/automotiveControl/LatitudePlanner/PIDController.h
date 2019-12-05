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

#include <string>
#include <opencv2/core/core.hpp>

namespace via {
namespace automotive {
namespace latitude {


class PIDControlInput {
public:
    PIDControlInput();
    float carSpeed;
    float planDesiredSteerAngle;
    float curSteerAngle;
    float steerWhellControlAngleLimit;
    float steerTorqueLimit;
    bool steerControllable;
};


class PIDControlOutput {
public:
    PIDControlOutput();
    float ctlSteerValue;
    float steeringAngle; 
    float steeringTorque;
    bool steerControllable;

    float tErr;
    float tP;
    float tI;
    float tD;
    float tF;
    float actuatorFactor;
    float timeSlice;
};

class ControlItem {
public:
    ControlItem();
    float param;
    float param_InMinSpeed;
    float param_InMaxSpeed;
    float torque;
};

class ActuatorSmootherFilter : public ControlItem {
public:
    ActuatorSmootherFilter();
    void reset(float steer = 0);
    void update(float steer, float speed_knh, float minSpeed, float maxSpped);
    float getSteerValue();

    float value;
};

class PIDController
{
public:
    PIDController();
    ~PIDController();

    void update(PIDControlInput &in, PIDControlOutput &out);

    /**
    @brief Load & save Configuration by tag <Controller>
    */
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);
    bool save(const std::string &exportFullPath);
    bool save(cv::FileStorage &fs);


private:
    ControlItem ctl_P;
    ControlItem ctl_I;
    ControlItem ctl_D;
    ControlItem ctl_F;
    ActuatorSmootherFilter actuatorSmoother;

    float integralValue;
    float prevError;
};

}
}
}



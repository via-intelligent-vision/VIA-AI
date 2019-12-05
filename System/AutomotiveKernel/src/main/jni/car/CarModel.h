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

#include <car/CarContext.h>

namespace via {
namespace car {

struct PathNode {
    float x;
    float y;
    float yaw;
    float steerAngle;
    float lateralAccel;
    float time;
};


class CarModel
{
public:
    CarModel();
    ~CarModel();
    void init(via::car::CarContext &carCtx);
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);

    void getDrivingTrajectory(float timeCount, int timeStepCount, float velocity_kmh,
                              float steerAngleStart, float steerAngleEnd,
                              float steerTurningRate, float steerMaxTrunRate, float steerControlDelayTime_s,
                              PathNode *tC);

    void getDrivingTrajectory_K(float timeCount, int timeStepCount, float velocity_kmh,
                              float steerAngleStart, float steerAngleEnd, float steerTrunRate, float steerControlDelayTime_s,
                              PathNode *tC);

private:

    float TireModel(float slipAngle);

    via::car::CarContext carCtx;
    float mParam_InMinSpeed;
    float mParam_InMaxSpeed;
    float mParam_StiffnessGain;

    float mParam_B;
    float mParam_C;
    float mParam_D;
    float mParam_E;
    float mParam_Friction;
};


}
}


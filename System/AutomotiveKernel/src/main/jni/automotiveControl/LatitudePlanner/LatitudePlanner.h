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

#ifndef VIA_ADAS_LATITUDEPLANNER_H
#define VIA_ADAS_LATITUDEPLANNER_H

#include <iostream>
#include <ostream>

#include <car/CarDefines.h>
#include <car/CarContext.h>
#include <car/CarModel.h>
#include <vBus/CANbusDefines.h>
#include "mobile360/CameraCoord/CameraModule.h"
#include "mobile360/SensingComponent/SensingSamples.h"
#include "mobile360/LaneDetector/LaneModel.h"
#include "automotiveControl/LatitudePlanner/LatitudePlan.h"
#include "tools/TimeTag.h"

namespace via {
namespace automotive {

class LatitudePlanner {
public:
    LatitudePlanner();
    void init(via::car::CarContext *refCarContext, camera::CameraModule *refCameraModule);
    bool isInit();
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);

    bool update(via::car::CarModel *carModel,
                via::canbus::CANParams_Speed *canSpeed,
                via::canbus::CANParams_SteeringSensor *canSteeringSensor,
                via::sensing::LaneSample *sensingLane,
                via::automotive::LatitudePlan &result);
    void setCameraModule(camera::CameraModule *refCameraModule);

    void stopRecord();
    bool startRecord(std::string path, bool appendFile);
    bool isRecording();
    std::string getRecordPath();

private:
    void _stopRecord();

    // object
    std::mutex mutexObj;
    unsigned int mFrameCounter;
    bool mFlag_isActive;
    via::tools::TimeTag planTimeTag;

    // Reference Moule
    via::car::CarContext *refCarContext;
    camera::CameraModule *refCameraModule;

    // Data
    via::automotive::LatitudePlan prevResult;
    via::car::CarAxleParams carAxleParams;
    via::car::CarBodyShellParams carBodyShellParams;
    via::car::CarTireParams carTireParams;
    via::car::CarSteeringParams carSteeringParams;

    double trajectoryError(via::car::PathNode *tC, int sampleCount, via::sensing::lane::LaneMarkingContext &guidance, float laneWidthcm, float laneLength_m);
    double calcTrajectoryError_Drep(cv::Point2f *tL, cv::Point2f *tR, int sampleCount,
                               cv::Point3f &laneP0_L, cv::Point3f &laneP1_L, cv::Point3f &laneP2_L,
                               cv::Point3f &laneP0_R, cv::Point3f &laneP1_R, cv::Point3f &laneP2_R,
                               float laneWidthcm);
    int mParam_ManualSpeed;
    bool mParam_ApplyDriverWheel;
    float mParam_ManualSteering;
    int mParam_PlaneUpdateTime;
    bool mParam_UseKinematic;

    float mParam_LaneLengthGain_MinSpeed;
    float mParam_LaneLengthGain_MinValue;
    float mParam_LaneLengthGain_MaxSpeed;
    float mParam_LaneLengthGain_MaxValue;

    float mParam_MPC_Gain_Offset;
    float mParam_MPC_Gain_pB;
    float mParam_MPC_Time;
    int mParam_MPC_OutputPathIndex;

    float mParam_IntuitiveSteering_MaxValue;
    float mParam_IntuitiveSteering_LearningRate;

    float mParam_DriftMinFactor;
    float mParam_LearnRate_Drift;

    float mParam_RawPredectionFactor;

    float mParam_LaneOffsetcm;

    float mParam_AngleOffset_IN;
    float mParam_AngleOffset_OUT;

    float mParam_SteerTurningRateFactor;


    // recoder
    std::mutex recoderMutex;
    bool b_IsRecording;
    double recordStartTime;
    std::string recordPath;
    std::ofstream recordStream;
//    void record(via::automotive::latitude::PIDControlInput &pidIn ,
//                via::automotive::latitude::PIDControlOutput &pidOut,
//                float outSteerTorqueValue);

};


}
}
#endif //VIA_ADAS_LATITUDEPLANNER_H

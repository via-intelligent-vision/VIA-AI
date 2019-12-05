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


#include <unistd.h>
#include <vector>
#include <opencv2/core/core.hpp>
#include "mobile360/adas-core/utils/mathTool.h"
#include "LatitudePlanner.h"

//--------------------------------------------------------------------------------------------------
#include <android/log.h>
#define  LOG_TAG    "LatitudePlanner"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


//--------------------------------------------------------------------------------------------------
using namespace via::camera;
using namespace via::sensing;
using namespace via::automotive;
using namespace std;
using namespace cv;

//--------------------------------------------------------------------------------------------------
#define TAG_Controller                  "Controller"
#define TAG_LatitudePlane               "LatitudePlane"
#define TAG_Param_ManualSpeed           "Param_ManualSpeed"
#define TAG_Param_ApplyDriverWheel      "Param_ApplyDriverWheel"
#define TAG_Param_ManualSteering        "Param_ManualSteering"
#define TAG_Param_PlaneUpdateTime       "Param_PlaneUpdateTime"
#define TAG_Param_UseKinematic          "Param_UseKinematic"

#define TAG_Param_LaneLengthGain_MinSpeed          "Param_LaneLengthGain_MinSpeed"
#define TAG_Param_LaneLengthGain_MinValue          "Param_LaneLengthGain_MinValue"
#define TAG_Param_LaneLengthGain_MaxSpeed          "Param_LaneLengthGain_MaxSpeed"
#define TAG_Param_LaneLengthGain_MaxValue          "Param_LaneLengthGain_MaxValue"

#define TAG_Param_MPC_Gain_Offset            "Param_MPC_Gain_Offset"
#define TAG_Param_MPC_Gain_pB               "Param_MPC_Gain_pB"
#define TAG_Param_MPC_Time                  "Param_MPC_Time"
#define TAG_Param_MPC_OutputPathIndex        "Param_MPC_OutputPathIndex"

#define TAG_Param_LaneOffsetcm                  "Param_LaneOffsetcm"


#define TAG_Param_IntuitiveSteering_MaxValue          "Param_IntuitiveSteering_MaxValue"
#define TAG_Param_IntuitiveSteering_LearningRate       "Param_IntuitiveSteering_LearningRate"

#define TAG_Param_RawPredectionFactor   "Param_RawPredectionFactor"

#define TAG_Param_AngleOffset_IN    "Param_AngleOffset_IN"
#define TAG_Param_AngleOffset_OUT   "Param_AngleOffset_OUT"

#define TAG_Param_SteerTurningRateFactor "Param_SteerTurningRateFactor"

#define TAG_Param_DriftMinFactor      "Param_DriftMinFactor"
#define TAG_Param_LearnRate_Drift      "Param_LearnRate_Drift"


//--------------------------------------------------------------------------------------------------

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
LatitudePlanner::LatitudePlanner()
{
    refCarContext = NULL;
    refCameraModule = NULL;

    b_IsRecording = false;
    recordStartTime = 0;
    recordPath ="";

    mFrameCounter = 0;

    mParam_IntuitiveSteering_MaxValue = 0;
    mParam_IntuitiveSteering_LearningRate = 0.0f;
    mParam_DriftMinFactor = 0;
    mParam_LearnRate_Drift = 0;

    mParam_UseKinematic = false;
    mParam_ManualSpeed = -1;
    mParam_ApplyDriverWheel = false;
    mParam_ManualSteering = 0;
    mParam_PlaneUpdateTime = 50;

    mParam_LaneLengthGain_MinSpeed = 30.0f;
    mParam_LaneLengthGain_MinValue = 1.0f;
    mParam_LaneLengthGain_MaxSpeed = 145.0f;
    mParam_LaneLengthGain_MaxValue = 1.0f;

    mParam_MPC_Gain_Offset = 0;
    mParam_MPC_Gain_pB = 0;
    mParam_MPC_Time = 3;
    mParam_MPC_OutputPathIndex = 1;

    mParam_LaneOffsetcm = 0;

    mParam_IntuitiveSteering_MaxValue = 0;
    mParam_IntuitiveSteering_LearningRate = 0.0f;
    mParam_RawPredectionFactor = 1.0f;

    mParam_AngleOffset_IN = 0;
    mParam_AngleOffset_OUT = 0;

    mParam_SteerTurningRateFactor = 1.0f;
}

void LatitudePlanner::init(via::car::CarContext *carContext, camera::CameraModule *cameraModule)
{
    LOGE("LatitudePlanner::init");
    refCarContext = carContext;
    carContext->getCarAxleParams(carAxleParams);
    carContext->getCarBodyShellParams(carBodyShellParams);
    carContext->getCarTireParams(carTireParams);
    carContext->getCarSteeringParams(carSteeringParams);
    
    setCameraModule(cameraModule);
}


bool LatitudePlanner::load(const std::string &xmlPath)
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

    return ret;
}

bool LatitudePlanner::load(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        FileNode nodeController;    // Due to compatibility, <LDWS> <FCWS> <Camera> is accepted.
        if (fs[TAG_Controller].isNamed()) {
            nodeController = fs[TAG_Controller];
        }

        if (!nodeController.isNone()) {
            FileNode nodeLatitudePlane;
            if (nodeController[TAG_LatitudePlane].isNamed()) {
                nodeLatitudePlane = nodeController[TAG_LatitudePlane];
            }

            if (!nodeLatitudePlane.isNone()) {
                nodeLatitudePlane[TAG_Param_ManualSpeed] >> this->mParam_ManualSpeed;
                nodeLatitudePlane[TAG_Param_ApplyDriverWheel] >> this->mParam_ApplyDriverWheel;
                nodeLatitudePlane[TAG_Param_ManualSteering] >> this->mParam_ManualSteering;
                nodeLatitudePlane[TAG_Param_PlaneUpdateTime] >> this->mParam_PlaneUpdateTime;
                nodeLatitudePlane[TAG_Param_UseKinematic] >> this->mParam_UseKinematic;


                nodeLatitudePlane[TAG_Param_LaneLengthGain_MinSpeed] >> this->mParam_LaneLengthGain_MinSpeed;
                nodeLatitudePlane[TAG_Param_LaneLengthGain_MinValue] >> this->mParam_LaneLengthGain_MinValue;
                nodeLatitudePlane[TAG_Param_LaneLengthGain_MaxSpeed] >> this->mParam_LaneLengthGain_MaxSpeed;
                nodeLatitudePlane[TAG_Param_LaneLengthGain_MaxValue] >> this->mParam_LaneLengthGain_MaxValue;

                nodeLatitudePlane[TAG_Param_MPC_Gain_Offset] >> this->mParam_MPC_Gain_Offset;
                nodeLatitudePlane[TAG_Param_MPC_Gain_pB] >> this->mParam_MPC_Gain_pB;
                nodeLatitudePlane[TAG_Param_MPC_Time] >> this->mParam_MPC_Time;
                nodeLatitudePlane[TAG_Param_MPC_OutputPathIndex] >> this->mParam_MPC_OutputPathIndex;

                nodeLatitudePlane[TAG_Param_LaneOffsetcm] >> this->mParam_LaneOffsetcm;


                nodeLatitudePlane[TAG_Param_IntuitiveSteering_MaxValue] >> this->mParam_IntuitiveSteering_MaxValue;
                nodeLatitudePlane[TAG_Param_IntuitiveSteering_LearningRate] >> this->mParam_IntuitiveSteering_LearningRate;



                nodeLatitudePlane[TAG_Param_RawPredectionFactor] >> this->mParam_RawPredectionFactor;

                nodeLatitudePlane[TAG_Param_AngleOffset_IN] >> this->mParam_AngleOffset_IN;
                nodeLatitudePlane[TAG_Param_AngleOffset_OUT] >> this->mParam_AngleOffset_OUT;

                nodeLatitudePlane[TAG_Param_SteerTurningRateFactor] >> this->mParam_SteerTurningRateFactor;


                nodeLatitudePlane[TAG_Param_DriftMinFactor] >> this->mParam_DriftMinFactor;
                nodeLatitudePlane[TAG_Param_LearnRate_Drift] >> this->mParam_LearnRate_Drift;

                ret = true;
            }
            else {

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
        LOGE("this->mParam_ManualSpeed       %d", this->mParam_ManualSpeed);
        LOGE("this->mParam_ApplyDriverWheel  %d", this->mParam_ApplyDriverWheel);
        LOGE("this->mParam_ManualSteering    %f", this->mParam_ManualSteering);
        LOGE("this->mParam_PlaneUpdateTime    %d", this->mParam_PlaneUpdateTime);
        LOGE("this->mParam_UseKinematic    %d", this->mParam_UseKinematic);

        LOGE("this->mParam_LaneLengthGain_MinSpeed    %f", this->mParam_LaneLengthGain_MinSpeed);
        LOGE("this->mParam_LaneLengthGain_MinValue    %f", this->mParam_LaneLengthGain_MinValue);
        LOGE("this->mParam_LaneLengthGain_MaxSpeed    %f", this->mParam_LaneLengthGain_MaxSpeed);
        LOGE("this->mParam_LaneLengthGain_MaxValue    %f", this->mParam_LaneLengthGain_MaxValue);

        LOGE("this->mParam_MPC_Gain_Offset         %f", this->mParam_MPC_Gain_Offset);
        LOGE("this->mParam_MPC_Gain_pB              %f", this->mParam_MPC_Gain_pB);
        LOGE("this->mParam_MPC_Time                  %f", this->mParam_MPC_Time);
        LOGE("this->mParam_MPC_OutputPathIndex    %f", this->mParam_MPC_OutputPathIndex);

        LOGE("this->mParam_LaneOffsetcm           %f", this->mParam_LaneOffsetcm);

        LOGE("this->mParam_IntuitiveSteering_MaxValue  %f", this->mParam_IntuitiveSteering_MaxValue);
        LOGE("this->mParam_IntuitiveSteering_LearningRate  %f", this->mParam_IntuitiveSteering_LearningRate);

        LOGE("this->mParam_RawPredectionFactor  %f", this->mParam_RawPredectionFactor);

        LOGE("this->mParam_SteerTurningRateFactor  %f", this->mParam_SteerTurningRateFactor);

        LOGE("this->Param_DriftMinFactor  %f", this->mParam_DriftMinFactor);
        LOGE("this->Param_LearnRate_Drift  %f", this->mParam_LearnRate_Drift);
        dbg = false;
    }

    return ret;
}


void LatitudePlanner::setCameraModule(camera::CameraModule *cameraModule)
{
    refCameraModule = cameraModule;

    CalibrationPatternData caliPattern;
    refCameraModule->getCalibrationPatternData(caliPattern);

    if(refCameraModule != NULL) {
        LOGE("setCameraModule , camera type  %d", (int) cameraModule->getCameraType());
    }
}

bool LatitudePlanner::isInit()
{
    return (refCarContext != NULL && refCameraModule != NULL);
}

bool LatitudePlanner::isRecording() {
    return b_IsRecording;
}

std::string LatitudePlanner::getRecordPath() {
    return recordPath;
}

void LatitudePlanner::_stopRecord() {
    if(recordStream.is_open()) {
        recordStream.close();
        LOGE("close recordStream");
    }
    b_IsRecording = false;
}

void LatitudePlanner::stopRecord() {
    std::lock_guard<std::mutex> lock(recoderMutex);
    _stopRecord();
}

bool LatitudePlanner::startRecord(std::string path, bool appendFile) {
    std::lock_guard<std::mutex> lock(recoderMutex);

    // stop record first.
    _stopRecord();

    recordPath = path;

    if(appendFile) {
        recordStream.open(recordPath, std::ios::out | std::ios::app);
    }
    else {
        recordStream.open(recordPath, std::ios::out | std::ios::trunc);
    }

    recordStartTime = via::tools::getms();
    b_IsRecording = recordStream.is_open();

    LOGE("recordPath %s .... %d", recordPath.c_str(), b_IsRecording);
    return b_IsRecording;
}

bool LatitudePlanner::update(via::car::CarModel *carModel,
                             via::canbus::CANParams_Speed *canSpeed,
                             via::canbus::CANParams_SteeringSensor *canSteeringSensor,
                             via::sensing::LaneSample *sensingLane,
                             via::automotive::LatitudePlan &result)
{
    bool ret = false;
    if(planTimeTag.diffNow() < this->mParam_PlaneUpdateTime) return ret;
    planTimeTag.updateNow();

    double curTime = via::tools::getms();
    //LOGE("ABC update new LatitudePlanner");
    do {
        if(carModel == NULL || canSpeed == NULL || canSteeringSensor == NULL || sensingLane == NULL) {
            result.reset();
            break;
        }

        canSteeringSensor->steerAngle -= this->mParam_AngleOffset_IN;


        // check steer
        const int STEER_LIMIT = 25;
        if(fabs(canSteeringSensor->steerAngle) > STEER_LIMIT) {
            result.planStartSteerAngle = canSteeringSensor->steerAngle;
            result.planDesiredSteerAngle = 0;
            result.isPlanValid = false;
            break;
        }

        // check steering rate between previous plane time.
        const float steerMaxTurnRate = 18.0f; // unit : degree /s
        float steerTurningRate = 0.0;
        if(this->prevResult.isPlanValid) {
            double timeDiff = via::tools::getms() - this->prevResult.planTime;
            if(timeDiff > 0.0  && timeDiff < 1000.0) {
                double steerDiff = canSteeringSensor->steerAngle - this->prevResult.planStartSteerAngle;
                steerTurningRate = (float)(steerDiff *1000.0 / timeDiff);
            }
        }
        steerTurningRate *= mParam_SteerTurningRateFactor;

        // precheck lane sample
        bool isLaneDetected = (sensingLane->mLaneStatus != LaneSample::SampleStatus::NoDetected) &&
                (sensingLane->mLaneStatus != LaneSample::SampleStatus::Calibrating) &&
                (sensingLane->mLaneStatus != LaneSample::SampleStatus::Unknown);

        if (!isLaneDetected) {
            //LOGE("(sensingLane->mLaneStatus != LaneDetectSample::SampleStatus::Detected) ");

            result.planStartSteerAngle = canSteeringSensor->steerAngle;
            result.planDesiredSteerAngle = 0;
            result.isPlanValid = false;

            // ---------------------------------------------------
            // TODO : Remove
            bool forceValid = false;
            float steerAngleStart = 0;
            float steerAngleEnd = 0;
            float speed = canSpeed->roughSpeed;
            if(speed < 22) speed = 22;

            if(this->mParam_ManualSteering != 0) {
                steerAngleEnd = this->mParam_ManualSteering;
                forceValid = true;
            }
            if(this->mParam_ManualSpeed != 0) {
                speed = this->mParam_ManualSpeed;
                forceValid = true;
            }


            // Generate trajectory
            via::car::PathNode trajectoryC[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
            float drivingTime = 100.0f / ((speed * 1000.0f) / 3600.0f);

            if( this->mParam_UseKinematic) {
                carModel->getDrivingTrajectory_K(drivingTime, LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT, speed, steerAngleStart, steerAngleEnd, 5, 0.2, trajectoryC);
            }
            else {
                carModel->getDrivingTrajectory(drivingTime, LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT, speed, steerAngleStart, steerAngleEnd, steerTurningRate, 5, 0.2, trajectoryC);
            }
            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mTrajectory_L[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(trajectoryC[i].x - 100, trajectoryC[i].y, 0.0f);
            }
            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mTrajectory_R[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(trajectoryC[i].x + 100, trajectoryC[i].y, 0.0f);
            }

            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mTrajectory_L[i] = Point2f(0.0f, 0.0f);
                result.mTrajectory_R[i] = Point2f(0.0f, 0.0f);
            }
            result.mLaneScore_L = 0.0f;
            result.mLaneScore_R = 0.0f;
            result.planTime = curTime;
            result.planStartSteerAngle = steerAngleStart;
            result.planDesiredSteerAngle = steerAngleEnd;
            result.planTotalSteeringTime = drivingTime;
            result.isSteerOverControl = false;
            result.isPlanValid = forceValid;
            // TODO : Remove
            // ---------------------------------------------------
            break;
        }

        // check speed
        float vehicleSpeed = canSpeed->roughSpeed;
        if(vehicleSpeed < 22) vehicleSpeed = 22;

        // update Lane model
        via::sensing::lane::LaneModelContext laneCtx;
        sensingLane->mLaneModelCtx.copyTo(laneCtx);

        laneCtx.parabola.cC += mParam_LaneOffsetcm;

        // compute MPC time
        float speedGain = interpolate<float>(this->mParam_LaneLengthGain_MinValue,  this->mParam_LaneLengthGain_MaxValue, vehicleSpeed, this->mParam_LaneLengthGain_MinSpeed, this->mParam_LaneLengthGain_MaxSpeed);
        float mpcTime = this->mParam_MPC_Time * speedGain;



        // DO Calculation for Lane MPC (dummy MCP now) ...
        // TODO :  Refine this
        const float steerControlDelayTime_s = 0.1f;
        via::car::PathNode trajectoryC[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
        cv::Point3f laneObjBtmCenter;
        //float drivingTime = 0.0f;
        float steerAngleStart = canSteeringSensor->steerAngle;
        float steerAngleEnd = 0;
        bool isPlaneValid = false;

        int resampleCount = LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT;
        vector<Point3f> mLaneAnchors_CCC((unsigned long)resampleCount);
        vector<Point3f> mLaneAnchors_CCL((unsigned long)resampleCount);
        vector<Point3f> mLaneAnchors_CCR((unsigned long)resampleCount);
        do {
            if(fabs(canSteeringSensor->steerAngle) > STEER_LIMIT) break;

            float laneSideOffset_m = 0;
            float laneSamplingTime_s = 0;

            // resample lane sample
            cv::Point3f mLaneObjAnchor0_L, mLaneObjAnchor1_L, mLaneObjAnchor2_L;
            cv::Point3f mLaneObjAnchor0_R, mLaneObjAnchor1_R, mLaneObjAnchor2_R;

            float laneSamplingLength_m= mpcTime * vehicleSpeed * 0.277777777777f;
            do {
                float lengthStep = (laneSamplingLength_m * 100) / (resampleCount - 1); //50m

                for (int si = 0; si < resampleCount; si++) {
                    Point3f anchorC, anchorL, anchorR;
                    float dy = si * lengthStep;
                    anchorC.y = dy;
                    anchorC.x = laneCtx.parabola.cA * anchorC.y * anchorC.y + laneCtx.parabola.cB * anchorC.y + laneCtx.parabola.cC;
                    anchorC.z = 0;

                    float W = laneCtx.width;
                    float ex = (W / (2 * (laneCtx.parabola.cB + laneCtx.parabola.cA * dy) * (laneCtx.parabola.cB + laneCtx.parabola.cA * dy) + 2));
                    float ey = ((W * (laneCtx.parabola.cB + laneCtx.parabola.cA * dy)) / (2 * (laneCtx.parabola.cB + laneCtx.parabola.cA * dy) * (laneCtx.parabola.cB + laneCtx.parabola.cA * dy) + 2));
                    anchorL.y = dy + ey;
                    anchorL.x = anchorC.x - ex;
                    anchorL.z = 0;

                    anchorR.y = dy + ey;
                    anchorR.x = anchorC.x + ex;
                    anchorR.z = 0;

                    mLaneAnchors_CCC[si] = anchorC;
                    mLaneAnchors_CCL[si] = anchorL;
                    mLaneAnchors_CCR[si] = anchorR;
                }

                // resample 3 points
                mLaneObjAnchor0_L = mLaneAnchors_CCL[0];
                mLaneObjAnchor1_L = mLaneAnchors_CCL[resampleCount / 2];
                mLaneObjAnchor2_L = mLaneAnchors_CCL[resampleCount - 1];
                mLaneObjAnchor0_R = mLaneAnchors_CCR[0];
                mLaneObjAnchor1_R = mLaneAnchors_CCR[resampleCount / 2];
                mLaneObjAnchor2_R = mLaneAnchors_CCR[resampleCount - 1];

                laneObjBtmCenter = mLaneAnchors_CCC[0];
            } while (false);


            // How many times achive the end of sampleing trajectory?
            //float distancem = (float)(cv::norm(mLaneObjAnchor0_L - mLaneObjAnchor1_L) + cv::norm(mLaneObjAnchor1_L - mLaneObjAnchor2_L)) / 100.0f;

            // iterate the result.
            float degA = -22;
            float degB = 22;
            double eA = 0.0, eB = 0.0;
            int inter = 64;
            if(degA < -STEER_LIMIT) degA = -STEER_LIMIT;
            if(degB >  STEER_LIMIT) degB = STEER_LIMIT;

            do {
                if(this->mParam_UseKinematic) {
                    carModel->getDrivingTrajectory_K(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degA, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                }
                else {
                    carModel->getDrivingTrajectory(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degA, steerTurningRate, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                }
                eA = trajectoryError(trajectoryC, resampleCount, laneCtx.parabola, laneCtx.width, laneSamplingLength_m);
            } while (false);

            do {
                if(this->mParam_UseKinematic) {
                    carModel->getDrivingTrajectory_K(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degB, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                }
                else {
                    carModel->getDrivingTrajectory(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degB, steerTurningRate, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                }
                eB = trajectoryError(trajectoryC, resampleCount, laneCtx.parabola, laneCtx.width, laneSamplingLength_m);
            } while (false);

            //while (inter > 0 && fabs(degA - degB) > 1.0f) {
            while (inter > 0 && fabs(degA - degB) > 0.1f && (fabs(eA) > 0.0001 || fabs(eB) > 0.0001f)) {
                if (eA < eB) {
                    degB = 0.15f *degA + 0.85f * degB;
                    if(this->mParam_UseKinematic) {
                        carModel->getDrivingTrajectory_K(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degB, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                    }
                    else {
                        carModel->getDrivingTrajectory(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degB, steerTurningRate, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                    }
                    eB = trajectoryError(trajectoryC, resampleCount, laneCtx.parabola, laneCtx.width, laneSamplingLength_m);
                }
                else {
                    degA = 0.85f* degA + 0.15f * degB;
                    if(this->mParam_UseKinematic) {
                        carModel->getDrivingTrajectory_K(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degA, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                    }
                    else {
                        carModel->getDrivingTrajectory(mpcTime, resampleCount, vehicleSpeed, steerAngleStart, degA, steerTurningRate, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
                    }
                    eA = trajectoryError(trajectoryC, resampleCount, laneCtx.parabola, laneCtx.width, laneSamplingLength_m);
                }
                inter--;
            }

            // avg end angle
            steerAngleEnd = (degA + degB) * 0.5f;
            isPlaneValid = true;



            if(b_IsRecording) {
                std::lock_guard<std::mutex> lock(recoderMutex);
                if(recordStream.is_open()) {
                    double timeDiff = (curTime - recordStartTime) * 0.001;
                    std::stringstream value;
                    value << timeDiff << ","
                          << laneCtx.parabola.cA << ","
                          << laneCtx.parabola.cB << ","
                          << laneCtx.parabola.cC << ","
                          << laneCtx.width << ","
                          << vehicleSpeed << ","
                          << steerAngleStart << ","
                          << steerAngleEnd << ","
                          << mpcTime << ","
                          << steerTurningRate
                          << endl;
                    recordStream.write(value.str().c_str(), strlen(value.str().c_str()));
                    static int CC = 0;
                    if(CC % 100 ==0) {
                        recordStream.flush();
                    }
                    CC++;
                }
            }
        } while(false);


        // Do intuitiveSteering
        float intuitiveSteering = 0;
        do {
            float cloestOffset = interpolate<float>(40.0f, 100.0f, laneCtx.width, 350, 450);

            // P control.
            float speedGain = 1.0f;
            if (laneCtx.parabola.cC < 0) {
                float diff = (float)((laneCtx.parabola.cC + 0.5f * laneCtx.width) - (0.5f * carBodyShellParams.vehicleWidth));
                intuitiveSteering = interpolate<float>( this->mParam_IntuitiveSteering_MaxValue, 0, diff, 0, cloestOffset) * speedGain;
            }
            else {
                // get left len anchor
                float diff = (-0.5f * carBodyShellParams.vehicleWidth) - (float)(laneCtx.parabola.cC - 0.5f * laneCtx.width);
                intuitiveSteering = -1.0f * interpolate<float>( this->mParam_IntuitiveSteering_MaxValue, 0, diff, 0, cloestOffset) * speedGain;
                //intuitiveSteering = interpolate<float>(-this->mParam_IntuitiveSteering_MaxValue, 0, diff, 0, cloestOffset) * speedGain;
                //intuitiveSteering = -1.0f * interpolate<float>(this->mParam_IntuitiveSteering_MaxValue, 0, diff, 0, cloestOffset) * speedGain;
            }
        } while (false);


        // TODO : Remove
        if(this->mParam_ManualSteering != 0) {
            steerAngleEnd = this->mParam_ManualSteering;
          //  drivingTime = 100.0f / ((vehicleSpeed * 1000.0f) / 3600.0f);
        }
//        if(this->mParam_ApplyDriverWheel) {
//            steerAngleEnd = steerAngleStart;
//            drivingTime = 100.0f / ((vehicleSpeed * 1000.0f) / 3600.0f);
//        }
        //TODO: --------------------------------------------


        // Generate output data
        if (isLaneDetected && isPlaneValid) {
            if(this->mParam_UseKinematic) {
                carModel->getDrivingTrajectory_K(mpcTime, LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT, vehicleSpeed, steerAngleStart, steerAngleEnd, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
            }
            else {
                carModel->getDrivingTrajectory(mpcTime, LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT, vehicleSpeed, steerAngleStart, steerAngleEnd,
                                               steerTurningRate, steerMaxTurnRate, steerControlDelayTime_s, trajectoryC);
            }

            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mTrajectory_L[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(trajectoryC[i].x - 50, trajectoryC[i].y, 0.0f);
            }
            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mTrajectory_R[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(trajectoryC[i].x + 50, trajectoryC[i].y, 0.0f);
            }

            for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                result.mLaneAnchors_L[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(mLaneAnchors_CCL[i].x, mLaneAnchors_CCL[i].y, 0.0f);
                result.mLaneAnchors_R[i] = refCameraModule->coordCvt_ObjCoord_To_NormalizedImgCoord(mLaneAnchors_CCR[i].x, mLaneAnchors_CCR[i].y, 0.0f);
            }
            result.mLaneScore_L = laneCtx.driftRatio_L;
            result.mLaneScore_R = laneCtx.driftRatio_R;
            //result.mLaneScore_L = laneCtx.prob_L;
            //result.mLaneScore_R = laneCtx.prob_R;

            result.planTime = curTime;
            result.planStartSteerAngle = steerAngleStart;
            result.planDesiredSteerAngle = trajectoryC[mParam_MPC_OutputPathIndex].steerAngle + intuitiveSteering + this->mParam_AngleOffset_OUT;
            result.planTotalSteeringTime = mpcTime;
            result.mLaneObjBtmCenter = laneObjBtmCenter;
            result.isPlanValid = true;
        }
        else {
            result.planTime = curTime;
            result.planStartSteerAngle = steerAngleStart;
            result.planDesiredSteerAngle = 0;
            result.isPlanValid = false;
        }


    } while(false);

    this->prevResult = result;
    this->mFrameCounter++;
    ret = true;

    return ret;
}

double LatitudePlanner::calcTrajectoryError_Drep(cv::Point2f *tL, cv::Point2f *tR, int sampleCount,
                                            cv::Point3f &laneP0_L, cv::Point3f &laneP1_L, cv::Point3f &laneP2_L,
                                            cv::Point3f &laneP0_R, cv::Point3f &laneP1_R, cv::Point3f &laneP2_R,
                                            float laneWidthcm)
{
    double E = 0;


    do {
        Point2f refP = (Point2f(laneP2_L.x, laneP2_L.y) + Point2f(laneP2_R.x, laneP2_R.y)) * 0.5f;
        Point2f pCenter = (tL[sampleCount -1] + tR[sampleCount -1]) * 0.5f;

        refP.y *= 0.0001f;
        pCenter.y *= 0.0001f;

        E = cv::norm(pCenter - refP);
    } while(false);

    return E;

    // find the last node, which y in [p0] - [p1]
    int lastIndex = 0;
    Point3f laneCenter = (laneP1_L + laneP1_R) * 0.5f;
    for (int i = 0; i < sampleCount; i++) {
        Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
        lastIndex = i;
        if (pCenter.y > laneCenter.y) break;
    }


    for (int i = 0; i < lastIndex; i++) {
        //Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
        //double diffCL = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y));
        //double diffCR = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y));
        double diffLL = abs(mathTool::CalcDistancePointToLine(Point2f(tL[i].x, tL[i].y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y)));
        double diffLR = abs(mathTool::CalcDistancePointToLine(Point2f(tL[i].x, tL[i].y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y)));
        double diffRL = abs(mathTool::CalcDistancePointToLine(Point2f(tR[i].x, tR[i].y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y)));
        double diffRR = abs(mathTool::CalcDistancePointToLine(Point2f(tR[i].x, tR[i].y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y)));
        //E += (diffL * diffL) + (diffR * diffR);
        double diffL = laneWidthcm - (diffLL + diffLR);
        double diffR = laneWidthcm - (diffRL + diffRR);
        //double diffC = laneWidthm - (diffCL + diffCR);
        E += diffL * diffL + diffR * diffR;
    }

    for (int i = lastIndex; i < sampleCount; i++) {
        Point2f refP = (Point2f(laneP2_L.x, laneP2_L.y) + Point2f(laneP2_R.x, laneP2_R.y)) * 0.5f;
        Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
        double diffCL = mathTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
        double diffCR = mathTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
        double diffLL = mathTool::CalcDistancePointToLine(Point2f(tL[i].x, tL[i].y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
        double diffLR = mathTool::CalcDistancePointToLine(Point2f(tL[i].x, tL[i].y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
        double diffRL = mathTool::CalcDistancePointToLine(Point2f(tR[i].x, tR[i].y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
        double diffRR = mathTool::CalcDistancePointToLine(Point2f(tR[i].x, tR[i].y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
        //E += (diffL * diffL) + (diffR * diffR);
        double diffL = laneWidthcm - (diffLL + diffLR);
        double diffR = laneWidthcm - (diffRL + diffRR);
        double diffC = laneWidthcm - (diffCL + diffCR);
        //E += diffL * diffL + diffR * diffR + diffC * diffC;


        if (i == (sampleCount - 1)) {
            E = cv::norm(pCenter - refP);
//            if (diffC > 0 && diffC < 1.0f) diffC = 1.0f;
//            if (diffC < 0 && diffC > -1.0f) diffC = 1.0f;
//            double r0 = fabs(diffCL) / fabs(diffCR);
//            double r1 = fabs(diffCR) / fabs(diffCL);
//            if (r0 > r1)
//                E = r0 * diffC * diffC;
//            else
//                E = r1 * diffC * diffC;
        }
        else {
            E += (diffL * diffL + diffR * diffR + diffC * diffC);
        }

        // Part 1
//        if ((i == (sampleCount - 1)) && diffCL > 0 && diffCR > 0) {
//            double r0 = diffCL / diffCR;
//            double r1 = diffCR / diffCL;
//            if (r0 > r1)
//                E *= r0;
//            else
//                E *= r1;
//        }
//        else {
//            E += (diffL * diffL + diffR * diffR + diffC * diffC);
//        }

        // Part 2
//        if ((i == (sampleCount-1)) && diffCL > 0 && diffCR > 0) {
//            double r0 = diffCL / diffCR;
//            double r1 = diffCR / diffCL;
//            if(r0 > r1)
//                E += (diffL * diffL + diffR * diffR + diffC * diffC) * r0;
//            else
//                E += (diffL * diffL + diffR * diffR + diffC * diffC) * r1;
//        }
//        else {
//            E += (diffL * diffL + diffR * diffR + diffC * diffC);
//        }
//
    }




//    double E = 0;
//
//    // find the last node, which y in [p0] - [p1]
//    int lastIndex = 0;
//    Point3f laneCenter = (laneP1_L + laneP1_R) * 0.5f;
//    for (int i = 0; i < sampleCount; i++) {
//        Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
//        lastIndex = i;
//        if (pCenter.y > laneCenter.y) break;
//    }
//
//    for (int i = 0; i < lastIndex; i++) {
//        Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }
//
//    for (int i = lastIndex; i < sampleCount; i++) {
//        Point2f pCenter = (tL[i] + tR[i]) * 0.5f;
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pCenter.x, pCenter.y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }



//    for (int i = 0; i < lastIndexL; i++) {
//        Point3f pL = Point3f(tL[i].x, tL[i].y, 0.0f);
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pL.x, pL.y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pL.x, pL.y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }
//
//    for (int i =  lastIndexL; i < sampleCount; i++) {
//        Point3f pL = Point3f(tL[i].x, tL[i].y, 0.0f);
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pL.x, pL.y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pL.x, pL.y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }
//
//    for (int i = 0; i < lastIndexR; i++) {
//        Point3f pR = Point3f(tR[i].x, tR[i].y, 0.0f);
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pR.x, pR.y), Point2f(laneP0_L.x, laneP0_L.y), Point2f(laneP1_L.x, laneP1_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pR.x, pR.y), Point2f(laneP0_R.x, laneP0_R.y), Point2f(laneP1_R.x, laneP1_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }
//
//    for (int i = lastIndexR; i < sampleCount; i++) {
//        Point3f pR = Point3f(tR[i].x, tR[i].y, 0.0f);
//        double diffL = MathematicalTool::CalcDistancePointToLine(Point2f(pR.x, pR.y), Point2f(laneP1_L.x, laneP1_L.y), Point2f(laneP2_L.x, laneP2_L.y));
//        double diffR = MathematicalTool::CalcDistancePointToLine(Point2f(pR.x, pR.y), Point2f(laneP1_R.x, laneP1_R.y), Point2f(laneP2_R.x, laneP2_R.y));
//        E += (diffL * diffL) + (diffR * diffR);
//    }




    return E;
}


double LatitudePlanner::trajectoryError(via::car::PathNode *tC, int sampleCount, via::sensing::lane::LaneMarkingContext &guidance, float laneWidthcm, float laneLength_m)
{
    double E = 0;

    const float rad90 = 90.0 * CV_PI / 180.0f;
    float lengthStep = (laneLength_m * 100) / (sampleCount - 1); //50m
    for (int si = 0; si < sampleCount; si++) {
        float dy = si * lengthStep;

        Point2f v0, v1;
        v0.y = dy - 0.1f;
        v0.x = guidance.cA * v0.y * v0.y + guidance.cB * v0.y + guidance.cC;
        v1.y = dy + 0.1f;
        v1.x = guidance.cA * v1.y * v1.y + guidance.cB * v1.y + guidance.cC;

        Point2f v = v1 - v0;

        Point2f vModel;
        vModel.x = cos(rad90 + tC[si].yaw);
        vModel.y = sin(rad90 + tC[si].yaw);

        float dot = v.dot(vModel) / (cv::norm(v) * cv::norm(vModel));
        if (dot > 1) dot = 1;
        else if (dot < -1) dot = -1;

        E += acos(dot) * 180.0f / CV_PI;
    }

    for (int i = 0; i < sampleCount ; i++) {
        Point2f refC;
        refC.y = tC[i].y;
        refC.x = (guidance.cA * refC.y * refC.y + guidance.cB * refC.y + guidance.cC) ;

        //double err = cv::norm(refC - pCenter);
        double err = fabs(refC.x - tC[i].x);
        E += err * err * mParam_MPC_Gain_pB;
        //if (i > sampleCount / 2) E += err * err * err * err;
    }

    do {
        via::car::PathNode pCenter = tC[sampleCount -1];
        Point3f refC;
        refC.y = pCenter.y;
        refC.x = (guidance.cA * refC.y * refC.y + guidance.cB * refC.y + guidance.cC) ;
        refC.z = 0;

        double err = fabs(pCenter.x - refC.x);
        E += err * err * mParam_MPC_Gain_Offset;
    } while (false);

    return E;
}
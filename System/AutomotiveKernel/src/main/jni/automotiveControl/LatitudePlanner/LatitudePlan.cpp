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

#include <automotiveControl/LatitudePlanner/LatitudePlan.h>

using namespace cv;
using namespace via::automotive;

LatitudePlan::LatitudePlan()
{
    reset() ;
}

void LatitudePlan::reset()
{
    for(int i = 0 ; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
        mTrajectory_L[i] = Point2f(0.0f, 0.0f);
        mTrajectory_R[i] = Point2f(0.0f, 0.0f);
        mLaneAnchors_L[i] = Point2f(0.0f, 0.0f);
        mLaneAnchors_R[i] = Point2f(0.0f, 0.0f);
    }
    mLaneScore_L = 0.0f;
    mLaneScore_R = 0.0f;
    mLaneObjBtmCenter = Point3f(0.0f, 0.0f, 0.0f);
    planStartSteerAngle = 0;
    planDesiredSteerAngle = 0;
    planTotalSteeringTime = 1;
    isPlanValid = false;
    isSteerControllable = false;
    isSteerOverControl = false;
    planTime = 0;
}

LatitudePlan &LatitudePlan::operator=(const LatitudePlan &src) {
    if (this != &src) {
        for (int i = 0; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT; i++) {
            this->mTrajectory_L[i] = src.mTrajectory_L[i];
            this->mTrajectory_R[i] = src.mTrajectory_R[i];
            this->mLaneAnchors_L[i] = src.mLaneAnchors_L[i];
            this->mLaneAnchors_R[i] = src.mLaneAnchors_R[i];
        }
        this->mLaneScore_L = src.mLaneScore_L;
        this->mLaneScore_R = src.mLaneScore_R;
        this->mLaneObjBtmCenter = src.mLaneObjBtmCenter;
        this->planStartSteerAngle = src.planStartSteerAngle;
        this->planDesiredSteerAngle = src.planDesiredSteerAngle;
        this->planTotalSteeringTime = src.planTotalSteeringTime;
        this->isPlanValid = src.isPlanValid;
        this->isSteerControllable = src.isSteerControllable;
        this->isSteerOverControl = src.isSteerOverControl;
        this->planTime = src.planTime;
    }
    return *this;
}
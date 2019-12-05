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

#ifndef VIA_ADAS_LATITUDEPLAN_H
#define VIA_ADAS_LATITUDEPLAN_H

#include <opencv2/core/core.hpp>

namespace via {
namespace automotive {

static const int LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT = 16;

class LatitudePlan
{
public:
    LatitudePlan();
    void reset();
    LatitudePlan &operator=(const LatitudePlan &src);

    /**
    @brief  Trajectory anchors is normalized image coordinate.
        Ln      Rn
        |       |
        |       |
        L1      R1
        |       |
        L0      R0
    */
    float mLaneScore_L;
    float mLaneScore_R;
    cv::Point2f mLaneAnchors_L[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
    cv::Point2f mLaneAnchors_R[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
    cv::Point2f mTrajectory_L[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
    cv::Point2f mTrajectory_R[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT];
    cv::Point3f mLaneObjBtmCenter;
    float planStartSteerAngle;
    float planDesiredSteerAngle;
    float planTotalSteeringTime;    // Time to steer to desired angle , unit : second
    bool isPlanValid;
    bool isSteerControllable;
    bool isSteerOverControl;
    long long planTime;
};

}
}
#endif //VIA_ADAS_LATITUDEPLAN_H
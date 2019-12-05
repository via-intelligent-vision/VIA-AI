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

package com.viatech.automotive.LatitudePlanner;

public class LatitudePlan
{
    // Note : keep this value same with : automotiveControl\LatitudePlanner\LatitudePlan.h
    public static final int LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT = 16;

    public LatitudePlan() {
        mTrajectory_L = new float[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2];   // x y x y x y ....
        mTrajectory_R = new float[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2];   // x y x y x y ....
        mLaneAnchors_L = new float[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2];   // x y x y x y ....
        mLaneAnchors_R = new float[LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2];   // x y x y x y ....
        reset();
    }

    public void reset() {
        mLaneScore_L = 0.0f;
        mLaneScore_R = 0.0f;
        planeStartSteerAngle = 0;
        planDesiredSteerAngle = 0;
        isPlanValid = false;
        isSteerControllable = false;
        isSteerOverControl = false;
    }

    public void copyTo(LatitudePlan plane) {
        plane.planeStartSteerAngle = this.planeStartSteerAngle;
        plane.planDesiredSteerAngle = this.planDesiredSteerAngle;
        plane.isPlanValid = this.isPlanValid;
        plane.isSteerControllable = this.isSteerControllable;
        plane.isSteerOverControl = this.isSteerOverControl;
        plane.mLaneScore_L = this.mLaneScore_L;
        plane.mLaneScore_R = this.mLaneScore_R;
        System.arraycopy(this.mTrajectory_L, 0, plane.mTrajectory_L, 0, this.mTrajectory_L.length);
        System.arraycopy(this.mTrajectory_R, 0, plane.mTrajectory_R, 0, this.mTrajectory_R.length);
        System.arraycopy(this.mLaneAnchors_L, 0, plane.mLaneAnchors_L, 0, this.mLaneAnchors_L.length);
        System.arraycopy(this.mLaneAnchors_R, 0, plane.mLaneAnchors_R, 0, this.mLaneAnchors_R.length);
    }

    /**
     @brief  Trajectory anchors is normalized image coordinate.
        n is LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT
        L0  --- L1 --- L2 --------- Ln

        R0  --- R1 --- R2 --------- Rn
     */
    public float mLaneAnchors_L[];
    public float mLaneAnchors_R[];
    public float mLaneScore_L;
    public float mLaneScore_R;

    public float mTrajectory_L[];
    public float mTrajectory_R[];
    public float planeStartSteerAngle;
    public float planDesiredSteerAngle;
    public boolean isPlanValid;
    public boolean isSteerControllable;
    public boolean isSteerOverControl;
};
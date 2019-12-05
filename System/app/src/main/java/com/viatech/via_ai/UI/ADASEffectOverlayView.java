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

package com.viatech.via_ai.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.viatech.automotive.LatitudePlanner.LatitudePlan;
import com.viatech.sensing.DetectorTypes;
import com.viatech.sensing.SensingModule;
import com.viatech.sensing.SensingSamples;
import com.viatech.utility.tool.OverlayView;
import com.viatech.vBus.CANbusData;


public class ADASEffectOverlayView extends OverlayView {
    private SensingSamples.LaneDetectSample mSample_LaneDetect;
    private SensingSamples.VehicleDetectSample mSample_VehicleDetect;
    private SensingSamples.BlindSpotDetectSample mSample_BlindSpotDetect;
    private SensingSamples.SpeedLimitDetectSample mSample_SpeedLimitDetect;
    private SensingSamples.EnvironmentSample mSample_Environment;
    private SensingSamples.ObjectDetectSample mSample_ObjectDetect;
    private SensingSamples.TrafficLightSample mSample_TrafficLight;
    private CANbusData.CANParams_SteeringSensor mCANParams_SteeringSensor;
    private CANbusData.CANParams_SafetyFeature  mCANParams_SafetyFeature;
    private LatitudePlan mLatitudePlan;


    public ADASEffectOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSample_LaneDetect = new SensingSamples.LaneDetectSample();
        mSample_VehicleDetect = new SensingSamples.VehicleDetectSample();
        mSample_BlindSpotDetect = new SensingSamples.BlindSpotDetectSample();
        mSample_SpeedLimitDetect = new SensingSamples.SpeedLimitDetectSample();
        mSample_Environment = new SensingSamples.EnvironmentSample();
        mSample_ObjectDetect = new SensingSamples.ObjectDetectSample();
        mSample_TrafficLight = new SensingSamples.TrafficLightSample();

        mCANParams_SteeringSensor = new CANbusData.CANParams_SteeringSensor();
        mCANParams_SafetyFeature = new CANbusData.CANParams_SafetyFeature();

        mLatitudePlan = new LatitudePlan();
    }

    @Override
    public void setCallback(DrawCallback c) {
        super.setCallback(c);
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.LaneDetectSample laneSample) {
        if(srcModule.isDetectorActive(DetectorTypes.LDWS)) {
            mSample_LaneDetect.mLaneWidth = laneSample.mLaneWidth;
            mSample_LaneDetect.mFrameSize[0] = laneSample.mFrameSize[0];
            mSample_LaneDetect.mFrameSize[1] = laneSample.mFrameSize[1];
            for(int i = 0 ; i < SensingSamples.LaneDetectSample.RESAMPLE_COUNT; i++) {
                mSample_LaneDetect.mLaneAnchor_L[i].x = laneSample.mLaneAnchor_L[i].x;
                mSample_LaneDetect.mLaneAnchor_L[i].y = laneSample.mLaneAnchor_L[i].y;
                mSample_LaneDetect.mLaneAnchor_R[i].x = laneSample.mLaneAnchor_R[i].x;
                mSample_LaneDetect.mLaneAnchor_R[i].y = laneSample.mLaneAnchor_R[i].y;
            }
            mSample_LaneDetect.mLaneWidth = laneSample.mLaneWidth;
            mSample_LaneDetect.mLaneStatus = laneSample.mLaneStatus;
        }
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.VehicleDetectSample vehicleSample) {
        if(srcModule.isDetectorActive(DetectorTypes.FCWS) || srcModule.isDetectorActive(DetectorTypes.FCWS_DL)) {
            mSample_VehicleDetect.mForwardVehicle.mObjTypeIndex = vehicleSample.mForwardVehicle.mObjTypeIndex;
            mSample_VehicleDetect.mForwardVehicle.mDistance = vehicleSample.mForwardVehicle.mDistance;
            mSample_VehicleDetect.mForwardVehicle.mScore = vehicleSample.mForwardVehicle.mScore;
            mSample_VehicleDetect.mForwardVehicle.mMinAnchor[0] = vehicleSample.mForwardVehicle.mMinAnchor[0];
            mSample_VehicleDetect.mForwardVehicle.mMinAnchor[1] = vehicleSample.mForwardVehicle.mMinAnchor[1];
            mSample_VehicleDetect.mForwardVehicle.mMaxAnchor[0] = vehicleSample.mForwardVehicle.mMaxAnchor[0];
            mSample_VehicleDetect.mForwardVehicle.mMaxAnchor[1] = vehicleSample.mForwardVehicle.mMaxAnchor[1];
        }
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.ObjectDetectSample sample) {
        if(srcModule.isDetectorActive(DetectorTypes.FCWS_DL)) {
            for(int i = 0 ; i < sample.mSampleCount ; i++) {
                mSample_ObjectDetect.mObjectDataList[i].copyFrom(sample.mObjectDataList[i]);
            }
            mSample_ObjectDetect.mSampleCount = sample.mSampleCount;
            mSample_ObjectDetect.mFocusObjectId = sample.mFocusObjectId;
        }
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.TrafficLightSample sample) {
        if (srcModule.isDetectorActive(DetectorTypes.TLD)) {
            for (int i = 0; i < sample.mSampleCount; i++) {
                mSample_TrafficLight.mObjectDataList[i].copyFrom(sample.mObjectDataList[i]);
            }
            mSample_TrafficLight.mSampleCount = sample.mSampleCount;
        }
    }


    public void setSensingSample(SensingModule srcModule, SensingSamples.BlindSpotDetectSample bsdSample) {
        if(srcModule.isDetectorActive(DetectorTypes.BSD_L)) {
            mSample_BlindSpotDetect.mIsLeftWarning = bsdSample.mIsLeftWarning;
        }
        if(srcModule.isDetectorActive(DetectorTypes.BSD_R)) {
            mSample_BlindSpotDetect.mIsRightWarning = bsdSample.mIsRightWarning;
        }
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.EnvironmentSample envSample) {
        if(srcModule.isDetectorActive(DetectorTypes.Weather)) {
            mSample_Environment.mWeatherType = envSample.mWeatherType;
        }
    }

    public void setSensingSample(SensingModule srcModule, SensingSamples.SpeedLimitDetectSample sample) {
        if(srcModule.isDetectorActive(DetectorTypes.SLD)) {
            mSample_SpeedLimitDetect.mSpeedLimit_1 = sample.mSpeedLimit_1;
            mSample_SpeedLimitDetect.mSpeedLimit_2 = sample.mSpeedLimit_2;
        }
    }

    public void setLatitudePlan(LatitudePlan latitudePlan) {
        mLatitudePlan.planeStartSteerAngle = latitudePlan.planeStartSteerAngle;
        mLatitudePlan.planDesiredSteerAngle = latitudePlan.planDesiredSteerAngle;
        mLatitudePlan.isPlanValid = latitudePlan.isPlanValid;
        mLatitudePlan.isSteerControllable = latitudePlan.isSteerControllable;
        mLatitudePlan.isSteerOverControl = latitudePlan.isSteerOverControl;
        mLatitudePlan.mLaneScore_L = latitudePlan.mLaneScore_L;
        mLatitudePlan.mLaneScore_R = latitudePlan.mLaneScore_R;
        System.arraycopy(latitudePlan.mTrajectory_L, 0, mLatitudePlan.mTrajectory_L, 0, latitudePlan.mTrajectory_L.length);
        System.arraycopy(latitudePlan.mTrajectory_R, 0, mLatitudePlan.mTrajectory_R, 0, latitudePlan.mTrajectory_R.length);
        System.arraycopy(latitudePlan.mLaneAnchors_L, 0, mLatitudePlan.mLaneAnchors_L, 0, latitudePlan.mLaneAnchors_L.length);
        System.arraycopy(latitudePlan.mLaneAnchors_R, 0, mLatitudePlan.mLaneAnchors_R, 0, latitudePlan.mLaneAnchors_R.length);
    }

    public void setCANParams_SteeringSensor(CANbusData.CANParams_SteeringSensor Data) {
        mCANParams_SteeringSensor.steerAngle = Data.steerAngle;
        mCANParams_SteeringSensor.steerAngleRate = Data.steerAngleRate;
        mCANParams_SteeringSensor.steerStatus = Data.steerStatus;
        mCANParams_SteeringSensor.steerControlActive = Data.steerControlActive;
    }

    public void setCANParams_SafetyFeature( CANbusData.CANParams_SafetyFeature data) {
        mCANParams_SafetyFeature.isBrakePressed = data.isBrakePressed;
        mCANParams_SafetyFeature.isEnabled_ACC = data.isEnabled_ACC;
        mCANParams_SafetyFeature.isEnabled_LKS = data.isEnabled_LKS;
        mCANParams_SafetyFeature.isGasPressed = data.isGasPressed;
    }

    public SensingSamples.LaneDetectSample getSample_LaneDetect() {
        return mSample_LaneDetect;
    }

    public SensingSamples.VehicleDetectSample getSample_VehicleDetect() {
        return mSample_VehicleDetect;
    }

    public SensingSamples.BlindSpotDetectSample getSample_BlindSpotDetect() {
        return mSample_BlindSpotDetect;
    }

    public SensingSamples.SpeedLimitDetectSample getSample_SpeedLimitDetect() {
        return mSample_SpeedLimitDetect;
    }

    public SensingSamples.EnvironmentSample getSample_Environment() {
        return mSample_Environment;
    }


    @Override
    public synchronized void draw(Canvas canvas) {

        super.draw(canvas);

        DrawSensingSamples.drawSensingSample_FCWS(canvas, mSample_VehicleDetect, 0);
        if(mSample_LaneDetect.mLaneStatus == SensingSamples.LaneDetectSample.SampleStatus.Calibrating) {
            DrawSensingSamples.drawSensingSample_LDWS(canvas, mSample_LaneDetect);
        }
        else {
            DrawSensingSamples.drawLatitudePlan(canvas, mLatitudePlan, mSample_LaneDetect, mCANParams_SteeringSensor, mCANParams_SafetyFeature);
            //DrawSensingSamples.drawSensingSample_LDWS(canvas, mSample_LaneDetect);
        }
        DrawSensingSamples.drawSensingSample_ObjectDetect(canvas, mSample_ObjectDetect);
        DrawSensingSamples.drawSymbol(canvas);
        DrawSensingSamples.drawSensingSample_TrafficLight(canvas, mSample_TrafficLight);

        //DrawADASFeedback.drawWatermark(canvas);
    }

    public boolean loadLabels(String labelsFilePath) {
        //return DrawADASFeedback.loadLabels(labelsFilePath);
        return DrawSensingSamples.loadLabels(labelsFilePath);
    }
}

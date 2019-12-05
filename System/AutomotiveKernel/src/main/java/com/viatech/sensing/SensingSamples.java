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

package com.viatech.sensing;

/**
 * This class contain all samples of sensing module.
 */
public class SensingSamples {
    /**
     *  The type of sensing samples.
     */
    public enum SampleTypes {
        /** Default value*/
        Unknown(0),
        /** Sample contain the result from {@link DetectorTypes#LDWS} */
        LaneDetectSample(1),
        /** Sample contain the result from {@link DetectorTypes#FCWS} , and forward object in {@link DetectorTypes#FCWS_DL} */
        VehicleDetectSample(2),
        /** Sample contain the result from {@link DetectorTypes#BSD_L} and {@link DetectorTypes#BSD_R} */
        BlindSpotDetectSample(3),
        /** Sample contain the result from {@link DetectorTypes#SLD} */
        SpeedLimitDetectSample(4),
        /** Sample contain the result from {@link DetectorTypes#Weather} */
        EnvironmentSample(5),
        /** Sample contain the result from {@link DetectorTypes#FCWS_DL} */
        ObjectDetectSample(6),
        /** Sample contain the result from {@link DetectorTypes#TLD} */
        TrafficLightDetectSample(7),
        ;

        private int mIndex = -1;
        SampleTypes(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }

        public static SampleTypes getWeatherType(int index)
        {
            for (int i = 0; i < SampleTypes.values().length; i++) {
                if (index == SampleTypes.values()[i].getIndex()) {
                    return SampleTypes.values()[i];
                }
            }
            return Unknown;
        }
    }

    /**
     About SensingSample,  SensingSample is the base class for all ADAS samples.
     */
    public static class SensingSample
    {
        public SensingSample() {
            mSampleTime_ms = 0;
        }

        public void setSampleTime(double t) {
            mSampleTime_ms = t;
        }

        public double getSampleTime() {
            return mSampleTime_ms;
        }

        private double mSampleTime_ms;
    };

    /**
     *  ObjectData is the object class for object detector (FCWS, FCWS_DL ....)
     */
    public static class ObjectData {
        /** Object type index */
        public int mObjTypeIndex;
        /** The confidence value of this object, the range of value is [0.0~1.0] */
        public float mScore;
        /** A image point expressed as 2D array , array [0] is x , [1] is y. This value create a bounding box with {@link ObjectData#mMaxAnchor} */
        public float []mMinAnchor;
        /** A image point expressed as 2D array , array [0] is x , [1] is y. This value create a bounding box with {@link ObjectData#mMinAnchor} */
        public float []mMaxAnchor;
        /** The distance of object, unit is cm*/
        public float mDistance;
        /** The reaction time of object, unit is s , -1 is untracked object.*/
        public float mReactionTime;

        public ObjectData() {
            mMinAnchor = new float[] {0.0f, 0.0f};
            mMaxAnchor = new float[] {0.0f, 0.0f};
            reset();
        }

        /** Reset to default value. all value will set to 0.*/
        public void reset() {
            mObjTypeIndex = 0;
            mScore = 0;
            mMinAnchor[0] = 0.0f;
            mMinAnchor[1] = 0.0f;
            mMaxAnchor[0] = 0.0f;
            mMaxAnchor[1] = 0.0f;
            mDistance = 0;
            mReactionTime = -1;
        }

        public ObjectData(int objTypeIndex, float score, float minX, float minY,  float maxX, float maxY, float distance, float reactionTime) {
            mObjTypeIndex = objTypeIndex;
            mScore = score;
            mMinAnchor[0] = minX;
            mMinAnchor[1] = minY;
            mMaxAnchor[0] = maxX;
            mMaxAnchor[1] = maxY;
            mDistance = distance;
            mReactionTime = reactionTime;

        }

        /** Copy data from another data.*/
        public void copyFrom(ObjectData src) {
            this.mObjTypeIndex = src.mObjTypeIndex;
            this.mScore = src.mScore;
            this.mMinAnchor[0] = src.mMinAnchor[0] ;
            this.mMinAnchor[1] = src.mMinAnchor[1];
            this.mMaxAnchor[0] = src.mMaxAnchor[0];
            this.mMaxAnchor[1] = src.mMaxAnchor[1];
            this.mDistance = src.mDistance;
            this.mReactionTime = src.mReactionTime;
        }
    };

    public static class LaneAnchor
    {
        public float x;
        public float y;

        LaneAnchor() {
            x = 0;
            y = 0;
        }

        LaneAnchor(float x, float y) {
            set(x,y);
        }

        void set(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    /**
     Store the lane detector data. <br>
     The Lane anchors is normalized image coordinate.<br>
     * <pre>
     * {@code
     * The Lane anchors is normalized image coordinate.
     *     L2      R2
     *     |       |
     *     |       |
     *     L1      R1
     *     |       |
     *     |       |
     *     L0      R0
     * }
     *</pre>
     */
    public static class LaneDetectSample  extends SensingSample
    {
        /**
         * The enum of lane detection sample status.
         */
        public enum SampleStatus {
            /** Unknown lane status, Get this value if detector {@link DetectorTypes#LDWS} not attach on task, or system not init, */
            Unknown (0),
            /** Not in use*/
            SoftDisable (1),
            /** Detect lane*/
            Detected (2),
            /** No lane detected*/
            NoDetected (3),
            /** Vehicle is departed from left side, or left wheel cross to lane marking */
            LeftDeparture (4),
            /** Vehicle is departed from left side, or right wheel cross to lane marking */
            RightDeparture (5),
            /** System in auto calibration */
            Calibrating (6)
            ;

            private int mIndex = -1;
            SampleStatus(int index)
            {
                mIndex = index;
            }

            public int getIndex()
            {
                return mIndex;
            }

            public static SampleStatus getWeatherType(int index)
            {
                for (int i = 0; i < SampleStatus.values().length; i++) {
                    if (index == SampleStatus.values()[i].getIndex()) {
                        return SampleStatus.values()[i];
                    }
                }
                return Unknown;
            }
        };

        public LaneDetectSample() {
            mFrameSize = new float [] {0.0f , 0.0f};
            mLaneAnchor_L = new LaneAnchor [RESAMPLE_COUNT];
            mLaneAnchor_R = new LaneAnchor [RESAMPLE_COUNT];
            for(int i = 0 ; i < RESAMPLE_COUNT ; i++) {
                mLaneAnchor_L[i] = new LaneAnchor();
                mLaneAnchor_R[i] = new LaneAnchor();
            }
            mLaneWidth = 0.0f;
            mLaneStatus = SampleStatus.Unknown;
        }

        /** Reset to default value. all value will set to 0, and status will set to {@link SampleStatus#Unknown}*/
        public void reset() {
            mFrameSize[0] = 0.0f;
            mFrameSize[1] = 0.0f;
            for(int i = 0 ; i < RESAMPLE_COUNT ; i++) {
                mLaneAnchor_L[i].set(0.0f, 0.0f);
                mLaneAnchor_R[i].set(0.0f, 0.0f);
            }
            mLaneWidth = 0.0f;
            mLaneStatus = SampleStatus.Unknown;
        }

        public final static int RESAMPLE_COUNT = 9;
        /** The detected image size expressed as 2D array , array [0] is width , [1] is height*/
        public float []mFrameSize;
        /** A part of lane image point expressed as 2D array , array [0] is x , [1] is y. Value is normalized to [0.0 - 1.0] */
        public LaneAnchor []mLaneAnchor_L;
        /** A part of lane image point expressed as 2D array , array [0] is x , [1] is y. Value is normalized to [0.0 - 1.0] */
        public LaneAnchor []mLaneAnchor_R;
        /** Lane width, unit : m. */
        public float mLaneWidth;   // unit : meter
        /** Lane detect status, ref : {@link SampleStatus} */
        public SampleStatus mLaneStatus;
};

    /**
     Store the vehicle detector data. (FCWS)
     */
    public static class VehicleDetectSample  extends SensingSample
    {
        public VehicleDetectSample() {
            mForwardVehicle = new ObjectData();
        }

        public void reset() {
            mForwardVehicle.reset();
        }

        /** Object detected by FCWS detector*/
        public ObjectData mForwardVehicle;
    };

    /**
     Store the object detector result. (FCWS_DL), The maximum detected object is limit by {@link ObjectDetectSample#MAX_SAMPLE_COUNT},
     and in each detection step, the real detected object count is defined by {@link ObjectDetectSample#mSampleCount}
     */
    public static class ObjectDetectSample extends SensingSample
    {
        /** The maximum object count in this sample*/
        public static final int MAX_SAMPLE_COUNT = 64;  // Keep this value same as [ SensingSamples.h , value ObjectDetectSample::MAX_SAMPLE_COUNT ]

        public ObjectDetectSample() {
            mObjectDataList = new ObjectData[MAX_SAMPLE_COUNT];
            for(int i = 0 ; i < MAX_SAMPLE_COUNT; i++) {
                mObjectDataList[i] = new ObjectData();
            }
        }

        /** Reset to default value. all value will set to 0.*/
        public void reset() {
            for(int i = 0 ; i < MAX_SAMPLE_COUNT; i++) {
                mObjectDataList[i].reset();
            }
            mSampleCount = 0;
            mFocusObjectId = 0;
        }

        /** The count of detected object (or called valid object) in this sample. <br>
                *     List : [ 0 ---------(valid data)---------- mSampleCount ---------------(invalid data)---------------MAX_SAMPLE_COUNT ]
                */
        public int mSampleCount;
        /** Forward collision object id in {@link ObjectDetectSample#mSampleCount}, if no front object, this value will be -1. <p>
                * for example, <br>
                *      if detector detect 5 object in this sample, and object 2 is front object, {@link ObjectDetectSample#mFocusObjectId} will be 2. <br>
                *     if detector detect 5 object in this sample, and no object in the front, {@link ObjectDetectSample#mFocusObjectId} will be -1. */
        public int mFocusObjectId;

        /** Object data list, the length of array is same as {@link ObjectDetectSample#mSampleCount}. Value {@link ObjectDetectSample#mSampleCount} is used to specify valid object bound in this sample*/
        public ObjectData mObjectDataList [];
    };

    /**
     Store the traffic light detector result. (TLD), The maximum detected object is limit by {@link TrafficLightSample#MAX_SAMPLE_COUNT},
     and in each detection step, the real detected object count is defined by {@link TrafficLightSample#mSampleCount}
     */
    public static class TrafficLightSample extends SensingSample
    {
        /** The maximum object count in this sample*/
        public static final int MAX_SAMPLE_COUNT = 8;  // Keep this value same as [ SensingSamples.h , value TrafficLightSample::MAX_SAMPLE_COUNT ]

        public TrafficLightSample() {
            mObjectDataList = new ObjectData[MAX_SAMPLE_COUNT];
            for(int i = 0 ; i < MAX_SAMPLE_COUNT; i++) {
                mObjectDataList[i] = new ObjectData();
            }
        }

        /** Reset to default value. all value will set to 0.*/
        public void reset() {
            for(int i = 0 ; i < MAX_SAMPLE_COUNT; i++) {
                mObjectDataList[i].reset();
            }
            mSampleCount = 0;
        }

        /** The count of detected object (or called valid object) in this sample. <br>
         *     List : [ 0 ---------(valid data)---------- mSampleCount ---------------(invalid data)---------------MAX_SAMPLE_COUNT ]
         */
        public int mSampleCount;

        /** Object data list, the length of array is same as {@link TrafficLightSample#mSampleCount}. Value {@link TrafficLightSample#mSampleCount} is used to specify valid object bound in this sample*/
        public ObjectData mObjectDataList [];
    };

    /**
     Store the blind spot detector data.
    */
    public static class BlindSpotDetectSample  extends SensingSample
    {
        public BlindSpotDetectSample() {
            reset();
        }

        /** Reset to default value. all value will set to 0.*/
        public void reset() {
            mIsLeftWarning = false;
            mIsRightWarning = false;
        }

        /** If detect left blind spot have object moving close to vehicle, this value wile be true, otherwise false.*/
        public boolean mIsLeftWarning;
        /** If detect left blind spot have object moving close to vehicle, this value wile be true, otherwise false.*/
        public boolean mIsRightWarning;
    };

    /**
     Store the speed limit data.
    */
    public static class SpeedLimitDetectSample  extends SensingSample
    {
        public SpeedLimitDetectSample()  {
            reset();
        }

        /** Reset to default value. all value will set to 0.*/
        public void reset() {
            mSpeedLimit_1 = 0;
            mSpeedLimit_2 = 0;
        }

        public int mSpeedLimit_1;
        public int mSpeedLimit_2;
    };

    /**
      Store the environment data.
    */
    public static class EnvironmentSample  extends SensingSample
    {
        /**
         * The enum of weather type.
         */
        public enum WeatherTypes {
            /** Unknown weather, Get this value if detector {@link DetectorTypes#Weather} not attach on task, or system not init, */
            Unknown(0),
            /** Day weather or bright environment */
            Day(1),
            /** Night weather or dark environment */
            Night(2),
            /** Not in use*/
            DayRaining(3),
            /** Not in use*/
            NightRaining(4),
            ;
            private int mIndex = -1;
            WeatherTypes(int index)
            {
                mIndex = index;
            }

            public int getIndex()
            {
                return mIndex;
            }

            public static WeatherTypes getWeatherType(int index)
            {
                for (int i = 0; i < WeatherTypes.values().length; i++) {
                    if (index == WeatherTypes.values()[i].getIndex()) {
                        return WeatherTypes.values()[i];
                    }
                }
                return Unknown;
            }
        }

        public EnvironmentSample() {
            reset();
        }

        /** Reset to default value. {@link EnvironmentSample#mWeatherType will set to {@link WeatherTypes#Unknown}}*/
        public void reset() {
            mWeatherType = WeatherTypes.Unknown;
        }

        public WeatherTypes mWeatherType;
    };


    /**
     Store the environment data.
     */
    public static class CalibrationSample extends SensingSample {
        /**
         * The enum of calibration status.
         */
        public enum CalibStatus {
            /** Unknown weather, Get this value if detector {@link DetectorTypes#Weather} not attach on task, or system not init, */
            Unknown(0),
            /** Day weather or bright environment */
            Success(1),
            /** Night weather or dark environment */
            Fail(2),
            /** Not in use*/
            Calibrating(4),
            ;

            private int mIndex = -1;

            CalibStatus(int index)
            {
                mIndex = index;
            }

            public int getIndex()
            {
                return mIndex;
            }

            public static CalibStatus getCalibStatus(int index)
            {
                for (int i = 0; i < CalibStatus.values().length; i++) {
                    if (index == CalibStatus.values()[i].getIndex()) {
                        return CalibStatus.values()[i];
                    }
                }
                return Unknown;
            }
        }
    }
}

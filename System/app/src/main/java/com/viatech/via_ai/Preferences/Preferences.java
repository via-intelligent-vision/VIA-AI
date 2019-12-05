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

package com.viatech.via_ai.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.viatech.camera.CameraTypes;
import com.viatech.car.CarTypes;
import com.viatech.via_ai.Media.CameraPermutation;
import com.viatech.via_ai.R;


public class Preferences
{
    // Singleton
    private final static String mPreferencesKey = "VIA_ADAS_Preferences";
    private static Preferences instance = null;

    public static Preferences getInstance() {
        if (instance == null) {
            synchronized(Preferences.class) {
                if(instance == null) {
                    instance = new Preferences();
                }
            }
        }
        return instance;
    }

    private static String getString(SharedPreferences settings, String key, String defValue) {
        String v = null;
        try {
            v = settings.getString(key, null);
        }
        catch (Exception e) {
        }

        if(v == null) {
            return defValue;
        }
        else {
            return v;
        }
    }

    private static int getInt(SharedPreferences settings, String key, String defValue) {
        Integer ret = null;
        try {
            String v = settings.getString(key, null);
            ret = Integer.parseInt(v);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(ret == null) {
            return Integer.parseInt(defValue);
        }
        else {
            return ret;
        }
    }

    private static Boolean getBoolean(SharedPreferences settings, String key, Boolean defValue) {
        //return  settings.getBoolean(key, defValue);
        Boolean ret = null;
        try {
            ret = settings.getBoolean(key, defValue);
        }
        catch (Exception e) {

        }
        if(ret == null) {
            return defValue;
        }
        else {
            return ret;
        }
//        String v = settings.getString(key, null);
//        if(v == null) {
//            return Boolean.parseBoolean(defValue);
//        }
//        else {
//            return Boolean.parseBoolean(v);
//        }
    }
    /**
        @brief PreferenceUnit interface, Every must implement these function.
        */
    private interface PreferencesUnitImpl {
        void load(Context context, SharedPreferences settings);
        void save(Context context, SharedPreferences.Editor editor);
    }

    public class ConfigurationData implements PreferencesUnitImpl {
        private String mSensingCfgPath;
        private String mAutomotiveCfgPath;

        public ConfigurationData() {
        }

        public String getSensingCfgPath() {
            return mSensingCfgPath;
        }

        public String getAutomotiveCfgPath() {
            return mAutomotiveCfgPath;
        }

        public void setSensingCfgPath(String path) {
            mSensingCfgPath = path;
        }

        public void setAutomotiveCfgPath(String path) {
            mAutomotiveCfgPath = path;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_ConfigurationPath);
            mSensingCfgPath = getString(settings, key, resources.getString(R.string.prefDefaultValue_ADAS_ConfigurationPathDefaultValue));

            key = resources.getString(R.string.prefKey_ADAS_AutomotiveConfigurationPath);
            mAutomotiveCfgPath = getString(settings, key, resources.getString(R.string.prefDefaultValue_ADAS_AutomotiveConfigurationPathDefaultValue));

        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_ConfigurationPath);
            editor.putString(key, mSensingCfgPath);

            key = resources.getString(R.string.prefKey_ADAS_AutomotiveConfigurationPath);
            editor.putString(key, mAutomotiveCfgPath);
        }
    }

    public class FrameSourceData implements PreferencesUnitImpl {
        public static final int CSI_CAMERA_LIMIT =  1;
        private boolean mIsCameraSource;
        private int mCameraID;
        private int mCameraSourceWidth;
        private int mCameraSourceHeight;
        private int mFrameWidth [];
        private int mFrameHeight [];
        private CameraTypes mCameraType[];
        private String mCameraCalibPath[];
        private float mCameraInstallHeight[];
        private float mCameraToCenterOffset[];
        private CameraPermutation mCameraPermutation;
        private String mPlaybackPath;

        public FrameSourceData() {
            mFrameWidth = new int [CSI_CAMERA_LIMIT];
            mFrameHeight = new int [CSI_CAMERA_LIMIT];
            mCameraType = new CameraTypes [CSI_CAMERA_LIMIT];
            mCameraCalibPath = new String [CSI_CAMERA_LIMIT];
            mCameraInstallHeight = new float [CSI_CAMERA_LIMIT];
            mCameraToCenterOffset = new float [CSI_CAMERA_LIMIT];
        }

        public boolean isUseCamera() {
            return mIsCameraSource;
        }

        public String getVideoPath() {
            return mPlaybackPath;
        }

        public int getCameraID() {
            return mCameraID;
        }

        public int getFrameWidth(int frameId) {
            return mFrameWidth[frameId];
        }

        public int getFrameHeight(int frameId) {
            return mFrameHeight [frameId];
        }

        public CameraTypes getCameraTypes(int frameId) {
            return mCameraType [frameId];
        }

        public String getCameraCalibPath(int frameId) {
            return mCameraCalibPath[frameId];
        }

        public void setCameraCalibPath(int frameId, String path) {
            mCameraCalibPath[frameId] = path;
        }

        public float getCameraInstallHeight(int frameId) {
            return mCameraInstallHeight[frameId];
        }

        public float getCameraToCenterOffset(int frameId) {
            return mCameraToCenterOffset[frameId];
        }

        public int getCameraSourceWidth() {
            return mCameraSourceWidth;
        }

        public int getCameraSourceHeight() {
            return mCameraSourceHeight;
        }

        public CameraPermutation getCameraPermutation() {
            return mCameraPermutation;
        }

        public int getCameraCount() {
            int ret = 0;
            switch (mCameraPermutation) {
                case Camera1_In_Frame:
                    ret = 1;
                    break;
                case Camera2_In_Frame_1x2:
                    ret = 2;
                    break;
                case Camera3_In_Frame_1x3:
                    ret = 3;
                    break;
                case Camera4_In_Frame_1x4:
                    ret = 4;
                    break;
                case Camera4_In_Frame_2x2:
                    ret = 4;
                    break;
                case Uknown:
                default:
                    ret = 1;
                    break;
            }
            return ret;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key , dStr;

            key = resources.getString(R.string.prefKey_CameraResource_ADAS_CameraDevices);
            mCameraID = getInt(settings, key, resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_CameraDevicesEntry));

            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameSource);
            mIsCameraSource = !getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_CameraResource_ADAS_FrameSource));    // false is camera, true is video

            key = resources.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceWidth);
            mCameraSourceWidth = getInt(settings, key, resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_CameraSourceWidth));

            key = resources.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceHeight);
            mCameraSourceHeight = getInt(settings, key, resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_CameraSourceHeight));

            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameWidth_0);
                mFrameWidth[i] =  getInt(settings, key, dStr);
            }

            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameHeight_0);
                mFrameWidth[i] =  getInt(settings, key, dStr);
            }

//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_0);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameWidth_0);
//            mFrameWidth[0] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_1);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameWidth_1);
//            mFrameWidth[1] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_2);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameWidth_2);
//            mFrameWidth[2] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_3);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameWidth_3);
//            mFrameWidth[3] = getInt(settings, key, dStr);


//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_0);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameHeight_0);
//            mFrameHeight[0] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_1);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameHeight_1);
//            mFrameHeight[1] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_2);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameHeight_2);
//            mFrameHeight[2] = getInt(settings, key, dStr);
//
//            key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_3);
//            dStr = resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_FrameHeight_3);
//            mFrameHeight[3] = getInt(settings, key, dStr);


            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.pref_CameraResource_ADAS_CameraModuleEntryDefaultValue_0);
                mCameraType[i] = CameraTypes.getType(getInt(settings, key, dStr));
            }

            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_CameraConfigurationPath_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.prefDefaultValue_CameraResource_CameraConfigurationPathDefaultValue);
                mCameraCalibPath[i] = getString(settings, key, dStr);
            }

            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_CameraInstallHeight_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.prefDefaultValue_CameraResource_CameraInstallHeightDefaultValue);
                mCameraInstallHeight[i] = Float.parseFloat(getString(settings, key, dStr));
            }

            for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_CameraToCenterOffset_0);
                key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                dStr = resources.getString(R.string.prefDefaultValue_CameraResource_CameraToCenterOffsetDefaultValue);
                mCameraToCenterOffset[i] = Float.parseFloat(getString(settings, key, dStr));
            }


            key = resources.getString(R.string.prefKey_CameraResource_ADAS_CameraPermutation);
           mCameraPermutation = CameraPermutation.getType(getInt(settings, key, resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_CameraPermutationEntry)));

            key = resources.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath);
            mPlaybackPath = getString(settings, key, resources.getString(R.string.prefDefaultValue_CameraResource_ADAS_PlaybackPath));

        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();
            String key;

            editor.putString(resources.getString(R.string.prefKey_CameraResource_ADAS_CameraDevices), String.valueOf(mCameraID));
            editor.putString(resources.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceWidth), String.valueOf(mCameraSourceWidth));
            editor.putString(resources.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceHeight), String.valueOf(mCameraSourceHeight));
            for(int i = 0 ; i < CSI_CAMERA_LIMIT ; i++) {
                key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_0);
                key = key.substring(0, key.length() -2) + "_" + Integer.toString(i);
                editor.putString(key, String.valueOf(mFrameWidth[i]));

                key = resources.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_0);
                key = key.substring(0, key.length() -2) + "_" + Integer.toString(i);
                editor.putString(key, String.valueOf(mFrameHeight[i]));

                key = resources.getString(R.string.prefKey_CameraResource_CameraConfigurationPath_0);
                key = key.substring(0, key.length() -2) + "_" + Integer.toString(i);
                editor.putString(key, String.valueOf(mCameraCalibPath[i]));

                key = resources.getString(R.string.prefKey_CameraResource_CameraInstallHeight_0);
                key = key.substring(0, key.length() -2) + "_" + Integer.toString(i);
                editor.putString(key, String.valueOf(mCameraInstallHeight[i]));

                key = resources.getString(R.string.prefKey_CameraResource_CameraToCenterOffset_0);
                key = key.substring(0, key.length() -2) + "_" + Integer.toString(i);
                editor.putString(key, String.valueOf(mCameraToCenterOffset[i]));
            }
            editor.putString(resources.getString(R.string.prefKey_CameraResource_ADAS_CameraPermutation), String.valueOf(mCameraPermutation.getIndex()));
            editor.putBoolean(resources.getString(R.string.prefKey_CameraResource_ADAS_FrameSource), !mIsCameraSource);
            editor.putString(resources.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath), mPlaybackPath);
        }
    }

    public class FrameRecordData implements PreferencesUnitImpl {
        private boolean mEnableRecord;
        private String mRecordPath;
        private int mRecordFPS;
        private int mRecordInterval;
        private int mEncodeBitRate;

        public FrameRecordData() {
        }

        public void copyTo(FrameRecordData dst) {
            dst.mRecordPath = this.mRecordPath;
            dst.mEnableRecord = this.mEnableRecord;
            dst.mRecordFPS = this.mRecordFPS;
            dst.mRecordInterval = this.mRecordInterval;
            dst.mEncodeBitRate = this.mEncodeBitRate;
        }

        public String getRecordPath() {
            return mRecordPath;
        }

        public boolean isRecordEnable() {
            return mEnableRecord;
        }

        public int getEncodeBitRate() {
            return mEncodeBitRate;
        }

        public int getRecorInterval_s() {
            return mRecordInterval;
        }

        public int getRecordFPS() {
            return mRecordFPS;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_Recoder_VideoRecord);
            mEnableRecord = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_Recoder_VideoRecord));

            key = resources.getString(R.string.prefKey_Recoder_VideoRecordPath);
            mRecordPath = getString(settings, key, resources.getString(R.string.prefDefaultValue_Recoder_VideoRecordPath));

            key = resources.getString(R.string.prefKey_Recoder_VideoRecordFPS);
            mRecordFPS = getInt(settings, key, resources.getString(R.string.prefDefaultValue_Recoder_VideoRecordFPS));

            key = resources.getString(R.string.prefKey_Recoder_VideoRecordInterval);
            mRecordInterval = getInt(settings, key, resources.getString(R.string.prefDefaultValue_Recoder_VideoRecordInterval));

            key = resources.getString(R.string.prefKey_Recoder_VideoRecordBitrate);
            mEncodeBitRate = getInt(settings, key, resources.getString(R.string.prefDefaultValue_Recoder_VideoRecordBitrate));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_Recoder_VideoRecord), mEnableRecord);
            editor.putString(resources.getString(R.string.prefKey_Recoder_VideoRecordPath), mRecordPath);
            editor.putString(resources.getString(R.string.prefKey_Recoder_VideoRecordFPS), String.valueOf(mRecordFPS));
            editor.putString(resources.getString(R.string.prefKey_Recoder_VideoRecordInterval), String.valueOf(mRecordInterval));
            editor.putString(resources.getString(R.string.prefKey_Recoder_VideoRecordBitrate), String.valueOf(mEncodeBitRate));
        }
    }

    public class DeveloperData implements PreferencesUnitImpl {
        private boolean mEnableAutoLaunch;
        private boolean mEnableCurveLaneDetection;
        private boolean mIsShowObjectBillboard;

        public DeveloperData() {
        }

        public boolean isAutoLaunch() {
            return mEnableAutoLaunch;
        }

        public boolean isShowObjectBillboard() {
            return mIsShowObjectBillboard;
        }

        public boolean isEnableCurveLaneDetection() {
            return mEnableCurveLaneDetection;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_Developer_ShowObjectBillboard);
            mIsShowObjectBillboard = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_Developer_ShowObjectBillboard));

            key = resources.getString(R.string.prefKey_Developer_AutoLaunch);
            mEnableAutoLaunch = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_Developer_AutoLaunch));

            key = resources.getString(R.string.prefKey_Developer_CurveLaneDetection);
            mEnableCurveLaneDetection = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_Developer_CurveLaneDetection));

        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_Developer_ShowObjectBillboard), mIsShowObjectBillboard);
            editor.putBoolean(resources.getString(R.string.prefKey_Developer_AutoLaunch), mEnableAutoLaunch);
            editor.putBoolean(resources.getString(R.string.prefKey_Developer_CurveLaneDetection), mEnableCurveLaneDetection);
        }
    }

    public class AudioData implements PreferencesUnitImpl {
        private boolean mIsEnableTTS;
        private boolean mIsEnableBeep;

        public AudioData() {
            mIsEnableTTS = false;
            mIsEnableBeep = false;
        }

        public boolean isEnableTTS() {
            return mIsEnableTTS;
        }

        public boolean isEnableBeep() {
            return mIsEnableBeep;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_TTS_Status);
            mIsEnableTTS = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_TTS_Status));

            key = resources.getString(R.string.prefKey_ADAS_Beep_Status);
            mIsEnableBeep = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_Beep_Status));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_TTS_Status), mIsEnableTTS);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_Beep_Status), mIsEnableBeep);
        }
    }

    public class VehicleData implements PreferencesUnitImpl {
        private boolean mEnableVehiclebus;
        private boolean mRecordCANbus;
        private String mRecordPath;
        private CarTypes mCarType;

        public VehicleData() {
        }

        public void copyTo(VehicleData dst) {
            dst.mEnableVehiclebus = this.mEnableVehiclebus;
            dst.mRecordCANbus = this.mRecordCANbus;
            dst.mRecordPath = this.mRecordPath;
            dst.mCarType = this.mCarType;
        }

        public boolean isEnableVehiclebus() {
            return mEnableVehiclebus;
        }

        public boolean isRecordCANbus() {
            return mRecordCANbus;
        }

        public String getRecordPath() {
            return mRecordPath;
        }

        public CarTypes getCarType() {
            return mCarType;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_VehicleBus_BusStatus);
            mEnableVehiclebus = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_VehicleBus_BusStatus));

            key = resources.getString(R.string.prefKey_Recoder_VehicleBusRecord);
            mRecordCANbus = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_VehicleBus_BusStatus));

            key = resources.getString(R.string.prefKey_Recoder_VehicleBusRecordPath);
            mRecordPath = getString(settings, key, resources.getString(R.string.prefDefaultValue_Recoder_VehicleBusRecordPath));

            key = resources.getString(R.string.prefKey_VehicleBus_VehicleType);
            mCarType = CarTypes.getType(getInt(settings, key, resources.getString(R.string.prefDefaultValue_VehicleBus_VehicleType)));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_VehicleBus_BusStatus), mEnableVehiclebus);
            editor.putBoolean(resources.getString(R.string.prefKey_Recoder_VehicleBusRecord), mRecordCANbus);
            editor.putString(resources.getString(R.string.prefKey_Recoder_VehicleBusRecordPath), mRecordPath);
            editor.putString(resources.getString(R.string.prefKey_VehicleBus_VehicleType), String.valueOf(mCarType.getIndex()));
        }
    }

    public class LaneDepartureWarningData implements PreferencesUnitImpl {
        private boolean mEnable;
        private boolean mEnableWarningAudio;
        private int mMinimumSpeed;

        public LaneDepartureWarningData() {
        }

        public boolean isEnable() {
            return mEnable;
        }

        public boolean isEnableWarningAudio() {
            return mEnableWarningAudio;
        }

        public int getMinimumSpeed() {
            return mMinimumSpeed;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_LDW_DetectorStatus);
            mEnable = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_LDW_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_LDW_WarningAudio);
            mEnableWarningAudio = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_LDW_WarningAudio));

            key = resources.getString(R.string.prefKey_ADAS_LDW_DetectorEnableSpeed);
            mMinimumSpeed = getInt(settings, key, resources.getString(R.string.prefDefaultValue_ADAS_LDW_DetectorEnableSpeed));

        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_LDW_DetectorStatus),mEnable);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_LDW_WarningAudio), mEnableWarningAudio);
            editor.putString(resources.getString(R.string.prefKey_ADAS_LDW_DetectorEnableSpeed), String.valueOf(mMinimumSpeed));
        }
    }

    public class ForwardCollisionWarningData implements PreferencesUnitImpl {
        private boolean mEnable;
        private boolean mEnableWarningAudio;
        private boolean mIsDeepLearningMode;
        private float mRemindReactionTime = 2.7f;
        private float mAlertReactionTime = 1.0f;
        private float mUrgentReactionTime = 0.7f;

        public ForwardCollisionWarningData() {
        }

        public boolean isEnable() {
            return mEnable;
        }

        public boolean isEnableWarningAudio() {
            return mEnableWarningAudio;
        }

        public boolean isDeepLearningMode() {
            return mIsDeepLearningMode;
        }

        public float getRemindReactionTime() {
            return mRemindReactionTime;
        }

        public float getAlertReactionTime() {
            return mAlertReactionTime;
        }

        public float getUrgentReactionTime() {
            return mUrgentReactionTime;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_FCW_DetectorStatus);
            mEnable = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_FCW_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_FCW_WarningAudio);
            mEnableWarningAudio = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_FCW_WarningAudio));

            key = resources.getString(R.string.prefKey_ADAS_FCW_DetectorMode);
            mIsDeepLearningMode = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_FCW_DetectorMode));

        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_FCW_DetectorStatus), mEnable);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_FCW_WarningAudio), mEnableWarningAudio);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_FCW_DetectorMode), mIsDeepLearningMode);
        }
    }

    public class BlindSpotWarningData implements PreferencesUnitImpl {
        private boolean mEnable_L;
        private boolean mEnable_R;
        private boolean mEnableWarningAudio;

        public BlindSpotWarningData() {
        }

        public boolean isEnable_L() {
            return mEnable_L;
        }

        public boolean isEnable_R() {
            return mEnable_R;
        }

        public boolean isEnableWarningAudio() {
            return mEnableWarningAudio;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_BSD_L_DetectorStatus);
            mEnable_L = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_BSD_L_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_BSD_R_DetectorStatus);
            mEnable_R = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_BSD_R_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_BSD_WarningAudio);
            mEnableWarningAudio = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_BSD_WarningAudio));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_BSD_L_DetectorStatus), mEnable_L);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_BSD_R_DetectorStatus), mEnable_R);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_BSD_WarningAudio), mEnableWarningAudio);
        }
    }

    public class SpeedLimitWarningData implements PreferencesUnitImpl {
        private boolean mEnable;
        private boolean mEnableWarningAudio;

        public SpeedLimitWarningData() {
        }

        public boolean isEnable() {
            return mEnable;
        }

        public boolean isEnableWarningAudio() {
            return mEnableWarningAudio;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_SLD_DetectorStatus);
            mEnable = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_SLD_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_SLD_WarningAudio);
            mEnableWarningAudio = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_SLD_WarningAudio));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_SLD_DetectorStatus), mEnable);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_SLD_WarningAudio), mEnableWarningAudio);
        }
    }

    public class TrafficSignalData implements PreferencesUnitImpl {
        private boolean mEnable;
        private boolean mEnableWarningAudio;

        public TrafficSignalData() {
        }

        public boolean isEnable() {
            return mEnable;
        }

        public boolean isEnableWarningAudio() {
            return mEnableWarningAudio;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            Resources resources = context.getResources();
            String key;

            key = resources.getString(R.string.prefKey_ADAS_TLD_DetectorStatus);
            mEnable = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_TLD_DetectorStatus));

            key = resources.getString(R.string.prefKey_ADAS_TLD_WarningAudio);
            mEnableWarningAudio = getBoolean(settings, key, resources.getBoolean(R.bool.prefDefaultValue_ADAS_TLD_WarningAudio));
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            Resources resources = context.getResources();

            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_TLD_DetectorStatus), mEnable);
            editor.putBoolean(resources.getString(R.string.prefKey_ADAS_TLD_WarningAudio), mEnableWarningAudio);
        }
    }


    public class AdaptiveCruiseControlData implements PreferencesUnitImpl {
        private String Key_Enable = "ACC_Enable";
        private final boolean dEnable = false;
        private boolean mEnable;

        public AdaptiveCruiseControlData() {
            mEnable = dEnable;
        }

        public boolean isEnable() {
            return mEnable;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            mEnable = getBoolean(settings,Key_Enable, dEnable);
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            editor.putBoolean(Key_Enable, mEnable);
        }
    }

    public class LaneKeerpingAssistData implements PreferencesUnitImpl {
        private String Key_Enable = "LKAS_Enable";
        private final boolean dEnable = false;
        private boolean mEnable;

        public LaneKeerpingAssistData() {
            mEnable = dEnable;
        }

        public boolean isEnable() {
            return mEnable;
        }

        @Override
        public void load(Context context, SharedPreferences settings) {
            mEnable = getBoolean(settings,Key_Enable, dEnable);
        }

        @Override
        public void save(Context context, SharedPreferences.Editor editor) {
            editor.putBoolean(Key_Enable, mEnable);
        }
    }

    /**
        @beirf Preference datas.
        */
    private ConfigurationData mConfigurationData;
    private FrameSourceData mFrameSourceData;
    private FrameRecordData mFrameRecordData;
    private DeveloperData mDeveloperData;
    private AudioData mAudioData;
    private VehicleData mVehicleData;
    private LaneDepartureWarningData mLaneDepartureWarningData;
    private ForwardCollisionWarningData mForwardCollisionWarningData;
    private BlindSpotWarningData mBlindSpotWarningData;
    private SpeedLimitWarningData mSpeedLimitWarningData;
    private TrafficSignalData mTrafficSignalData;
    private AdaptiveCruiseControlData mAdaptiveCruiseControlData;
    private LaneKeerpingAssistData mLaneKeerpingAssistData;

    // Functions
    public Preferences() {
        // Alloc value units
        mConfigurationData = new ConfigurationData();
        mFrameRecordData = new FrameRecordData();
        mFrameSourceData = new FrameSourceData();
        mDeveloperData = new DeveloperData();
        mAudioData = new AudioData();
        mVehicleData = new VehicleData();
        mLaneDepartureWarningData = new LaneDepartureWarningData();
        mForwardCollisionWarningData = new ForwardCollisionWarningData();
        mBlindSpotWarningData = new BlindSpotWarningData();
        mSpeedLimitWarningData = new SpeedLimitWarningData();
        mTrafficSignalData = new TrafficSignalData();
        mAdaptiveCruiseControlData = new AdaptiveCruiseControlData();
        mLaneKeerpingAssistData = new LaneKeerpingAssistData();
    }

    public SharedPreferences getLocalPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
        //return context.getSharedPreferences(mPreferencesKey, Context.MODE_PRIVATE);
    }

    public boolean load(Context context) {
        boolean ret = false;

        if(context != null) {
            // Check init first...
            final SharedPreferences defaultValueSp = context.getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
            if(!defaultValueSp.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
                PreferenceManager.setDefaultValues(context, R.xml.pref_system, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_adas_setting, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_developer_setting, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_recoder_setting, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_vehiclebus_setting, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_camera_source_setting, true);
            }

            SharedPreferences sp = getLocalPreferences(context);
            mConfigurationData.load(context, sp);
            mFrameSourceData.load(context, sp);
            mFrameRecordData.load(context, sp);
            mDeveloperData.load(context, sp);
            mAudioData.load(context, sp);
            mVehicleData.load(context, sp);
            mLaneDepartureWarningData.load(context, sp);
            mForwardCollisionWarningData.load(context, sp);
            mBlindSpotWarningData.load(context, sp);
            mSpeedLimitWarningData.load(context, sp);
            mTrafficSignalData.load(context, sp);
            mAdaptiveCruiseControlData.load(context, sp);
            mLaneKeerpingAssistData.load(context, sp);
            
            ret = true;
        }
        return ret;
    }

    public boolean save(Context context)
    {
        boolean ret = false;

        if(context != null) {
            SharedPreferences sp = getLocalPreferences(context);
            SharedPreferences.Editor editor = sp.edit();

            mConfigurationData.save(context, editor);
            mFrameSourceData.save(context, editor);
            mFrameRecordData.save(context, editor);
            mDeveloperData.save(context, editor);
            mVehicleData.save(context, editor);
            mAudioData.save(context, editor);
            mLaneDepartureWarningData.save(context, editor);
            mForwardCollisionWarningData.save(context, editor);
            mBlindSpotWarningData.save(context, editor);
            mSpeedLimitWarningData.save(context, editor);
            mSpeedLimitWarningData.save(context, editor);
            mAdaptiveCruiseControlData.save(context, editor);
            mLaneKeerpingAssistData.save(context, editor);

            editor.apply();
            ret = true;
        }
        return ret;
    }

    public ConfigurationData getConfigurationData() {
        return mConfigurationData;
    }

    public FrameRecordData getFrameRecordData() {
        return mFrameRecordData;
    }

    public FrameSourceData getFrameSourceData() {
        return mFrameSourceData;
    }

    public DeveloperData getDeveloperData() {
        return mDeveloperData;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }

    public LaneDepartureWarningData getLaneDepartureWarningData() {
        return mLaneDepartureWarningData;
    }

    public ForwardCollisionWarningData getForwardCollisionWarningData() {
        return mForwardCollisionWarningData;
    }

    public BlindSpotWarningData getBlindSpotWarningData() {
        return mBlindSpotWarningData;
    }

    public SpeedLimitWarningData getSpeedLimitWarningData() {
        return mSpeedLimitWarningData;
    }

    public TrafficSignalData getTrafficSignalData () {
        return mTrafficSignalData;
    }

    public AudioData getAudioData() {
        return mAudioData;
    }

    public AdaptiveCruiseControlData getAdaptiveCruiseControlData() {
        return mAdaptiveCruiseControlData;
    }

    public LaneKeerpingAssistData getLaneKeerpingAssistData() {
        return mLaneKeerpingAssistData;
    }

}

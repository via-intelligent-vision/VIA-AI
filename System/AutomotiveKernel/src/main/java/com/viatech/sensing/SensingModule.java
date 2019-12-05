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

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import com.viatech.camera.CameraModule;
import com.viatech.exception.NoDetectorMatchedException;
import com.viatech.media.FrameFormat;
import com.viatech.resource.ResourceManager;
import com.viatech.resource.RuntimeLoadableDataTypes;
import com.viatech.vBus.CANbusModule;

import java.nio.ByteBuffer;
import java.util.EnumSet;

public class SensingModule {
    private final static String TAG = "SensingＭodule";
    public interface Callbacks {
        void onCalibrationFinish(boolean success);
    }

    static boolean mLibraryLoadStatus = false;

    static {
        try {
            System.loadLibrary("VIA-AI");
            mLibraryLoadStatus = true;
        }
        catch(java.lang.UnsatisfiedLinkError e) {
            Log.e(TAG, "Fail to load library ... " + e.getMessage());
            mLibraryLoadStatus = false;
        }
    }

    public enum SensingMode {
        Idle (0),
        Sensing (1),
        Release (2),
        OnTheFlyCalibration (3),
        ;

        private int mIndex = -1;
        SensingMode(int index)
        {
            mIndex = index;
        }

        public int getIndex()
        {
            return mIndex;
        }

        public static SensingMode getType(int index)
        {
            for (int i = 0; i < SensingMode.values().length; i++) {
                if (index == SensingMode.values()[i].getIndex()) {
                    return SensingMode.values()[i];
                }
            }
            return Idle;
        }
    };

    private Callbacks mCallback = null;
    private SensingMode mMode = SensingMode.Idle;
    private long mNatvieObjAddress = 0;
    private int mActiveDetectorFlags = 0;
    private EnumSet<DetectorTypes> mSupportDetectorSet = EnumSet.of(DetectorTypes.NONE);
    private EnumSet<DetectorTypes> mActiveDetectorSet = EnumSet.of(DetectorTypes.NONE);
    private volatile boolean bIsInitSuccess = false;

    public void setCallback(Callbacks callback) {
        mCallback = callback;
    }

    private native long jni_create(int detectorModuleFlags, long cameraModuleAddress, long canbusModuleAddress);
    public SensingModule(DetectorTypes type, @Nullable CANbusModule canbusModule, @Nullable CameraModule cameraModule)
    {
        this.mNatvieObjAddress = 0;

        // Set default modules.
        this.mActiveDetectorSet = EnumSet.of(type);
        this.mSupportDetectorSet = this.mActiveDetectorSet.clone();

        for(DetectorTypes detector : mActiveDetectorSet) {
            mActiveDetectorFlags |= detector.getIndex();
        }

        allocSamples();

        // create native module
        long cameraModuleAddress = 0;
        long canbusModuleAddress = 0;
        if(cameraModule != null) cameraModuleAddress = cameraModule.getModuleNativeAddress();
        if(canbusModule != null) canbusModuleAddress = canbusModule.getModuleNativeAddress();
        mNatvieObjAddress = jni_create(mActiveDetectorFlags, cameraModuleAddress, canbusModuleAddress);
    }

    public SensingModule(EnumSet<DetectorTypes> typeSet, @Nullable CANbusModule canbusModule, @Nullable CameraModule cameraModule)
    {
        this.mNatvieObjAddress = 0;

        // Set default modules.
        this.mActiveDetectorSet = typeSet.clone();
        this.mSupportDetectorSet = this.mActiveDetectorSet.clone();

        for(DetectorTypes detector : mActiveDetectorSet) {
            mActiveDetectorFlags |= detector.getIndex();
        }

        allocSamples();

        // create native module
        long cameraModuleAddress = 0;
        long canbusModuleAddress = 0;
        if(cameraModule != null) cameraModuleAddress = cameraModule.getModuleNativeAddress();
        if(canbusModule != null) canbusModuleAddress = canbusModule.getModuleNativeAddress();
        mNatvieObjAddress = jni_create(mActiveDetectorFlags, cameraModuleAddress, canbusModuleAddress);
    }

    private native boolean jni_updateCameraModule(long natvieObjAddress, long cameraModuleAddress, int detectorModuleFlags);
    public boolean updateCameraModule(CameraModule cameraModule)
    {
        boolean ret = false;
        long rAddress = cameraModule.getModuleNativeAddress();

        if(mNatvieObjAddress == 0) {
            throw new IllegalAccessError("Sensing module doesn't created.");
        }
        else if(rAddress == 0) {
            throw new IllegalAccessError("camera module doesn't created.");
        }
        else {
            ret = jni_updateCameraModule(mNatvieObjAddress, rAddress, mActiveDetectorFlags);
        }

        return ret;
    }


    // TODO : Add exception , module couldn't toggle unexist module in constructor.
    public void toggleDetectorModule(DetectorTypes type, boolean enable)
    {
        // check
        if(this.mSupportDetectorSet.contains(type)) {
            if (enable) {
                mActiveDetectorSet.add(type);
                mActiveDetectorFlags |= type.getIndex();
            } else {
                mActiveDetectorSet.remove(type);
                mActiveDetectorFlags &= ~type.getIndex();
            }
        }
        else {
            throw new IllegalArgumentException("TRy to toggle nonsupport detector <" + type.toString() + ">. Only " + mSupportDetectorSet.toString() + " support in this module.");
        }
    }

    public boolean isDetectorActive(DetectorTypes type)
    {
        return mActiveDetectorSet.contains(type);
    }


    private native boolean jni_init(long natvieObjAddress, String cfgPath);
    public boolean init(Context context, String cfgPath)
    {
        if(mLibraryLoadStatus == true) {
            restoreResources(context);
            bIsInitSuccess = jni_init(mNatvieObjAddress, cfgPath);
        }
        return isInitSuccess();
    }
    
    public boolean isInitSuccess() {
        return bIsInitSuccess;
    }

    private native void jni_release(long natvieObjAddress, int detectorModuleFlags);
    public void release()
    {
        if(mLibraryLoadStatus == true) {
            jni_release(mNatvieObjAddress, mActiveDetectorFlags);
        }
    }


    private native int jni_getFrameQueueCount(long natvieObjAddress);
    public int getFrameQueueCount() {
        int ret = 0;
        if(mLibraryLoadStatus == true) {
            ret = jni_getFrameQueueCount(mNatvieObjAddress);
        }
        return ret;
    }


    private native int jni_detect(long natvieObjAddress,
                                  boolean dumpDebugInfo,
                                  boolean externalCANBus,
                                  int speed,
                                  float steeringAngle, float steeringRatio,
                                  boolean dirIndicatorOn_L, boolean dirIndicatorOn_R);
    public void detect(boolean dumpDbg) {
        if(mLibraryLoadStatus == true) {
            detect(dumpDbg, 0, 0, 0, false, false);
        }
    }

    public void detect(boolean dumpDbg, int speed, float steeringAngle, float steeringRatio, boolean dirIndicatorOn_L, boolean dirIndicatorOn_R) {
        if(mLibraryLoadStatus == true) {
            SensingMode prevMode = mMode;

            int ret = jni_detect(mNatvieObjAddress, dumpDbg, true, speed, steeringAngle, steeringRatio, dirIndicatorOn_L, dirIndicatorOn_R);

            mMode = SensingMode.getType(ret);
            if(prevMode == SensingMode.OnTheFlyCalibration && mMode == SensingMode.Sensing) {
                Log.i(TAG, "ABC Calibration finish");
                if(mCallback != null) {
                    int calibStatus = jni_getCalibrationStatus(mNatvieObjAddress);
                    SensingSamples.CalibrationSample.CalibStatus status = SensingSamples.CalibrationSample.CalibStatus.getCalibStatus(calibStatus);
                    if(status ==  SensingSamples.CalibrationSample.CalibStatus.Success) {
                        mCallback.onCalibrationFinish(true);
                    }
                    else if(status ==  SensingSamples.CalibrationSample.CalibStatus.Fail) {
                        mCallback.onCalibrationFinish(false);
                    }

                }
            }
        }
    }

    public SensingModule.SensingMode getMode()
    {
        return mMode;
    }

    private native boolean jni_restoreConfiguration(long natvieObjAddress, String path);
    public boolean restoreConfiguration(String path) {
        boolean ret = false;
        if(mLibraryLoadStatus == true) {
            ret = jni_restoreConfiguration(mNatvieObjAddress, path);
        }
        return ret;
    }

    // About Internal Frame Buffer
    private native boolean jni_bufferFrame(long natvieObjAddress, ByteBuffer yFramePtr, ByteBuffer uFramePtr, ByteBuffer vFramePtr,
                                           int frameWidth, int frameHeight,
                                           int yStepStride, int uStepStride, int vStepStride,
                                           int yPixelStride, int uPixelStride, int vPixelStride,
                                           int roiX, int roiY, int roiWidth, int roiHeight);
    public boolean bufferFrame(ByteBuffer yPlaneBuffer, ByteBuffer uPlaneBuffer, ByteBuffer vPlaneBuffer,
                               int frameWidth, int frameHeight,
                               int yStepStride, int uStepStride, int vStepStride,
                               int yPixelStride, int uPixelStride, int vPixelStride,
                               int roiX, int roiY, int roiWidth, int roiHeight)
    {
        boolean ret = false;
        if((roiX % 4) != 0 || (roiY % 4) != 0) {
            throw new IllegalArgumentException("ROI [x, y,] must be a multiple of 4");
        }
        if((roiWidth & 0x01) != 0 || (roiHeight & 0x01) != 0) {
            throw new IllegalArgumentException("ROI [width, height] must be even number.");
        }

        if(mLibraryLoadStatus == true) {
            ret = this.jni_bufferFrame(mNatvieObjAddress,
                    yPlaneBuffer, uPlaneBuffer, vPlaneBuffer,
                    frameWidth, frameHeight,
                    yStepStride, uStepStride, vStepStride,
                    yPixelStride, uPixelStride, vPixelStride,
                    roiX, roiY, roiWidth, roiHeight);
        }
        return ret;
    }

    private native boolean jni_bufferFrame_NV12(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    public boolean bufferFrame_NV12(ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight)
    {
        boolean ret = false;

        if(mLibraryLoadStatus == true) {
            ret= jni_bufferFrame_NV12(mNatvieObjAddress, buffer, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
        }
        return ret;
    }

    private native boolean jni_bufferFrame_ARGB8888(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    public boolean bufferFrame_ARGB8888(ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight)
    {
        boolean ret = false;

        if(mLibraryLoadStatus == true) {
            ret= jni_bufferFrame_ARGB8888(mNatvieObjAddress, buffer, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
        }
        return ret;
    }

    private native boolean jni_bufferFrame_nativeAddress(long natvieObjAddress, long bufferAddrress, int frameFormat, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    private native boolean jni_bufferFrame_directByteBuffer(long natvieObjAddress, ByteBuffer buffer, int frameFormat, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    public boolean bufferFrame(@NonNull long bufferNativeAddress, @NonNull FrameFormat fmt, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight)
    {
        boolean ret = false;

        if(mLibraryLoadStatus == true) {
            boolean isFmtValid = false;
            switch (fmt) {
                case NV12:
                case ARGB8888:
                    isFmtValid = true;
                    break;
                default:
                    isFmtValid = false;
                    break;
            }

            if(!isFmtValid) {
                throw new IllegalArgumentException("Unsupported frame format : " + fmt.toString());
            }
            else {
                ret = jni_bufferFrame_nativeAddress(mNatvieObjAddress, bufferNativeAddress, fmt.getIndex(), frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
            }
        }
        return ret;
    }

    public boolean bufferFrame(@NonNull ByteBuffer directBuffer, @NonNull FrameFormat fmt, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight)
    {
        boolean ret = false;

        if(mLibraryLoadStatus == true) {
            boolean isFmtValid = false;
            switch (fmt) {
                case NV12:
                case ARGB8888:
                    isFmtValid = true;
                    break;
                default:
                    isFmtValid = false;
                    break;
            }

            if(!isFmtValid) {
                throw new IllegalArgumentException("Unsupported frame format : " + fmt.toString());
            }
            else {
                ret = jni_bufferFrame_directByteBuffer(mNatvieObjAddress, directBuffer, fmt.getIndex(), frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
            }
        }
        return ret;
    }


    private native boolean jni_bufferFrame_bitmap(long natvieObjAddress, Bitmap bitmap, int roiX, int roiY, int roiWidth, int roiHeight);
    public boolean bufferFrame(Bitmap bitmap, int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;

        if(mLibraryLoadStatus == true) {
            // check bitmap config first.
            switch (bitmap.getConfig()) {
                case ARGB_8888:
                    ret= jni_bufferFrame_bitmap(mNatvieObjAddress, bitmap, roiX, roiY, roiWidth, roiHeight);
                    break;
                default:
                    throw new IllegalArgumentException("Non support bitmap config : " + bitmap.getConfig().name());
            }
        }
        return ret;
    }

    private native boolean jni_setRuntimeLoadableData(long natvieObjAddress, int type, String path, String name);
    public boolean setRuntimeLoadableData(RuntimeLoadableDataTypes type, String path, String name) throws NoDetectorMatchedException
    {
        boolean ret = false;
        boolean noDetectorMatched = false;

        switch (type) {
            case FCWS_CascadeModel:
                if(mActiveDetectorSet.contains(DetectorTypes.FCWS)) {
                    ret = jni_setRuntimeLoadableData(mNatvieObjAddress, type.getIndex(), path, name);
                }
                else {
                    noDetectorMatched = true;
                }
                break;

            case FCWS_DL_Model:
            case FCWS_DL_DSPLib:
            case FCWS_DL_Label:
                if(mActiveDetectorSet.contains(DetectorTypes.FCWS_DL)) {
                    ret = jni_setRuntimeLoadableData(mNatvieObjAddress,type.getIndex(), path, name);
                }
                else {
                    noDetectorMatched = true;
                }
                break;

            case SLD_Model:
            case SLD_Proto:
            case SLD_Label:
            case SLD_NightModel:
            case SLD_PrefetchModel:
                if(mActiveDetectorSet.contains(DetectorTypes.SLD)) {
                    ret = jni_setRuntimeLoadableData(mNatvieObjAddress,type.getIndex(), path, name);
                }
                else {
                    noDetectorMatched = true;
                }
                break;
            case Weather_ClassifyModel:
                if(mActiveDetectorSet.contains(DetectorTypes.Weather)) {
                    ret = jni_setRuntimeLoadableData(mNatvieObjAddress,type.getIndex(), path, name);
                }
                else {
                    noDetectorMatched = true;
                }
                break;
            case TLD_Pattern_1:
            case TLD_Pattern_2:
            case TLD_Pattern_3:
                if(mActiveDetectorSet.contains(DetectorTypes.TLD)) {
                    ret = jni_setRuntimeLoadableData(mNatvieObjAddress,type.getIndex(), path, name);
                }
                else {
                    noDetectorMatched = true;
                }
                break;
        }

        if(noDetectorMatched) {
            throw new NoDetectorMatchedException("No detector [" + mActiveDetectorSet.toString() + "] match this resource type : " + type);
        }
        return ret;
    }

    private void restoreResources(Context context) {
        ResourceManager mResourceManager = new ResourceManager(context);
        String restorePath = context.getApplicationContext().getFilesDir().toString();

        // restore FCWS classifier
        if(this.isDetectorActive(DetectorTypes.FCWS)) {
            try {
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_CascadeModel, restorePath);
                String resourceName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_CascadeModel);

                this.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_CascadeModel, restorePath, resourceName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // restore weather classifier
        if(this.isDetectorActive(DetectorTypes.Weather)) {
            try {
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.Weather_ClassifyModel, restorePath);
                String resourceName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.Weather_ClassifyModel);

                this.setRuntimeLoadableData(RuntimeLoadableDataTypes.Weather_ClassifyModel, restorePath, resourceName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // restore SLD classifier
        if(this.isDetectorActive(DetectorTypes.SLD)) {
            try {
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Label, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Model, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Proto, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_NightModel, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_PrefetchModel, restorePath);

                String modelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Model);
                String protoName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Proto);
                String labelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Label);
                String nightModelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_NightModel);
                String prefetchModelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_PrefetchModel);

                if (modelName != null && protoName != null && labelName != null && nightModelName != null && prefetchModelName != null) {
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Label, restorePath, labelName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Model, restorePath, modelName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Proto, restorePath, protoName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_NightModel, restorePath, nightModelName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_PrefetchModel, restorePath, prefetchModelName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // TODO : Generate labels for FCWS_DL
        if(this.isDetectorActive(DetectorTypes.FCWS_DL)) {
            try {
                String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_DL_Model, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_DL_Label, restorePath);

                String modelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_DL_Model);
                String labelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_DL_Label);

                if(modelName != null && labelName != null) {
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_Model, restorePath, modelName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_Label, restorePath, labelName);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_DSPLib, nativeLibraryDir, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // restore TLD classifier
        if(this.isDetectorActive(DetectorTypes.TLD)) {
            try {
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.TLD_Pattern_1, restorePath);
                mResourceManager.restoreResource(RuntimeLoadableDataTypes.TLD_Pattern_2, restorePath);

                String pattern1Name = mResourceManager.getResourceName(RuntimeLoadableDataTypes.TLD_Pattern_1);
                String pattern2Name = mResourceManager.getResourceName(RuntimeLoadableDataTypes.TLD_Pattern_2);

                if (pattern1Name != null && pattern2Name != null) {
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.TLD_Pattern_1, restorePath, pattern1Name);
                    this.setRuntimeLoadableData(RuntimeLoadableDataTypes.TLD_Pattern_2, restorePath, pattern2Name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private native long jni_getModuleNativeAddress(long addr);
    public long getModuleNativeAddress()
    {
        return jni_getModuleNativeAddress(mNatvieObjAddress);
    }

    private native String jni_getConfiguration(long natvieObjAddress, int detectorModuleFlags);
    public String getConfiguration()
    {
        String ret = null;
        if(mLibraryLoadStatus == true) {
            ret = jni_getConfiguration(mNatvieObjAddress, mActiveDetectorFlags);
        }
        return ret;
    }


    private native boolean jni_registerRelatedModule(long natvieObjAddress, long relatedObjAddress, int relatedDetector);
    public boolean registerRelatedModule(SensingModule module, DetectorTypes relatedDetector) {
        boolean ret = false;
        long rAddress = module.getModuleNativeAddress();

        if(mNatvieObjAddress == 0) {
            throw new IllegalAccessError("Sensing module doesn't created.");
        }
        else if(rAddress == 0) {
            throw new IllegalAccessError("Related sensing module doesn't created.");
        }
        else {
            ret = jni_registerRelatedModule(mNatvieObjAddress, rAddress, relatedDetector.getIndex());
        }
        return ret;
    }


    /**
        @brief Functions about calibration
     */
    private native int jni_getCalibrationStatus(long natvieObjAddress);
    private native boolean jni_enableOnTheFlyCalibration(long natvieObjAddress, float cameraInstalledHeight, float cameraToCenterOffset);

    public boolean enableOnTheFlyCalibration(float cameraInstalledHeight, float cameraToCenterOffset) {
        return jni_enableOnTheFlyCalibration(mNatvieObjAddress, cameraInstalledHeight, cameraToCenterOffset);
    }

    public EnumSet<DetectorTypes> getDetectorSet() {
        return mActiveDetectorSet;
    }

    /**
     @brief Sensing samples
     */
    private SensingSamples.LaneDetectSample mSample_LaneDetect;
    private SensingSamples.VehicleDetectSample mSample_VehicleDetect;
    private SensingSamples.BlindSpotDetectSample mSample_BlindSpotDetect;
    private SensingSamples.SpeedLimitDetectSample mSample_SpeedLimitDetect;
    private SensingSamples.EnvironmentSample mSample_Environment;
    private SensingSamples.ObjectDetectSample mSample_ObjectDetect;
    private SensingSamples.TrafficLightSample mSample_TrafficLight;

    private void allocSamples() {
        if(mSupportDetectorSet.contains(DetectorTypes.LDWS))
            mSample_LaneDetect = new SensingSamples.LaneDetectSample();

        if(mSupportDetectorSet.contains(DetectorTypes.FCWS) || mSupportDetectorSet.contains(DetectorTypes.FCWS_DL))
            mSample_VehicleDetect = new SensingSamples.VehicleDetectSample();

        if(mSupportDetectorSet.contains(DetectorTypes.BSD_L) || mSupportDetectorSet.contains(DetectorTypes.BSD_R))
            mSample_BlindSpotDetect = new SensingSamples.BlindSpotDetectSample();

        if(mSupportDetectorSet.contains(DetectorTypes.SLD))
            mSample_SpeedLimitDetect = new SensingSamples.SpeedLimitDetectSample();

        if(mSupportDetectorSet.contains(DetectorTypes.FCWS_DL))
            mSample_ObjectDetect = new SensingSamples.ObjectDetectSample();

        if(mSupportDetectorSet.contains(DetectorTypes.Weather))
            mSample_Environment = new SensingSamples.EnvironmentSample();

        if(mSupportDetectorSet.contains(DetectorTypes.TLD))
            mSample_TrafficLight = new SensingSamples.TrafficLightSample();
    }

    public SensingSamples.SensingSample getSensingSample(SensingSamples.SampleTypes type) {
        SensingSamples.SensingSample ret = null;
        switch (type) {
            case LaneDetectSample:
                ret = mSample_LaneDetect;
                break;
            case VehicleDetectSample:
                ret = mSample_VehicleDetect;
                break;
            case BlindSpotDetectSample:
                ret = mSample_BlindSpotDetect;
                break;
            case SpeedLimitDetectSample:
                ret = mSample_SpeedLimitDetect;
                break;
            case EnvironmentSample:
                ret = mSample_Environment;
                break;
            case ObjectDetectSample:
                ret = mSample_ObjectDetect;
                break;
            case TrafficLightDetectSample:
                ret = mSample_TrafficLight;
                break;
        }
        return  ret;
    }



    /**
        @brief set function is for JNI
        */
    private float tmpLaneL[] = null, tmpLaneR[] = null, tmpScore[] = null;
    private native boolean jni_updateLaneData(long natvieObjAddress, float []laneL, float [] laneR, float [] score);
    private void setSample_LaneDetect(float frameSizeWidth, float frameSizeHeight, float laneWidth, int laneStatus) {
        mSample_LaneDetect.mFrameSize[0] = frameSizeWidth;
        mSample_LaneDetect.mFrameSize[1] = frameSizeHeight;
        mSample_LaneDetect.mLaneWidth = laneWidth;
        mSample_LaneDetect.mLaneStatus = SensingSamples.LaneDetectSample.SampleStatus.getWeatherType(laneStatus);

        if(tmpScore == null) tmpScore = new float[2];
        if(tmpLaneL == null) tmpLaneL = new float[SensingSamples.LaneDetectSample.RESAMPLE_COUNT * 2];
        if(tmpLaneR == null) tmpLaneR = new float[SensingSamples.LaneDetectSample.RESAMPLE_COUNT * 2];
        jni_updateLaneData(mNatvieObjAddress, tmpLaneL, tmpLaneR, tmpScore);
        int cc = 0;
        for(int pi = 0 ; pi < SensingSamples.LaneDetectSample.RESAMPLE_COUNT; pi++) {
            mSample_LaneDetect.mLaneAnchor_L[pi].x = tmpLaneL[cc];
            mSample_LaneDetect.mLaneAnchor_R[pi].x = tmpLaneR[cc++];
            mSample_LaneDetect.mLaneAnchor_L[pi].y = tmpLaneL[cc];
            mSample_LaneDetect.mLaneAnchor_R[pi].y = tmpLaneR[cc++];
        }
    }

    private void setSample_VehicleDetect(int objTypeIndex, float score,
                                         float minX, float minY,  float maxX, float maxY,
                                         float distance, float reactionTime) {
        mSample_VehicleDetect.mForwardVehicle.mObjTypeIndex = objTypeIndex;
        mSample_VehicleDetect.mForwardVehicle.mScore = score;
        mSample_VehicleDetect.mForwardVehicle.mMinAnchor[0] = minX;
        mSample_VehicleDetect.mForwardVehicle.mMinAnchor[1] = minY;
        mSample_VehicleDetect.mForwardVehicle.mMaxAnchor[0] = maxX;
        mSample_VehicleDetect.mForwardVehicle.mMaxAnchor[1] = maxY;
        mSample_VehicleDetect.mForwardVehicle.mDistance = distance;
        mSample_VehicleDetect.mForwardVehicle.mReactionTime = reactionTime;
    }

    private void setSample_BlindSpotDetect(boolean mIsLeftWarning, boolean mIsRightWarning, boolean isFromLeft, boolean isFromRight) {
        if(isFromLeft) {
            mSample_BlindSpotDetect.mIsLeftWarning = mIsLeftWarning;
        }

        if(isFromRight) {
            mSample_BlindSpotDetect.mIsRightWarning = mIsRightWarning;
        }
    }

    private void setSample_SpeedLimitDetect(int mSpeedLimit_1, int mSpeedLimit_2) {
        mSample_SpeedLimitDetect.mSpeedLimit_1 = mSpeedLimit_1;
        mSample_SpeedLimitDetect.mSpeedLimit_2 = mSpeedLimit_2;
    }

    private void setSample_Environment(int weatherType) {
        mSample_Environment.mWeatherType = SensingSamples.EnvironmentSample.WeatherTypes.getWeatherType(weatherType);
    }

    public void setSample_setObjectSampleStatus(int sampleCount, int focusObjectId) {
        mSample_ObjectDetect.mSampleCount = sampleCount;
        mSample_ObjectDetect.mFocusObjectId = focusObjectId;
    }

    public void setSample_AddObjectData(int index, int objTypeIndex, float score, float minX, float minY,  float maxX, float maxY, float distance) {
        mSample_ObjectDetect.mObjectDataList[index].mObjTypeIndex = objTypeIndex;
        mSample_ObjectDetect.mObjectDataList[index].mScore = score;
        mSample_ObjectDetect.mObjectDataList[index].mMinAnchor[0] = minX;
        mSample_ObjectDetect.mObjectDataList[index].mMinAnchor[1] = minY;
        mSample_ObjectDetect.mObjectDataList[index].mMaxAnchor[0] = maxX;
        mSample_ObjectDetect.mObjectDataList[index].mMaxAnchor[1] = maxY;
        mSample_ObjectDetect.mObjectDataList[index].mDistance = distance;
    }

    public void setSample_setTrafficLightSampleStatus(int sampleCount) {
        mSample_TrafficLight.mSampleCount = sampleCount;
    }

    public void setSample_AddTrafficLightData(int index, int objTypeIndex, float score, float minX, float minY,  float maxX, float maxY) {
        mSample_TrafficLight.mObjectDataList[index].mObjTypeIndex = objTypeIndex;
        mSample_TrafficLight.mObjectDataList[index].mScore = score;
        mSample_TrafficLight.mObjectDataList[index].mMinAnchor[0] = minX;
        mSample_TrafficLight.mObjectDataList[index].mMinAnchor[1] = minY;
        mSample_TrafficLight.mObjectDataList[index].mMaxAnchor[0] = maxX;
        mSample_TrafficLight.mObjectDataList[index].mMaxAnchor[1] = maxY;
        mSample_TrafficLight.mObjectDataList[index].mDistance = 0;
    }


}

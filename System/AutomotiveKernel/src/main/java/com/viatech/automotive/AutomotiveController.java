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

package com.viatech.automotive;

import android.util.Log;

import com.viatech.automotive.LatitudePlanner.LatitudePlan;
import com.viatech.automotive.event.EventLevels;
import com.viatech.automotive.event.EventSlot;
import com.viatech.automotive.event.EventTypes;
import com.viatech.camera.CameraLocationTypes;
import com.viatech.camera.CameraModule;
import com.viatech.car.CarTypes;
import com.viatech.sensing.SensingModule;
import com.viatech.vBus.CANbusModule;

import java.io.File;
import java.io.IOException;

public class AutomotiveController {
    private final static String TAG = "AutomotiveController";

    private static boolean mLibraryLoadStatus = false;

    static {
        try {
            System.loadLibrary("VIA-AI");
            mLibraryLoadStatus = true;
        }
        catch(java.lang.UnsatisfiedLinkError e) {
            Log.e(TAG, "Fail to load library .... " + e.getMessage());
            mLibraryLoadStatus = false;
        }
    }

    private long mNatvieObjAddress = 0;
    private CarTypes mCarType;
    private LatitudePlan mLatitudePlan;
    private EventSlot mPrevEvent;
    private EventSlot mLastEvent;
    private String mCalibExportPath;


    private void checkFileExist(String fPath) throws IOException {
        File f = new File(fPath);
        if (!f.exists()) {
            throw new IOException("Fail not exist : "+ fPath);
        }

        if(!f.canRead()) {
            throw new IOException("Fail to R/W file " + fPath);
        }
    }

    private void checkExportPath(String fPath) throws IOException {
        File f = new File(fPath);
        if(f.isFile()) {
            throw new IOException("Couldn't set file as export path.");
        }
        else if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new IOException("Fail to create directory " + fPath);
            }
        }
        else if (!f.canWrite() || !f.canRead()) {
            throw new IOException("Fail to R/W directory " + fPath);
        }
    }

    private native long jni_create(int carType, String cfgPath, String calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset);
    public AutomotiveController(CarTypes types, String cfgPath, String calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset) throws IOException {
        // check file exist
        checkFileExist(cfgPath);
        checkExportPath(calibExportPath);

        mCarType = types;
        mCalibExportPath = calibExportPath+ "/autoCalib.xml";
        mNatvieObjAddress = jni_create(types.getIndex(), cfgPath, mCalibExportPath, cameraInstalledHeight, cameraToCenterOffset);
        mLatitudePlan = new LatitudePlan();
        mLastEvent = new EventSlot();
        mPrevEvent = new EventSlot();

        Log.i(TAG, "Calibration Export Path " + mCalibExportPath);

    }

    private native void jni_release(long moduleAddr);
    public void release() {
        jni_release(mNatvieObjAddress);
        mNatvieObjAddress = 0;
    }


    boolean b_IsRecording = false;
    String recordPath = "";
    public boolean isRecording() {
        return b_IsRecording;
    }

    public String getRecordPath() {
        return recordPath;
    }

    private native void jni_stopRecord(long addr);
    public void stopRecord() {
        synchronized (this) {
            jni_stopRecord(mNatvieObjAddress);
            b_IsRecording = false;
        }
    }

    private native boolean jni_startRecord(long addr, String path, boolean appendFile);
    public void startRecord(String path, String name, boolean appendFile) {
        synchronized (this) {
            recordPath = path + "/" + name;
            b_IsRecording = jni_startRecord(mNatvieObjAddress, recordPath, appendFile);
        }
    }

    public long getModuleNativeAddress() {
        return mNatvieObjAddress;
    }

    private native void jni_runAutoControl(long moduleAddr);
    public void runAutoControl() {
        if(mNatvieObjAddress != 0) {
            mLastEvent.copyTo(mPrevEvent);  // keep event

            jni_runAutoControl(mNatvieObjAddress);  // run control and update event in jni.

            if(mOnEventChangeListener != null) {
                if(mPrevEvent.getType() != mLastEvent.getType()) mOnEventChangeListener.onEventChange(mPrevEvent, mLastEvent);
            }
        }
        else {
            throw new IllegalStateException("Controller not create");
        }
    }

    public String getCalibExportPath() {
        return mCalibExportPath;
    }

    // Method is called by jni
    private native boolean jni_updateLatitudePlanTrajectory(long natvieObjAddress, float []trajectoryL, float [] trajectoryR);
    private native boolean jni_updateLatitudePlanLaneData(long natvieObjAddress, float []laneL, float [] laneR, float [] score);
    private void updateLatitudePlan(float planStartSteerAngle, float planDesiredSteerAngle, boolean isValid, boolean isSteerControllable, boolean isSteerOverControl) {
        mLatitudePlan.planeStartSteerAngle = planStartSteerAngle;
        mLatitudePlan.planDesiredSteerAngle = planDesiredSteerAngle;
        mLatitudePlan.isPlanValid = isValid;
        mLatitudePlan.isSteerControllable = isSteerControllable;
        mLatitudePlan.isSteerOverControl = isSteerOverControl;
        jni_updateLatitudePlanTrajectory(mNatvieObjAddress, mLatitudePlan.mTrajectory_L, mLatitudePlan.mTrajectory_R);

        float score [] = new float[2];
        jni_updateLatitudePlanLaneData(mNatvieObjAddress, mLatitudePlan.mLaneAnchors_L, mLatitudePlan.mLaneAnchors_R, score);
        mLatitudePlan.mLaneScore_L = score[0];
        mLatitudePlan.mLaneScore_R = score[1];
    }

    // Events
    // Method is called by jni
    public interface OnEventChangeListener {
        public void onEventChange(EventSlot lastEvent, EventSlot curEvent);
    }
    private OnEventChangeListener mOnEventChangeListener = null;

    private void updateEvent(int type, int level, double time) {
        mLastEvent.setType(EventTypes.getType(type));
        mLastEvent.setLevel(EventLevels.getType(level));
        mLastEvent.setTime(time);
    }

    public EventSlot getEvent() {
        return mLastEvent;
    }

    public void setOnEventChangeListener(OnEventChangeListener listener) {
        mOnEventChangeListener = listener;
    }


    // Canbus
    private native boolean jni_registerCANbusModule(long moduleAddr, long refAddr);
    public boolean registerCANbusModule(CANbusModule ref) {
        boolean ret = false;


        if(mNatvieObjAddress != 0) {
            //long refAddr = ref.getModuleNativeAddress();
            long refAddr = 0;
            if(ref != null) refAddr = ref.getModuleNativeAddress();

            if(refAddr != 0) {
                ret = jni_registerCANbusModule(mNatvieObjAddress, refAddr);
            }
            else {
                throw new IllegalArgumentException("CANbusModule doesn't created.");
            }
        }
        else {
            throw new IllegalArgumentException("Controller doesn't created.");
        }
        return ret;
    }


    private native boolean jni_registerCameraModule(long moduleAddr, long refAddr);
    public boolean registerCameraModule(CameraModule ref) {
        boolean ret = false;
        //long refAddr = ref.getModuleNativeAddress();

        if(mNatvieObjAddress != 0) {
            long refAddr = 0;
            if(ref != null) refAddr = ref.getModuleNativeAddress();

            if(refAddr != 0) {
                ret = jni_registerCameraModule(mNatvieObjAddress, refAddr);
            }
            else {
                throw new IllegalArgumentException("CameraModule doesn't created.");
            }
        }
        else {
            throw new IllegalArgumentException("Controller doesn't created.");
        }
        return ret;
    }


    private native boolean jni_registerSensingModule_Lane(long moduleAddr, long refAddr);
    public boolean registerSensingModule_Lane(SensingModule ref) {
        boolean ret = false;
        long refAddr = ref.getModuleNativeAddress();

        if(mNatvieObjAddress != 0) {
            if(refAddr != 0) {
                ret = jni_registerSensingModule_Lane(mNatvieObjAddress, refAddr);
            }
            else {
                throw new IllegalArgumentException("SensingModule doesn't created.");
            }
        }
        else {
            throw new IllegalArgumentException("Controller doesn't created.");
        }
        return ret;
    }


    private native boolean jni_registerSensingModule_ForwardVehicle(long moduleAddr, long refAddr);
    public boolean registerSensingModule_ForwardVehicle(SensingModule ref) {
        boolean ret = false;
        long refAddr = ref.getModuleNativeAddress();

        if(mNatvieObjAddress != 0) {
            if(refAddr != 0) {
                ret = jni_registerSensingModule_ForwardVehicle(mNatvieObjAddress, refAddr);
            }
            else {
                throw new IllegalArgumentException("SensingModule doesn't created.");
            }
        }
        else {
            throw new IllegalArgumentException("Controller doesn't created.");
        }
        return ret;
    }

    /**
      Get data of controller.
     @return  A non-null object {@link com.via.adas.automotive.LatitudePlanner.LatitudePlan} will return.
     */
    public LatitudePlan getLatitudePlan() {
        return mLatitudePlan;
    }

    /**
     @brief Functions about calibration
     */
    private native boolean jni_runCameraCalibration(long natvieObjAddress, int cameraLocation, float cameraInstalledHeight, float cameraToCenterOffset);

    public boolean runCameraCalibration(CameraLocationTypes cameraLocation, float cameraInstalledHeight, float cameraToCenterOffset) {
        boolean ret = false;
        if(mNatvieObjAddress == 0) {
            throw new IllegalAccessError("Sensing module doesn't created.");
        }
        else {
            ret = jni_runCameraCalibration(mNatvieObjAddress, cameraLocation.getIndex(), cameraInstalledHeight, cameraToCenterOffset);
        }
        return ret;
    }






    // TODO : Remove
    private native void jni_setCtl_SteeringControl(long natvieObjAddress, boolean steerTorqueRequest, long steerTorque);
    public void setCtl_SteeringControl(boolean steerTorqueRequest, long steerTorque)
    {
        if(mNatvieObjAddress == 0) {
            throw new IllegalAccessError("Sensing module doesn't created.");
        }
        else {
            jni_setCtl_SteeringControl(mNatvieObjAddress, steerTorqueRequest, steerTorque);
        }
    }


}

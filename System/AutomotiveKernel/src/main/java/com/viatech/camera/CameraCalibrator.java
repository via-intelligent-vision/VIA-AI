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

package com.viatech.camera;


import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Size;

import com.viatech.exception.ObjectNotCreatedException;
import com.viatech.exception.UnsupportedFrameFormtException;
import com.viatech.media.FrameFormat;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCalibrator {

    static {
        System.loadLibrary("VIA-AI");
    }

    private long mNatvieObjAddress;
    private float mCameraMatrix_3x3[];
    private float mDistCoeff_5x1[];
    private Size mCalibSize;
    private boolean mIsCalibrated;
    private double repositoryRatio;

    /** Constructor of class
    */
    public CameraCalibrator() {
        mNatvieObjAddress = 0;
        mCameraMatrix_3x3 = new float [3*3];
        mDistCoeff_5x1 = new float [5];
        mCalibSize = new Size(1280, 720);
        mIsCalibrated = false;
        repositoryRatio = 0.0;
    }


    private native long jni_create();
    private native boolean jni_init(long objAddr, int boardSize_w, int boardSize_h, float gridSize_w, float gridSize_h) ;
    public boolean init(int boardSize_w, int boardSize_h, float gridSize_w, float gridSize_h) throws ObjectNotCreatedException {
        if(mNatvieObjAddress == 0) {
            mNatvieObjAddress = jni_create();
        }

        if(mNatvieObjAddress != 0) {
            return jni_init(mNatvieObjAddress, boardSize_w, boardSize_h, gridSize_w, gridSize_h);
        }
        else {
            throw new ObjectNotCreatedException("Object create fail.");
        }
    }

    private native void jni_release(long objAddr);
    public void release() {
        if(mNatvieObjAddress != 0) jni_release(mNatvieObjAddress);
        mNatvieObjAddress = 0;
    }

    public native boolean jni_save(long objAddr, String cameraName, String exportPath);
    public boolean save(@NonNull String cameraName, @NonNull String exportPath) {
        if(mNatvieObjAddress != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "camera_calibration_" + sdf.format(new Date()) + ".xml";

            return jni_save(mNatvieObjAddress, cameraName, exportPath + "/" + fileName);
        }
        else {
            throw new ObjectNotCreatedException("Object create fail.");
        }
    }

    private native boolean jni_findPattern_NV12(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    private native boolean jni_findPattern_BGR888(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    private native boolean jni_findPattern_RGB888(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    private native boolean jni_findPattern_ARGB888(long natvieObjAddress, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);
    //public boolean findPattern(FrameFormat fmt, ByteBuffer buffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight) throws ObjectNotCreatedException {
    public boolean findPattern(FrameFormat fmt, ByteBuffer buffer, int frameWidth, int frameHeight) throws ObjectNotCreatedException, UnsupportedFrameFormtException {
        boolean ret = false;
        if(mNatvieObjAddress == 0) throw new ObjectNotCreatedException("Object doesn't created, call init() before this operation.");

        switch (fmt) {
            case NV12:
            case NV21:
            case YUV420P:
                ret= jni_findPattern_NV12(mNatvieObjAddress, buffer, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
                break;
            case BGR888:
                ret= jni_findPattern_BGR888(mNatvieObjAddress, buffer, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
                break;
            case RGB888:
                ret= jni_findPattern_RGB888(mNatvieObjAddress, buffer, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
                break;
            case ARGB8888:
                ret= jni_findPattern_ARGB888(mNatvieObjAddress, buffer, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
                break;
            case Unknown:
            default:
                throw new UnsupportedFrameFormtException(fmt.toString());
        }
        repositoryRatio = jni_getRepositoryRatio(mNatvieObjAddress);

        return ret;
    }

    private native boolean jni_isCalibReady(long objAddr);
    public boolean isCalibReady() {
        return jni_isCalibReady(mNatvieObjAddress);
    }

    private native void jni_getDistCoeff(long objAddr, float []distCoeff);
    private native void jni_getCameraMatrix(long objAddr, float []cameraMatrix);
    private native void jni_getCalibSize(long objAddr, int []size);
    private native double jni_getRepositoryRatio(long objAddr);
    private native double jni_calibrate(long objAddr);
    public double calibrate() throws ObjectNotCreatedException {
        if(mNatvieObjAddress == 0) throw new ObjectNotCreatedException("Object doesn't created, call init() before this operation.");

        double rms = jni_calibrate(mNatvieObjAddress);
        if(rms >= 0.0) {
            int []tmp_size = new int[2];
            jni_getCameraMatrix(mNatvieObjAddress, mCameraMatrix_3x3);
            jni_getDistCoeff(mNatvieObjAddress, mDistCoeff_5x1);
            jni_getCalibSize(mNatvieObjAddress, tmp_size);

            mCalibSize = new Size(tmp_size[0], tmp_size[1]);
            mIsCalibrated = true;
        }

        return rms;
    }

    public double getRepositoryRatio(){
        return repositoryRatio;
    }

    public float[] getCameraMatrix() {
        return mCameraMatrix_3x3;
    }

    public float[] getDistCoeff() {
        return mDistCoeff_5x1;
    }

    public Size getCalibSize() {
        return mCalibSize;
    }

    public boolean isCalibrated() {
        return mIsCalibrated;
    }
}

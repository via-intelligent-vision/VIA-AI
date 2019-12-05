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

import android.support.annotation.NonNull;
import android.util.Log;

public class CameraModule {
    private final static String TAG = "SensingＭodule";

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

    private long mNatvieObjAddress = 0;
    private CameraTypes mCameraType;
    private CameraLocationTypes mCameraLocation;
    private volatile boolean bIsInitSuccess = false;
    private double []mCameraMatrix;
    private double []mDistCoeff;
    private double []mExtrinsic;

    private native long jni_create(int cameraType, int location, String instrinsicPath, String extrinsicPath);

    public CameraModule(CameraTypes types, CameraLocationTypes location, @NonNull String instrinsicPath, @NonNull String extrinsicPath) {
        this();
        init(types, location, instrinsicPath, extrinsicPath);
    }

    public CameraModule() {
        mCameraType = CameraTypes.Unknown;
        mCameraLocation = CameraLocationTypes.Unknown;
        mNatvieObjAddress = 0;
        mCameraMatrix = null;
        mDistCoeff = null;
        mExtrinsic = null;
    }

    public void init(CameraTypes types, CameraLocationTypes location, @NonNull String instrinsicPath, @NonNull String extrinsicPath) {
        mCameraType = types;
        mCameraLocation = location;
        mNatvieObjAddress = jni_create(mCameraType.getIndex(), mCameraLocation.getIndex(), instrinsicPath, extrinsicPath);
        if(mNatvieObjAddress != 0) {
            bIsInitSuccess = true;
        }
        mCameraMatrix = new double[3 * 3];
        mDistCoeff = new double[1 * 5];
        mExtrinsic = new double[4 * 4];
        updateData();
    }

    public boolean isInitSuccess() {
        return bIsInitSuccess;
    }

    private native void jni_release(long moduleAddress);
    public void release() {
        if(mNatvieObjAddress != 0) {
            jni_release(mNatvieObjAddress);
            mNatvieObjAddress = 0;
        }
    }

    public long getModuleNativeAddress() {
        return mNatvieObjAddress;
    }

    private native boolean jni_isStable(long moduleAddress);

    public boolean isStable() {
        boolean ret = false;
        if(mNatvieObjAddress != 0) {
            ret = jni_isStable(mNatvieObjAddress);
        }
        return ret;
    }

    private native boolean jni_getCameraMatrix(long moduleAddress, double []cameraMatrix_3x3);
    private native boolean jni_getExtrinsic(long moduleAddress, double []extrinsic_4x4);
    private native boolean jni_getDistCoeffs(long moduleAddress, double []distCoeff_1x5);

    public boolean updateData() {
        boolean ret = false;
        if(mNatvieObjAddress != 0) {
            ret = jni_getCameraMatrix(mNatvieObjAddress, mCameraMatrix);
            ret &= jni_getDistCoeffs(mNatvieObjAddress, mDistCoeff);
            ret &= jni_getExtrinsic(mNatvieObjAddress, mExtrinsic);
        }
        return ret;
    }

    public double[] getCameraMatrix() {
        return mCameraMatrix;
    }
    public double[] getDistCoeffs() {
        return mDistCoeff;
    }

    public double[] getExtrinsic() {
        return mExtrinsic;
    }

    private native boolean jni_coordCvt(long moduleAddress, int type, double x, double y, double z, double[]ret);

    public double[] coordCvt(CoordCvtTypes type, double x, double y, double z) {
        double []ret = null;
        if(mNatvieObjAddress != 0) {
            ret = new double[3];
            jni_coordCvt(mNatvieObjAddress, type.id(), x, y, z, ret);
        }
        return ret;
    }

    public double[] coordCvt(CoordCvtTypes type, double x, double y) {
        return coordCvt(type, x, y);
    }


}

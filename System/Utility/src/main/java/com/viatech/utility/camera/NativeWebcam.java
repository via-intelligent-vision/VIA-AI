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

package com.viatech.utility.camera;

import java.io.File;
import java.nio.ByteBuffer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

public class NativeWebcam {
    static {
        System.loadLibrary("viautility");
    }

    private static String TAG = "NativeWebcam";
    private static final int DEFAULT_IMAGE_WIDTH = 720;
    private static final int DEFAULT_IMAGE_HEIGHT = 480;

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private int format = 0x11;


    private native int startCamera(String deviceName, int width, int height);
    private native void processCamera();
    private native boolean cameraAttached();
    private native void stopCamera();
    private native void loadNextFrame(Bitmap bitmap);

    public native void next(Surface s, ByteBuffer dest);
    public native int dequeueFrameNative(Surface s, ByteBuffer dest, int w, int h, int format);
    public native long getFramePointer(int index);
    public native void queueFrameNative(int bufIndex);
    public native void setWatermark(AssetManager amr,String filename, int w, int h);

    public synchronized int dequeueFrame(Surface s, ByteBuffer dest) {
        return dequeueFrameNative(s,dest,mWidth,mHeight,format);
    }

    public synchronized void queueFrame(int index) {
        queueFrameNative(index);
    }


    public NativeWebcam(String deviceName, int width, int height) {
        mWidth = width;
        mHeight = height;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        connect(deviceName, mWidth, mHeight);
    }

    public NativeWebcam(String deviceName) {
        this(deviceName, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    private void connect(String deviceName, int width, int height) {
        boolean deviceReady = true;

        File deviceFile = new File(deviceName);
        if(deviceFile.exists()) {
            if(!deviceFile.canRead()) {
                Log.d(TAG, "Insufficient permissions on " + deviceName +
                        " -- does the app have the CAMERA permission?");
                deviceReady = true;
            }
        } else {
            Log.w(TAG, deviceName + " does not exist");
            deviceReady = false;
        }

        if(deviceReady) {
            Log.i(TAG, "Preparing camera with device name " + deviceName);
//            Log.d("HANK", "Result:"+startCamera(deviceName, width, height));
            startCamera(deviceName, width, height);
        }
    }

    public Bitmap getFrame() {
        loadNextFrame(mBitmap);
        return mBitmap;
    }

    public void stop() {
        stopCamera();
    }

    public boolean isAttached() {
        return cameraAttached();
    }
}

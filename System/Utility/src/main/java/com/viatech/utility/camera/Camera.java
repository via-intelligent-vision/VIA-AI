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

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.viatech.utility.tool.NativeRender;
import com.viatech.utility.video.VIARecorder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static android.graphics.ImageFormat.NV21;

public class Camera extends VIACamera {

    android.hardware.Camera mCamera = null;
    int mIndex = 0;
    byte[][] rawDatas = new byte[3][];
    final static String TAG = "Camera";
    int mWidth = 0;
    int mHeight = 0;
    Callback mCallback = null;

    SurfaceView mDisplaySurfaceView = null;
    SurfaceTexture mDisplaySurfaceTexture = null;

    @Override
    public void close() {
        Log.e(TAG, "in close");
        if(mVIARecorder!=null) {
            mVIARecorder.stop();
            mVIARecorder = null;
        }
        bRecord = false;
        Log.e(TAG, "in setPreviewCallback");
        mCamera.setPreviewCallback(null);
        Log.e(TAG, "in stopPreview");
        mCamera.stopPreview();
        Log.e(TAG, "in release");
        mCamera.release();
        mCamera = null;
        if(rawDirectBuffer!=null) {
            rawDirectBuffer = null;
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void loop(boolean f) {

    }

    @Override
    public void enableRecord(String path,int bitrate, int fps, int  perodicTimeInSec, VIARecorder.FileListener fileListener) {
        bRecord = true;
        try {
            mVIARecorder = new VIARecorder(path,"record-",mWidth,mHeight,bitrate,fps,perodicTimeInSec, VIARecorder.Mode.YUV420SemiPlanar);
            mVIARecorder.addFileListener(fileListener);
        } catch (Exception e) {
            e.printStackTrace();
            bRecord = false;
            mVIARecorder = null;
        }
    }

    @Override
    public void start() {
        mCamera.startPreview();
    }

    ByteBuffer rawDirectBuffer = null;
    VIARecorder mVIARecorder = null;

    boolean bRecord = false;

    @Override
    public void setCallback(Callback c) {
        mCallback = c;
        rawDirectBuffer = ByteBuffer.allocateDirect(mWidth*mHeight*3/2);
    }
    private byte[] tmpByteArray = null;
    private boolean bIsBufferBorrowed = false;
    private Object mLock = new Object();
    @Override
    public long dequeueBufferAndGetPointer() {
        if(bIsBufferBorrowed) return -1;
        rawDirectBuffer.position(0);
        rawDirectBuffer.put(tmpByteArray,0,mWidth*mHeight*3/2);
        bIsBufferBorrowed = true;
        long pt = NativeRender.getPointerFromByteBuffer(rawDirectBuffer,0);

        return pt;
    }

    @Override
    public Image dequeueBuffer() {
        return null;
    }

    public ByteBuffer dequeueBufferAndGetByteBuffer() {
        if(bIsBufferBorrowed) return null;
        rawDirectBuffer.position(0);
        rawDirectBuffer.put(tmpByteArray,0,mWidth*mHeight*3/2);
        bIsBufferBorrowed = true;
        long pt = NativeRender.getPointerFromByteBuffer(rawDirectBuffer,0);

        return rawDirectBuffer;
    }

    protected Camera(Context c, int index, int width, int height, SurfaceTexture displaySurfaceTexture) {

        mIndex = index;
        mWidth = width;
        mHeight = height;

        mCamera = android.hardware.Camera.open(mIndex);

        android.hardware.Camera.Parameters p  = mCamera.getParameters();
        List sizes = mCamera.getParameters().getSupportedPreviewSizes();
        Log.i(TAG, "Supported Size: " + sizes.size()) ;
        for (int i=0;i< sizes.size();i++){
            android.hardware.Camera.Size result = (android.hardware.Camera.Size) sizes.get(i);
            Log.i(TAG, "Supported Size. Width: " + result.width + " ,height : " + result.height);
        }

        p.setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_ACTION);

        p.setPreviewFormat(NV21);
        p.setPreviewSize(mWidth, mHeight);
        //p.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        p.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED);

        p.set("orientation", "landscape");
        p.setRotation(0);
        mCamera.setParameters(p);

        mDisplaySurfaceTexture = displaySurfaceTexture;

        try {
            mCamera.setPreviewTexture(mDisplaySurfaceTexture);
            for(int i=0;i<rawDatas.length;i++) {
                rawDatas[i] = new byte[mWidth*mHeight*3/2];//NV12
                mCamera.addCallbackBuffer(rawDatas[i]);
            }

            mCamera.setPreviewCallback(new android.hardware.Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, android.hardware.Camera camera) {

                    if(mVIARecorder!=null && bRecord) {
                        mVIARecorder.recordFrameByByteArray(bytes);
                    }

                    tmpByteArray = bytes;

                    synchronized (mLock) {
                        if(mCallback!=null) {
                            if (!bIsBufferBorrowed) {
                                mCallback.onFrameReady();
                            }
                        }
                    }
                    mCamera.addCallbackBuffer(bytes);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Camera(Context c, int index, int width, int height, SurfaceView displaySurfaceView) {

        mIndex = index;
        mWidth = width;
        mHeight = height;

        mCamera = android.hardware.Camera.open(mIndex);

        android.hardware.Camera.Parameters p  = mCamera.getParameters();
        List sizes = mCamera.getParameters().getSupportedPreviewSizes();
        Log.i(TAG, "Supported Size: " + sizes.size()) ;
        for (int i=0;i< sizes.size();i++){
            android.hardware.Camera.Size result = (android.hardware.Camera.Size) sizes.get(i);
            Log.i(TAG, "Supported Size. Width: " + result.width + " ,height : " + result.height);
        }

        p.setPreviewFormat(NV21);
        p.setPreviewSize(mWidth, mHeight);
        //p.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        p.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED);
        p.set("orientation", "landscape");
        // For Android 2.2 and above
        mCamera.setDisplayOrientation(0);
        // Uncomment for Android 2.0 and above
        p.setRotation(0);
        mCamera.setParameters(p);


        mDisplaySurfaceView = displaySurfaceView;

        try {
            mCamera.setPreviewDisplay(mDisplaySurfaceView.getHolder());
            for(int i=0;i<rawDatas.length;i++) {
                rawDatas[i] = new byte[mWidth*mHeight*3/2];//NV12
                mCamera.addCallbackBuffer(rawDatas[i]);
            }

            mCamera.setPreviewCallback(new android.hardware.Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, android.hardware.Camera camera) {

                    if(mVIARecorder!=null && bRecord) {
                        mVIARecorder.recordFrameByByteArray(bytes);
                    }

                    tmpByteArray = bytes;

                    synchronized (mLock) {
                        if(mCallback!=null) {
                            if (!bIsBufferBorrowed) {
                                mCallback.onFrameReady();
                            }
                        }
                    }
                    mCamera.addCallbackBuffer(bytes);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    @Override
    public void queueBuffer() {
        synchronized (mLock) {
            bIsBufferBorrowed = false;
        }
    }
}

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
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.viatech.utility.tool.FPSCalculator;
import com.viatech.utility.video.VIARecorder;

import java.nio.ByteBuffer;

public class NativeCamera extends VIACamera {
    SurfaceView mDisplaySurfaceView = null;
    Surface mDisplaySurface = null;
    Thread mMainLoop = null;
    NativeWebcam mWebcam = null;
    VIARecorder mRecorder = null;
    int mWidth = 0;
    int mHeight = 0;
    Context mContext;
    final static String TAG = "NativeCamera";
    final static int REQUEST_FPS = 25;
    long diffTimes = 1000/REQUEST_FPS;

    int borrowedBufferIndex = -1;
    Object mLock = new Object();
    String mDeviceName = "";

    @Override
    public void loop(boolean f) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void close() {
        bRecord = false;
        mRecorder.stop();
        mRecorder = null;
        bStop = true;
        try {
            mMainLoop.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mMainLoop = null;
        mWebcam.stop();
        mWebcam = null;
    }

    protected NativeCamera(Context context, String deviceName, int width, int height, SurfaceView surfaceView) {
        mWidth = width;
        mHeight = height;
        mDisplaySurfaceView = surfaceView;
        mContext = context;
        mDisplaySurface = mDisplaySurfaceView.getHolder().getSurface();
        mDeviceName = deviceName;
    }
    boolean bRecord  =false;

    @Override
    public void enableRecord(String path,int bitrate, int fps, int  perodicTimeInSec, VIARecorder.FileListener fileListener) {
        try {
            mRecorder = new VIARecorder(path,"record-",mWidth,mHeight,bitrate,fps,perodicTimeInSec, VIARecorder.Mode.YUV420SemiPlanar);
            mRecorder.addFileListener(fileListener);
            bRecord = true;
        } catch (Exception e) {
            bRecord = false;
            e.printStackTrace();
        }
    }


    boolean  bStop  = false;
    long start;
    FPSCalculator fps;
    private boolean bPreview= true;
    long diff;
    private int currentDequeueIndex = -1;

    @Override
    public void start() {
        mMainLoop = new Thread(new Runnable() {
            @Override
            public void run() {
//                mWebcam.setWatermark(mContext.getAssets(), "newlogo_157x36.yuv" , 157, 36);
                mWebcam = new NativeWebcam(mDeviceName, mWidth, mHeight);

                fps = new FPSCalculator();
                boolean bUsed = true;
                while (!bStop) {
                    start = System.currentTimeMillis();
                    if (mWebcam.isAttached()) {
                        ByteBuffer encodeInputByteBuffer = null;
                        int inputBufferIndex = -1;
                        if (mRecorder != null && bRecord) {

                            inputBufferIndex = mRecorder.getInputBufferIndex();
                            encodeInputByteBuffer = mRecorder.getInputByteBufferByIndex(inputBufferIndex);
                        }


                        boolean bShowPreview = bPreview;

//                                    if (skipCount > 0) {
//                                        bShowPreview = false;
//                                    } else {
//                                        bShowPreview = bPreview;
//                                    }

                        final int bufIndex = mWebcam.dequeueFrame(bShowPreview ? mDisplaySurface : null, encodeInputByteBuffer);

                        Log.d(TAG, "Buffindex:"+bufIndex);
                        if (bufIndex == -1) {
                            continue;
                        }

                        currentDequeueIndex = bufIndex;

                        synchronized (mLock) {
                            if (mCallback != null && borrowedBufferIndex == -1) {
                                mCallback.onFrameReady();
                            } else {
                                mWebcam.queueFrame(bufIndex);
                            }
                            currentDequeueIndex = -1;
                        }

//                        if (bDOADAS && !mAdasAnalysis.isAnalysing()) {
//                            long pImage = mWebcam.getFramePointer(bufIndex);
//                            mAdasAnalysis.doAsyncAnalysis(bufIndex, pImage);
//                        } else {
//                            mWebcam.queueFrame(bufIndex);
//                        }

                        fps.calculate();
                        if (mRecorder != null && inputBufferIndex >= 0 && bRecord) {
                            mRecorder.recordFrameByIndex(inputBufferIndex, System.currentTimeMillis());
                        }

                    }
                    diff = System.currentTimeMillis() - start;
                    Log.d(TAG,"Sleep:"+diff+" ms");
                    if (diff <= diffTimes) {
                        try {
                            Thread.sleep(diffTimes - diff);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG,"Out of mainloop");
            }
        });

        mMainLoop.start();
    }

    Callback mCallback = null;
    @Override
    public void setCallback(Callback c) {
        mCallback = c;
    }

    @Override
    public void queueBuffer() {
        synchronized (mLock) {
            if (borrowedBufferIndex != -1) {
                mWebcam.queueFrame(borrowedBufferIndex);
                borrowedBufferIndex = -1;
            }
        }
    }

    public long dequeueBufferAndGetPointer() {
        if(borrowedBufferIndex!=-1 || currentDequeueIndex==-1) return -1;

        borrowedBufferIndex = currentDequeueIndex;
        return mWebcam.getFramePointer(borrowedBufferIndex);
    }

    @Override
    public Image dequeueBuffer() {
        return null;
    }

    public ByteBuffer dequeueBufferAndGetByteBuffer() {
        return null;
    }
}

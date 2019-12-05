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

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.MediaCodecInfo;
import android.view.Surface;
import android.view.SurfaceView;

import com.viatech.utility.tool.NativeRender;
import com.viatech.utility.video.VIARecorder;

import java.nio.ByteBuffer;

public class FakeCameraGPU extends VIACamera {
    AvcDecoderAdvance mAvcDecoderAdvance = null;
    SurfaceView mDisplaySurfaceView = null;
    String mFilePath = "";
    ByteBuffer mBufferToADAS;
    long pBuffer = -1;
    long mDequeueBuffer = -1;
    Object mLock = new Object();
    Object mLuck_AvcDecoderAdvance = new Object();
    VIACamera.Callback mCallback = null;

    Surface otherSurface;

    AvcDecoderAdvance.FrameListener mFrameListener = new AvcDecoderAdvance.FrameListener() {
        @Override
        public void onFrameDecoded(ByteBuffer b, int offset, int size, int w, int h, int s, int c) {

            if(mAvcDecoderAdvance == null) return;

            //YV12
            if (c == 19) {
                // IMX6 case
                int videoHeight;
                int videoWidth;
                synchronized (mLuck_AvcDecoderAdvance) {
                    videoHeight = mAvcDecoderAdvance.getHeight();
                    videoWidth = mAvcDecoderAdvance.getWidth();
                }

                if (mBufferToADAS == null)
                    mBufferToADAS = ByteBuffer.allocateDirect(w * h * 3 / 2);

                pBuffer = NativeRender.copyYV12ToByteBufferNV12(b, videoWidth, videoHeight, w, h, mBufferToADAS);



            } else if (c == 21) {
                pBuffer = NativeRender.getPointerFromByteBuffer(b, 0);


            }

            synchronized (mLock) {
                if(mDequeueBuffer==-1) {
                    if(mCallback!=null) {
                        mCallback.onFrameReady();
                    }
                }
            }

            pBuffer = -1;

        }

        @Override
        public void onEOS() {
            if(mCallback!=null) {
                mCallback.onEOS();
            }
        }
    };

    @Override
    public void loop(boolean f) {
        mAvcDecoderAdvance.setLoop(f);
    }

    public long getCurrentPosition() {
        if(mAvcDecoderAdvance==null) return -1;
        return mAvcDecoderAdvance.getCurrentPosition();
    }

    public long getDuration() {
        if(mAvcDecoderAdvance==null) return -1;
        return mAvcDecoderAdvance.getDuration();
    }

    protected FakeCameraGPU(String videoPath, SurfaceView surfaceView) {
        mAvcDecoderAdvance = new AvcDecoderAdvance();
        mDisplaySurfaceView = surfaceView;
        mFilePath = videoPath;
    }

    SurfaceTexture mSurfaceTexture = null;

    protected FakeCameraGPU(String videoPath, SurfaceTexture surfaceTexture) {
        mAvcDecoderAdvance = new AvcDecoderAdvance();
        mSurfaceTexture = surfaceTexture;
        mFilePath = videoPath;
    }

    public void setOtherSurface(Surface surface) {
        otherSurface = surface;
    }


    @Override
    public void enableRecord(String path, int bitrate, int fps, int perodicTimeInSec, VIARecorder.FileListener fileListener) {
        return;
    }

    Surface mSurface;

    @Override
    public void start() {
        if(mSurfaceTexture!=null) {
            mSurface = new Surface(mSurfaceTexture);
        } else if (mDisplaySurfaceView != null) {
            mSurface = mDisplaySurfaceView.getHolder().getSurface();
        }

        synchronized (mLuck_AvcDecoderAdvance) {
            mAvcDecoderAdvance.init(mFilePath, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, mSurface, otherSurface, mFrameListener);
            mAvcDecoderAdvance.start();
        }
    }

    @Override
    public void setCallback(Callback c) {
        mCallback = c;
    }

    @Override
    public void queueBuffer() {
        mDequeueBuffer = -1;
    }

    @Override
    public long dequeueBufferAndGetPointer() {
        if(mDequeueBuffer==-1 && pBuffer!=-1) {
            mDequeueBuffer = pBuffer;
            return mDequeueBuffer;
        }

        return -1;
    }

    @Override
    public Image dequeueBuffer() {
        return null;
    }

    @Override
    public void close() {
        synchronized (mLuck_AvcDecoderAdvance) {
            mAvcDecoderAdvance.close();
            mAvcDecoderAdvance = null;
        }
    }

    @Override
    public void resume() {
        synchronized (mLuck_AvcDecoderAdvance) {
            if (null != mAvcDecoderAdvance) {
                mAvcDecoderAdvance.resumePlay();
            }
        }
    }

    @Override
    public void pause() {
        synchronized (mLuck_AvcDecoderAdvance) {
            if (null != mAvcDecoderAdvance) {
                mAvcDecoderAdvance.pause();
            }
        }
    }

    @Override
    public ByteBuffer dequeueBufferAndGetByteBuffer() {
        return null;
    }
}

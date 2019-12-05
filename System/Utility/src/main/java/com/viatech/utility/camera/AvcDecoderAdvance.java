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

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.viatech.utility.tool.Helper;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AvcDecoderAdvance extends Thread {
    private static final String VIDEO = "video/";
    private static final String TAG = "VideoDecoder";
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private MediaCodec mDecoderDisplay;
    private FrameListener frameListener;
    private int mWidth = 0;
    private int mHeight = 0;
    private long mDuration = 0;

    private int mOutputColorFormat = -1;
    private int mOutputWidth = -1;
    private int mOutputHeight = -1;
    private int mOutputStride = -1;

    public interface FrameListener {
        void onFrameDecoded(ByteBuffer b, int offset, int size, int outWidth, int outHeight, int outStride, int outColorFormat);
        void onEOS();
    }

    private boolean eosReceived;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public long getCurrentPosition() {
        if(mExtractor!=null) return mExtractor.getSampleTime();
        else return 0;
    }

    public long getDuration() {
        return mDuration;
    }


    public boolean init(String filePath, int color_format, Surface surface, Surface otherSurface,  FrameListener listener) {
        eosReceived = false;
        try {
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(filePath);

            this.frameListener = listener;

            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);

                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(VIDEO)) {
                    mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(mime);
                    mDecoderDisplay = MediaCodec.createDecoderByType(mime);
                    try {
                        Log.d(TAG, "format : " + format);
                        mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                        mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                        mDuration = format.getLong(MediaFormat.KEY_DURATION);
                        mDecoderDisplay.configure(format,surface,null,0);

                        if(otherSurface==null) {
                            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);
                        }
                        mDecoder.configure(format, otherSurface, null, 0 /* Decoder */);
                        if(otherSurface!=null) bOtherSurfaceMode = true;

                    } catch (IllegalStateException e) {
                        Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                        return false;
                    }

                    mDecoder.start();
                    mDecoderDisplay.start();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean init(String filePath, int color_format, Surface surface,  FrameListener listener) {
        eosReceived = false;
        try {
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(filePath);

            this.frameListener = listener;

            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);

                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(VIDEO)) {
                    mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(mime);
                    mDecoderDisplay = MediaCodec.createDecoderByType(mime);
                    try {
                        Log.d(TAG, "format : " + format);
                        mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                        mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                        mDuration = format.getLong(MediaFormat.KEY_DURATION);
                        mDecoderDisplay.configure(format,surface,null,0);

                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);

                        mDecoder.configure(format, null, null, 0 /* Decoder */);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                        return false;
                    }

                    mDecoder.start();
                    mDecoderDisplay.start();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    int inputIndex = -1;
    int inputIndex2 = -1;
    int outIndex = Integer.MIN_VALUE;
    int outIndex2 = Integer.MIN_VALUE;
    Object pauseLock = new Object();
    boolean pause = false;
    boolean bOtherSurfaceMode = false;

    public void pause() {
        pause = true;
    }

    public void resumePlay() {
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    boolean loop = false;
    public void setLoop(boolean f) {
        loop = f;
    }


    @TargetApi(21)
    @Override
    public void run() {
        BufferInfo info = new BufferInfo();


        boolean isInput = true;
        boolean first = false;
        long startWhen = -1;

        while (!eosReceived) {
            if(pause) {
                pause = false;
                synchronized (pauseLock) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (isInput) {
                try {
                    if (inputIndex == -1)
                        inputIndex = mDecoder.dequeueInputBuffer(10000);
                    if (inputIndex2 == -1)
                        inputIndex2 = mDecoderDisplay.dequeueInputBuffer(10000);
                } catch (IllegalStateException e) {
                    break;
                }

                if (inputIndex >= 0 && inputIndex2>=0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    ByteBuffer inputBuffer = null;
                    ByteBuffer inputBuffer2 = null;
                    if(Helper.isUpperThanAPI21()) {
                        inputBuffer = mDecoder.getInputBuffer(inputIndex);
                        inputBuffer2 = mDecoderDisplay.getInputBuffer(inputIndex2);
                    } else {
                        inputBuffer = mDecoder.getInputBuffers()[inputIndex];
                        inputBuffer2 = mDecoderDisplay.getInputBuffers()[inputIndex2];
                    }
//                    ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);
                    int sampleSize = mExtractor.readSampleData(inputBuffer, 0);
                    inputBuffer2.position(0);
                    inputBuffer2.put(inputBuffer);

                    if (mExtractor.advance() && sampleSize > 0) {
                        mDecoder.queueInputBuffer(inputIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
                        mDecoderDisplay.queueInputBuffer(inputIndex2, 0, sampleSize, mExtractor.getSampleTime(), 0);

                    } else {

                        if(loop) {
                            mExtractor.seekTo(0,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                            continue;
                        } else {
                            break;
                        }
                    }

                    inputIndex = -1;
                    inputIndex2 = -1;
                }
            }

            if(outIndex == Integer.MIN_VALUE)
                outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
            if(outIndex2 == Integer.MIN_VALUE)
                outIndex2 = mDecoderDisplay.dequeueOutputBuffer(info, 10000);

            if(outIndex2<0) {
                outIndex2 = Integer.MIN_VALUE;
            }

            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    outIndex = Integer.MIN_VALUE;
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                    MediaFormat format = mDecoder.getOutputFormat();
                    mOutputHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    mOutputWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    mOutputStride = format.getInteger(MediaFormat.KEY_STRIDE);
                    mOutputColorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                    outIndex = Integer.MIN_VALUE;

                    break;

                case MediaCodec.INFO_TRY_AGAIN_LATER:
//				Log.d(TAG, "INFO_TRY_AGAIN_LATER");
                    outIndex = Integer.MIN_VALUE;

                    break;

                default:
                    if(outIndex2 == Integer.MIN_VALUE) {
                        break;
                    } else {
                        ByteBuffer decodedBuffer = null;

                        if(!bOtherSurfaceMode) {
                            if (Helper.isUpperThanAPI21()) {
                                decodedBuffer = mDecoder.getOutputBuffers()[outIndex];//mDecoder.getOutputBuffer(outIndex);
                            } else {
                                decodedBuffer = mDecoder.getOutputBuffer(outIndex);
                            }
                            if (frameListener != null) {
                                frameListener.onFrameDecoded(decodedBuffer, info.offset, info.size, mOutputWidth, mOutputHeight, mOutputStride, mOutputColorFormat);
                            }
                        }

                        mDecoder.releaseOutputBuffer(outIndex, true);
                        mDecoderDisplay.releaseOutputBuffer(outIndex2, true);

                        // Disable temporary, need refine by Hank
                        if(true) {
                            if (startWhen == -1) {
                                startWhen = System.currentTimeMillis();
                                try {
                                    Thread.sleep(30);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                long diff = System.currentTimeMillis() - startWhen;
                                diff = 33 - diff;
                                if (diff > 0) {
                                    try {
                                        Thread.sleep(diff);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                startWhen = System.currentTimeMillis();
                            }
                        }

                        outIndex = Integer.MIN_VALUE;
                        outIndex2 = Integer.MIN_VALUE;
                        break;
                    }
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        if(null!=frameListener) frameListener.onEOS();
        try {
            mDecoder.stop();
        } catch (IllegalStateException e) {

        }
        try {
            mDecoderDisplay.stop();
        } catch (IllegalStateException e) {

        }
        try {
        mDecoder.release();
        } catch (IllegalStateException e) {

        }
        try {
        mDecoderDisplay.release();
        } catch (IllegalStateException e) {

        }
        mExtractor.release();
    }

    public void close() {
        eosReceived = true;
        resumePlay();
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
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

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AvcDecoder extends Thread {
    private static final String VIDEO = "video/";
    private static final String TAG = "VideoDecoder";
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
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


    public boolean init(String filePath, int color_format, FrameListener listener) {
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
                    try {
                        Log.d(TAG, "format : " + format);
                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);
                        mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                        mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                        mDuration = format.getLong(MediaFormat.KEY_DURATION);

                        mDecoder.configure(format, null, null, 0 /* Decoder */);

                    } catch (IllegalStateException e) {
                        Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                        return false;
                    }

                    mDecoder.start();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void run() {
        BufferInfo info = new BufferInfo();
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();

        boolean isInput = true;
        boolean first = false;
        long startWhen = 0;

        while (!eosReceived) {
            if (isInput) {
                int inputIndex = mDecoder.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    ByteBuffer inputBuffer = mDecoder.getInputBuffers()[inputIndex];
//                    ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);
                    int sampleSize = mExtractor.readSampleData(inputBuffer, 0);

                    if (mExtractor.advance() && sampleSize > 0) {
                        mDecoder.queueInputBuffer(inputIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);

                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isInput = false;
                    }
                }
            }

            int outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    mDecoder.getOutputBuffers();
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                    MediaFormat format = mDecoder.getOutputFormat();
                    mOutputHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    mOutputWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    mOutputStride = format.getInteger(MediaFormat.KEY_STRIDE);
                    mOutputColorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                    break;

                case MediaCodec.INFO_TRY_AGAIN_LATER:
//				Log.d(TAG, "INFO_TRY_AGAIN_LATER");
                    break;

                default:
//                    if (!first) {
//                        startWhen = System.currentTimeMillis();
//                        first = true;
//                    }
//                    try {
//                        long sleepTime = (info.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
//                        Log.d(TAG, "info.presentationTimeUs : " + (info.presentationTimeUs / 1000) + " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime);
//
//                        if (sleepTime > 0)
//                            Thread.sleep(sleepTime);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
                    ByteBuffer decodedBuffer = mDecoder.getOutputBuffers()[outIndex];//mDecoder.getOutputBuffer(outIndex);
                    if(frameListener != null) {
                        frameListener.onFrameDecoded(decodedBuffer, info.offset, info.size, mOutputWidth, mOutputHeight, mOutputStride, mOutputColorFormat);
                    }

                    mDecoder.releaseOutputBuffer(outIndex, true /* Surface init */);
                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        if(null!=frameListener) frameListener.onEOS();
        mDecoder.stop();
        mDecoder.release();
        mExtractor.release();
    }

    public void close() {
        eosReceived = true;
    }
}
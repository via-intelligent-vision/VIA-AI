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

package com.viatech.utility.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AvcEncoder {

    private MediaCodec mediaCodec;
    private byte[] sps;
    private byte[] pps;
    private Surface mInputSurface; //  ColorFormat_Surface use only

    static public class EncodeParameters {
        int width;
        int height;
        int bitrate;
        int color_format;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getBitrate() {
            return bitrate;
        }

        public void setBitrate(int bitrate) {
            this.bitrate = bitrate;
        }

        public int getColor_format() {
            return color_format;
        }

        public void setColor_format(int color_format) {
            this.color_format = color_format;
        }

        public EncodeParameters(int w, int h, int b, int c) {
            width = w;
            height = h;
            bitrate = b;
            color_format = c;
        }

        public EncodeParameters(EncodeParameters e) {
            width = e.getWidth();
            height = e.getHeight();
            bitrate = e.getBitrate();
            color_format = e.getColor_format();
        }
    }

    public interface EncodedFrameListener {
        void onFirstSpsPpsEncoded(byte[] sps, byte[] pps);
//        void onFrameEncoded(byte[] nalu);
        boolean onFrameEncoded(ByteBuffer nalu, MediaCodec.BufferInfo info);
    }

    private EncodedFrameListener mFrameListener;
    private EncodeParameters mEncodeParameters;
    public AvcEncoder(EncodeParameters encodeParameters, EncodedFrameListener listener) throws IOException {

        mEncodeParameters = new EncodeParameters(encodeParameters);
        mediaCodec = MediaCodec.createEncoderByType("video/avc");
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mEncodeParameters.getWidth(), mEncodeParameters.getHeight());
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mEncodeParameters.getBitrate());
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mEncodeParameters.getColor_format());
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mFrameListener = listener;
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        if(mEncodeParameters.getColor_format()== MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) {
            mInputSurface = mediaCodec.createInputSurface();
        }
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void close() {
        if(bLooping) {
            bLooping = false;
            if(drainThread!=null) {
                try {
                    drainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drainThread = null;
            }
        }

        mediaCodec.stop();
        mediaCodec.release();
    }

    public void offerEncoder(byte[] b) {
        try {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
                inputBuffer.clear();

                inputBuffer.put(b);

                mediaCodec.queueInputBuffer(inputBufferIndex, 0, mEncodeParameters.getWidth()*mEncodeParameters.getHeight()*3/2, 0, 0);

            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];


                if (sps != null && pps != null) {
                    if(null != mFrameListener) {
                        mFrameListener.onFrameEncoded(outputBuffer,bufferInfo);
                    }
                } else {

                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);

                    //SPS
                    if (outData[0]==0x00 && outData[1]==0x00 && outData[2]==0x00 && outData[3]==0x01 && outData[4] == 103) {
                        System.out.println("parsing sps/pps");
                    } else {
                        System.out.println("something is amiss?");
                        break;
                    }
                    int ppsIndex = 1;

                    while(!(outData[ppsIndex]==0x00 && outData[ppsIndex+1]==0x00 && outData[ppsIndex+2]==0x00 && outData[ppsIndex+3]==0x01 && outData[ppsIndex+4]==104)) {
                        ppsIndex++;
                    }

                    sps = new byte[ppsIndex];
                    System.arraycopy(outData, 0 , sps, 0, sps.length);
                    pps = new byte[outData.length - ppsIndex];
                    System.arraycopy(outData, ppsIndex, pps, 0, pps.length);
                    if (null != mFrameListener) {
                        mFrameListener.onFirstSpsPpsEncoded(sps, pps);
                    }
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public int getInputBufferIndex() {
        return mediaCodec.dequeueInputBuffer(-1);
    }

    public ByteBuffer getInputBufferByIndex(int index) {
        return mediaCodec.getInputBuffers()[index];
    }

    public synchronized void run(int i, long timestamp) {
        boolean bNeedFlush = false;
        mediaCodec.queueInputBuffer(i, 0, mEncodeParameters.getWidth()*mEncodeParameters.getHeight()*3/2, timestamp, MediaCodec.BUFFER_FLAG_SYNC_FRAME);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];

            if (sps != null && pps != null) {
                if(null != mFrameListener) {
                    bNeedFlush = mFrameListener.onFrameEncoded(outputBuffer, bufferInfo);
                    if(bNeedFlush) break;
                }
            } else {
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                //SPS
                if (outData[0]==0x00 && outData[1]==0x00 && outData[2]==0x00 && outData[3]==0x01 && outData[4] == 103) {
                    System.out.println("parsing sps/pps");
                } else {
                    System.out.println("something is amiss?");
                    break;
                }
                int ppsIndex = 1;

                while(!(outData[ppsIndex]==0x00 && outData[ppsIndex+1]==0x00 && outData[ppsIndex+2]==0x00 && outData[ppsIndex+3]==0x01 && outData[ppsIndex+4]==104)) {
                    ppsIndex++;
                }

                sps = new byte[ppsIndex];
                System.arraycopy(outData, 0 , sps, 0, sps.length);
                pps = new byte[outData.length - ppsIndex];
                System.arraycopy(outData, ppsIndex, pps, 0, pps.length);
                if (null != mFrameListener) {
                    mFrameListener.onFirstSpsPpsEncoded(sps, pps);
                }
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
        if(bNeedFlush) mediaCodec.flush();
    }
    public void offerEncoder(ByteBuffer b, int offset, int size) {
        try {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
                inputBuffer.clear();
//                byte[] bbb = new byte[mEncodeParameters.getWidth()*mEncodeParameters.getHeight()*3/2];
//                b.get(bbb);
                inputBuffer.put(b);

                mediaCodec.queueInputBuffer(inputBufferIndex, 0, mEncodeParameters.getWidth()*mEncodeParameters.getHeight()*3/2, 0, 0);

            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];


                if (sps != null && pps != null) {
                    if(null != mFrameListener) {
                        mFrameListener.onFrameEncoded(outputBuffer, bufferInfo);
                    }
                } else {
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    //SPS
                    if (outData[0]==0x00 && outData[1]==0x00 && outData[2]==0x00 && outData[3]==0x01 && outData[4] == 103) {
                        System.out.println("parsing sps/pps");
                    } else {
                        System.out.println("something is amiss?");
                        break;
                    }
                    int ppsIndex = 1;

                    while(!(outData[ppsIndex]==0x00 && outData[ppsIndex+1]==0x00 && outData[ppsIndex+2]==0x00 && outData[ppsIndex+3]==0x01 && outData[ppsIndex+4]==104)) {
                        ppsIndex++;
                    }

                    sps = new byte[ppsIndex];
                    System.arraycopy(outData, 0 , sps, 0, sps.length);
                    pps = new byte[outData.length - ppsIndex];
                    System.arraycopy(outData, ppsIndex, pps, 0, pps.length);
                    if (null != mFrameListener) {
                        mFrameListener.onFirstSpsPpsEncoded(sps, pps);
                    }
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    /*
        Surface Mode. ( ColorFormat_Surface )
     */
    private Thread drainThread = null;
    boolean bLooping = false;
    public void start() {
        if(mInputSurface==null) return;
        bLooping = true;
        mediaCodec.start();
        drainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(bLooping) {
                    drainEncode();
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        drainThread.setPriority(Thread.MAX_PRIORITY);
        drainThread.start();
    }

    @TargetApi(21)
    public synchronized void drainEncode() {
        boolean bNeedFlush = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = -1;
        try {
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);

            if (sps != null && pps != null) {
                if(null != mFrameListener) {
                    bNeedFlush = mFrameListener.onFrameEncoded(outputBuffer, bufferInfo);
                    if(bNeedFlush) break;
                }
            } else {
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                //SPS
                if (outData[0]==0x00 && outData[1]==0x00 && outData[2]==0x00 && outData[3]==0x01 && outData[4] == 103) {
                    System.out.println("parsing sps/pps");
                } else {
                    System.out.println("something is amiss?");
                    break;
                }
                int ppsIndex = 1;

                while(!(outData[ppsIndex]==0x00 && outData[ppsIndex+1]==0x00 && outData[ppsIndex+2]==0x00 && outData[ppsIndex+3]==0x01 && outData[ppsIndex+4]==104)) {
                    ppsIndex++;
                }

                sps = new byte[ppsIndex];
                System.arraycopy(outData, 0 , sps, 0, sps.length);
                pps = new byte[outData.length - ppsIndex];
                System.arraycopy(outData, ppsIndex, pps, 0, pps.length);
                if (null != mFrameListener) {
                    mFrameListener.onFirstSpsPpsEncoded(sps, pps);
                }
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10);
        }
        if(bNeedFlush) mediaCodec.flush();
    }

}
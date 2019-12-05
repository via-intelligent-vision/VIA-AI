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
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.viatech.utility.tool.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_STRIDE;

@TargetApi(21)
public class MediaPlayerGrabber implements Runnable {
    private static final String TAG = "MediaPlayerGrabber";
    private static final boolean VERBOSE = false;
    private static final long DEFAULT_TIMEOUT_US = 10000;

    public static final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;

    private boolean stopDecode = false;
    private boolean stopDisplay = false;

    private String videoFilePath;
    private Throwable throwable;
    private Thread childThread;

    private Callback callback;

    boolean sawOutputEOS = false;

    boolean ready = false;

    long displayCounter = 0;


    public void interruptThread() {
        childThread.interrupt();
    }

    public interface Callback {
        void onFrameReady(ByteBuffer buffer, int offset, int width, int height, int colorFormat, int stride);
        void onImageReady(Image image);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void stop() {
        try {
            stopDisplay = true;
            if(displayThread!=null) displayThread.join();

            stopDecode = true;
            if(childThread!=null) childThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            decode();
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
        }
    }

    public void setDataSource(String file) {
        this.videoFilePath = file;
    }

    public void prepare() throws FileNotFoundException, IOException {
        File f = new File(videoFilePath);
        if(f.exists() && f.canRead()) {

        } else {
            if(!f.exists()) new FileNotFoundException("File not found");
            if(!f.canRead()) new IOException("File permission deined");
        }
    }

    private void decode() throws Throwable {
        if (childThread == null) {
            childThread = new Thread(this, "decode");
            childThread.start();
            if (throwable != null) {
                throw throwable;
            }
        }
    }

    public void run() {
        try {
            videoDecode(videoFilePath);
        } catch (Throwable t) {
            throwable = t;
        }
    }

    private void videoDecode(String videoFilePath) throws IOException {
        MediaExtractor extractor = null;
        MediaCodec decoder = null;
        try {
            File videoFile = new File(videoFilePath);
            extractor = new MediaExtractor();
            extractor.setDataSource(videoFile.toString());
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + videoFilePath);
            }
            extractor.selectTrack(trackIndex);
            MediaFormat mediaFormat = extractor.getTrackFormat(trackIndex);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            showSupportedColorFormat(decoder.getCodecInfo().getCapabilitiesForType(mime));
            if (isColorFormatSupported(decodeColorFormat, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
                Log.i(TAG, "set decode color format to type " + decodeColorFormat);
            } else {
                Log.i(TAG, "unable to set decode color format, color format type " + decodeColorFormat + " not supported");
            }

            width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            decodeFrames(decoder, extractor, mediaFormat);
            decoder.stop();
        } finally {
            Log.d(TAG, "finally");
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }

    public static ArrayList<Integer> showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
        System.out.print("supported color format: ");
        ArrayList<Integer> ret = new ArrayList<>();

        int i =0;
        for (int c : caps.colorFormats) {
            ret.add(c);
            System.out.print(c + "\t");
        }
        System.out.println();
        return ret;
    }

    private boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) {
                return true;
            }
        }
        return false;
    }

    int outputFrameCount = 0;
    int width = 0;//mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
    int height = 0;//mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
    ByteBuffer[] inputByteBuffers = null;
    ByteBuffer[] outputByteBuffers = null;

    private void decodeFrames(final MediaCodec decoder, MediaExtractor extractor, MediaFormat mediaFormat) {
        boolean sawInputEOS = false;
        sawOutputEOS = false;
        decoder.configure(mediaFormat, null, null, 0);
        decoder.start();

        if(!Helper.isUpperThanAPI21()) {
            inputByteBuffers = decoder.getInputBuffers();
            outputByteBuffers = decoder.getOutputBuffers();
        }

        while (!sawOutputEOS && !stopDecode) {
            if (!sawInputEOS) {
                int inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
                if (inputBufferId >= 0) {
                    ByteBuffer inputBuffer = null;
                    if (Helper.isUpperThanAPI21()) {
                        inputBuffer = decoder.getInputBuffer(inputBufferId);
                    } else {
                        inputBuffer = inputByteBuffers[inputBufferId];
                    }

                    int sampleSize = extractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, 0);
                        sawInputEOS = false;
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    } else {
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0);
                        extractor.advance();
                    }
                }

            }

            if(displayThread==null) {
                displayThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!sawOutputEOS && !stopDisplay) {
                            frameDisplay(decoder);
                        }
                    }
                });
                displayThread.start();
            }
        }

    }

    Thread displayThread = null;
    long time = -1;

    private void frameDisplay(MediaCodec decoder) {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        int srcColorFormat = 0;
        int srcStride = 0;
        int outputBufferId = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US);
        if (outputBufferId >= 0) {
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                sawOutputEOS = false;
            }

            boolean doRender = (info.size != 0);
            if (doRender) {
                outputFrameCount++;

                ByteBuffer b = null;
                Image image = null;

                if (Helper.isUpperThanAPI21()) {
                    image = decoder.getOutputImage(outputBufferId);
                    if(image!=null) {
//                        Log.d(TAG, "image:" + image.getWidth() + "," + image.getHeight() + "," + image.getFormat());
                    } else {
                        b = decoder.getOutputBuffer(outputBufferId);
                        MediaFormat format = decoder.getOutputFormat(outputBufferId);
                        srcColorFormat = format.getInteger(KEY_COLOR_FORMAT);
                        srcStride = format.getInteger(KEY_STRIDE);
                    }
                } else {
                    b = outputByteBuffers[outputBufferId];
                }

                if(time==-1) time = System.currentTimeMillis();
                else {
                    long diff = (System.currentTimeMillis()-time);
                    if(diff<33) {
                        //waitMs((33*1000-diff)/1000);
                        waitMs((33-diff));
                    }
                    time = System.currentTimeMillis();
                }

                if(callback!=null) {
                    if(image!=null) callback.onImageReady(image);
                    else if (b!=null) callback.onFrameReady(b,info.offset, width, height, srcColorFormat , srcStride);
                }

                decoder.releaseOutputBuffer(outputBufferId, false);
            }
        }
    }

    private void waitMs(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {

        }
    }


    private static int selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }
        return -1;
    }
}

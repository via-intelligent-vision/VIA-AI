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

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.viatech.utility.tool.FPSCalculator;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class VIARecorder {

    public enum Mode {
        Surface,
        YUV420SemiPlanar,
    }

    private int COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;

    MediaMuxer mMediaMuxer = null;
    AvcEncoder mAvcEncoder = null;
    int mVideoTrack = -1;
    int mFrameCount = 0;
    int mFPS = 0;
    int mFrameDiffTimes = 0;
    long mTime = 0;
    int mWidth = 0;
    int mHeight = 0;
    String mPrefix;

    MediaFormat mMediaFormat;
    boolean bFormatReady = false;
    long mPeriodicTimeInSec;
    String mPath = "";
    Object mLock = new Object();
    long mFileStartTime = -1;
    String outputPath;
    FileListener mFileListener = null;
    Vector<FileListener> mFileListeners = new Vector<>();
    Mode mMode = Mode.YUV420SemiPlanar;

    public interface FileListener {
        void OnFileComplete(String filePath);
        void OnFileCreate(String filePath);
    }

    public void addFileListener(FileListener f) {
        mFileListeners.add(f);
    }
//
//    public void setFileListener(FileListener f) {
//        mFileListener = f;
//    }

    public void stop() {
        synchronized (mLock) {
            if (mMediaMuxer != null) {
                mMediaMuxer.stop();
                mMediaMuxer.release();
                mMediaMuxer = null;
            }

            if(mAvcEncoder != null) {
                mAvcEncoder.close();
                mAvcEncoder = null;
                bStarted = false;
            }
        }
    }

    public boolean isStarted() {
        return bStarted;
    }

    private void createRecordFile() {
        synchronized (mLock) {
            if (mMediaMuxer != null) {
                mMediaMuxer.stop();
                mMediaMuxer.release();
                mMediaMuxer = null;
                if(mFileListeners.size()!=0) {
                    for(FileListener f:mFileListeners) {
                        f.OnFileComplete(outputPath);
                    }
                }
//                if(mFileListener!=null) mFileListener.OnFileComplete(outputPath);
            }

            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
                outputPath = new File(mPath + "/",
                        mPrefix + df.format(new Date()) + "-" + mWidth + "x" + mHeight + ".mp4").toString();
                mMediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mVideoTrack = -1;
                mFrameCount = 0;
                mTime = 0;
                mFileStartTime = System.currentTimeMillis();
//                if(mFileListener!=null) mFileListener.OnFileCreate(outputPath);
                if(mFileListeners.size()!=0) {
                    for(FileListener f:mFileListeners) {
                        f.OnFileCreate(outputPath);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getInputBufferIndex() {
        return mAvcEncoder.getInputBufferIndex();
    }

    public ByteBuffer getInputByteBufferByIndex(int index) {
        return mAvcEncoder.getInputBufferByIndex(index);
    }

    public void recordFrameByIndex(int index, long timestamp) {
        mAvcEncoder.run(index, timestamp);
    }

    public void recordFrameByByteArray(byte[] buffer) { mAvcEncoder.offerEncoder(buffer);}


    public boolean bSurfaceGetted = false;
    public Surface getInputSurface() { bSurfaceGetted = true; return mAvcEncoder.getInputSurface(); }

    public void start() {
        if(mAvcEncoder!=null) {
            mAvcEncoder.start();
            bStarted = true;
        }
    }

    boolean bStarted = false;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getFPS() {
        return mFPS;
    }

    public int getFrameDiffTimes() {
        return mFrameDiffTimes;
    }

    long previousEncodeTime = -1;
    FPSCalculator mFPSCalculator = new FPSCalculator("Encode FPS");

    public boolean needEncode() {
        if(previousEncodeTime==-1) {
            previousEncodeTime = System.currentTimeMillis();
            mFPSCalculator.calculate();
            return true;
        }
        if((System.currentTimeMillis() - previousEncodeTime) >= getFrameDiffTimes()) {
            previousEncodeTime = System.currentTimeMillis();
            mFPSCalculator.calculate();
            return true;
        }
        return false;
    }

    boolean bFourInOneRecord = false;

    public boolean isFourInOneRecord() {
        return bFourInOneRecord;
    }

    public void setFourInOneRecord(boolean b) {
        bFourInOneRecord = b;
    }

    public interface MuxerCallback {
        void OnMuxerWriterFrame(long timestamp);
    }

    MuxerCallback mMuxerCallback = null;

    public void setMuxerCallback(MuxerCallback callback) {
        mMuxerCallback = callback;
    }

    public VIARecorder(String path, String prefix,int width, int height, int bitrate, int fps, long perodicTimeInSec, Mode mode){
        mMode = mode;
        mWidth = width;
        mHeight = height;
        mPrefix = prefix;
        if(mMode.equals(Mode.YUV420SemiPlanar)) {
            COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        } else if (mMode.equals(Mode.Surface)) {
            COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        }

        AvcEncoder.EncodeParameters parameters = new AvcEncoder.EncodeParameters(width,height,bitrate, COLOR_FORMAT);
        mFPS = fps;
        mFrameDiffTimes = 1000/mFPS;
        mPeriodicTimeInSec = perodicTimeInSec;
        mPath = path;

        File f = new File(path);
        if(!f.exists()) {
            f.mkdirs();
        }
        f = null;

        try {
            mMediaFormat = new MediaFormat();
            mMediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
            mMediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
            mMediaFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC);
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mAvcEncoder = new AvcEncoder(parameters, new AvcEncoder.EncodedFrameListener() {
                @Override
                public void onFirstSpsPpsEncoded(byte[] sps, byte[] pps) {
                    mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
                    mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
                    bFormatReady = true;
                }

                @Override
                public boolean onFrameEncoded(ByteBuffer nalu, MediaCodec.BufferInfo info) {
                    if(!bStarted) return false;

                    mFrameCount++;
                    info.presentationTimeUs = System.currentTimeMillis()*1000;
                    boolean bFlush = false;
//                    mTime += mFrameDiffTimes;
                    if(((info.flags&MediaCodec.BUFFER_FLAG_KEY_FRAME)==1) && (System.currentTimeMillis()-mFileStartTime)>=mPeriodicTimeInSec*1000) {
                        createRecordFile();
                        bFlush = false;
                    }

                    synchronized (mLock) {
                        if (mMediaMuxer != null && bFormatReady) {
                            if (mVideoTrack == -1) {
                                mVideoTrack = mMediaMuxer.addTrack(mMediaFormat);
                                mMediaMuxer.start();
                            }
                            if(null!=mMuxerCallback) {
                                mMuxerCallback.OnMuxerWriterFrame(info.presentationTimeUs/1000);
                            }
                            mMediaMuxer.writeSampleData(mVideoTrack, nalu, info);
                        }
                    }
                    return bFlush;
                }
            });
        } catch (IOException e) {
            Log.d("VIARecorder", "VIARecorder: "+e.toString());
        }
    }
}

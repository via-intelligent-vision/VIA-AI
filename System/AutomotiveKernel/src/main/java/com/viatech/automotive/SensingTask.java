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

package com.viatech.automotive;

import android.util.Log;

import com.viatech.exception.IllegalTaskAccessException;
import com.viatech.sensing.SensingModule;

public class SensingTask implements Runnable {
    private String TAG = SensingTask.class.getName();
    private Object mSensingToggleLock;
    private int mID;
    private volatile Thread mThread;
    private volatile boolean mIsActive;
    private volatile boolean mIsSensing;
    private SensingModule mSensingModule;
    private double fpsCounter = 0;


    public SensingTask(int id) {
        mID = id;
        mThread = null;
        mIsActive = false;
        mIsSensing = false;
        mSensingModule = null;
        mSensingToggleLock = new Object();
    }

    public boolean isActive() {
        return mIsActive;
    }

    public boolean isSensing() {
        return mIsSensing;
    }

    public void setSensingModule(SensingModule sensingModule) throws IllegalTaskAccessException {
        if(isActive() || isSensing()) {
            throw new IllegalTaskAccessException("Sensing module couldn't be changed in [active/sensing].");
        }
        else {
            mSensingModule = sensingModule;
        }
    }

    public void toggleSensing(boolean enableSensing) throws IllegalTaskAccessException {
        if(isActive()) {
            throw new IllegalTaskAccessException("Sensing status couldn't be changed in non-active status.");
        }
        else {
            synchronized (mSensingToggleLock) {
                mIsSensing = enableSensing;
                mSensingToggleLock.notifyAll();
            }
        }
    }

    public boolean start(int newPriority) {
        boolean ret = false;

        if(mThread != null) {
            Log.i(TAG, "release previous task ....");
            release();
        }

        if(mSensingModule != null) {
            mIsActive = true;
            mIsSensing = true;
            fpsCounter = 0;
            mThread = new Thread(this);
            mThread.setPriority(newPriority);
            mThread.start();
        }
        else {
            Log.i(TAG, "No sensing module assign to this task , id : "+ mID);
        }

        return ret;
    }

    public void release() {
        mIsSensing = false;
        mIsActive = false;

        synchronized (mSensingToggleLock) { // notify thread.
            mSensingToggleLock.notifyAll();
        }

        try {
            if(mThread != null) {
                mThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyFrameReady() {
        synchronized (mSensingToggleLock) {
            mSensingToggleLock.notifyAll();
        }
    }

    @Override
    public void run() {
        long startTime = 0;
        int CC = 1;
        while (isActive()) {
            do {
                if(mSensingModule == null) break;
                if(!isSensing()) break;

                if(mSensingModule.getFrameQueueCount() > 0) {
                    if(startTime == 0) startTime = System.currentTimeMillis();
                    mSensingModule.detect(false);
                    fpsCounter++;

                    long endTime = System.currentTimeMillis();
                    double FPS = (fpsCounter * 1000.0) / (double)(endTime - startTime);

                    if(CC == 0) {
                     //   Log.d(TAG, "module with detector " + mSensingModule.getDetectorSet() + " , FPS : " + FPS);
                    }
                    CC++;
                    if(CC >= 30) CC = 0;
                }
                else {
                    //Log.e(TAG, "Frame buffer dry");
                    break;
                }
            } while(false);

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

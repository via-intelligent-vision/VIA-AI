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

package com.viatech.utility.tool;

import android.util.Log;

public class FPSCalculator {
    long totalCount;
    int fps;
    int numberOfCountFPS = 0;
    int totalFPSCount = 0;
    long time = -1;
    long startTime = -1;
    int avg = -1;
    int max = -1;
    int min = -1;
    long endTime = 0;
    String TAG = "FPS";
    String PREFIX_TAG = "";

    public int getTotalCount() {
        return (int)totalCount;
    }

    public FPSCalculator() {

    }

    public FPSCalculator(String prefix) {
        PREFIX_TAG = prefix;
        TAG = PREFIX_TAG + TAG;
    }


    public void calculate() {
        if(time==-1) time = System.currentTimeMillis();
        if(startTime==-1) startTime = System.currentTimeMillis();


        endTime = System.currentTimeMillis();
        fps++;
        totalCount++;
        if(System.currentTimeMillis()-time >= 1000) {
            Log.d(TAG, "FPS:"+fps);
            time = System.currentTimeMillis();
            if(max==-1) max = fps;
            if(min==-1) min = fps;
            if(fps>max) max = fps;
            if(fps<min) min = fps;
            numberOfCountFPS++;
            totalFPSCount += fps;
            fps = 0;
        }
    }

    public int getMAX() {
        return max;
    }

    public int getMIN() {
        return min;
    }

    public int getAVG() {
        if(numberOfCountFPS==0) return 0;
        return (int) (totalFPSCount/numberOfCountFPS);
    }
}

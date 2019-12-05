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

package com.viatech.via_ai.System;

public enum SystemEvents {
    // level 0
    AutomaticSystemDisable (EventLevels.Level_0, "Automatic System Abort", 1),
    CalibrationError (EventLevels.Level_0, "Calibration invalid , please calibrate system first.", 0),

    // level 1
    LaneDeparture (EventLevels.Level_1, "Lane Departure", 0),


    // level 2
    LaneMissing (EventLevels.Level_2, "No Lane Detected", 0),
    CurvatureOverControl (EventLevels.Level_2, "Curvature Over Control", 0),
    SteeringWheelRequired (EventLevels.Level_2, "Steering Wheel Required", 0),

    // level 3
    AutomaticSystemEnable (EventLevels.Level_3, "Automatic System Start", 1),
    CalibrationStart (EventLevels.Level_3, "Calibration Start", 0),
    CalibrationFail (EventLevels.Level_3, "Calibration Fail", 1),
    CalibrationSuccess (EventLevels.Level_3, "Calibration Success", 1),

    // level 4
    SystemInit(EventLevels.Level_4, null, 1),
    SensingSystemStart(EventLevels.Level_4, "Sensing Ready", 0),

    // level 10
    NoEvent (EventLevels.Level_N, null, 0),

    ;


    private EventLevels mLevel = EventLevels.Level_N;
    private String mText;
    private int mRingCode;  // ToneGenerator.TONE_CDMA_*
    private int mBeepId;

    SystemEvents(EventLevels level, String text, int ringCode)
    {
        mLevel = level;
        mText = text;
        mRingCode = ringCode;
    }

    public EventLevels getLevel()
    {
        return mLevel;
    }

    public String getText()
    {
        return mText;
    }

    private void setText(String text) {
        this.mText = text;
    }

    public int getRingCode() {
        return mRingCode;
    }
}

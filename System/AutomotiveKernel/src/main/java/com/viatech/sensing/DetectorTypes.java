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

package com.viatech.sensing;

/**
 * The enum of sensing detector.
 */

public enum DetectorTypes
{
    /** Default value */
    NONE    (0),
    /** Forward Collision Warning (ver. Computer Vision ) */
    FCWS    (1),
    /** Forward Collision Warning (ver. Deep Learning ) . This detector is depend on Qualcomm SNPE, please check the devices is support or ignore this detector, use {@link DetectorTypes#FCWS} replace. */
    FCWS_DL (256),
    /** Lane Departure Warning*/
    LDWS    (2),
    /** Blind Spot Detection - Left Side */
    BSD_L    (4),
    /** Blind Spot Detection - Right Side */
    BSD_R    (8),
    /** Speed Limit Detection, The signage design follows the style set out by the "Vienna Convention on Road Signs and Signals".  */
    SLD      (16),
    /**  Not in use */
    BCWS    (32),
    /**  Detect weather, ref {@link SensingSamples.EnvironmentSample.WeatherTypes} */
    Weather     (64),
    /**  Not in use */
    Pedestrian  (128),
    /**  Traffic Light Detection */
    TLD  (512),
    ;

    private int mIndex = -1;
    DetectorTypes(int index)
    {
        mIndex = index;
    }

    public int getIndex()
    {
        return mIndex;
    }

    public DetectorTypes getDetectorType(int index)
    {
        for (int i = 0; i < DetectorTypes.values().length; i++) {
            if (index == DetectorTypes.values()[i].getIndex()) {
                return DetectorTypes.values()[i];
            }
        }
        return NONE;
    }
}

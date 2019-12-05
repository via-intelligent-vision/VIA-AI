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

package com.viatech.camera;

/**
 The enum of camera types(vendors) support in system.
 */
public enum CameraTypes {
    /**
     @brief Unknown camera type.
     */
    Unknown (0),
    /**
     @brief Custom the instrinsic.
     */
    Custom (1),
    /**
     @brief format : Camera_Vendor, ModuleName, Sensor, FOV
     */
    Sharp_Module_OV10640_Fov50 (2),
    HTC_U11plus_Unknown_Unknown (3),
    HTC_U11eyes_Unknown_Unknown (4),
    HTC_U12plus_Unknown_Unknown (8),
    GooglePixel2_Unknown_Unknown (5),
    FIC_Imx6Pad_Unknown_Fov78 (6),
    REC_SonyISX016_FOV40 (7),
    ;

    private int mIndex = -1;
    CameraTypes(int index)
    {
        mIndex = index;
    }

    public int getIndex()
    {
        return mIndex;
    }

    public static CameraTypes getType(int index)
    {
        for (int i = 0; i < CameraTypes.values().length; i++) {
            if (index == CameraTypes.values()[i].getIndex()) {
                return CameraTypes.values()[i];
            }
        }
        return Unknown;
    }
};

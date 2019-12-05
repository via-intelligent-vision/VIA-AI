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
 The enum of camera install location. Keep same as mobile360\CameraCoord\CoordCvtTypes.h
 */
public enum CoordCvtTypes
{
    Unknown(0),
    ImgCoord_To_UndistCoord(1),
    ImgCoord_To_ObjCoord_ZeroZ(2),
    NormalizedImgCoord_To_ObjCoord_ZeroZ(3),
    UndistCoord_To_ObjCoord_ZeroZ(4),
    ObjCoord_To_NormalizedImgCoord(5),
    NormalizedImgCoord_To_cmDistance(6),
    ;

    private int mIndex = -1;
    CoordCvtTypes(int index)
    {
        mIndex = index;
    }

    public int id()
    {
        return mIndex;
    }

    public static CoordCvtTypes getCameraLocationType(int index)
    {
        for (int i = 0; i < CoordCvtTypes.values().length; i++) {
            if (index == CoordCvtTypes.values()[i].id()) {
                return CoordCvtTypes.values()[i];
            }
        }
        return Unknown;
    }
}
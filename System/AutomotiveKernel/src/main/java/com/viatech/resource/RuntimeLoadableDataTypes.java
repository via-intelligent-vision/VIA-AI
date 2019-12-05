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


package com.viatech.resource;


/***
 Keep value same as define in : VIA_ADAS\libadas\src\main\jni\sensing\types.h
 */
public enum RuntimeLoadableDataTypes {
    NONE (0),
    FCWS_CascadeModel (1),         //Haar Model
    FCWS_DL_Model   (2),
    FCWS_DL_Label   (3),
    FCWS_DL_DSPLib  (4),
    SLD_Model       (5),
    SLD_Proto       (6),
    SLD_Label       (7),
    Weather_ClassifyModel (8),
    SLD_NightModel (9),
    SLD_PrefetchModel   (10),
    TLD_Pattern_1       (11),
    TLD_Pattern_2       (12),
    TLD_Pattern_3       (13),
    ;

    private int mIndex = -1;
    RuntimeLoadableDataTypes(int index)
    {
        mIndex = index;
    }

    public int getIndex()
    {
        return mIndex;
    }

    public static RuntimeLoadableDataTypes getType(int index)
    {
        for (int i = 0; i < RuntimeLoadableDataTypes.values().length; i++) {
            if (index == RuntimeLoadableDataTypes.values()[i].getIndex()) {
                return RuntimeLoadableDataTypes.values()[i];
            }
        }
        return NONE;
    }
}


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

#ifndef __YUV__H__
#define __YUV__H__

#include <jni.h>
#include <stdio.h>

int YUV_TABLE[5][256];

/* Public: Generate and cache the lookup table necessary to convert from YUV to
 * ARGB.
 */
void cache_yuv_lookup_table(int table[5][256]);

/* Private: Convert an Y'UV42 image to an ARGB image.
 *
 * src - the source image buffer.
 * width - the width of the image.
 * height - the height of the image.
 * rgb_buffer - output buffer for RGB data from the conversion.
 * y_buffer - output buffer for alpha (Y) data from the conversion.
 */
void yuyv422_to_argb(unsigned char *src, int width, int height, int* rgb_buffer,
        int* y_buffer);
void NV21TOARGB(const unsigned char *src,const unsigned int *dst,int width,int height);

#endif // __YUV__H__

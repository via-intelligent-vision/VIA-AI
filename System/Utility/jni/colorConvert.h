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

#ifndef __COLORCONVERT_H__
#define __COLORCONVERT_H__

#define COLOR_FORMAT_NV21 17

#define FLAG_DIRECTION_FLIP_HORIZONTAL	0x01
#define FLAG_DIRECTION_FLIP_VERTICAL	0x02
#define FLAG_DIRECTION_ROATATION_0 		0x10
#define FLAG_DIRECTION_ROATATION_90		0x20
#define FLAG_DIRECTION_ROATATION_180	0x40
#define FLAG_DIRECTION_ROATATION_270	0x80

void NV21TOYUV420SP(const unsigned char *src,const unsigned char *dst,int ySize);

void NV12TONV21(const unsigned char *y_src,const unsigned char *uv_src,const unsigned char *dst,int ySize);
void NV12TOARGB(const unsigned char *y_src,const unsigned char *uv_src,const unsigned int *dst,int width,int height);

void YUV420SPTOYUV420P(const unsigned char *src,const unsigned char *dst,int ySize);
void NV21TOYUV420P(const unsigned char *src,const unsigned char *dst,int ySize);
void NV21TOARGB(const unsigned char *src,const unsigned int *dst,int width,int height);
void NV21Transform(const unsigned char *src,const unsigned char *dst,int dstWidth,int dstHeight,int directionFlag);
void NV21TOYUV(const unsigned char *src,const unsigned char *dstY,const unsigned char *dstU,const unsigned char *dstV,int width,int height);
void FIXGLPIXEL(const unsigned int *src,unsigned int *dst,int width,int height);
#endif
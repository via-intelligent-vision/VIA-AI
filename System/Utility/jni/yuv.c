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

#include "yuv.h"
#include <stdio.h>

void cache_yuv_lookup_table(int table[5][256]) {
    for(int i = 0; i < 256; i++) {
        table[0][i] = 1192 * (i - 16);
        if(table[0][i] < 0) {
            table[0][i] = 0;
        }

        table[1][i] = 1634 * (i - 128);
        table[2][i] = 833 * (i - 128);
        table[3][i] = 400 * (i - 128);
        table[4][i] = 2066 * (i - 128);
    }
}



void NV21TOARGB(const unsigned char *src,const unsigned int *dst,int width,int height)
{

    int frameSize = width * height;

    int i = 0, j = 0,yp = 0;
    int uvp = 0, u = 0, v = 0;
    int y1192 = 0, r = 0, g = 0, b = 0;
    unsigned int *target=dst;
    for (j = 0, yp = 0; j < height; j++)
    {
        uvp = frameSize + (j >> 1) * width;
        u = 0;
        v = 0;
        for (i = 0; i < width; i++, yp++)
        {
            int y = (0xff & ((int) src[yp])) - 16;
            if (y < 0)
                y = 0;
            if ((i & 1) == 0)
            {
                v = (0xff & src[uvp++]) - 128;
                u = (0xff & src[uvp++]) - 128;
            }

            y1192 = 1192 * y;
            r = (y1192 + 1634 * v);
            g = (y1192 - 833 * v - 400 * u);
            b = (y1192 + 2066 * u);

            if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            if (b < 0) b = 0; else if (b > 262143) b = 262143;
            target[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }
}

void yuyv422_to_argb(unsigned char *src, int width, int height, int* rgb_buffer,
        int* y_buffer) {
    if((!rgb_buffer || !y_buffer)) {
        return;
    }

    int frameSize = width * height * 2;
    int* lrgb = &rgb_buffer[0];
    int* lybuf = &y_buffer[0];
    for(int i = 0; i < frameSize; i += 4) {
        unsigned char y1, y2, u, v;
        y1 = src[i];
        u = src[i + 1];
        y2 = src[i + 2];
        v = src[i + 3];

        int y1192_1 = YUV_TABLE[0][y1];
        int r1 = (y1192_1 + YUV_TABLE[1][v]) >> 10;
        int g1 = (y1192_1 - YUV_TABLE[2][v] - YUV_TABLE[3][u]) >> 10;
        int b1 = (y1192_1 + YUV_TABLE[4][u]) >> 10;

        int y1192_2 = YUV_TABLE[0][y2];
        int r2 = (y1192_2 + YUV_TABLE[1][v]) >> 10;
        int g2 = (y1192_2 - YUV_TABLE[2][v] - YUV_TABLE[3][u]) >> 10;
        int b2 = (y1192_2 + YUV_TABLE[4][u]) >> 10;

        r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
        g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
        b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
        r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
        g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
        b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;

        *lrgb++ = 0xff000000 | b1 << 16 | g1 << 8 | r1;
        *lrgb++ = 0xff000000 | b2 << 16 | g2 << 8 | r2;

        if(lybuf != NULL) {
            *lybuf++ = y1;
            *lybuf++ = y2;
        }
    }
}

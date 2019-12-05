#pragma once

enum class LUT_PixelFormat : unsigned char
{
    LUT_PixelFmt_Unknow = 0,
    LUT_PixelFmt_GRAY = 1,
    LUT_PixelFmt_BGR = 2,
    LUT_PixelFmt_HSV = 3,
    LUT_PixelFmt_NV12 = 4,
    LUT_PixelFmt_NV21 = 5,
    LUT_PixelFmt_YUV420P = 6
};


#pragma once
namespace media {

enum class FrameFormatType : unsigned int {
    FrameFmt_Unknown = 0,
    FrameFmt_RGB888 = 1,
    FrameFmt_BGR888 = 2,
    FrameFmt_ARGB8888 = 3,
    FrameFmt_NV12 = 4,
    FrameFmt_NV21 = 5,
    FrameFmt_YUV420P = 6,
};

}   // media

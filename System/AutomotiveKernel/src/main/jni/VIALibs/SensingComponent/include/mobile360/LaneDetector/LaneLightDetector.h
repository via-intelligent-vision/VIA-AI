#pragma once

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/calib3d.hpp>
#include "mobile360/adas-core/utils/ScaledLookupTable.h"
#include "mobile360/adas-core/media/frame_types.h"

namespace sensing {

class LaneLightDetector
{

public:
    enum WarningType
    {
        None,
        Left,
        Right
    };

    LaneLightDetector();
    WarningType Detect(unsigned char *grayRoadImgData, media::FrameFormatType frameType, cv::Size &roadImgSize, cv::Point2f& outLeftP1, cv::Point2f& outLeftP2, cv::Point2f& outRightP1, cv::Point2f& outRightP2);

private:

    void setupLUT(media::FrameFormatType frameType, cv::Size &roadImgData);
    cv::Vec4f FitCandidateGroundLine(cv::Vec4f &groundLines, cv::Mat &filteredFrame_8UC1, int extendGroundWidth);

    // LUT 
    ScaledLookupTable mScaledGrayFrameLUT;
    inline void CoordCvt_LUT_To_Frame(cv::Point2f lutCoord, cv::Point2f &frameCoord);
    inline void CoordCvt_Frame_To_LUT(cv::Point2f frameCoord, cv::Point2f &lutCoord);

    // Frame data
    cv::Mat mScaledGrayFrame;
};

} // namespace sensing

#pragma once

#include <opencv2/core.hpp>
#include "mobile360/adas-core/media/frame_types.h"

namespace sensing {
namespace lane {

/**
@brief BGR LookUp Table for perspective.
*/
class PerspectiveLookupTable
{
public:
    PerspectiveLookupTable();
    ~PerspectiveLookupTable();

    bool Empty();
    bool SizeCheck(media::FrameFormatType frameType, cv::Size inFrameSize, cv::Rect inFrameLookupROI);
    bool Lookup(cv::Mat *dstImg_8UC1, cv::Mat *dstImg_8UC3,
                media::FrameFormatType frameType, unsigned char *frameData, cv::Size frameSize, cv::Rect frameLookupROI,
                cv::Scalar paddingColor = cv::Scalar(0, 0, 0));

    bool Generate(media::FrameFormatType frameType, cv::Size frameSize, cv::Rect frameLookupROI, cv::Size perspectiveImgSize, cv::Mat &perspectiveMatrix);
    void Release();
    cv::Rect GetFrameLookupROI();

private:
    media::FrameFormatType mFramePixelFmt;
    cv::Size mFrameSize;
    cv::Rect mFrameLookupROI;
    cv::Mat  mPerspectiveMatrix;
    cv::Size mPerspectiveImgSize;
    int *mLUTa;
    int *mLUTb;
    int *mLUTc;

    bool GenerateTableFrom_NV12(std::vector<cv::Point2f> *cameraCoords);
    bool GenerateTableFrom_NV21(std::vector<cv::Point2f> *cameraCoords);
    bool GenerateTableFrom_BGR(std::vector<cv::Point2f> *cameraCoords);

    bool Lookup_BGR_to_BGR(cv::Mat *prespectiveImg, cv::Mat *sourceImg, cv::Scalar paddingColor);
    bool Lookup_BGR_to_Gray_BGR(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *sourceBGR, cv::Scalar paddingColor);
    bool Lookup_BGR_to_Gray(cv::Mat *prespectiveImg_8UC1, unsigned char *sourceBGR, cv::Scalar paddingColor);

    bool Lookup_NV12_to_Gray_BGR(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV12, cv::Scalar paddingColor);
    bool Lookup_NV12_to_Gray(cv::Mat *prespectiveImg_8UC1, unsigned char *sourceNV12, cv::Scalar paddingColor);
    
    bool Lookup_NV21_to_Gray_BGR(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV21, cv::Scalar paddingColor);
    bool Lookup_NV21_to_Gray(cv::Mat *prespectiveImg_8UC1, unsigned char *sourceNV21, cv::Scalar paddingColor);

};

} // namespace lane
} // namespace sensing


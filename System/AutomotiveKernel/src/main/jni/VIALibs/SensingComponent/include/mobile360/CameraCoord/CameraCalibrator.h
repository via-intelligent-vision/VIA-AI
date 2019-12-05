#ifndef VIA_AI_CAMERACALIBRATOR_H
#define VIA_AI_CAMERACALIBRATOR_H
// ------------------------------------------------------------------------------------------------
#include <vector>
#include <mutex>
#include <opencv2/core.hpp>
#include "mobile360/CameraCoord/CameraTypes.h"
#include "mobile360/adas-core/media/frame_types.h"

namespace via {
namespace camera {
// ------------------------------------------------------------------------------------------------
class CameraCalibrator {
public:
    CameraCalibrator();
    bool init(cv::Size2d boardSize, cv::Size2f gridSize_);
    bool findPattern(unsigned char *frameData, media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI, std::vector<cv::Point2f> *points = NULL);
    bool isCalibReady();
    double calibrate();
    bool save(std::string cameraName, const std::string &exportFullPath);
    bool save(std::string cameraName, cv::FileStorage &fs);

    float getRepositoryRatio();
    cv::Mat getCameraMatrix();
    cv::Mat getDistCoeff();
    cv::Size getCalibSize();

private:
    const int REPO_LIMIT;
    cv::Mat cameraMatrix_;
    cv::Mat distCoeff_;
    cv::Size frameSize_;
    cv::Size2d boardSize_;
    cv::Size2f gridSize_;
    std::vector<std::vector<cv::Point2f>> imgAnchorsList_;

};

// ------------------------------------------------------------------------------------------------
}   // namespace camera
}   // namespace via
// ------------------------------------------------------------------------------------------------
#endif //VIA_AI_CAMERACALIBRATOR_H

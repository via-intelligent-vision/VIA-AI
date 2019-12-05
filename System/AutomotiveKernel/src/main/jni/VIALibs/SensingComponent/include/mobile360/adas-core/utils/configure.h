#ifndef CONFIGURE_H
#define CONFIGURE_H

#include "opencv2/opencv.hpp"
#include <exception> 
#include <stdexcept>

#define TAG_LDWS                    "LDWS"
#define TAG_FCWS                    "FCWS"
#define TAG_Camera                  "Camera"
#define TAG_Name                    "Name"
#define TAG_InputImage              "InputImage"
#define TAG_CameraType              "CameraType"
#define TAG_Width                   "Width"
#define TAG_Height                  "Height"
#define TAG_CalibImgSize            "CalibImgSize"
#define TAG_Extrinsic               "Extrinsic"
#define TAG_Intrinsic               "Intrinsic"
#define TAG_CameraMatrix            "CameraMatrix"
#define TAG_DistCoeffs              "DistCoeffs"
#define TAG_PatternGeometry         "PatternGeometry"
#define TAG_DistanceFromVehicle     "DistanceFromVehicle"
#define TAG_VehiclePoleInCamera "VehiclePoleInCamera"
#define TAG_ImageAnchor             "ImageAnchor"
#define TAG_p0                      "p0"
#define TAG_p1                      "p1"
#define TAG_p2                      "p2"
#define TAG_p3                      "p3"

class AdasCvConfigure {
  public:
    AdasCvConfigure() = default;
    AdasCvConfigure(std::string file) : file_(file) {  
        Open(file);
    }

    virtual ~AdasCvConfigure() {
        if (storage_.isOpened()) {
            storage_.release();
        }
    }

    virtual bool LoadConfigure(const std::string &file) { return false; }

    bool Open(const std::string &file) {
        if (storage_.isOpened()) {
            storage_.release();
        }
        bool ret = storage_.open(file, cv::FileStorage::READ);
        if (!ret) {
            std::string error = "Open file error :" + file;
            throw std::runtime_error(error);
        }

        file_ = file;

        return true;
    }

    cv::Point2f ParsePoint2f(cv::FileNode node) {
        cv::Point2f point(0, 0);
        if (node.size() == 2) {
            cv::FileNodeIterator it = node.begin(); // Go through the node
            point.x = (float)*it;
            point.y = (float)*(++it);
        }
        return point;
    }

  protected:
    cv::FileStorage storage_;
    std::string file_;
};

#endif


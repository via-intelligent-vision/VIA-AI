#pragma once

#include "opencv2/core.hpp"
#include "mobile360/LaneDetector/LaneModelContext.h"
#include "mobile360/CameraCoord/CameraModule.h"

namespace via {
namespace sensing {
namespace lane {

class LaneModel
{
public:
    class UpdateData {
    public:
        UpdateData();
        bool anchorIsObj;
        cv::Point2f nLaneAnchor0_L; // normalized anchor
        cv::Point2f nLaneAnchor1_L;
        cv::Point2f nLaneAnchor2_L;
        cv::Point2f nLaneAnchor0_R;
        cv::Point2f nLaneAnchor1_R;
        cv::Point2f nLaneAnchor2_R;
        float laneWidth;   // unit : cm
        float scoreL;
        float scoreR;
        bool isDetected;
        float speed;
    };

    void init();
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);
    void update(LaneModel::UpdateData *data, via::camera::CameraModule *cameraModule);
    void getModel(LaneModelContext &result);
    void setDriftRatio(float ratioL, float ratioR);
    void getDriftRatio(float &ratioL, float &ratioR);


    static double getCurvature(LaneMarkingContext &laneMarking, double x = 30 * 100);

    LaneModel();
    ~LaneModel();

private:
    LaneModelContext outputLane;
    LaneModelContext internalLane;
    LaneMarkingContext laneCtx_L;
    LaneMarkingContext laneCtx_R;

    float mParam_LearnRate_cA;
    float mParam_LearnRate_cB;
    float mParam_LearnRate_cC;

    float mParam_DriftMinFactor;
    float mParam_LearnRate_Drift;
    float mDriftRatio_L;
    float mDriftRatio_R;
};


}   //namespace lane
}   //namespace sensing
}   //namespace via

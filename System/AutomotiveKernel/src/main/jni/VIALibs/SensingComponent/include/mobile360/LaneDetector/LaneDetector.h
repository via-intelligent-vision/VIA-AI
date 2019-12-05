#pragma once

#include <list>
#include <queue>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/calib3d.hpp>

#include "mobile360/adas-core/utils/configure.h"
#include "mobile360/adas-core/media/frame_types.h"
#include "mobile360/LaneDetector/PerspectiveLookupTable.h"
#include "mobile360/CameraCoord/CameraModule.h"

namespace sensing {

/* -----------------------------------------------------------------------
* About LaneDetectData
*/
class LaneDetectSample
{
public:
    enum class SampleStatus : unsigned char {
        Unknow,
        Detected,
        NoDetected,
        LeftDeparture,
        RightDeparture
    };

    LaneDetectSample();
    void reset();

    LaneDetectSample& operator=(LaneDetectSample &dst);
    /**
    @brief LDWS sample.
    The Lane anchors is normalized image coordinate.
        L2      R2
        |       |
        |       |
        L1      R1
        |       |
        |       |
        L0      R0
    */
    cv::Point2f mLaneAnchor0_L;
    cv::Point2f mLaneAnchor1_L;
    cv::Point2f mLaneAnchor2_L;
    cv::Point2f mLaneAnchor0_R;
    cv::Point2f mLaneAnchor1_R;
    cv::Point2f mLaneAnchor2_R;
    float mScoreL;
    float mScoreR;
    float mLaneWidth_cm;
    SampleStatus mLaneStatus;
};

/**
    @brief Calibration pattern information
    PatternAnchors are normalized to [0.0f~1.0f],  p0 is in the top-left anchor of pattern, and sort p0~p3 as clockwisw.
    p0 ----- p1
    |       |
    p3 ----- p2
*/
class CalibrationPattern {
public:
    float patternDistanceFromVehicle;
    cv::Size2f patternSize_cm;
    cv::Point2f iPatternAnchors[4];
    cv::Point2f iVehiclePoleInCamera;
};


class SceneGeometryData {
public:
    SceneGeometryData() {
        enable = false;
        pullDownPixel = 0;
        accumlatedAnchorList = NULL;
        sceneOffset = cv::Point2f(0.0f, 0.0f);
        scaleDownCounter = 0;

        for (int i = 0; i < OBSTACLE_LIMIT; i++) {
            groundObstacleAnchorList[i].first = false;
            groundObstacleAnchorList[i].second.resize(4);
        }
    }
    ~SceneGeometryData() {
        if (accumlatedAnchorList != NULL) {
            delete []accumlatedAnchorList;
        }
    }

    bool enable;
    sensing::lane::PerspectiveLookupTable LUT_CameraToGround;
    cv::Size sceneSize;
    cv::Size2f unit_PixelToMeter;
    cv::Size2f unit_MeterToPixel;
    cv::Mat matrix_ImgToGround;
    cv::Mat matrix_GroundToImg;
    cv::Point2f imgAnchors[4];
    cv::Point2f groundAnchors[4];
    cv::Point2f groundCameraAnchor;
    cv::Point2f sceneOffset;
    cv::Point3i *accumlatedAnchorList;
    int scaleDownCounter;

    const static int OBSTACLE_LIMIT = 8;
    std::pair<bool, std::vector<cv::Point> > groundObstacleAnchorList[OBSTACLE_LIMIT];

    float pullDownPixel;
};


enum LaneDepartureType
{
    Normal,
    NoDetecte,
    LeftDeparture,
    RightDeparture,
};

enum LaneDetectionType
{
    RegularLane,
    DummyLane,
    FakeLane
};

enum LaneMarkingType
{
    MarkingType_None,
    MarkingType_Regular,
    MarkingType_Solid,
    MarkingType_Dash,
    MarkingType_DoubleSolid,
    MarkingType_Dotted,
};

enum LaneMarkingColorType
{
    MarkingColor_Regular,
    MarkingColor_White,
    MarkingColor_Yellow,
    MarkingColor_Red,
    MarkingColor_Count,
};

class GroundLaneContex
{
public:
    GroundLaneContex()
    {
        isHistoryStable = false;
        isLaneChangedFromHistory = false;
        cameraArea = 0;
        cameraLaneMarking_L = cv::Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
        cameraLaneMarking_R = cv::Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
        area = -1;
        center = cv::Point2f(0.0f, 0.0f);
        laneMarkingExist_L = false;
        laneMarkingExist_R = false;
        laneLengthTop_Meter = 0.0f;
        laneLengthBottom_Meter = 0.0f;
        laneMarkingType_L = LaneMarkingType::MarkingType_Regular;
        laneMarkingType_R = LaneMarkingType::MarkingType_Regular;
        laneMarkingColorType_L = LaneMarkingColorType::MarkingColor_Regular;
        laneMarkingColorType_R = LaneMarkingColorType::MarkingColor_Regular;
        laneMarkingHeat_L = 0;
        laneMarkingHeat_R = 0;
        laneMarking_L = cv::Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
        laneMarking_R = cv::Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
        centerLaneMarking = cv::Vec4f(0.0f, 0.0f, 0.0f, 0.0f);
        laneDepartureType = LaneDepartureType::NoDetecte;
        laneDetectionType = LaneDetectionType::FakeLane;
        laneOffset = cv::Point2f(0.0f, 0.0f);
        laneScore_L = 0.0f;
        laneScore_R = 0.0f;
    };

    ~GroundLaneContex()
    {
        //laneCnvex.clear();
    };

    int mId;
    bool isHistoryStable;
    bool isLaneChangedFromHistory;


    double cameraArea;
    cv::Vec4f cameraLaneMarking_L;
    cv::Vec4f cameraLaneMarking_R;
    std::vector<cv::Point2f> cameraLaneCnvex;


    double area;
    cv::Point2f center;
    bool laneMarkingExist_L;
    bool laneMarkingExist_R;
    float laneLengthTop_Meter;
    float laneLengthBottom_Meter;
    LaneMarkingType laneMarkingType_L;
    LaneMarkingType laneMarkingType_R;
    LaneMarkingColorType laneMarkingColorType_L;
    LaneMarkingColorType laneMarkingColorType_R;
    float laneMarkingHeat_L;
    float laneMarkingHeat_R;
    cv::Vec4f laneMarking_L;
    cv::Vec4f laneMarking_R;
    cv::Vec4f centerLaneMarking;
    std::vector<cv::Point2f> curveLaneMarking_L;
    std::vector<cv::Point2f> curveLaneMarking_R;
    //vector<Point2f> laneCnvex;
    LaneDepartureType laneDepartureType;
    LaneDetectionType laneDetectionType;
    cv::Point2f laneOffset;
    float laneScore_L;
    float laneScore_R;
};


class LaneDetector;
class LdwsConfigure : public AdasCvConfigure {
public:
    LdwsConfigure() = default;
    LdwsConfigure(std::string file) : AdasCvConfigure(file) { }
    bool LoadConfigure(LaneDetector *ldws, const std::string &file);
    bool ExportConfiguration(LaneDetector *ldws, cv::FileStorage &fs);
};

class LaneDetector
{
    friend class LdwsConfigure;
public:
    enum WarningType
    {
        None,
        Left,
        Right
    };
    LaneDetector();    // default constructor is needed in LDWS.
    LaneDetector(const std::string &file);
    ~LaneDetector();

    float CalcGroundDistance(float srcImgX, float srcImgY);
    void SetVehiclePosistion(std::vector<cv::Vec3f> &vehicleList);
    void SetVehiclePosistion(int id, cv::Point2f objAnchorL, cv::Point2f objAnchorR);
    void SetRoadSliceLimitDistance(float limitMeter);
    bool SetRoadSliceCount(int sliceCount);
    std::string GetIdentifyComment();
    float GetLaneMarkingAngle(bool isLeft);
    std::vector<cv::Rect> *GetRoadSliceList();
    std::vector<cv::Rect> *GetRoadSliceList_L();
    std::vector<cv::Rect> *GetRoadSliceList_R();
    std::vector<cv::Rect> *GetRoadMarkingList();
    cv::Mat GetPerspectiveMatrix();
    double GetRoadPredictionThreshold();
    bool LoadConfiguration(std::string xmlPath);
    bool ExportConfiguration(std::string &exportPath);
    bool ExportConfiguration(cv::FileStorage &fs);
    bool RebuildExtrinsic(via::camera::CameraModule *cameraCtx);

    WarningType Detect(unsigned char *roadImgData, cv::Size roadImgSize, cv::Point2f& p0_L, cv::Point2f& p1_L, cv::Point2f& p2_L, cv::Point2f& p0_R, cv::Point2f& p1_R, cv::Point2f& p2_R);
    WarningType Detect(unsigned char *roadImgData, cv::Size roadImgSize, cv::Point2f& outLeftP1, cv::Point2f& outLeftP2, cv::Point2f& outRightP1, cv::Point2f& outRightP2);
    bool Detect(unsigned char *frameData, media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI, LaneDetectSample &result);

private:
    void LoadDefaultData();
    void LoadDefaultConfiguration();
    bool GenerateLUT(media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI);
    bool GenerateLUT_Zwei(media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI);
    bool UpdateLUT_Zwei(GroundLaneContex *curLane, media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI);


    // Regular Data
    cv::Size mProcessFrameSize;
    float mVehicleWidthMeter;
    float mDepartureWarningMeter;

    float mUnit_CamereSetupOffsetMeter;
    float mUnit_LaneChangeThresholdMeter;
    float mUnit_LaneWidthMaximumMeter; // regular lan 3m ~ 3.75m
    float mUnit_LaneWidthMinimumMeter;
    float mUnit_RoadSliceDetectionLimitMeter;

    // Extrinsic Data
    cv::Size mDefaultPatternSize_CM;
    cv::Point2f mVanishingPoint;
    cv::Point2f mPerspectiveVehicleHeadAnchor;

    // Dummy Data
    cv::Mat mGroundFrameBGR;
    cv::Mat mGroundFrameGray;
    cv::Mat mGroundFiltedFrame;

    // Internal Data
    via::camera::CameraModule *mCameraCtx;
    std::string mIdentifyComment;
    sensing::lane::PerspectiveLookupTable mLUT_CameraToGround;

    bool mIsDetectionHistoryStable;
    int mLaneMissingDetectCounter;
    int mLaneMissingDetectLimit;
    int mDetectionStableLimit;
    std::list<GroundLaneContex *> mFundamentalGroundLaneHistory;
    std::list<GroundLaneContex *> mDetectionGroundLaneHistory;
    std::list<GroundLaneContex *> mNeighborGroundLaneHistory_L;
    std::list<GroundLaneContex *> mNeighborGroundLaneHistory_R;
    std::vector<GroundLaneContex *> mLaneContextPool;
    GroundLaneContex mAvgLaneCtx;
    GroundLaneContex mAccLaneCtx;
    GroundLaneContex avgLaneCtx;
    int mAccLaneCounter;
    int mAccLaneCounterLimit;
    int mAccThresholdOTSU =0;
    double mSimpleShapeFilterThreshold = 0;
    int mRoadSliceCount;
    float mRoadSliceLimitDistance;
    std::vector<cv::Rect> mRoadSliceList;
    std::vector<cv::Rect> mRoadSliceList_L;
    std::vector<cv::Rect> mRoadSliceList_R;
    float mLaneMarkingDegree_L;
    float mLaneMarkingDegree_R;
    cv::Point2f mLaneRenderCtrlAnchors_L[4];
    cv::Point2f mLaneRenderCtrlAnchors_R[4];

    cv::Point2f mAuxLaneRenderCtrlAnchors_L[4];
    cv::Point2f mAuxLaneRenderCtrlAnchors_R[4];
    bool mNightMode;
    int mLaneUnavailableCounter;
    int mLaneUnavailableLimit;
    std::vector<cv::Rect> mRoadMarkingList;

    // Module flags.
    bool mModuleFlag_EnableBottsDotsLaneDetection;

    // Modules
    void Module_FundamentalHistoryManager(GroundLaneContex *curLaneCtx);

    // Funstions
    GroundLaneContex *DetecteDummyGroundLane(std::vector<cv::Vec4f> &candidateGroundLines);
    GroundLaneContex *DetecteFakeGroundLane(std::vector<cv::Vec4f> &candidateGroundLines, std::list<GroundLaneContex *> &detectionGroundLaneHistory);
    GroundLaneContex *DetecteGroundDotLane(std::vector<cv::Vec4f> &candidateGroundLines, std::vector<cv::Vec4f> &dotLines);

    void UpdateGroundLaneContext(GroundLaneContex *dstLaneCtx, cv::Vec4f newLaneMarkingL, cv::Vec4f newLaneMarkingR);

    LaneDepartureType DetecteDepartureStatus(GroundLaneContex *currentLaneCtx);

    WarningType DetecteWarningType(LaneDepartureType departureType);

    GroundLaneContex *DetecteFundamentalGroundLane(std::vector<GroundLaneContex *> &candidateGroundLanes, std::list<GroundLaneContex *> &groundLaneHistory, int compareLimit = -1);

    bool IsLaneChangedFromHistory(SceneGeometryData &scene, GroundLaneContex *sceneLane, GroundLaneContex *testLane);

    bool IsLaneCompatibleWithHistory(SceneGeometryData &scene, GroundLaneContex *sceneLane, GroundLaneContex *currentLane);

    bool IsDetectionHistoryStable(int considerCount, float acceptPercentage);

    bool IsDetectionHistoryDrift(int considerCount, float acceptPercentage);

    void IsolationPixelsFilter(cv::Mat dstImg, cv::Mat filtedImg);

    void IsolationPixelsFilter(cv::Mat &inputOutputImg);

    void PredictComplexMarkingArea(std::vector<cv::Rect> &predictDotArea, cv::Mat &srcBinaryImg);

    cv::Vec4f FitCandidateGroundLine(cv::Vec4f &groundLines, cv::Mat *filteredFrame_8U, int extendGroundWidth, bool &isFitted);

    void MergeCandidateLine(std::vector<cv::Vec4f> &mergedLine, std::vector<cv::Vec4f> &candidateLines, float mergeOffset_Meter);

    void GetGroundLineFittingPoints(std::vector<cv::Point2f> &linePoints, cv::Vec4f &groundLines, cv::Mat *filteredFrame_8U, int ExtendgroundWidth = 4);

    void GetNearestGroundLineFittingPoints(std::vector<cv::Point2f> &linePoints, cv::Vec4f &groundLines, cv::Mat *filteredFrame_8U, int ExtendgroundWidth = 4);

    void DetecteCandidateGroundLines(std::vector<cv::Vec4f> &candidateGroundLines, cv::Size &detectedImgSize, cv::Mat *filteredFrame_8UC1);

    void DetecteCandidateGroundLines_NXP(std::vector<cv::Vec4f> &candidateGroundLines, cv::Size &detectedImgSize, cv::Mat *filteredFrame_8UC1);

    void DetecteCandidateGroundLanes(std::vector<GroundLaneContex *> &candidateGroundLanes, std::vector<cv::Vec4f> &fitCandidateGroundLines, cv::Size groundFrameSize);

    void DetecteCandidateNeighborGroundLanes(std::vector<GroundLaneContex *> &candidateNeighborGroundLanes, std::vector<cv::Vec4f> &fitCandidateGroundLines, GroundLaneContex *fundamentalGroundLane, bool isLeft);

    void VanishingPointVerification(cv::Size &detecteImgSize, std::vector<cv::Vec4f> &candidateGroundLines);

    void CandidateLineVerification(std::vector<cv::Vec4f> &houghGroundLines, std::vector<cv::Vec4f> &candidateGroundLines, std::vector<cv::Vec4f> &pruneGroundLines_L, std::vector<cv::Vec4f> &pruneGroundLines_R);

    void CalcPerspectiveCameraCoord(SceneGeometryData &scene, cv::Size &detecteImgSize);

    void CalcPerspectiveVehicleFrontCoord(std::vector<cv::Point2f> &patternGroundCoord);

    void CalibrateROI(cv::Rect &roi, cv::Size srcImgSize);

    cv::Size2f mSceneHeightScale;
    SceneGeometryData mSceneA;
    SceneGeometryData mSceneB;

    CalibrationPattern mCaliPattern;

    const int ACCUMLATED_LIMIT = 5;
    std::list<cv::Mat> accumlatedFrameList;
    std::list<std::pair<cv::Mat, cv::Point2f> > accumlatedFrameList_Zwei;

    bool DetectSceneA(unsigned char *frameData, media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI);
    bool DetectSceneB(unsigned char *frameData, media::FrameFormatType frameType, cv::Size frameSize, cv::Rect detectImgROI, LaneDetectSample &result);
    void SetVehiclePosistion_(SceneGeometryData &scene, int id, cv::Point2f objAnchorL, cv::Point2f objAnchorR);

    GroundLaneContex laneContexZero;
    GroundLaneContex laneContexEin;
    GroundLaneContex laneContexZwei;
    int mMissingCount_15m;

    int mLife_IgnoreGroundArea;
};


} // namespace sensing


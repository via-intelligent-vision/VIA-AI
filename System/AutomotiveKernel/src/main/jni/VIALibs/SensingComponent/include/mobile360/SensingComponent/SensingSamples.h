#ifndef VIA_ADAS_SENSINGSAMPLES_H
#define VIA_ADAS_SENSINGSAMPLES_H
// ----------------------------------------------------------------------------------------------------------------------------------------------------
#include <sys/time.h>
#include <vector>
#include "opencv2/core/core.hpp"
#include "mobile360/CameraCoord/CameraModule.h"
#include "mobile360/LaneDetector/LaneModelContext.h"

// ----------------------------------------------------------------------------------------------------------------------------------------------------
namespace via {
namespace sensing {

// ----------------------------------------------------------------------------------------------------------------------------------------------------
enum class SampleTypes : unsigned char{
    Unknown         = 0,
    Lane            = 1,
    ForwardVehicle  = 2,
    BlindSpot_L     = 3,
    BlindSpot_R     = 4,
    SpeedLimit      = 5,
    Environment     = 6,
    Object_DL       = 7,
    TrafficLight    = 8,
    Calibration     = 9,
};

/**
 @brief ObjectData is the base class for object detector (FCWS, FCWS_DL ....)
*/
class ObjectData {
public:
    int mObjTypeIndex;
    float mScore;
    cv::Point2f mMinAnchor;
    cv::Point2f mMaxAnchor;
    float mDistance;
    float mReactionTime;

    ObjectData() {
        reset();
    }

    void reset() {
        mObjTypeIndex = 0;
        mScore = 0;
        mMinAnchor.x = 0;
        mMinAnchor.y = 0;
        mMaxAnchor.x = 0;
        mMaxAnchor.y = 0;
        mDistance = 0;
        mReactionTime = 0;
    }

    ObjectData(int objTypeIndex, float score, float minX, float minY,  float maxX, float maxY, float distance) {
        mObjTypeIndex = objTypeIndex;
        mScore = score;
        mMinAnchor.x  = minX;
        mMinAnchor.y  = minY;
        mMaxAnchor.x  = maxX;
        mMaxAnchor.y  = maxY;
        mDistance = distance;
    }

    void copyFrom(ObjectData &src) {
        this->mObjTypeIndex   = src.mObjTypeIndex;
        this->mScore          = src.mScore;
        this->mMinAnchor      = src.mMinAnchor;
        this->mMaxAnchor      = src.mMaxAnchor;
        this->mDistance       = src.mDistance;
    }
};

/*
 @brief About SensingSample
    SensingSample is the base class for all ADAS samples.
*/
class SensingSample
{
public :
    SensingSample();
    SensingSample(SampleTypes type);
    virtual void reset() = 0;
    void onSample();
    long long getSampleTime();
    SampleTypes type(); 

private:
    long long mSampleTime_ms;
    SampleTypes type_;
};

/*
 @brief Store the lane detector data.
*/
class LaneSample : public SensingSample
{
public:
    enum class SampleStatus : unsigned char {
        Unknown = 0,
        SoftDisable = 1,
        Detected = 2,
        NoDetected = 3,
        LeftDeparture = 4,
        RightDeparture = 5,
        Calibrating = 6,
    };

    LaneSample &operator=(LaneSample &src);
    LaneSample();
    void reset();
    void copyTo(LaneSample &dst);

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
    const static int RESAMPLE_COUNT = 9;
    cv::Size mFrameSize;
    cv::Point2f mLaneAnchor_L[RESAMPLE_COUNT];
    cv::Point2f mLaneAnchor_R[RESAMPLE_COUNT];
    cv::Point2f mRawAnchor0_L;
    cv::Point2f mRawAnchor1_L;
    cv::Point2f mRawAnchor2_L;
    cv::Point2f mRawAnchor0_R;
    cv::Point2f mRawAnchor1_R;
    cv::Point2f mRawAnchor2_R;
    float mLaneWidth;   // unit : cm
    SampleStatus mLaneStatus;
    sensing::lane::LaneModelContext mLaneModelCtx;
};

/*
 @brief Store the vehicle detector data. (FCWS & FCWS_DL)
*/
class VehicleSample : public SensingSample
{
public:
    VehicleSample();
    void reset();

    // FCW sample
    sensing::ObjectData mForwardVehicle;
};

/**
@brief Store the object detector result. (FCWS_DL)
 */
class ObjectDetectSample : public SensingSample
{
public:
    static const size_t MAX_SAMPLE_COUNT = 64;

    ObjectDetectSample();
    void reset();

    // Forward object for collision detection
    int mFocusObjectId;

    // detected object (n - 1 , 1 is mForwardObject).
    int mSampleCount;
    ObjectData mObjectDataList[MAX_SAMPLE_COUNT];
};

/**
@brief Store the object detector result. (FCWS_DL)
 */
class TrafficLightDetectSample : public SensingSample
{
public:
    static const size_t MAX_SAMPLE_COUNT = 8;

    TrafficLightDetectSample();
    void reset();

    // detected object (n - 1 , 1 is mForwardObject).
    size_t mSampleCount;
    ObjectData mTrafficLightList[TrafficLightDetectSample::MAX_SAMPLE_COUNT];
};

/*
 @brief Store the blind spot detector data.
*/
class BlindSpotSample_L : public SensingSample
{
public:
    BlindSpotSample_L();
    void reset();

    bool isWarning;
};

/*
 @brief Store the blind spot detector data.
*/
class BlindSpotSample_R : public SensingSample
{
public:
    BlindSpotSample_R();
    void reset();

    bool isWarning;
};

/*
 @brief Store the speed limit data.
*/
class SpeedLimitSample : public SensingSample
{
public:
    SpeedLimitSample();
    void reset();

    int mSpeedLimit_1;
    int mSpeedLimit_2;
};

/*
 @brief Store the environment data.
*/
class EnvironmentSample : public SensingSample
{
public:
    enum class WeatherType : unsigned char {
        Unknown     = 0x0,
        Day         = 0x1,
        Night       = 0x2,
        Raining     = 0x4,
    };

    EnvironmentSample();
    void reset();

    WeatherType mWeatherType;
};


/*
 @brief Store the environment data.
*/
class OnTheFlyCalibrationSample : public SensingSample
{
public:
    enum class CalibrationStatus : unsigned char {
        Unknown     = 0x0,
        Success     = 0x1,
        Fail       = 0x2,
        Calibrating  = 0x4,

    };

    OnTheFlyCalibrationSample();
    void reset();

    camera::CameraModule mCalibratedCamera;
    CalibrationStatus mStatus;
    int mEpoch;
    int mEpochLimit;
};


class RuntimeLoadDataPathSamples
{
public:
    RuntimeLoadDataPathSamples();
    std::string FCWS_CascadeModel;
    std::string FCWS_DL_Model;
    std::string FCWS_DL_Label;
    std::string FCWS_DL_DSPLib;
    std::string SLD_Model;
    std::string SLD_Proto;
    std::string SLD_Label;
    std::string SLD_NightModel;
    std::string SLD_PrefetchModel;
    std::string Weather_ClassifyModel;
    std::string TLD_Pattern_1;
    std::string TLD_Pattern_2;
    std::string TLD_Pattern_3;
};


// ----------------------------------------------------------------------------------------------------------------------------------------------------
}   // namespace sensing
}   // namespace via
#endif //VIA_ADAS_SENSINGSAMPLES_H

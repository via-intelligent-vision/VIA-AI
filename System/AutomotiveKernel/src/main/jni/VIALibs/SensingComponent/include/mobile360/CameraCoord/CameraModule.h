#ifndef CAMERA_MODULE_H
#define CAMERA_MODULE_H
// ------------------------------------------------------------------------------------------------
#include <vector>
#include <mutex>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include "mobile360/adas-core/utils/configure.h"
#include "mobile360/CameraCoord/CameraTypes.h"

namespace via {
namespace camera {

enum class CameraLocationTypes : unsigned int {
    Location_Unknown = 0,
    Location_Front = 1,
    Location_Right = 2,
    Location_Left = 3,
    Location_Rear = 4,
};
/**
    @brief Calibration pattern information
    PatternAnchors are normalized to [0.0f~1.0f],  p0 is in the top-left anchor of pattern, and sort p0~p3 as clockwisw.
    p0 ----- p1
    |       |
    p3 ----- p2
*/
struct CalibrationPatternData {
    cv::Size2f patternSize; // unit : cm
    cv::Point2f nPatternAnchorP0;
    cv::Point2f nPatternAnchorP1;
    cv::Point2f nPatternAnchorP2;
    cv::Point2f nPatternAnchorP3;
    float patternDistanceFromVehicle; // unit : cm
};

/**
@brief Configuration for camera module.
*/
class CameraConfigure : public AdasCvConfigure {
public:
    static const CameraTypes DEFAULT_CAMERA_TYPE;

    CameraConfigure();
    CameraConfigure(CameraTypes type, CameraLocationTypes location);
    virtual ~CameraConfigure();
    /**
    @brief Load & save Configuration with tag <Camera> , <LDWS>, <FCWS>.
    */
    virtual bool load(const std::string &instrinsicPath, const std::string extrinsicPath);
    virtual bool save(const std::string &exportFullPath);
    virtual bool save(cv::FileStorage &fs);
    bool LoadConfigure(const std::string &file);

    void copyTo(CameraConfigure &dst);

    /**
    @brief Build extrinsic or perspective.
    This function is called when load() fiinish automatically.
    If you use set functions below, you must call buildScene() manually when all set() finish. This step is used to correct the params of extrinsic & perspective ,
        * setCameraType(),
        * setCalibrationPatternInfo(),
        * setCameraMatrix(),
        * setDistCoeff(),
        * setCameraMatrix()
    */
    bool buildScene();

    bool isExtrinsicEmpty();
    bool isIntrinsicEmpty();
    bool isFrameSizeMatching(cv::Size checkSize);
    CameraTypes getCameraType();
    CameraLocationTypes getCameraLocation();
    std::string getErrMsg() { return errMsg; };
    cv::Size getFrameCalibrationSize();
    cv::Size getFrameSize();
    cv::Mat getCameraMatrix();
    cv::Mat getDistCoeff();
    cv::Mat getExtrinsic();
    cv::Mat getRvec();
    cv::Mat getTvec();
    void getCalibrationPatternData(CalibrationPatternData &dstPattern);

    void setCameraType(CameraTypes type, CameraLocationTypes location = CameraLocationTypes::Location_Front, int calWidth = 0, int calHeight = 0, float *cameraMatrix_3x3f = NULL, float *distCoeff_5x1f = NULL);
    void setCalibrationPatternInfo(cv::Size2f patternSize, cv::Point2f normalizePatternAnchorP0, cv::Point2f normalizePatternAnchorP1, cv::Point2f normalizePatternAnchorP2, cv::Point2f normalizePatternAnchorP3, float patternDistanceFromVehicle);
    void setFrameSize(cv::Size frameSize);
    void setCameraMatrix(cv::Mat cameraMatrix_3x3f);
    void setDistCoeff(cv::Mat distCoeff_5x1f);
    void setExtrinsic(cv::Mat extrinsic_4x4d);
    void setRvec(cv::Mat rvec_3x1d);
    void setTvec(cv::Mat tvec_3x1d);

protected:

    void reset();

    /**
    @brief Solve extrinsic or perspective.
    */
    bool solveExtrinsic(std::vector<cv::Point3f> &objectPoints, std::vector<cv::Point2f> &imagePoints);
    bool solvePerspective(std::vector<cv::Point3f> &objectPoints, std::vector<cv::Point2f> &imagePoints);
    void refreshExtrinsic();

    // TODO: these element must move to AdasCvConfigure
    std::mutex cfgMutex;
    std::string errMsg;

    /**
    @brief Camera types. this type decide the intrinsic of camera.
    */
    CameraTypes cameraType;
    CameraTypes configCameraType;
    CameraLocationTypes cameraLocation;
    std::string cameraName;

    /**
    @brief Intrinsic data
    */
    cv::Size frameCalibrationSize;     // frmae Size in calibration
    cv::Mat cameraMatrix;
    cv::Mat distCoeff;

    /**
    @brief Extrinsic data
    In CameraConfigure, no interface to calculate extrinsic except the extrinsic inside XML.
    If the runtime extrinsic calcuation is need, use CameraModule replaced.
    */
    cv::Mat extrinsic;    // 4x4 matrix(double) , constructed by rvec & tvec
    cv::Mat rvec_3x1d;
    cv::Mat rvec_3x3d;
    cv::Mat tvec_3x1d;

    /**
    @brief Pattern informations
    PatternAnchors are normalized to [0.0f~1.0f],  p0 is in the top-left anchor of pattern, and sort p0~p3 as clockwisw.
    p0 ----- p1
    |       |
    p3 ----- p2
    */
    CalibrationPatternData caliPattern;

    /**
    @brief Perspective matrix
    In CameraConfigure, no interface to calculate perspective except the perspective matrix inside XML.
    If the runtime perspective calcuation is need, use CameraModule replaced.

    Even thought we could build camera module with Instrinsic/Extrinsic, the camera type from customers is difficult to predict.
    In 0-plane distance/width measurment, perspectvie matrix is a similar solution as Instrinsic/Extrinsic.
    But, If user need auto calibtation solution, Instrinsic/Extrinsic is needed.
    */
    cv::Size frameSize;    // Camera frmae size, may different to the frameCalibrationSize
    cv::Mat perspectiveMatrix;
    cv::Mat perspectiveInvMatrix;
    cv::Size2f unitCvt_PerspectivePixelToMeter;
    cv::Size2f unitCvt_MeterToPerspectivePixel;
    cv::Point2f vehiclePoleInCamera;
    cv::Point2f perspectiveVehicleHeadAnchor;

private:
    bool loadIntrinsic(const std::string &instrinsicPath);
    bool loadExtrinsic(const std::string extrinsicPath);

};


/**
@brief The camera model of this class is based on openCV camera model.
*/
class CameraModule : public CameraConfigure
{
public:
    CameraModule();
    CameraModule(CameraTypes type, CameraLocationTypes location = CameraLocationTypes::Location_Front);
    CameraModule(CameraTypes type, CameraLocationTypes location, std::string name, int caliWidth, int caliHeight, float *cameraMatrix_3x3f, float *distCoeff_5x1f);
    ~CameraModule();
    void copyTo(CameraModule &dst);
    bool load(const std::string &instrinsicPath, const std::string extrinsicPath);

    /**
        @brief Refresh calibration pattern, replace by virtual pattern.
            Call this function after auto calibration,  or set extrinsic/perspective  manually.
        */
    bool invokeVirtualPattern();


    bool isExtrinsicStable();

    /**
    @brief Check input frame size is equalized to current camera or not.
    @return true if size matching, otherwise return false.
    */
    bool frameSizeCheck(cv::Size &size);

    // TODO : Convert function for Extrinsic to normalized coordinate
    // [Extrinsic only] Undistort image coordinate to undistortion coordinate.
    cv::Point2f coordCvt_ImgCoord_To_UndistCoord(double imgCoordx, double imgCoordy, cv::Mat *cutomCameraMatrix_3x3f);

    // [Extrinsic only] Reprojects image coordinate(2D) to object coordinate (3D, z=0).
    cv::Point3f coordCvt_ImgCoord_To_ObjCoord_ZeroZ(double imgCoordx, double imgCoordy);

    // [Extrinsic only] Reprojects image coordinate(2D) to object coordinate (3D, z=0).
    cv::Point3f coordCvt_NormalizedImgCoord_To_ObjCoord_ZeroZ(double imgCoordx, double imgCoordy);

    // [Extrinsic only] Reprojects unstortion coordinate(2D) to object coordinate (3D, z=0).
    cv::Point3f coordCvt_UndistCoord_To_ObjCoord_ZeroZ(double undistCoordx, double undistCoordy);

    // [Extrinsic only] Project object coordinate(3D) to an image coordinate (2D).
    cv::Point2f coordCvt_ObjCoord_To_NormalizedImgCoord(double objCoordx, double objCoordy, double objCoordz);

    // [Extrinsic only] Calculate the <vehicle - object> distance from image coordinate
    double coordCvt_NormalizedImgCoord_To_cmDistance(double nImgCoordx, double inIgCoordy);

    /**
    @brief  [Perspective only] Calculate the <vehicle - object> distance from image coordinate
    Flag isNormalized is used to identify imgCoord is normalized to [0.0~1.0] or not.
    In general, using normalized coordinate as input is recommended to reduce coordinate sync problem of developer.
    */
    double calcDistance(double imgCoordx, double imgCoordy, bool isNormalized = false);

    /**
    @brief [Perspective only] These 3 function is reserved interface for FCWS.
    Be sure to the coordinate of input line must match the frameSize coordinate.
    */
    bool CheckLenInRange(const std::vector<cv::Point2f> &line, float lower, float upper);
    std::vector<cv::Point2f> TransLineToReal(std::vector<cv::Point2f> &line);
    std::vector<cv::Point2f> TransLine(std::vector<cv::Point2f> &line);

    void forceExtrinsicStable(bool isStable);

private:
    bool b_isExtrinsicStable;
    bool isValid();
};

}   // namespace camera
}   // namespace via
// ------------------------------------------------------------------------------------------------
// CAMERA_MODULE_H
#endif

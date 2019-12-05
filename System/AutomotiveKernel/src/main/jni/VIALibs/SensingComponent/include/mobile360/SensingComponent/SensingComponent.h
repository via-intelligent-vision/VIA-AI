#ifndef VIA_ADAS_ADASCOMPONENT_H
#define VIA_ADAS_ADASCOMPONENT_H

#include <mutex>
#include <vector>
#include <queue>
#include <string.h>
#include <memory>
#include "mobile360/CameraCoord/CameraModule.h"
#include "mobile360/adas-core/media/frame_types.h"
#include "mobile360/SensingComponent/types.h"
#include "mobile360/SensingComponent/SensingSamples.h"

// ---------------------------------------------------------------------------------------------------------------------------------------------------
namespace via {
namespace sensing {

/* ---------------------------------------------------------------------------------------------------------------------------------------------------
 *  About SensingＭodule
*/
#define MAX_COMPONENT_BUFFER_COUNT 2
#define MAX_RELATED_SENSING_COMPONENT_COUNT 4

class SignalBundle {
public:
    SignalBundle() {
        speed_ = 0;
        steeringRatio = 12.3;
        isDirIndicatorOn_L_ = false;
        isDirIndicatorOn_R_ = false;
        steerAngle_ = 0;
        isCANBusOnline_ = false;
    }

    void copyTo(SignalBundle &dst) {
        dst.steerAngle_         = this->steerAngle_;
        dst.steeringRatio       = this->steeringRatio;
        dst.speed_              = this->speed_;
        dst.isDirIndicatorOn_L_ = this->isDirIndicatorOn_L_;
        dst.isDirIndicatorOn_R_ = this->isDirIndicatorOn_R_;
        dst.isCANBusOnline_     = this->isCANBusOnline_;
    }

    float steerAngle_;
    float steeringRatio;
    float speed_;
    bool isDirIndicatorOn_L_;
    bool isDirIndicatorOn_R_;
    bool isCANBusOnline_;
};


class SensingComponent {
public:
    //friend class FrameBuffer;

    enum class ComponentMode : unsigned char {
        Idle = 0,
        Sensing = 1,
        Release = 2,
        OnTheFlyCalibration = 3,
    };


    SensingComponent();
    ~SensingComponent();
    void create(u_int32_t detectors, camera::CameraModule *refCameraModule);
    bool init(std::string &cfgPath);
    bool detect(SignalBundle &sigBundle);
    void release();
    bool doAutoCalibration(float cameraInstalledHeight, float cameraToCenterOffset);
    void toggleOnTheFlyCalibration(bool enable);
    bool exportConfig(std::string exportPath);
    bool exportConfig(cv::FileStorage &fs);

    int bufferFrame_BGR888(void *frame, int width, int hieght, int roiX, int roiY, int roiWidth, int roiHeight);
    int bufferFrame_RGBA8888(void *frame, int width, int hieght, int roiX, int roiY, int roiWidth, int roiHeight);
    int bufferFrame_NV12(void *frame, int width, int hieght, int roiX, int roiY, int roiWidth, int roiHeight);
    int bufferFrame(void* yFramePtr, void* uFramePtr, void* vFramePtr,
                    int frameWidth, int frameHeight,
                    int yStepStride, int uStepStride, int vStepStride,
                    int yPixelStride, int uPixelStride, int vPixelStride,
                    int roiX, int roiY, int roiWidth, int roiHeight);
    bool registerRelatedComponent(SensingComponent * component, u_int32_t relatedDetectors);
    bool updateCamera(camera::CameraModule *refCameraModule);

    bool setRuntimeLoadableData(RuntimeLoadDataTypes type, const char *path, const char *name);

    size_t getFrameBufferQueueCount();
    unsigned int getDetectors();
    SensingComponent::ComponentMode getComponentMode();
    SensingSample *getSample(SampleTypes type);
    bool isDetectorSupport(DetectorTypes detector);

    void lockComponent();
    void unlockComponent();

    void dumpTime(bool enable, int time_ms);
private:
    class ComponentImpl;
    std::shared_ptr<ComponentImpl> pimpl_;

};

// ---------------------------------------------------------------------------------------------------------------------------------------------------
}   // namespace m360
}   // namespace via
#endif //VIA_ADAS_ADASCOMPONENT_H

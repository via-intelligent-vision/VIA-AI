#ifndef MOBILE360_SENSING_SENSINGTASK_H
#define MOBILE360_SENSING_SENSINGTASK_H
// ----------------------------------------------------------------------------------------------------------
#include <mutex>
#include <vector>
#include <queue>
#include <pthread.h>
#include "mobile360/SensingComponent/SensingComponent.h"

// ----------------------------------------------------------------------------------------------------------
namespace via {
namespace sensing {

// ----------------------------------------------------------------------------------------------------------
class SensingTask {
public:
    enum class TaskMode : unsigned char {
        NOT_INIT,
        INIT,
        WAIT_DATA,
        SENSING,
        RELEASE,
    };

    SensingTask();
    ~SensingTask();
    void create(u_int32_t detectors, camera::CameraModule *refCamera);
    bool init(std::string &cfgPath);
    void run();
    void release();

    // buffer funcs
    bool bufferSignal(SignalBundle &sigBundle);
    int bufferFrame_BGR888(void *frame, int width, int height, int roiX, int roiY, int roiWidth, int roiHeight);
    int bufferFrame_RGBA8888(void *frame, int width, int height, int roiX, int roiY, int roiWidth, int roiHeight);
    int bufferFrame_NV12(void *frame, int width, int height, int roiX, int roiY, int roiWidth, int roiHeight);

    // set funcs    
    bool setRuntimeLoadableData(RuntimeLoadDataTypes type, const char *path, const char *name);
    bool registerRelatedTask(SensingTask *task, u_int32_t relatedDetectors);
    void dumpTime(bool enable, int time_ms);

    // query funcs
    SensingSample *getSample(SampleTypes type); 

protected:
    void threadEntry();


private:
    void _wakeup();

    // task thread data
    static void *pthreadEntryFunc(void *thiz);
    pthread_mutex_t conditionMutex_;
    pthread_cond_t condition_;
    pthread_t thread_; 
    
    void setMode(TaskMode mode);
    TaskMode getMode();

    // datas
    TaskMode mode_;
    SensingComponent cmp_;
    SignalBundle signalBundle_;
    
};
// ----------------------------------------------------------------------------------------------------------

}   // namespace sensing
}   // namespace via
// ----------------------------------------------------------------------------------------------------------

#endif //VIA_ADAS_ADASCOMPONENT_H

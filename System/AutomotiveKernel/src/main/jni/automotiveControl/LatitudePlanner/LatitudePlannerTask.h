/* /////////////////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//                                 MIT License
//                            Copyright (c) 2019 VIA, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//
// ////////////////////////////////////////////////////////////////////////////////////////////// */

#ifndef VIA_ADAS_LATITUDEPLANNERTASK_H
#define VIA_ADAS_LATITUDEPLANNERTASK_H

#include <pthread.h>
#include <automotiveControl/LatitudePlanner/LatitudePlanner.h>

namespace via {
namespace automotive {

class LatitudePlannerTask {
public:
    enum class TaskStatus : unsigned char {
        create,
        init,
        active,
        idle,
        planning,
        release,
    };

    struct pthreadData {
        pthreadData() {
            isRunning = false;
        }
        bool isRunning;
        pthread_mutex_t conditionMutex;
        pthread_cond_t condition;
        pthread_t thread;
        void *args;
    };

    struct DataBundle {
        via::car::CarModel carModel;
        via::canbus::CANParams_Speed canSpeed;
        via::canbus::CANParams_SteeringSensor canSteeringSensor;
        via::sensing::LaneSample sensingLane;
        via::automotive::LatitudePlan latitudePlan;
    };

    LatitudePlannerTask();
    ~LatitudePlannerTask();
    void init(via::automotive::LatitudePlanner *planner, via::car::CarModel *carModel);
    void active();
    void release();
    bool update(via::canbus::CANParams_Speed *canSpeed,
                via::canbus::CANParams_SteeringSensor *canSteeringSensor,
                via::sensing::LaneSample *sensingLane);
    void getLastResult(via::automotive::LatitudePlan *result);
    TaskStatus getStatus();
    bool isInit();

protected:
    void threadEntry();
    void setTaskStatus(TaskStatus status);
    void setPlanResult(via::automotive::LatitudePlan *result);
    pthreadData *getpThreadData();
    DataBundle *getDataBundle();
    via::automotive::LatitudePlanner *getPlanner();

private:
    struct pthreadData planningThreadData;
    struct DataBundle dataBundle;
    via::automotive::LatitudePlanner *planner;
    TaskStatus taskStatus;
    pthread_mutex_t resourceMutex;
    static void * pthreadEntryFunc(void * This);    // dummy pthread emtry.

};

}
}



#endif //VIA_ADAS_LATITUDEPLANNERTASK_H

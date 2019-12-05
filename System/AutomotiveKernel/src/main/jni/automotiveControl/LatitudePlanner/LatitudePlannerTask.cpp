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

#include <automotiveControl/LatitudePlanner/LatitudePlannerTask.h>

//--------------------------------------------------------------------------------------------------
#include <android/log.h>
#define  LOG_TAG    "LatitudePlanner"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace via::automotive;

//--------------------------------------------------------------------------------------------------
static long long getms()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    double ms = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000 ;
    return (long long)ms;
}

//--------------------------------------------------------------------------------------------------
LatitudePlannerTask::LatitudePlannerTask() {
    planner = NULL;
    taskStatus = TaskStatus::create;

    pthread_mutex_init(&resourceMutex, NULL);
    pthread_mutex_init(&planningThreadData.conditionMutex, NULL);
    pthread_cond_init(&planningThreadData.condition, NULL);
    planningThreadData.thread = NULL;
    planningThreadData.args = NULL;
}

LatitudePlannerTask::~LatitudePlannerTask() {
    if(this->getStatus() != TaskStatus::release) release();
}

void LatitudePlannerTask::init(via::automotive::LatitudePlanner *planner, via::car::CarModel *carModel) {
    if(this->getStatus() != TaskStatus::create && this->getStatus() != TaskStatus::release) {
        LOGE("This task is active now, please release first.  %d", (int)this->getStatus() );
        throw std::runtime_error("This task is active now, please release first.");
    }

    this->planner = planner;
    this->dataBundle.carModel = *carModel;
    this->setTaskStatus(TaskStatus::init);
}

void LatitudePlannerTask::active() {
    if(this->getStatus() != TaskStatus::init && this->getStatus() != TaskStatus::release) {
        throw std::runtime_error("This task is already active, please release and reinit task.");
    }

    this->setTaskStatus(TaskStatus::active);

    // attach thread
    planningThreadData.args = this;
    if (pthread_create(&planningThreadData.thread, NULL, pthreadEntryFunc, planningThreadData.args)) {
        throw std::runtime_error("Fail to create planing thread in ");
    }
    planningThreadData.isRunning = true;
}

void LatitudePlannerTask::release() {
    if(planningThreadData.isRunning) {
        LOGE(" pthread_mutex_lock");
        pthread_mutex_lock(&planningThreadData.conditionMutex);
        this->setTaskStatus(TaskStatus::release);
        LOGE(" this->setTaskStatus(TaskStatus::release)");
        pthread_cond_signal(&planningThreadData.condition);
        pthread_mutex_unlock(&planningThreadData.conditionMutex);

        // wait thread finish
        LOGE("pthread_join");
        pthread_join(planningThreadData.thread, NULL);
        planningThreadData.thread = NULL;
    }
    else {
        this->setTaskStatus(TaskStatus::release);
        LOGE(" release.................");
    }
}

void LatitudePlannerTask::threadEntry() {

    while(this->getStatus() != LatitudePlannerTask::TaskStatus::release) {
        LatitudePlannerTask::pthreadData *threadData = this->getpThreadData();

        // wait signal to start planning
        pthread_mutex_lock(&threadData->conditionMutex);
        if(this->getStatus() != LatitudePlannerTask::TaskStatus::release) this->setTaskStatus(TaskStatus::idle);
        pthread_cond_wait(&threadData->condition, &threadData->conditionMutex);
        if(this->getStatus() != LatitudePlannerTask::TaskStatus::release) this->setTaskStatus(TaskStatus::planning);
        pthread_mutex_unlock(&threadData->conditionMutex);
        if(this->getStatus() == LatitudePlannerTask::TaskStatus::release) break;

        LatitudePlannerTask::DataBundle *dataBundle = this->getDataBundle();
        LatitudePlanner *planner = this->getPlanner();

        via::automotive::LatitudePlan result = dataBundle->latitudePlan;    // keep previous status
        long long startTime = getms();

        bool ret = planner->update(&dataBundle->carModel,
                        &dataBundle->canSpeed,
                        &dataBundle->canSteeringSensor,
                        &dataBundle->sensingLane,
                        result);
        //LOGE("planner->update !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  %d , %f" ,(int)dataBundle->sensingLane.mLaneStatus , result.planDesiredSteerAngle);
        long long endTime = getms();
        //if(ret) LOGE("latitudePlanner_ update time = %lld", endTime - startTime);

        setPlanResult(&result);
    }
    LOGE("LatitudePlannerTask release");
}

void *LatitudePlannerTask::pthreadEntryFunc(void * This) {
    ((LatitudePlannerTask *)This)->threadEntry();
    return NULL;
}

bool LatitudePlannerTask::update(via::canbus::CANParams_Speed *canSpeed,
                                via::canbus::CANParams_SteeringSensor *canSteeringSensor,
                                via::sensing::LaneSample *sensingLane) {
    bool ret = false;
    if(getStatus() == TaskStatus::idle) {
        pthread_mutex_lock(&resourceMutex);
        this->dataBundle.canSpeed = *canSpeed;
        this->dataBundle.canSteeringSensor = *canSteeringSensor;
        this->dataBundle.sensingLane = *sensingLane;
        pthread_mutex_unlock(&resourceMutex);

        LatitudePlannerTask::pthreadData *threadData = this->getpThreadData();
        pthread_mutex_lock(&threadData->conditionMutex);
        pthread_cond_signal(&threadData->condition);
        pthread_mutex_unlock(&threadData->conditionMutex);
        //LOGE("pthread_cond_signal(&threadData->condition)");
        ret = true;
    }
    return ret;
}

bool LatitudePlannerTask::isInit() {
    return this->getStatus() != TaskStatus::create;
}

LatitudePlannerTask::TaskStatus LatitudePlannerTask::getStatus(){
    return taskStatus;
}

void LatitudePlannerTask::setTaskStatus(LatitudePlannerTask::TaskStatus status) {
    taskStatus = status;
}

void LatitudePlannerTask::setPlanResult(via::automotive::LatitudePlan *result) {
    pthread_mutex_lock(&resourceMutex);
    this->dataBundle.latitudePlan = *result;
    pthread_mutex_unlock(&resourceMutex);
}

void  LatitudePlannerTask::getLastResult(via::automotive::LatitudePlan *result) {
    pthread_mutex_lock(&resourceMutex);
    *result = this->dataBundle.latitudePlan;
    pthread_mutex_unlock(&resourceMutex);
}

LatitudePlannerTask::pthreadData *LatitudePlannerTask::getpThreadData()
{
    return &planningThreadData;
}

LatitudePlannerTask::DataBundle *LatitudePlannerTask::getDataBundle() {
    return &dataBundle;
}

via::automotive::LatitudePlanner *LatitudePlannerTask::getPlanner() {
    return planner;
}
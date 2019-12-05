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

#include <jni.h>
#include <iostream>
#include <sstream>
#include <android/bitmap.h>
#include <vBus/CANbusModule.h>
#include "mobile360/CameraCoord/CameraModule.h"
#include "mobile360/SensingComponent/SensingComponent.h"
#include "mobile360/SensingComponent/SensingSamples.h"
#include "mobile360/SensingComponent/types.h"
#include "tools/TimeTag.h"
#include "jni_SensingModule.h"

#include <android/log.h>
#define  LOG_TAG    "jni_SensingModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// ---------------------------------------------------------------------------------------------------------------------------------------------------
using namespace via::canbus;
using namespace via::sensing;
using namespace via::camera;
using namespace std;

//----------------------------------------------------------------------------------------------------------------------------------------------------
namespace{
class native_section {
public:
    native_section() {
        mClass_objClass = NULL;
        mID_setSample_LaneDetect = NULL;
        mID_setSample_VehicleDetect = NULL;
        mID_setSample_BlindSpotDetect = NULL;
        mID_setSample_SpeedLimitDetect = NULL;
        mID_setSample_Environment = NULL;
        mID_setSample_setObjectSampleStatus = NULL;
        mID_setSample_AddObject = NULL;
        mID_setSample_setTrafficLightSampleStatus = NULL;
        mID_setSample_AddTrafficLightData = NULL;
        refCameraModule = NULL;
        refCANbusModule = NULL;
        obj_ = NULL;
    }

    ~native_section() {
        if (obj_ != NULL) {
            obj_->release();
            delete obj_;
            obj_ = NULL;
        }
    }

    bool init(JNIEnv *env, jobject jobj) {
        bool ret = false;
        std::ostringstream errStream;

        do {
            // Find Class
            jclass objClass = env->GetObjectClass(jobj);
            if (objClass == NULL) {
                errStream << "GetObjectClass is NULL , in " << __func__;
                break;
            } else {
                this->mClass_objClass = reinterpret_cast<jclass>(env->NewGlobalRef(objClass));
                env->DeleteLocalRef(objClass);
            }

            // Find Class methods
            this->mID_setSample_LaneDetect = env->GetMethodID(this->mClass_objClass, "setSample_LaneDetect", "(FFFI)V");
            if (mID_setSample_LaneDetect == NULL) {
                errStream << "GetMethodID is NULL : setSample_LaneDetect";
                break;
            }

            this->mID_setSample_VehicleDetect = env->GetMethodID(this->mClass_objClass, "setSample_VehicleDetect", "(IFFFFFFF)V");
            if (mID_setSample_VehicleDetect == NULL) {
                errStream << "GetMethodID is NULL : setSample_VehicleDetect";
                break;
            }

            this->mID_setSample_BlindSpotDetect = env->GetMethodID(this->mClass_objClass, "setSample_BlindSpotDetect", "(ZZZZ)V");
            if (mID_setSample_BlindSpotDetect == NULL) {
                errStream << "GetMethodID is NULL : setSample_BlindSpotDetect";
                break;
            }

            this->mID_setSample_SpeedLimitDetect = env->GetMethodID(this->mClass_objClass, "setSample_SpeedLimitDetect", "(II)V");
            if (mID_setSample_SpeedLimitDetect == NULL) {
                errStream << "GetMethodID is NULL : setSample_SpeedLimitDetect";
                break;
            }

            this->mID_setSample_Environment = env->GetMethodID(this->mClass_objClass, "setSample_Environment", "(I)V");
            if (mID_setSample_Environment == NULL) {
                errStream << "GetMethodID is NULL : setSample_Environment";
                break;
            }

            this->mID_setSample_setObjectSampleStatus = env->GetMethodID(this->mClass_objClass, "setSample_setObjectSampleStatus", "(II)V");
            if (mID_setSample_setObjectSampleStatus == NULL) {
                errStream << "GetMethodID is NULL : setSample_setObjectSampleStatus";
                break;
            }

            this->mID_setSample_AddObject = env->GetMethodID(this->mClass_objClass, "setSample_AddObjectData", "(IIFFFFFF)V");
            if (mID_setSample_AddObject == NULL) {
                errStream << "GetMethodID is NULL : setSample_AddObjectData";
                break;
            }

            this->mID_setSample_setTrafficLightSampleStatus = env->GetMethodID(this->mClass_objClass, "setSample_setTrafficLightSampleStatus", "(I)V");
            if (mID_setSample_setTrafficLightSampleStatus == NULL) {
                errStream << "GetMethodID is NULL : setSample_setTrafficLightSampleStatus";
                break;
            }

            this->mID_setSample_AddTrafficLightData = env->GetMethodID(this->mClass_objClass, "setSample_AddTrafficLightData", "(IIFFFFF)V");
            if (mID_setSample_AddTrafficLightData == NULL) {
                errStream << "GetMethodID is NULL : setSample_AddTrafficLightData";
                break;
            }

            LOGE("init native_data finish");
        } while(false);

        if(errStream.width() > 0) {
            LOGE("Error : %s , msg  %s", __func__ ,errStream.str().c_str());
        }
        else {
            ret = true;
        }

        return ret;
    }

    void release(JNIEnv *env) {
        if(mClass_objClass != NULL) env->DeleteGlobalRef(mClass_objClass);
        mClass_objClass = NULL;
    }

    jclass mClass_objClass;
    jmethodID mID_setSample_LaneDetect;
    jmethodID mID_setSample_VehicleDetect;
    jmethodID mID_setSample_BlindSpotDetect;
    jmethodID mID_setSample_SpeedLimitDetect;
    jmethodID mID_setSample_Environment;
    jmethodID mID_setSample_setObjectSampleStatus;
    jmethodID mID_setSample_AddObject;
    jmethodID mID_setSample_setTrafficLightSampleStatus;
    jmethodID mID_setSample_AddTrafficLightData;
    CameraModule *refCameraModule;
    CANbusModule *refCANbusModule;

    SensingComponent *obj_;

};
}
// ---------------------------------------------------------------------------------------------------------------------------------------------------
/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_create
 * Signature: (IJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_sensing_SensingModule_jni_1create
        (JNIEnv *env, jobject jobj, jint detectorModuleFlags, jlong cameraModuleAddress, jlong canbusModuleAddress)
{
    bool isValid = false;
    native_section *nData = new native_section();

    if(nData != NULL) {
        if (nData->init(env, jobj)) {
            nData->obj_ = new SensingComponent();
            if (nData->obj_ != NULL) {
                nData->refCameraModule = (CameraModule *) cameraModuleAddress;
                nData->refCANbusModule = (CANbusModule *) canbusModuleAddress;
                nData->obj_->create((u_int32_t) detectorModuleFlags, nData->refCameraModule);
                isValid = true;
            }
        }
        else {
            LOGE("module native init fail. in %s", __func__);
        }
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    if(!isValid && nData != NULL) {
        delete nData;
        nData = NULL;
    }

    return (jlong) nData;
}
/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_init
 * Signature: (IJJLjava/lang/String;)J
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1init
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jstring cfgPath)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        const char *cPath = (cfgPath == NULL) ? NULL : env->GetStringUTFChars(cfgPath, NULL);

        std::string sdtPath = cPath;
        bool success = module->init(sdtPath);
        if(cPath != NULL) env->ReleaseStringUTFChars(cfgPath, cPath);

        if(success) ret = JNI_TRUE;
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_release
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_viatech_sensing_SensingModule_jni_1release
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jint detectorModuleFlags)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        LOGE("Java_com_viatech_sensing_SensingModule_jni_1release");
        SensingComponent *module = nData->obj_;
        module->release();
        delete module;
    }
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_getFrameQueueCount
 * Signature: (JJ)Z
 */
JNIEXPORT jint JNICALL Java_com_viatech_sensing_SensingModule_jni_1getFrameQueueCount
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    jint ret = 0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        ret = (jint) module->getFrameBufferQueueCount();
    }
    return ret;
}


/*
 * Class:     com_viatech_sensing_SensingModule
 * Method:    jni_detect
 * Signature: (JZIFFZZ)I
 */
JNIEXPORT jint JNICALL Java_com_viatech_sensing_SensingModule_jni_1detect
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
                jboolean dumpDebugInfo,
                jboolean externalCANBus,
                jint speed,
                jfloat steeringAngle,
                jfloat steeringRatio,
                jboolean dirIndicatorOn_L,
                jboolean dirIndicatorOn_R)
{
    jint ret = (jint)SensingComponent::ComponentMode::Idle;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        //CANbusModule *refCANbusModule = (CANbusModule *)canbusModuleAddress;
        via::sensing::SignalBundle signalBundle;
        CANbusModule *refCANbusModule = nData->refCANbusModule;
        CANParams_Speed canSpeed;
        CANParams_DriverControllers canDriverCtls;
        CANParams_SteeringSensor canSteerSensor;
        if(refCANbusModule != NULL) {
            refCANbusModule->Rx_SpeedParams(canSpeed);
            refCANbusModule->Rx_DriverControllers(canDriverCtls);
            refCANbusModule->Rx_SteeringSensorParams(canSteerSensor);
            signalBundle.isCANBusOnline_ = refCANbusModule->isDongleConnected();
            signalBundle.speed_ = canSpeed.roughSpeed;
            signalBundle.isDirIndicatorOn_L_ = canDriverCtls.leftBlinkerOn;
            signalBundle.isDirIndicatorOn_R_ = canDriverCtls.rightBlinkerOn;
            signalBundle.steerAngle_ = canSteerSensor.steerAngle;
        }
        else {
            signalBundle.isCANBusOnline_ = false;
        }

        if(externalCANBus) {
            signalBundle.isCANBusOnline_ = (bool)externalCANBus;
            signalBundle.speed_ = speed;
            signalBundle.steerAngle_ = steeringAngle;
            signalBundle.steeringRatio = steeringRatio;
            signalBundle.isDirIndicatorOn_L_ = dirIndicatorOn_L;
            signalBundle.isDirIndicatorOn_R_ = dirIndicatorOn_R;
        }

        SensingComponent *module = nData->obj_;

        module->dumpTime(dumpDebugInfo, 1000);
        module->detect(signalBundle);
        ret = (jint)module->getComponentMode();

        std::ostringstream errStream;
        do {
            unsigned int detectorModuleFlags = module->getDetectors();

            // Update data to JAVA.
            // Find Class
            jclass objClass = env->GetObjectClass(jobj);
            if (objClass == NULL) {
                errStream << "GetObjectClass is NULL , in " << __func__;
                break;
            }

            // Find Class methods
            if(detectorModuleFlags & (u_int32_t)DetectorTypes::LDW) {
                via::sensing::LaneSample *sample = (via::sensing::LaneSample *)module->getSample(SampleTypes::Lane);
                env->CallVoidMethod(jobj, nData->mID_setSample_LaneDetect,
                                    (jfloat)sample->mFrameSize.width, (jfloat)sample->mFrameSize.height,
                                    (jfloat)sample->mLaneWidth , (jint)sample->mLaneStatus);
            }

            if((detectorModuleFlags & (u_int32_t)DetectorTypes::FCW) || (detectorModuleFlags & (u_int32_t)DetectorTypes::FCW_DL)) {
                    via::sensing::VehicleSample *sample = (via::sensing::VehicleSample *)module->getSample(SampleTypes::ForwardVehicle);
                    env->CallVoidMethod(jobj, nData->mID_setSample_VehicleDetect,
                                        (jint)sample->mForwardVehicle.mObjTypeIndex,
                                        (jfloat)sample->mForwardVehicle.mScore,
                                        (jfloat)sample->mForwardVehicle.mMinAnchor.x, (jfloat)sample->mForwardVehicle.mMinAnchor.y,
                                        (jfloat)sample->mForwardVehicle.mMaxAnchor.x, (jfloat)sample->mForwardVehicle.mMaxAnchor.y,
                                        (jfloat)sample->mForwardVehicle.mDistance,
                                        (jfloat)sample->mForwardVehicle.mReactionTime);

            }

            if((detectorModuleFlags & (u_int32_t)DetectorTypes::BSD_L) || (detectorModuleFlags & (u_int32_t)DetectorTypes::BSD_R)) {
                jboolean isFromLeft = JNI_FALSE;
                jboolean isFromRight = JNI_FALSE;
                if(detectorModuleFlags & (u_int32_t)DetectorTypes::BSD_L) isFromLeft = JNI_TRUE;
                if(detectorModuleFlags & (u_int32_t)DetectorTypes::BSD_R) isFromRight = JNI_TRUE;

                if(isFromLeft == JNI_TRUE) {
                    via::sensing::BlindSpotSample_L *sampleL = (via::sensing::BlindSpotSample_L *)module->getSample(SampleTypes::BlindSpot_L);
                    env->CallVoidMethod(jobj, nData->mID_setSample_BlindSpotDetect,
                                        (jboolean)sampleL->isWarning, JNI_FALSE, isFromLeft, isFromRight);
                }
                else {
                    via::sensing::BlindSpotSample_R *sampleR = (via::sensing::BlindSpotSample_R *) module->getSample(SampleTypes::BlindSpot_R);
                    env->CallVoidMethod(jobj, nData->mID_setSample_BlindSpotDetect,
                                        JNI_FALSE, (jboolean)sampleR->isWarning, isFromLeft, isFromRight);
                }
            }

            if((detectorModuleFlags & (u_int32_t)DetectorTypes::SLD)) {
                via::sensing::SpeedLimitSample *sample = (via::sensing::SpeedLimitSample *)module->getSample(SampleTypes::SpeedLimit);
                env->CallVoidMethod(jobj, nData->mID_setSample_SpeedLimitDetect,
                                    (jint)sample->mSpeedLimit_1, (jint)sample->mSpeedLimit_2);
            }

            if(detectorModuleFlags & (u_int32_t)DetectorTypes::Weather) {
                via::sensing::EnvironmentSample *sample = (via::sensing::EnvironmentSample *)module->getSample(SampleTypes::Environment);
                env->CallVoidMethod(jobj, nData->mID_setSample_Environment,
                                    (jint)sample->mWeatherType);

            }

            if(detectorModuleFlags & (u_int32_t)DetectorTypes::FCW_DL) {
                via::sensing::ObjectDetectSample *Lsample = (via::sensing::ObjectDetectSample *)module->getSample(SampleTypes::Object_DL);
                env->CallVoidMethod(jobj, nData->mID_setSample_setObjectSampleStatus,
                                    (jint)Lsample->mSampleCount,
                                    (jint)Lsample->mFocusObjectId);

                via::sensing::ObjectDetectSample *sample = (via::sensing::ObjectDetectSample *)module->getSample(SampleTypes::Object_DL);
                for(int i = 0; i < via::sensing::ObjectDetectSample::MAX_SAMPLE_COUNT && i < sample->mSampleCount; i++) {
                    env->CallVoidMethod(jobj, nData->mID_setSample_AddObject,
                                        (jint)i,
                                        (jint)sample->mObjectDataList[i].mObjTypeIndex,
                                        (jfloat)sample->mObjectDataList[i].mScore,
                                        (jfloat)sample->mObjectDataList[i].mMinAnchor.x,
                                        (jfloat)sample->mObjectDataList[i].mMinAnchor.y,
                                        (jfloat)sample->mObjectDataList[i].mMaxAnchor.x,
                                        (jfloat)sample->mObjectDataList[i].mMaxAnchor.y,
                                        (jfloat)sample->mObjectDataList[i].mDistance);
                }
            }

            if(detectorModuleFlags & (u_int32_t)DetectorTypes::TLD) {
                via::sensing::TrafficLightDetectSample *Lsample = (via::sensing::TrafficLightDetectSample *)module->getSample(SampleTypes::TrafficLight);
                env->CallVoidMethod(jobj, nData->mID_setSample_setTrafficLightSampleStatus,
                                    (jint)Lsample->mSampleCount);

                via::sensing::TrafficLightDetectSample *sample = (via::sensing::TrafficLightDetectSample *)module->getSample(SampleTypes::TrafficLight);
                for(int i = 0; i < TrafficLightDetectSample::MAX_SAMPLE_COUNT && i < sample->mSampleCount; i++) {
                    env->CallVoidMethod(jobj, nData->mID_setSample_AddTrafficLightData,
                                        (jint)i,
                                        (jint)sample->mTrafficLightList[i].mObjTypeIndex,
                                        (jfloat)sample->mTrafficLightList[i].mScore,
                                        (jfloat)sample->mTrafficLightList[i].mMinAnchor.x,
                                        (jfloat)sample->mTrafficLightList[i].mMinAnchor.y,
                                        (jfloat)sample->mTrafficLightList[i].mMaxAnchor.x,
                                        (jfloat)sample->mTrafficLightList[i].mMaxAnchor.y);
                }
            }
        } while(false);

        if(errStream.width() > 0) {
            LOGE("Error : %s , msg  %s", __func__ ,errStream.str().c_str());
        }

    }
    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_updateCameraModule
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1updateCameraModule
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jlong cameraAddr, jint detectorModuleFlags)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && cameraAddr != 0) {
        SensingComponent *module = nData->obj_;
        CameraModule *cameraModule = (CameraModule *) cameraAddr;
        if(module->updateCamera(cameraModule) == true) {
            ret = JNI_TRUE;
        }
    }
    return ret;
}


/*
 * Class:     com_viatech_sensing_SensingModule
 * Method:    jni_getCalibrationStatus
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_viatech_sensing_SensingModule_jni_1getCalibrationStatus
        (JNIEnv *, jobject, jlong moduleAddr)
{
    jint ret = 0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        OnTheFlyCalibrationSample *calibSample = (OnTheFlyCalibrationSample *)module->getSample(SampleTypes::Calibration);
        ret = static_cast<int>(calibSample->mStatus);
    }
    return ret;
}


/*
 * Class:     com_viatech_sensing_SensingModule
 * Method:    jni_restoreConfiguration
 * Signature: (JLjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1restoreConfiguration
        (JNIEnv *env, jobject, jlong moduleAddr, jstring jpath)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        const char *cPath = (jpath == NULL) ? NULL : env->GetStringUTFChars(jpath, NULL);
        std::string sPath = cPath;

        bool success = module->exportConfig(sPath);
        if(success) ret = JNI_TRUE;

        // release
        if(cPath != NULL) env->ReleaseStringUTFChars(jpath, cPath);
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame
 * Signature: (JJIIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame_1nativeAddress
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jlong bufferAddress,
         jint frameFormat, jint frameWidth, jint frameHeight,
         jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        media::FrameFormatType frameFormatType = (media::FrameFormatType)frameFormat;

        std::ostringstream errStream;
        int err = 0;
        switch(frameFormatType) {
            case media::FrameFormatType::FrameFmt_ARGB8888:
                err = module->bufferFrame_RGBA8888((void *) bufferAddress, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
                break;
            case media::FrameFormatType::FrameFmt_NV12:
                err = module->bufferFrame_NV12((void *)bufferAddress, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
                break;
            default:
                errStream << "Unsupport frame formate " << frameFormat ;
                LOGE("Error : in %s , msg  %s", __func__ ,errStream.str().c_str());
                break;
        }

        if(err == 1) ret = JNI_TRUE;
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame
 * Signature: (JLjava/nio/ByteBuffer;IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame_1directByteBuffer
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jobject nativBuffer,
         jint frameFormat, jint frameWidth, jint frameHeight,
         jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        media::FrameFormatType frameFormatType = (media::FrameFormatType)frameFormat;
        void *pFrameData = env->GetDirectBufferAddress(nativBuffer);

        std::ostringstream errStream;
        int err = 0;
        switch(frameFormatType) {
            case media::FrameFormatType::FrameFmt_ARGB8888:
                err = module->bufferFrame_RGBA8888((void *)pFrameData, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
                break;
            case media::FrameFormatType::FrameFmt_NV12:
                err = module->bufferFrame_NV12((void *)pFrameData, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
                break;
            default:
                errStream << "Unsupport frame formate " << frameFormat ;
                LOGE("Error : in %s , msg  %s", __func__ ,errStream.str().c_str());
                break;
        }

        if(err == 1) ret = JNI_TRUE;
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame
 * Signature: (JLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIIIIIIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
         jbyteArray yFramePtr, jbyteArray uFramePtr, jbyteArray vFramePtr,
         jint frameWidth, jint frameHeight,
         jint yStepStride, jint uStepStride, jint vStepStride,
         jint yPixelStride, jint uPixelStride, jint vPixelStride,
         jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    //AdasDebug("FPS", "Java_com_viatech_sensing_SensingModule_jni_1bufferFrame");
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        unsigned char *yFrame = (unsigned char *)env->GetDirectBufferAddress(yFramePtr);
        unsigned char *uFrame = (unsigned char *)env->GetDirectBufferAddress(uFramePtr);
        unsigned char *vFrame = (unsigned char *)env->GetDirectBufferAddress(vFramePtr);
        SensingComponent *module = nData->obj_;

        int err = module->bufferFrame(yFrame, uFrame, vFrame,
                                      frameWidth, frameHeight,
                                      yStepStride, uStepStride, vStepStride,
                                      yPixelStride, uPixelStride, vPixelStride,
                                      roiX, roiY, roiWidth, roiHeight);
        if(err == 1) ret = JNI_TRUE;
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame_NV12
 * Signature: (JJIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame_1NV12
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        int err = module->bufferFrame_NV12((void *)pFrameData, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
        if(err == 1) ret = JNI_TRUE;
    }

    return ret;
}


/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame_ARGB8888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame_1ARGB8888
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        int err = module->bufferFrame_RGBA8888((void *)pFrameData, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
        if(err == 1) ret = JNI_TRUE;
    }
    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_bufferFrame_bitmap
 * Signature: (JLjava/lang/Object;IIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1bufferFrame_1bitmap
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jobject jBitmap, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        do {
            SensingComponent *module = nData->obj_;
            AndroidBitmapInfo bmpInfo = {0};
            u_char *ptr = NULL;

            if (module == NULL) break;
            if (AndroidBitmap_getInfo(env, jBitmap, &bmpInfo) < 0) break;
            if (AndroidBitmap_lockPixels(env, jBitmap, (void **) &ptr)) break;

            int err = 0;
            switch (bmpInfo.format) {
                case AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGBA_8888:
                    err = module->bufferFrame_RGBA8888((void *) ptr, bmpInfo.width, bmpInfo.height, roiX, roiY, roiWidth, roiHeight);
                    break;
                default:
                    LOGE("Non support AndroidBitmapFormat : %d", bmpInfo.format);
                    break;
            }
            AndroidBitmap_unlockPixels(env, jBitmap);

            if (err == 1) ret = JNI_TRUE;
        } while (false);
    }
    return ret;
}
/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_setRuntimeLoadableData
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1setRuntimeLoadableData
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jint type, jstring path, jstring name)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;
        const char *cPath = (path == NULL) ? NULL : env->GetStringUTFChars(path, NULL);
        const char *cName = (name == NULL) ? NULL : env->GetStringUTFChars(name, NULL);

        bool success = module->setRuntimeLoadableData((RuntimeLoadDataTypes)type,  cPath, cName);
        if(success) ret = JNI_TRUE;

        // release
        if(cPath != NULL) env->ReleaseStringUTFChars(path, cPath);
        if(cName != NULL) env->ReleaseStringUTFChars(name, cName);
    }

    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_getConfiguration
 * Signature: (JI)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_viatech_sensing_SensingModule_jni_1getConfiguration
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jint)
{
    jstring ret = NULL;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {

    }
    return ret;
}
/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_getModuleNativeAddress
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_sensing_SensingModule_jni_1getModuleNativeAddress
        (JNIEnv *, jobject, jlong moduleAddr)
{
    jlong ret = 0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        ret = (jlong)nData->obj_;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_registerRelatedModule
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1registerRelatedModule
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jlong relModuleAddr, jint relatedDetector)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && relModuleAddr != 0) {
        SensingComponent *module = nData->obj_;
        SensingComponent *relmodule = (SensingComponent *) relModuleAddr;
        if(module->registerRelatedComponent(relmodule, (u_int32_t)relatedDetector) == true) {
            ret = JNI_TRUE;
        }
    }
    return ret;
}


/*
 * Class:     com_via_adas_sensing_SensingModule
 * Method:    jni_enableOnTheFlyCalibration
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1enableOnTheFlyCalibration
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jfloat cameraInstalledHeight, jfloat cameraToCenterOffset)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        SensingComponent *module = nData->obj_;

        module->doAutoCalibration(cameraInstalledHeight, cameraToCenterOffset);
    }
    return ret;
}


/*
 * Class:     com_viatech_sensing_SensingModule
 * Method:    jni_updateLaneData
 * Signature: (J[F[F[F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_sensing_SensingModule_jni_1updateLaneData
        (JNIEnv *env, jobject obj, jlong moduleAddr, jfloatArray jLaneL, jfloatArray jLaneR, jfloatArray jScores)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        jfloat *cLaneL = NULL;
        jfloat *cLaneR = NULL;
        jfloat *cScores = NULL;

        do {
            SensingComponent *module = nData->obj_;
            LaneSample *laneSample = (LaneSample *)module->getSample(SampleTypes::Lane);

            // Check array size
            jint lenLaneL = env->GetArrayLength(jLaneL);
            jint lenLaneR = env->GetArrayLength(jLaneR);
            jint lenScores = env->GetArrayLength(jScores);

            if((lenLaneL != (LaneSample::RESAMPLE_COUNT * 2)) ||
               (lenLaneR != (LaneSample::RESAMPLE_COUNT * 2)) ||
               (lenScores != 2)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }
            // get a pointer to the array
            cLaneL = env->GetFloatArrayElements(jLaneL, 0);
            if (cLaneL == NULL) break;

            cLaneR = env->GetFloatArrayElements(jLaneR, 0);
            if (cLaneR == NULL) break;

            cScores = env->GetFloatArrayElements(jScores, 0);
            if (cScores == NULL) break;

            // copy data
            jfloat *pcLaneL = cLaneL;
            jfloat *pcLaneR = cLaneR;
            for(int i = 0 ; i < LaneSample::RESAMPLE_COUNT ; i++) {
                *pcLaneL++ = (jfloat)laneSample->mLaneAnchor_L[i].x;
                *pcLaneL++ = (jfloat)laneSample->mLaneAnchor_L[i].y;
                *pcLaneR++ = (jfloat)laneSample->mLaneAnchor_R[i].x;
                *pcLaneR++ = (jfloat)laneSample->mLaneAnchor_R[i].y;
            }

            cScores[0] =  (jfloat)laneSample->mLaneModelCtx.prob_L;
            cScores[1] =  (jfloat)laneSample->mLaneModelCtx.prob_R;
        } while(false);

        if(cLaneL != NULL) env->ReleaseFloatArrayElements(jLaneL, cLaneL, 0);
        if(cLaneR != NULL) env->ReleaseFloatArrayElements(jLaneR, cLaneR, 0);
        if(cScores != NULL) env->ReleaseFloatArrayElements(jScores, cScores, 0);

        return ret;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}
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

#include <memory>
#include <automotiveControl/AutomotiveController.h>
#include "jni_AutomotiveController.h"

#include <android/log.h>
#define  LOG_TAG    "jni_SensingModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//----------------------------------------------------------------------------------------------------------------------------------------------------
using namespace via::automotive;
using namespace via::sensing;
using namespace via::camera;
using namespace via::car;
using namespace via::canbus;

//----------------------------------------------------------------------------------------------------------------------------------------------------
namespace{
class native_section {
public:
    native_section() {
        mClass_objClass = NULL;
        mID_updateLatitudePlan = NULL;
        mID_updateEvent = NULL;
        module = NULL;
    }

    ~native_section() {
        if (module != NULL) {
            module->release();
            delete module;
            module = NULL;
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
            this->mID_updateLatitudePlan = env->GetMethodID(this->mClass_objClass, "updateLatitudePlan", "(FFZZZ)V");
            if (this->mID_updateLatitudePlan == NULL) {
                errStream << "GetMethodID is NULL : setSample_LaneDetect";
                break;
            }

            this->mID_updateEvent = env->GetMethodID(this->mClass_objClass, "updateEvent", "(IID)V");
            if (this->mID_updateEvent == NULL) {
                errStream << "GetMethodID is NULL : setSample_LaneDetect";
                break;
            }

            LOGE("init native_data finish -------------------------------------------------------------------------------");
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
    jmethodID mID_updateLatitudePlan ;
    jmethodID mID_updateEvent;
    AutomotiveController *module;

};
}
//----------------------------------------------------------------------------------------------------------------------------------------------------
/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_create
 * Signature: (ILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1create
  (JNIEnv *env, jobject obj, jint carType, jstring jCfgPath, jstring jCalibExportPath, jfloat cameraInstalledHeight, jfloat cameraToCenterOffset)
{
    native_section *nData = new native_section();
    bool isValid = false;

    if(nData != NULL) {
        if(nData->init(env, obj)) {
            nData->module = new AutomotiveController();
            if (nData->module != NULL) {
                const char *cPath = (jCfgPath == NULL) ? NULL : env->GetStringUTFChars(jCfgPath, NULL);
                const char *cExportPath = (jCalibExportPath == NULL) ? NULL : env->GetStringUTFChars(jCalibExportPath, NULL);
                std::string stdPath = cPath;
                std::string stdExportPath = cExportPath;

                nData->module->init((via::car::CarTypes) carType, stdPath, stdExportPath, cameraInstalledHeight, cameraToCenterOffset);
                isValid = true;

                if (cPath != NULL) env->ReleaseStringUTFChars(jCfgPath, cPath);
                if (cExportPath != NULL) env->ReleaseStringUTFChars(jCalibExportPath, cExportPath);
            }
            else {
                LOGE("module allocation fail. in %s", __func__);
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

    return (jlong)nData;
}

/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_release
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1release
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->release(env);
        delete nData;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}


/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_stopRecord
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1stopRecord
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(moduleAddr != 0) {
        AutomotiveController *module = nData->module;
        std::unique_ptr<ControllerCommand> cmd (new StopRecordCommand());
        module->pushCommand(cmd);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_startRecord
 * Signature: (JLjava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1startRecord
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jstring path, jboolean appendFile)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        const char *cPath = (path == NULL) ? NULL : env->GetStringUTFChars(path, NULL);
        std::string stdPath = cPath;

        AutomotiveController *module = nData->module;
        std::unique_ptr<ControllerCommand> cmd (new StartRecordCommand(stdPath, (bool)appendFile));
        module->pushCommand(cmd);
        ret = (jboolean)module->isRecording();

        if(cPath != NULL) env->ReleaseStringUTFChars(path, cPath);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}


/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_runAutoControl
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1runAutoControl__J
  (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        AutomotiveController *module = nData->module;
        EventSlot event = module->runAutoControl();

        // Update data to JAVA
        std::ostringstream errStream;
        do {
            // Find Class methods
            LatitudePlan *latitudePlan = module->getLatitudePlan();
            env->CallVoidMethod(jobj, nData->mID_updateLatitudePlan,
                                (jfloat)latitudePlan->planStartSteerAngle,
                                (jfloat)latitudePlan->planDesiredSteerAngle,
                                (jboolean)latitudePlan->isPlanValid,
                                (jboolean)latitudePlan->isSteerControllable,
                                (jboolean)latitudePlan->isSteerOverControl);

            env->CallVoidMethod(jobj, nData->mID_updateEvent,
                                (jint)event.type,
                                (jint)event.level,
                                (jdouble)event.time);
        } while(false);

        if(errStream.width() > 0) {
            LOGE("Error : %s , msg  %s", __func__ ,errStream.str().c_str());
        }
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}


/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_registerCANbusModule
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1registerCANbusModule
  (JNIEnv *env, jobject obj, jlong moduleAddr, jlong refAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        AutomotiveController *module = nData->module;
        CANbusModule *caNbusModule = (CANbusModule *)refAddr;

        module->registerCANbusModule(caNbusModule);
        ret = JNI_TRUE;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}



/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_registerCameraModule
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1registerCameraModule
        (JNIEnv *env, jobject obj, jlong moduleAddr, jlong refAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        AutomotiveController *module = nData->module;
        CameraModule *cameraModule = (CameraModule *)refAddr;

        module->registerCameraModule(cameraModule);
        ret = JNI_TRUE;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_registerSensingModule_Lane
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1registerSensingModule_1Lane
  (JNIEnv *env, jobject obj, jlong moduleAddr, jlong refAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        AutomotiveController *module = nData->module;
        SensingComponent *sensingComponent = (SensingComponent *)refAddr;

        module->registerSensingModule_Lane(sensingComponent);
        ret = JNI_TRUE;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}


/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_registerSensingModule_ForwardVehicle
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1registerSensingModule_1ForwardVehicle
  (JNIEnv *env, jobject obj, jlong moduleAddr, jlong refAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        AutomotiveController *module = nData->module;
        SensingComponent *sensingComponent = (SensingComponent *)refAddr;

        module->registerSensingModule_ForwardVehicle(sensingComponent);
        ret = JNI_TRUE;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}



/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_updateLatitudePlan
 * Signature: (J[F[F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1updateLatitudePlanTrajectory
        (JNIEnv *env, jobject obj, jlong moduleAddr, jfloatArray jTrajectoryL, jfloatArray jTrajectoryR)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        jfloat *cTrajectoryL = NULL;
        jfloat *cTrajectoryR = NULL;

        do {
            AutomotiveController *module = nData->module;
            LatitudePlan *latitudePlan = module->getLatitudePlan();

            // Check array size
            jint lenTrajectoryL = env->GetArrayLength(jTrajectoryL);
            jint lenTrajectoryR = env->GetArrayLength(jTrajectoryR);
            if(lenTrajectoryL != (LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2)) {
                LOGE("jni_updateLatitudePlanTrajectory , Array size not match");
                break;
            }
            if(lenTrajectoryR != (LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2)) {
                LOGE("jni_updateLatitudePlanTrajectory , Array size not match");
                break;
            }

            // get a pointer to the array
            cTrajectoryL = env->GetFloatArrayElements(jTrajectoryL, NULL);
            if (cTrajectoryL == NULL) break;

            cTrajectoryR = env->GetFloatArrayElements(jTrajectoryR, NULL);
            if (cTrajectoryR == NULL) break;

            // copy data
            jfloat *pcTrajectoryL = cTrajectoryL;
            jfloat *pcTrajectoryR = cTrajectoryR;
            for(int i = 0 ; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                *pcTrajectoryL++ = (jfloat)latitudePlan->mTrajectory_L[i].x;
                *pcTrajectoryL++ = (jfloat)latitudePlan->mTrajectory_L[i].y;
                *pcTrajectoryR++ = (jfloat)latitudePlan->mTrajectory_R[i].x;
                *pcTrajectoryR++ = (jfloat)latitudePlan->mTrajectory_R[i].y;
            }
        } while(false);

        if(cTrajectoryL != NULL) env->ReleaseFloatArrayElements(jTrajectoryL, cTrajectoryL, 0);
        if(cTrajectoryR != NULL) env->ReleaseFloatArrayElements(jTrajectoryR, cTrajectoryR, 0);

        return ret;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_updateLatitudePlanLaneData
 * Signature: (J[F[F[F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1updateLatitudePlanLaneData
        (JNIEnv *env, jobject obj, jlong moduleAddr, jfloatArray jLaneL, jfloatArray jLaneR, jfloatArray jScores)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        jfloat *cLaneL = NULL;
        jfloat *cLaneR = NULL;
        jfloat *cScores = NULL;

        do {
            AutomotiveController *module = nData->module;
            LatitudePlan *latitudePlan = module->getLatitudePlan();

            // Check array size
            jint lenLaneL = env->GetArrayLength(jLaneL);
            jint lenLaneR = env->GetArrayLength(jLaneR);
            jint lenScores = env->GetArrayLength(jScores);

            if((lenLaneL != (LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2)) ||
               (lenLaneR != (LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT * 2)) ||
               (lenScores != 2)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }
            // get a pointer to the array
            cLaneL = env->GetFloatArrayElements(jLaneL, NULL);
            if (cLaneL == NULL) break;

            cLaneR = env->GetFloatArrayElements(jLaneR, NULL);
            if (cLaneR == NULL) break;

            cScores = env->GetFloatArrayElements(jScores, NULL);
            if (cScores == NULL) break;

            // copy data
            jfloat *pcLaneL = cLaneL;
            jfloat *pcLaneR = cLaneR;
            for(int i = 0 ; i < LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                *pcLaneL++ = (jfloat)latitudePlan->mLaneAnchors_L[i].x;
                *pcLaneL++ = (jfloat)latitudePlan->mLaneAnchors_L[i].y;
                *pcLaneR++ = (jfloat)latitudePlan->mLaneAnchors_R[i].x;
                *pcLaneR++ = (jfloat)latitudePlan->mLaneAnchors_R[i].y;
            }

            cScores[0] =  (jfloat)latitudePlan->mLaneScore_L;
            cScores[1] =  (jfloat)latitudePlan->mLaneScore_R;
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

/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_enableCameraCalibration
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1runCameraCalibration
        (JNIEnv *env, jobject obj, jlong moduleAddr, jint cameraLocation, jfloat cameraInstalledHeight, jfloat cameraToCenterOffset)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        via::camera::CameraLocationTypes locationType = (via::camera::CameraLocationTypes)cameraLocation;
        AutomotiveController *module = nData->module;

        std::unique_ptr<ControllerCommand> cmd (new StartCameraExtrinsicCalibrationCommand(locationType, (float)cameraInstalledHeight, (float)cameraToCenterOffset));
        module->pushCommand(cmd);
        //module->toggleCameraCalibration(locationType, cameraInstalledHeight, true);

        ret = JNI_TRUE;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}



/*
 * Class:     com_via_adas_automotive_AutomotiveController
 * Method:    jni_setCtl_SteeringControl
 * Signature: (JZJ)V
 */
JNIEXPORT void JNICALL Java_com_viatech_automotive_AutomotiveController_jni_1setCtl_1SteeringControl
        (JNIEnv *env, jobject obj, jlong moduleAddr, jboolean steerTorqueRequest, jlong steerTorque)
{
    native_section *nData = (native_section *)moduleAddr;
    if(nData != NULL) {
        AutomotiveController *module = nData->module;

        std::unique_ptr<ControllerCommand> cmd (new SetCruiseSpeedCommand((int)steerTorque));
        module->pushCommand(cmd);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

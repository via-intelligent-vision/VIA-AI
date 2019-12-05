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
#include <memory>
#include <iostream>
#include <sstream>
#include "jni_CANbusModule.h"
#include <vBus/CANbusModule.h>

#include <android/log.h>
#define  LOG_TAG    "jni_CANbusModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::canbus;
using namespace via::car;
using namespace std;

//----------------------------------------------------------------------------------------------------------------------------------------------------
namespace {

class native_section {
public:
    native_section() {
        mClass_objClass = NULL;
        mID_setCANData_CANHealth = NULL;
        mID_setCANdata_Speed = NULL;
        mID_setCANdata_SteeringSensor = NULL;
        mID_setCANdata_SteeringControl = NULL;
        mID_setCANdata_LKASHud = NULL;
        mID_setCANdata_ACCHud = NULL;
        mID_setCANdata_SafetyFeature = NULL;
        mID_setCANdata_DriverControllers = NULL;
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
            mID_setCANData_CANHealth = env->GetMethodID(this->mClass_objClass, "setCANData_CANHealth", "(ZZ)V");
            if (mID_setCANData_CANHealth == NULL) {
                errStream << "GetMethodID is NULL : setCANData_CANHealth";
            }

            mID_setCANdata_Speed = env->GetMethodID(this->mClass_objClass, "setCANdata_Speed", "(FFFFFF)V");
            if (mID_setCANdata_Speed == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_Speed";
            }

            mID_setCANdata_SteeringSensor = env->GetMethodID(this->mClass_objClass, "setCANdata_SteeringSensor", "(FFBZ)V");
            if (mID_setCANdata_SteeringSensor == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_SteeringSensor";
            }

            mID_setCANdata_SteeringControl = env->GetMethodID(this->mClass_objClass, "setCANdata_SteeringControl", "(ZJ)V");
            if (mID_setCANdata_SteeringControl == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_SteeringControl";
            }

            mID_setCANdata_LKASHud = env->GetMethodID(this->mClass_objClass, "setCANdata_LKASHud", "(BBB)V");
            if (mID_setCANdata_LKASHud == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_LKASHud";
            }

            mID_setCANdata_ACCHud = env->GetMethodID(this->mClass_objClass, "setCANdata_ACCHud", "(ZI)V");
            if (mID_setCANdata_ACCHud == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_ACCHud";
            }

            mID_setCANdata_SafetyFeature = env->GetMethodID(this->mClass_objClass, "setCANdata_SafetyFeature", "(ZZZZZ)V");
            if (mID_setCANdata_SafetyFeature == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_SafetyFeature";
            }

            mID_setCANdata_DriverControllers = env->GetMethodID(this->mClass_objClass, "setCANdata_DriverControllers", "(ZZB)V");
            if (mID_setCANdata_DriverControllers == NULL) {
                errStream << "GetMethodID is NULL : setCANdata_DriverControllers";
            }

            LOGE("init native_data finish -------------------------------------------------------------------------------");
        } while (false);

        if (errStream.width() > 0) {
            LOGE("Error : %s , msg  %s", __func__, errStream.str().c_str());
        }
        else {
            ret = true;
        }

        return ret;
    }

    void release(JNIEnv *env) {
        if (mClass_objClass != NULL) env->DeleteGlobalRef(mClass_objClass);
        mClass_objClass = NULL;
    }

    jclass mClass_objClass;
    jmethodID mID_setCANData_CANHealth;
    jmethodID mID_setCANdata_Speed;
    jmethodID mID_setCANdata_SteeringSensor;
    jmethodID mID_setCANdata_SteeringControl;
    jmethodID mID_setCANdata_LKASHud;
    jmethodID mID_setCANdata_ACCHud;
    jmethodID mID_setCANdata_SafetyFeature;
    jmethodID mID_setCANdata_DriverControllers;
    CANbusModule *module;
};

}   // namespace

//----------------------------------------------------------------------------------------------------------------------------------------------------
/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_getModuleNativeAddress
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_vBus_CANbusModule_jni_1getModuleNativeAddress
        (JNIEnv *, jobject, jlong moduleAddr)
{
    jlong ret = 0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        ret = (jlong)nData->module;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_init
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_vBus_CANbusModule_jni_1init
  (JNIEnv *env, jobject obj, jint carType, jint dongleType)
{
    native_section *nData = new native_section();
    bool isValid = false;

    if(nData != NULL) {
        if(nData->init(env, obj)) {
            nData->module = new CANbusModule();
            if (nData->module != NULL) {
                if (nData->module->init((CarTypes) carType, (CANDongleTypes) dongleType)) {
                    isValid = true;
                }
                else {
                    LOGE("module init fail. carType %d, dongleType %d ", (int)carType, (int)dongleType);
                }
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

    if(!isValid) {
        delete nData;
        nData = NULL;
    }

    return (jlong)nData;
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1release
  (JNIEnv *env, jobject obj, jlong moduleAddr)
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
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_exec
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1exec
  (JNIEnv *env, jobject obj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        nData->module->exec();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_isConnected
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_vBus_CANbusModule_jni_1isConnected
        (JNIEnv *env, jobject obj, jlong moduleAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        ret = (jboolean) nData->module->isDongleConnected();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_connect
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_vBus_CANbusModule_jni_1connect
        (JNIEnv *env, jobject obj, jlong moduleAddr, jint nativeDevFileDescriptor)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        DevFileDataBundle bundle;
        bundle.nativeDevFileDescriptor = nativeDevFileDescriptor;
        ret = (jboolean)nData->module->connectDongle((ConnectDataBundle *)&bundle);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_refresh
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1refresh
  (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        // Get JAVA method
        std::ostringstream errStream;
        do {
            // Get all data from module.
            CANHealth param_CANHealth;
            nData->module->Rx_CANHealth(param_CANHealth);
            LOGE("param_CANHealth %d %d", (int)param_CANHealth.controlsAllowed, (int)param_CANHealth.dongleConnected);
            env->CallVoidMethod(jobj, nData->mID_setCANData_CANHealth,
                                (jboolean)param_CANHealth.controlsAllowed,
                                (jboolean)param_CANHealth.dongleConnected);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");

            CANParams_Speed param_Speed;
            nData->module->Rx_SpeedParams(param_Speed);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_Speed,
                                (jfloat)param_Speed.roughSpeed,
                                (jfloat)param_Speed.engineSpeed,
                                (jfloat)param_Speed.wheelSpeed_FrontLeft,
                                (jfloat)param_Speed.wheelSpeed_FrontRight,
                                (jfloat)param_Speed.wheelSpeed_RearLeft,
                                (jfloat)param_Speed.wheelSpeed_RearRight);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");


            CANParams_SteeringSensor param_SteeringSensor;
            nData->module->Rx_SteeringSensorParams(param_SteeringSensor);
            LOGE("mID_setCANdata_SteeringSensor steerControlActive %d",param_SteeringSensor.steerControlActive);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_SteeringSensor,
                                (jfloat)param_SteeringSensor.steerAngle,
                                (jfloat)param_SteeringSensor.steerAngleRate,
                                (jbyte)param_SteeringSensor.steerStatus,
                                (jboolean)param_SteeringSensor.steerControlActive);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");

            CANParams_SteeringControl param_SteeringControl;
            nData->module->Rx_SteeringControlParams(param_SteeringControl);
            LOGE("mID_setCANdata_SteeringControl steerTorqueRequest %d",param_SteeringControl.steerTorqueRequest);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_SteeringControl,
                                (jboolean)param_SteeringControl.steerTorqueRequest,
                                (jlong)param_SteeringControl.steerTorque);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");

            CANParams_LKASHud param_LKASHud;
            nData->module->Rx_LKAS_HUD(param_LKASHud);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_LKASHud,
                                (jbyte)param_LKASHud.isLaneDetected,
                                (jbyte)param_LKASHud.laneType,
                                (jbyte)param_LKASHud.beep);

            CANParams_ACCHud param_ACCHud;
            nData->module->Rx_ACC_HUD(param_ACCHud);
            LOGE("mID_setCANdata_ACCHud accOn %d",param_ACCHud.accOn);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_ACCHud,
                                (jboolean)param_ACCHud.accOn,
                                (jint)param_ACCHud.cruiseSpeed);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");


            CANParams_SafetyFeature param_SafetyFeature;
            nData->module->Rx_SafetyFeature(param_SafetyFeature);
            LOGE("mID_setCANdata_SafetyFeature isControlSystemReady %d, isEnabled_ACC %d, isEnabled_LKS %d, isBrakePressed %d, isGasPressed %d",
                    param_SafetyFeature.isControlSystemReady,
                    param_SafetyFeature.isEnabled_ACC,
                    param_SafetyFeature.isEnabled_LKS,
                    param_SafetyFeature.isBrakePressed,
                    param_SafetyFeature.isGasPressed);

            env->CallVoidMethod(jobj, nData->mID_setCANdata_SafetyFeature,
                                (jboolean)param_SafetyFeature.isControlSystemReady,
                                (jboolean)param_SafetyFeature.isEnabled_ACC,
                                (jboolean)param_SafetyFeature.isEnabled_LKS,
                                (jboolean)param_SafetyFeature.isBrakePressed,
                                (jboolean)param_SafetyFeature.isGasPressed);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");


            CANParams_DriverControllers param_DriverControllers;
            nData->module->Rx_DriverControllers(param_DriverControllers);
            LOGE("mID_setCANdata_SteeringSensor r %d l %d",param_DriverControllers.leftBlinkerOn,param_DriverControllers.rightBlinkerOn);
            env->CallVoidMethod(jobj, nData->mID_setCANdata_DriverControllers,
                                (jboolean)param_DriverControllers.leftBlinkerOn,
                                (jboolean)param_DriverControllers.rightBlinkerOn,
                                (jbyte)param_DriverControllers.wiperStatus);
            LOGE("OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");

        } while(false);

        if(errStream.width() != 0) {
            LOGE("Error , msg : %s", errStream.str().c_str());
        }

    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}


/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_stopRecord
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1stopRecord
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        nData->module->stopRecord();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_startRecord
 * Signature: (JLjava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_vBus_CANbusModule_jni_1startRecord
        (JNIEnv *env, jobject jobj, jlong moduleAddr, jstring path, jboolean appendFile)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        const char *cPath = (path == NULL) ? NULL : env->GetStringUTFChars(path, NULL);
        std::string stdPath = cPath;

        nData->module->startRecord(stdPath, (bool)appendFile);
        ret = (jboolean)nData->module->isRecording();

        if(cPath != NULL) env->ReleaseStringUTFChars(path, cPath);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}


/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_setGPS
 * Signature: (JDD)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1setGPS
        (JNIEnv *, jobject, jlong moduleAddr, jdouble latitude, jdouble longitude)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        nData->module->setGPS(latitude, longitude);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_setCANData_CANHealth
 * Signature: (ZZ)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1manualCANData_1CANHealth
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
         jboolean controls_allowed, jboolean dongleConnected)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        CANHealth param;
        param.dongleConnected = dongleConnected;
        param.controlsAllowed = controls_allowed;
        nData->module->manual_CANHealth(param);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_setCANdata_Speed
 * Signature: (FFFFFF)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1manualCANdata_1Speed
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
         jfloat roughSpeed, jfloat engineSpeed, jfloat wheelSpeed_FrontLeft, jfloat wheelSpeed_FrontRight, jfloat wheelSpeed_RearLeft, jfloat wheelSpeed_RearRight)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        CANParams_Speed param;
        param.roughSpeed              = roughSpeed;
        param.engineSpeed             = engineSpeed;
        param.wheelSpeed_FrontLeft    = wheelSpeed_FrontLeft;
        param.wheelSpeed_FrontRight   = wheelSpeed_FrontRight;
        param.wheelSpeed_RearLeft     = wheelSpeed_RearLeft;
        param.wheelSpeed_RearRight    = wheelSpeed_RearRight;
        nData->module->manual_CANSpeedParams(param);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_setCANdata_SteeringSensor
 * Signature: (FFBZ)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1manualCANdata_1SteeringSensor
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
         jfloat steerAngle, jfloat steerAngleRate, jbyte steerStatus, jboolean steerControlActive)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        CANParams_SteeringSensor param;
        param.steerAngle          = steerAngle;
        param.steerAngleRate      = steerAngleRate;
        param.steerStatus         = (unsigned char)steerStatus;
        param.steerControlActive  = steerControlActive;
        //param.steerTorque         = steerTorque;
        nData->module->manual_CANSteeringSensorParams(param);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_via_adas_car_CANbusModule
 * Method:    jni_setCANdata_DriverControllers
 * Signature: (ZZB)V
 */
JNIEXPORT void JNICALL Java_com_viatech_vBus_CANbusModule_jni_1manualCANdata_1DriverControllers
        (JNIEnv *env, jobject jobj, jlong moduleAddr,
         jboolean leftBlinkerOn, jboolean rightBlinkerOn, jbyte wiperStatus)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL && nData->module != NULL) {
        CANParams_DriverControllers param;
        param.rightBlinkerOn  = rightBlinkerOn;
        param.leftBlinkerOn   = leftBlinkerOn;
        param.wiperStatus     = (unsigned char)wiperStatus;
        nData->module->manual_DriverControllers(param);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}
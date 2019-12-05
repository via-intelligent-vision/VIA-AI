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
#include <android/log.h>
#include "mobile360/CameraCoord/CoordCvtTypes.h"
#include "mobile360/CameraCoord/CameraModule.h"
#define  LOG_TAG    "jni_CameraModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

using namespace via::camera;

/*
 * Class:     com_via_adas_camera_CameraModule
 * Method:    jni_create
 * Signature: (IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_camera_CameraModule_jni_1create
  (JNIEnv *env, jobject jobj, jint cameraType, jint location, jstring jInstrinsicPath, jstring jExtrinsicPath)
{
    jlong ret = 0;
    const char *cInstrinsicPath = (jInstrinsicPath == nullptr) ? nullptr : env->GetStringUTFChars(jInstrinsicPath, nullptr);
    const char *cExtrinsicPath = (jExtrinsicPath == nullptr) ? nullptr : env->GetStringUTFChars(jExtrinsicPath, nullptr);
    std::string sInstrinsicPath = cInstrinsicPath;
    std::string sExtrinsicPath = cExtrinsicPath;

    if(cInstrinsicPath != nullptr)
        LOGE("cInstrinsicPath Path %s", cInstrinsicPath);
    else
        LOGE("cInstrinsicPath Path is NULL");

    if(cExtrinsicPath != nullptr)
        LOGE("cExtrinsicPath Path %s", cExtrinsicPath);
    else
        LOGE("cExtrinsicPath Path is NULL");

    CameraModule *module = new CameraModule((via::camera::CameraTypes)cameraType, (CameraLocationTypes)location);
    if(module != nullptr) {
        if(module->load(sInstrinsicPath, sExtrinsicPath)) {
            ret = (jlong)module;
        }
        else {
            delete module;
            ret = (jlong) nullptr;
        }
    }
    if(cInstrinsicPath != nullptr) env->ReleaseStringUTFChars(jInstrinsicPath, cInstrinsicPath);
    if(cExtrinsicPath != nullptr) env->ReleaseStringUTFChars(jExtrinsicPath, cExtrinsicPath);

    return ret;
}


/*
 * Class:     com_via_adas_camera_CameraModule
 * Method:    jni_release
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraModule_jni_1release
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    if(moduleAddr != 0) {
        CameraModule *module = (CameraModule *) moduleAddr;
        delete module;
    }
}

/*
 * Class:     com_via_adas_camera_CameraModule
 * Method:    jni_isValid
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraModule_jni_1isStable
        (JNIEnv *env, jobject jobj, jlong moduleAddr)
{
    jboolean ret = false;
    if(moduleAddr != 0) {
        CameraModule *module = (CameraModule *) moduleAddr;
        ret = (jboolean)module->isExtrinsicStable();
    }

    return ret;
}


/*
 * Class:     com_viatech_camera_CameraModule
 * Method:    jni_getCameraMatrix
 * Signature: (J[D)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraModule_jni_1getCameraMatrix
        (JNIEnv *env, jobject, jlong moduleAddr, jdoubleArray jDataAry)
{
    jboolean ret = JNI_FALSE;
    if(moduleAddr != 0) {
        do {
            CameraModule *module = (CameraModule *) moduleAddr;
            jint len = env->GetArrayLength(jDataAry);
            if (len != (3 * 3)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }

            jdouble *cDataAry = env->GetDoubleArrayElements(jDataAry, nullptr);
            if (cDataAry == nullptr) break;

            cv::Mat data = module->getCameraMatrix();
            cv::Mat data_64F;
            if(data.empty()) break;
            if((data.type() & CV_MAT_TYPE_MASK) != CV_64F) {
                data.convertTo(data_64F, CV_64F);
            }
            else {
                data_64F = data;
            }
            memcpy(cDataAry, data_64F.ptr(), sizeof(double) * data_64F.cols * data_64F.rows);

            if (cDataAry != nullptr) env->ReleaseDoubleArrayElements(jDataAry, cDataAry, 0);
        } while(false);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraModule
 * Method:    jni_getExtrinsic
 * Signature: (J[D)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraModule_jni_1getExtrinsic
        (JNIEnv *env, jobject, jlong moduleAddr, jdoubleArray jDataAry)
{
    jboolean ret = JNI_FALSE;
    if(moduleAddr != 0) {
        do {
            CameraModule *module = (CameraModule *) moduleAddr;
            jint len = env->GetArrayLength(jDataAry);
            if (len != (4 * 4)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }

            jdouble *cDataAry = env->GetDoubleArrayElements(jDataAry, nullptr);
            if (cDataAry == nullptr) break;

            cv::Mat data = module->getExtrinsic();
            cv::Mat data_64F;
            if(data.empty()) break;
            if((data.type() & CV_MAT_TYPE_MASK) != CV_64F) {
                data.convertTo(data_64F, CV_64F);
            }
            else {
                data_64F = data;
            }
            memcpy(cDataAry, data_64F.ptr(), sizeof(double) * data_64F.cols * data_64F.rows);

            if (cDataAry != nullptr) env->ReleaseDoubleArrayElements(jDataAry, cDataAry, 0);
        } while(false);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraModule
 * Method:    jni_getDistCoeffs
 * Signature: (J[D)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraModule_jni_1getDistCoeffs
        (JNIEnv *env, jobject, jlong moduleAddr, jdoubleArray jDataAry)
{
    jboolean ret = JNI_FALSE;
    if(moduleAddr != 0) {
        do {
            CameraModule *module = (CameraModule *) moduleAddr;
            jint len = env->GetArrayLength(jDataAry);
            if (len != (1 * 5)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }

            jdouble *cDataAry = env->GetDoubleArrayElements(jDataAry, nullptr);
            if (cDataAry == nullptr) break;

            cv::Mat data = module->getDistCoeff();
            cv::Mat data_64F;
            if(data.empty()) break;
            if((data.type() & CV_MAT_TYPE_MASK) != CV_64F) {
                data.convertTo(data_64F, CV_64F);
            }
            else {
                data_64F = data;
            }
            memcpy(cDataAry, data_64F.ptr(), sizeof(double) * data_64F.cols * data_64F.rows);

            if (cDataAry != nullptr) env->ReleaseDoubleArrayElements(jDataAry, cDataAry, 0);
        } while(false);
    }

    return ret;
}


/*
 * Class:     com_viatech_camera_CameraModule
 * Method:    jni_coordCvt
 * Signature: (JIDDD[D)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraModule_jni_1coordCvt
        (JNIEnv *env, jobject, jlong moduleAddr, jint typeID, jdouble x, jdouble y, jdouble z, jdoubleArray jDataAry)
{
    jboolean ret = JNI_FALSE;
    if(moduleAddr != 0) {
        do {
            CameraModule *module = (CameraModule *) moduleAddr;
            jint len = env->GetArrayLength(jDataAry);
            if (len != (1 * 3)) {
                LOGE("%s , Array size not match", __func__);
                break;
            }
            jdouble *cDataAry = env->GetDoubleArrayElements(jDataAry, nullptr);
            if (cDataAry == nullptr) break;

            cv::Point3f p3;
            cv::Point2f p2;
            ret = JNI_TRUE; // set true first, and set false if unknown,
            switch(typeID) {
                case static_cast<unsigned int>(CoordCvtTypes::ImgCoord_To_UndistCoord):
                    p2 = module->coordCvt_ImgCoord_To_UndistCoord(x, y, nullptr);
                    p3 = cv::Point3f(p2.x, p2.y, 0);
                    break;
                case static_cast<unsigned int>(CoordCvtTypes::ImgCoord_To_ObjCoord_ZeroZ):
                    p3 = module->coordCvt_ImgCoord_To_ObjCoord_ZeroZ(x, y);
                    break;
                case static_cast<unsigned int>(CoordCvtTypes::NormalizedImgCoord_To_ObjCoord_ZeroZ):
                    p3 = module->coordCvt_NormalizedImgCoord_To_ObjCoord_ZeroZ(x, y);
                    break;
                case static_cast<unsigned int>(CoordCvtTypes::UndistCoord_To_ObjCoord_ZeroZ):
                    p3 = module->coordCvt_UndistCoord_To_ObjCoord_ZeroZ(x, y);
                    break;
                case static_cast<unsigned int>(CoordCvtTypes::ObjCoord_To_NormalizedImgCoord):
                    p2 = module->coordCvt_ObjCoord_To_NormalizedImgCoord(x, y, z);
                    p3 = cv::Point3f(p2.x, p2.y, 0);
                    break;
                case static_cast<unsigned int>(CoordCvtTypes::NormalizedImgCoord_To_cmDistance):
                    p3.x = module->coordCvt_NormalizedImgCoord_To_cmDistance(x, y);
                    break;
                default:
                    ret = JNI_FALSE;
                    break;
            }

            if(ret != JNI_FALSE) {
                cDataAry[0] = p3.x;
                cDataAry[1] = p3.y;
                cDataAry[2] = p3.z;
            }

            if (cDataAry != nullptr) env->ReleaseDoubleArrayElements(jDataAry, cDataAry, 0);
        } while(false);
    }

    return ret;
}

#ifdef __cplusplus
}
#endif
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
#include <opencv2/imgproc.hpp>
#include "jni_CameraCalibrator.h"
#include "mobile360/CameraCoord/CameraCalibrator.h"

#include <android/log.h>
#define  LOG_TAG    "jni_CANbusModule"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//--------------------------------------------------------------------------------------------------

using namespace std;
using namespace cv;
using namespace via::camera;
//--------------------------------------------------------------------------------------------------
namespace {
class native_section {
public:
    native_section() {
        mClass_objClass = nullptr;
        mID_setCANData_CANHealth = nullptr;
        calibrator = nullptr;
    }

    ~native_section() {
        if (calibrator != nullptr) {
            delete calibrator;
            calibrator = nullptr;
        }
    }

    bool init(JNIEnv *env, jobject jobj) {
        bool ret = false;
        std::ostringstream errStream;

        do {
            // Find Class
            jclass objClass = env->GetObjectClass(jobj);
            if (objClass == nullptr) {
                errStream << "GetObjectClass is NULL , in " << __func__;
                break;
            } else {
                this->mClass_objClass = reinterpret_cast<jclass>(env->NewGlobalRef(objClass));
                env->DeleteLocalRef(objClass);
            }

//            // Find Class methods
//            mID_setCANData_CANHealth = env->GetMethodID(this->mClass_objClass, "setCANData_CANHealth", "(ZZ)V");
//            if (mID_setCANData_CANHealth == NULL) {
//                errStream << "GetMethodID is NULL : setCANData_CANHealth";
//            }


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
        if (mClass_objClass != nullptr) env->DeleteGlobalRef(mClass_objClass);
        mClass_objClass = nullptr;
    }

    jclass mClass_objClass;
    jmethodID mID_setCANData_CANHealth;

    CameraCalibrator *calibrator;
};

}   // namespace
//--------------------------------------------------------------------------------------------------

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1create
  (JNIEnv *env, jobject obj)
{
    native_section *nData = new native_section();
    bool isValid = false;

    if(nData != nullptr) {
        if(nData->init(env, obj)) {
            nData->calibrator = new CameraCalibrator();
            if (nData->calibrator != nullptr) {
                isValid = true;
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
        nData = nullptr;
    }

    return (jlong)nData;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_init
 * Signature: (JIIFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1init
  (JNIEnv *, jobject, jlong moduleAddr, jint boardSize_w, jint boardSize_h, jfloat gridSize_w, jfloat gridSize_h)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        ret = (jboolean)nData->calibrator->init(Size2d(boardSize_w, boardSize_h), Size2f(gridSize_w, gridSize_h));
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_save
 * Signature: (JLjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1save
        (JNIEnv *env, jobject, jlong moduleAddr, jstring jCameraName , jstring jExportPath)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        const char *cPath = env->GetStringUTFChars(jExportPath, NULL);
        const char *cName = env->GetStringUTFChars(jCameraName, NULL);
        std::string sdtPath = cPath;
        std::string sdtName = cName;

        ret = (jboolean)nData->calibrator->save(sdtName, sdtPath);

        if (cPath != NULL) env->ReleaseStringUTFChars(jExportPath, cPath);
        if (cName != NULL) env->ReleaseStringUTFChars(jCameraName, cName);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1release
  (JNIEnv *env, jobject, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr) {
        nData->release(env);
        delete nData;
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_NV12
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1NV12
        (JNIEnv *env, jobject, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        if(pFrameData != nullptr) {
            std::vector<cv::Point2f> points;
            ret = (jboolean)nData->calibrator->findPattern((unsigned char *)pFrameData,
                                                           media::FrameFormatType::FrameFmt_NV12,
                                                           Size((int)frameWidth, (int)frameHeight),
                                                           Rect((int)roiX, (int)roiY, (int)roiWidth, (int)roiHeight),
                                                           &points);
            if(ret) {
                cv::Rect box = cv::boundingRect(points);

            }
        }
    }

    return ret;
}


/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_BGR888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1BGR888
        (JNIEnv *env, jobject, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        if(pFrameData != nullptr) {
            std::vector<cv::Point2f> points;
            ret = (jboolean)nData->calibrator->findPattern((unsigned char *)pFrameData,
                                                           media::FrameFormatType::FrameFmt_BGR888,
                                                           Size((int)frameWidth, (int)frameHeight),
                                                           Rect((int)roiX, (int)roiY, (int)roiWidth, (int)roiHeight),
                                                           &points);
        }
    }

    return ret;
}


/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_RGB888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1RGB888
        (JNIEnv *env, jobject, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        if(pFrameData != nullptr) {
            std::vector<cv::Point2f> points;
            ret = (jboolean)nData->calibrator->findPattern((unsigned char *)pFrameData,
                                                           media::FrameFormatType::FrameFmt_RGB888,
                                                           Size((int)frameWidth, (int)frameHeight),
                                                           Rect((int)roiX, (int)roiY, (int)roiWidth, (int)roiHeight),
                                                           &points);
        }
    }

    return ret;
}


/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_ARGB888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1ARGB888
        (JNIEnv *env, jobject, jlong moduleAddr, jobject buffer,
         jint frameWidth, jint frameHeight, jint roiX, jint roiY, jint roiWidth, jint roiHeight)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        void *pFrameData = env->GetDirectBufferAddress(buffer);

        if(pFrameData != NULL) {
            std::vector<cv::Point2f> points;
            ret = (jboolean)nData->calibrator->findPattern((unsigned char *)pFrameData,
                                                           media::FrameFormatType::FrameFmt_ARGB8888,
                                                           Size((int)frameWidth, (int)frameHeight),
                                                           Rect((int)roiX, (int)roiY, (int)roiWidth, (int)roiHeight),
                                                           &points);
        }
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_isCalibReady
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1isCalibReady
  (JNIEnv *, jobject, jlong moduleAddr)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        ret = (jboolean)nData->calibrator->isCalibReady();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getDistCoeff
 * Signature: (J[F)D
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getDistCoeff
  (JNIEnv *env, jobject, jlong moduleAddr, jfloatArray jDistCoeff)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        cv::Mat distCoeff = nData->calibrator->getDistCoeff();
        jfloat *cDistCoeff = nullptr;

        do {
            if(distCoeff.empty()) break;

            const size_t arySize = 5*1;
            // Check array size
            if(env->GetArrayLength(jDistCoeff) != arySize) {
                LOGE("%s , Array size not match, need %d", __func__, arySize);
                break;
            }
            // get a pointer to the array
            cDistCoeff = env->GetFloatArrayElements(jDistCoeff, 0);
            if (cDistCoeff == nullptr) break;

            // copy data
            memcpy(cDistCoeff, distCoeff.ptr(), sizeof(float) * arySize);
        } while(false);

        if(cDistCoeff != nullptr) env->ReleaseFloatArrayElements(jDistCoeff, cDistCoeff, 0);

    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getCameraMatrix
 * Signature: (J[F)D
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getCameraMatrix
  (JNIEnv *env, jobject, jlong moduleAddr, jfloatArray jCameraMatrixAry)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        cv::Mat cameraMatrix = nData->calibrator->getCameraMatrix();
        jfloat *cCameraMatrixAry = nullptr;

        do {
            if(cameraMatrix.empty()) break;

            const size_t arySize = 3 * 3;
            // Check array size
            if(env->GetArrayLength(jCameraMatrixAry) != arySize) {
                LOGE("%s , Array size not match, need %d", __func__, arySize);
                break;
            }
            // get a pointer to the array
            cCameraMatrixAry = env->GetFloatArrayElements(jCameraMatrixAry, 0);
            if (cCameraMatrixAry == nullptr) break;

            // copy data
            memcpy(cCameraMatrixAry, cameraMatrix.ptr(), sizeof(float) * arySize);
        } while(false);

        if(cCameraMatrixAry != nullptr) env->ReleaseFloatArrayElements(jCameraMatrixAry, cCameraMatrixAry, 0);

    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getCalibSize
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getCalibSize
        (JNIEnv *env, jobject, jlong moduleAddr, jintArray jSizeAry)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        cv::Size calibSize = nData->calibrator->getCalibSize();
        jint *cSizeAry = nullptr;

        do {
            const size_t arySize = 2 * 1;
            // Check array size
            if(env->GetArrayLength(jSizeAry) != arySize) {
                LOGE("%s , Array size not match, need %d", __func__, arySize);
                break;
            }
            // get a pointer to the array
            cSizeAry = env->GetIntArrayElements(jSizeAry, 0);
            if (cSizeAry == nullptr) break;

            // copy data
            cSizeAry[0] = calibSize.width;
            cSizeAry[1] = calibSize.height;
        } while(false);

        if(cSizeAry != nullptr) env->ReleaseIntArrayElements(jSizeAry, cSizeAry, 0);

    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_calibrate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1calibrate
  (JNIEnv *, jobject, jlong moduleAddr)
{
    jdouble ret = 0.0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        ret = (jdouble)nData->calibrator->calibrate();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    return ret;
}

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getRepositoryRatio
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getRepositoryRatio
  (JNIEnv *, jobject, jlong moduleAddr)
{
    jdouble ret = 0.0;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != nullptr && nData->calibrator != nullptr) {
        ret = (jdouble)nData->calibrator->getRepositoryRatio();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }

    return ret;
}


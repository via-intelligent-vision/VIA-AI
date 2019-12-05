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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_viatech_camera_CameraCalibrator */

#ifndef _Included_com_viatech_camera_CameraCalibrator
#define _Included_com_viatech_camera_CameraCalibrator
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1create
        (JNIEnv *, jobject);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_init
 * Signature: (JIIFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1init
        (JNIEnv *, jobject, jlong, jint, jint, jfloat, jfloat);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_save
 * Signature: (JLjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1save
        (JNIEnv *, jobject, jlong, jstring, jstring);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1release
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_NV12
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1NV12
        (JNIEnv *, jobject, jlong, jobject, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_BGR888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1BGR888
        (JNIEnv *, jobject, jlong, jobject, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_RGB888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1RGB888
        (JNIEnv *, jobject, jlong, jobject, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_findPattern_ARGB888
 * Signature: (JLjava/nio/ByteBuffer;IIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1findPattern_1ARGB888
        (JNIEnv *, jobject, jlong, jobject, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_isCalibReady
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1isCalibReady
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getDistCoeff
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getDistCoeff
        (JNIEnv *, jobject, jlong, jfloatArray);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getCameraMatrix
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getCameraMatrix
        (JNIEnv *, jobject, jlong, jfloatArray);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getCalibSize
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getCalibSize
        (JNIEnv *, jobject, jlong, jintArray);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_calibrate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1calibrate
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_viatech_camera_CameraCalibrator
 * Method:    jni_getRepositoryRatio
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_viatech_camera_CameraCalibrator_jni_1getRepositoryRatio
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif

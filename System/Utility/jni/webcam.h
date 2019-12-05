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

#ifndef __WEBCAM_H__
#define __WEBCAM_H__

#include <jni.h>

#include "util.h"

static int DEVICE_DESCRIPTOR = -1;
int* RGB_BUFFER = NULL;
int* Y_BUFFER = NULL;

// These are documented on the Java side, in NativeWebcam
jint Java_com_viatech_utility_camera_NativeWebcam_startCamera(JNIEnv* env,
        jobject thiz, jstring deviceName, jint width, jint height);
void Java_com_viatech_utility_camera_NativeWebcam_loadNextFrame(JNIEnv* env,
        jobject thiz, jobject bitmap);
jboolean Java_com_viatech_utility_camera_NativeWebcam_cameraAttached(JNIEnv* env,
        jobject thiz);
void Java_com_viatech_utility_camera_NativeWebcam_stopCamera(JNIEnv* env,
        jobject thiz);

int Java_com_viatech_utility_camera_NativeWebcam_dequeueFrame(JNIEnv* env,
                                                          jobject thiz, jobject javaSurface, jbyteArray jdstByteArray, jint w, jint h, jint f);

#endif // __WEBCAM_H__

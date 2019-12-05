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

#include "webcam.h"
#include "yuv.h"
#include "util.h"
#include "video_device.h"
#include "capture.h"

#include <android/bitmap.h>
#include <malloc.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <android/asset_manager_jni.h>
#include <string.h>
#include <time.h>

void Java_com_viatech_utility_camera_NativeWebcam_next(JNIEnv* env,
                                                            jobject thiz, jobject javaSurface, jbyteArray jdstByteArray) {

    unsigned char *pDst = (unsigned char*)(*env)->GetDirectBufferAddress(env,jdstByteArray);


    if(javaSurface!=NULL) {
        ANativeWindow *window = ANativeWindow_fromSurface(env, javaSurface);
        if (window != NULL) {
            ANativeWindow_setBuffersGeometry(window, 720, 504, 0x11);
            ANativeWindow_Buffer buffer;
            if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
                process_next_frame(DEVICE_DESCRIPTOR, FRAME_BUFFERS, buffer.bits, pDst);
                ANativeWindow_unlockAndPost(window);
            }
        }
    } else {
        process_next_frame(DEVICE_DESCRIPTOR, FRAME_BUFFERS, NULL, pDst);
    }
}

long Java_com_viatech_utility_camera_NativeWebcam_getFramePointer(JNIEnv* env,
                                                             jobject thiz, jint index) {
    return (long)FRAME_BUFFERS[index].start;
}

int Java_com_viatech_utility_camera_NativeWebcam_dequeueFrameNative(JNIEnv* env,
                                                   jobject thiz, jobject javaSurface, jbyteArray jdstByteArray, jint w, jint h, jint f) {
    int index = -1;
    unsigned char *pDst = NULL;
    if(jdstByteArray!=NULL) {
        pDst = (unsigned char*)(*env)->GetDirectBufferAddress(env,jdstByteArray);
    }


    if(javaSurface!=NULL) {
        LOGE("HANK javaSurface !NULL");
        ANativeWindow *window = ANativeWindow_fromSurface(env, javaSurface);
        if (window != NULL) {
            ANativeWindow_setBuffersGeometry(window, w, h, f);
            ANativeWindow_Buffer buffer;
            if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
                LOGE("bufferStrid:%d",buffer.stride);
                index = dequeue_frame(DEVICE_DESCRIPTOR, FRAME_BUFFERS, buffer.bits, pDst, w, h);
                ANativeWindow_unlockAndPost(window);
            }
        }
    } else {
        LOGE("HANK javaSurface NULL");

        index = dequeue_frame(DEVICE_DESCRIPTOR, FRAME_BUFFERS, NULL, pDst, w, h);
    }
    return index;
}

void Java_com_viatech_utility_camera_NativeWebcam_queueFrameNative(JNIEnv* env,
                                                           jobject thiz, jint bufIndex) {
    queue_frame(DEVICE_DESCRIPTOR, bufIndex);
}

jboolean Java_com_viatech_utility_camera_NativeWebcam_setWatermark(JNIEnv* env, jobject thiz, jobject assetManager,jstring jFileName, jint watermarkWidth, jint watermarkHeight) {

    const char *fileName = (*env)->GetStringUTFChars(env, jFileName, 0);

    // use your string
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    AAsset* asset = AAssetManager_open(mgr, fileName, AASSET_MODE_UNKNOWN);
    if (NULL == asset) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "_ASSET_NOT_FOUND_");
        return JNI_FALSE;
    }
    long size = AAsset_getLength(asset);
    char* buffer = (char*) malloc (sizeof(char)*size);
    AAsset_read (asset,buffer,size);
    AAsset_close(asset);

    set_watermark(buffer, watermarkWidth, watermarkHeight);

    free(buffer);
    (*env)->ReleaseStringUTFChars(env, jFileName, fileName);
}



void Java_com_viatech_utility_camera_NativeWebcam_loadNextFrame(JNIEnv* env,
        jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    int result;
    if((result = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed, error=%d", result);
        return;
    }

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    int* colors;
    if((result = AndroidBitmap_lockPixels(env, bitmap, (void*)&colors)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed, error=%d", result);
    }

    if(!RGB_BUFFER || !Y_BUFFER) {
        LOGE("Unable to load frame, buffers not initialized");
        return;
    }

    process_camera(DEVICE_DESCRIPTOR, FRAME_BUFFERS, info.width, info.height,
            RGB_BUFFER, Y_BUFFER);

    int *lrgb = &RGB_BUFFER[0];
    for(int i = 0; i < info.width * info.height; i++) {
        *colors++ = *lrgb++;
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

jint Java_com_viatech_utility_camera_NativeWebcam_startCamera(JNIEnv* env,
        jobject thiz, jstring deviceName, jint width, jint height) {
    const char* dev_name = (*env)->GetStringUTFChars(env, deviceName, 0);
    int result = open_device(dev_name, &DEVICE_DESCRIPTOR);
    (*env)->ReleaseStringUTFChars(env, deviceName, dev_name);
    if(result == ERROR_LOCAL) {
        return result;
    }

    result = init_device(DEVICE_DESCRIPTOR, width, height);
    if(result == ERROR_LOCAL) {
        return result;
    }

    result = start_capture(DEVICE_DESCRIPTOR);
    if(result != SUCCESS_LOCAL) {
        stop_camera(&DEVICE_DESCRIPTOR, RGB_BUFFER, Y_BUFFER);
        LOGE("Unable to start capture, resetting device");
    } else {
        int area = width * height;
        RGB_BUFFER = (int*)malloc(sizeof(int) * area);
        Y_BUFFER = (int*)malloc(sizeof(int) * area);
    }

    return result;
}

void Java_com_viatech_utility_camera_NativeWebcam_stopCamera(JNIEnv* env,
        jobject thiz) {
    stop_camera(&DEVICE_DESCRIPTOR, RGB_BUFFER, Y_BUFFER);
}

jboolean Java_com_viatech_utility_camera_NativeWebcam_cameraAttached(JNIEnv* env,
        jobject thiz) {
    return DEVICE_DESCRIPTOR != -1;
}


static jobject getApplication(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = (*env)->FindClass(env, "android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID currentApplication = (*env)->GetStaticMethodID(env,
                                                                 activity_thread_clz,
                                                                 "currentApplication",
                                                                 "()Landroid/app/Application;");
        if (currentApplication != NULL) {
            application = (*env)->CallStaticObjectMethod(env, activity_thread_clz,
                                                         currentApplication);
        }
        (*env)->DeleteLocalRef(env, activity_thread_clz);
    }

    return application;
}

static const char *SIGN = "308202c1308201a9a00302010202047b65d970300d06092a864886f70d01010b05003011310f300d0603550403130648616e6b5775301e170d3137303432303033333135345a170d3432303431343033333135345a3011310f300d0603550403130648616e6b577530820122300d06092a864886f70d01010105000382010f003082010a0282010100a91f6f1565672a57ac260dbcdb5b0d41665213aaa3ecc7c225c7abc61e40bcc72af94080271d1e3168618020103d6eb33168647e29cab9035a3ac19b2885d264486d2c2e0d0dde65eeda1e10ae816d8e70abb0210d5b5628688948e863d0dbf6b644d22b4448baa2081adb8c191b8ba5fe0b19ba5ef0173500d1d084aa86fed50ae6f5ca40a6b5135f7ca2e036c92eaf14b9c35a9b9e51c533299368b5e35db07396733681baee8fdba447af924f4f5bc8831a74249bb7fb4714ed9a5293aa072220631a3f422aca04d10b748d3625c1f431e39f4158a0549cb874c4bd715680adce43cd777819d3c2d5086eac6f4ab568b11ea13acadb97be54b1d0378277470203010001a321301f301d0603551d0e041604148c3d65ca3d4c4102fad672e9be1ae83b8182ecd2300d06092a864886f70d01010b0500038201010078041eca501c0dbde7c57248b08ea9e06ecc8eb9e3acb97b8e1f77b7e675247a77f6eb77b3fc3f460dfa195b30565f6bf0d7ad50917ff1896f184a70bcd2915617d023699473e1c8ae4be7214eb14606c5e8ed7984204eafa775f15b3fee8838de70c2c82e4b139841d16f95bdb1a5b8c2e7ea1ec1c83cbb6340a307bd4d1b1c0a29709483e3d5ab9294606a4b0a669f1c953c4f2534d0439845d1a7030e567476d4fddc631d25b4a786b7ebcb94cebd54fc22f0b2e61bfd362899a358702e30a0aab73b0f876f17c226651ab4119a0d39c42c8c9dd78b2062dfa9c1c6cbce1568fef598a22874feb06ed32b9f6fce4158b55aefba60301cf8eddd1a2815e086";

int verifySign(JNIEnv *env) {
    // Application object
    jobject application = getApplication(env);
    if (application == NULL) {
        return JNI_ERR;
    }
    // Context(ContextWrapper) class
    jclass context_clz = (*env)->GetObjectClass(env,application);
    // getPackageManager()
    jmethodID getPackageManager = (*env)->GetMethodID(env,context_clz, "getPackageManager",
                                                      "()Landroid/content/pm/PackageManager;");
    // android.content.pm.PackageManager object
    jobject package_manager = (*env)->CallObjectMethod(env,application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = (*env)->GetObjectClass(env,package_manager);
    // getPackageInfo()
    jmethodID getPackageInfo = (*env)->GetMethodID(env,package_manager_clz, "getPackageInfo",
                                                   "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = (*env)->GetMethodID(env,context_clz, "getPackageName",
                                                   "()Ljava/lang/String;");
    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) ((*env)->CallObjectMethod(env,application, getPackageName));
    // PackageInfo object
    jobject package_info = (*env)->CallObjectMethod(env,package_manager, getPackageInfo, package_name, 64);
    // class PackageInfo
    jclass package_info_clz = (*env)->GetObjectClass(env,package_info);
    // field signatures
    jfieldID signatures_field = (*env)->GetFieldID(env,package_info_clz, "signatures",
                                                   "[Landroid/content/pm/Signature;");
    jobject signatures = (*env)->GetObjectField(env,package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = (*env)->GetObjectArrayElement(env,signatures_array, 0);
    jclass signature_clz = (*env)->GetObjectClass(env,signature0);

    jmethodID toCharsString = (*env)->GetMethodID(env,signature_clz, "toCharsString",
                                                  "()Ljava/lang/String;");
    // call toCharsString()
    jstring signature_str = (jstring) ((*env)->CallObjectMethod(env,signature0, toCharsString));

    // release
    (*env)->DeleteLocalRef(env,application);
    (*env)->DeleteLocalRef(env,context_clz);
    (*env)->DeleteLocalRef(env,package_manager);
    (*env)->DeleteLocalRef(env,package_manager_clz);
    (*env)->DeleteLocalRef(env,package_name);
    (*env)->DeleteLocalRef(env,package_info);
    (*env)->DeleteLocalRef(env,package_info_clz);
    (*env)->DeleteLocalRef(env,signatures);
    (*env)->DeleteLocalRef(env,signature0);
    (*env)->DeleteLocalRef(env,signature_clz);

    const char *sign = (*env)->GetStringUTFChars(env,signature_str, NULL);
    if (sign == NULL) {
//		LOGE("分配内存失败");
        return JNI_ERR;
    }


    int result = strcmp(sign, SIGN);
    (*env)->ReleaseStringUTFChars(env,signature_str, sign);
    (*env)->DeleteLocalRef(env,signature_str);
    if (result == 0) {
        return JNI_OK;
    }

    return JNI_ERR;
}

#define VERIFY_SIGN 0
#define VERIFY_PLATFORM 0
#define VERIFY_DATE 0

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
#ifdef DEBUG
//    const char *id = platformKit_platformID();
//	LOGE("ID: %s", id);
//        LOGE("CHECK Check1");

#else
#if VERIFY_SIGN
    if(verifySign(env) != JNI_OK) {de
        return JNI_ERR;
    }
    LOGE("Check Signed Key");
#endif
#if VERIFY_DATE
    // current date/time based on current system
    time_t now = time(0);
    struct tm *ltm = localtime(&now);
    int year = 1900+ltm->tm_year;
    int mon = 1+ltm->tm_mon;
    int day = ltm->tm_mday;
    int totalDate = year*10000+mon*100+day;
//    LOGE("HANK %d ",totalDate);
    if(totalDate>20180215 || totalDate<20180109) {
        LOGE("Out Of Date!");
        return JNI_ERR;
    }
#endif
#endif
//    const char *id = platformKit_platformID();
    cache_yuv_lookup_table(YUV_TABLE);
    return JNI_VERSION_1_6;
}

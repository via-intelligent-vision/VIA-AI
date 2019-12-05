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

#include <stdlib.h>
#include <jni.h>
#include <sstream>
#ifdef WITH_FASTCV
#include <fastcv.h>
#endif

#include <jni_Utility.h>

#include <android/log.h>
#define  LOG_TAG    "jni_systemInterface"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

#ifdef WITH_FASTCV
    if(fcvSetOperationMode(FASTCV_OP_PERFORMANCE) != 0) {
        if(fcvSetOperationMode(FASTCV_OP_CPU_PERFORMANCE) != 0) {

        }
    }
#endif

 //TODO : NOT applied yet.
#ifdef ENABLE_LICENSE_PROTECTION
    via::license::LicenseVerification licenseVerification;
    std::string cachePath = via::utility::getAppCacheStoragePath() + "/via.license";

    std::string androidID = via::utility::getAndroidID(vm);
    std::string signature = via::utility::getSignature(vm);
    uint32_t dateDec = via::utility::getDecDate();

#ifdef ENABLE_LICENSE_DEBUG_MESSAGE
    LOGE("date %d", dateDec);
#endif

#ifdef ENABLE_LICENSE_DEBUG_MESSAGE
    LOGE("native androidId %s", androidID.c_str());
#endif

    // dump signature  , remove in release
#ifdef ENABLE_LICENSE_DEBUG_MESSAGE
        LOGE("sinature %s", signature.c_str());
#endif

    int errNum;
    char *errmsg;
    bool isValid;
    //LOGE("L1 --------------------------------------------------------------------");
    isValid = via::license::LicenseVerification::getInstance().checkLicense(USE_HARDCODE_LICENSE, androidID, dateDec, signature);
    via::license::LicenseVerification::getInstance().getLastError(&errNum, &errmsg);
    if(errNum == 0) {
        //return JNI_VERSION_1_4;
    }
    else {
        LOGE("L1 error %d, %s", errNum, errmsg);
        return JNI_ERR;
    }

    //LOGE("L2 --------------------------------------------------------------------");
    isValid = via::license::LicenseVerification::getInstance().exportLicense(cachePath.c_str());
    via::license::LicenseVerification::getInstance().getLastError(&errNum, &errmsg);
    if(errNum == 0) {
        //return JNI_VERSION_1_4;
    }
    else {
        LOGE("L2 error %d, %s", errNum, errmsg);
        return JNI_ERR;
    }

//    LOGE("L3 --------------------------------------------------------------------");
//    isValid = via::license::LicenseVerification::getInstance().checkLicense(cachePath.c_str(), androidID, dateDec, signature);
//    via::license::LicenseVerification::getInstance().getLastError(&errNum, &errmsg);
//    LOGE("L3 error %d, %s", errNum, errmsg);

    LOGE("%s", via::license::LicenseVerification::getInstance().getInfo().c_str());


#endif

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{

#ifdef WITH_FASTCV
    fcvCleanUp();
#endif

}
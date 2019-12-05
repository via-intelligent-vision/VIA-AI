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


#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <string>
#include "jni_Utility.h"

#include <android/log.h>
#define  LOG_TAG    "jnu_utility"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// ----------------------------------------------------------------------------------------------------------------------------------------------------
using namespace std;
using namespace via::utility;
// ----------------------------------------------------------------------------------------------------------------------------------------------------

static jobject getApplication(JNIEnv *env)
{
    jobject application = NULL;
    jclass activity_thread_clz = env->FindClass("android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID currentApplication = env->GetStaticMethodID(activity_thread_clz, "currentApplication", "()Landroid/app/Application;");
        if (currentApplication != NULL) {
            application = env->CallStaticObjectMethod(activity_thread_clz, currentApplication);
        } else {
            LOGE("Software Error : Fail to find method, currentApplication() in ActivityThread.");
        }
        env->DeleteLocalRef(activity_thread_clz);
    } else {
        LOGE("Software Error : Fail to find class, android.app.ActivityThread");
    }

    return application;
}

static jobject getGlobalContext(JNIEnv *env)
{
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);

    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);
    return context;
}

static jstring _getAndroidID(JavaVM* vm, JNIEnv *env, jobject thiz)
{

    jclass c_settings_secure = env->FindClass("android/provider/Settings$Secure");
    jclass c_context = env->FindClass("android/content/Context");
    if(c_settings_secure == NULL || c_context == NULL){
        return NULL;
    }
    //Get the getContentResolver method
    jmethodID m_get_content_resolver = env->GetMethodID(c_context, "getContentResolver", "()Landroid/content/ContentResolver;");
    if(m_get_content_resolver == NULL){
        return NULL;
    }
    //Get the Settings.Secure.ANDROID_ID constant
    jfieldID f_android_id = env->GetStaticFieldID(c_settings_secure, "ANDROID_ID", "Ljava/lang/String;");

    if(f_android_id == NULL){
        return NULL;
    }
    jstring s_android_id = (jstring)env->GetStaticObjectField(c_settings_secure, f_android_id);

    //create a ContentResolver instance context.getContentResolver()
    jobject o_content_resolver = env->CallObjectMethod(getGlobalContext(env), m_get_content_resolver);
    if(o_content_resolver == NULL || s_android_id == NULL){
        return NULL;
    }
    //get the method getString
    jmethodID m_get_string = env->GetStaticMethodID(c_settings_secure, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    if(m_get_string == NULL){
        return NULL;
    }
    //get the Android ID
    jstring android_id = (jstring)env->CallStaticObjectMethod(c_settings_secure,
                                                              m_get_string,
                                                              o_content_resolver,
                                                              s_android_id);
    return android_id;
}

static  char* jstringTostr(JavaVM* vm, JNIEnv *env, jstring jstr)
{
    char* pStr = NULL;

    jclass     jstrObj   = env->FindClass("java/lang/String");
    jstring    encode    = env->NewStringUTF("utf-8");
    jmethodID  methodId  = env->GetMethodID(jstrObj, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray byteArray = (jbyteArray)env->CallObjectMethod(jstr, methodId, encode);
    jsize      strLen    = env->GetArrayLength(byteArray);
    jbyte      *jBuf     = env->GetByteArrayElements(byteArray, JNI_FALSE);

    if (jBuf != NULL) {
        pStr = (char*)malloc(strLen + 1);
        if (!pStr) {
            return NULL;
        }
        memcpy(pStr, jBuf, strLen);
        pStr[strLen] = 0;
    }

    env->ReleaseByteArrayElements(byteArray, jBuf, 0);
    //av_log(NULL, AV_LOG_INFO, "the android is end%s\n", pStr);
    return pStr;
}

std::string via::utility::getAndroidID(JavaVM* vm)
{
    JNIEnv* env = NULL;
    if(vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("vm->AttachCurrentThread(&env, NULL)");
        return NULL;
    }
    jstring androidID = _getAndroidID (vm, env, NULL);
    std::string aID = jstringTostr(vm, env, androidID);

    return aID;
}

std::string _getSignature(JavaVM* vm, JNIEnv *env) {
    std::string ret = "";

    // Application object
    do {
        jobject application = getApplication(env);
        if (application == NULL) {
            LOGE("Software Error : Fail to get application context fron JNIEnv.");
            break;
        }
        // Context(ContextWrapper) class
        jclass context_clz = env->GetObjectClass(application);
        // getPackageManager()
        jmethodID getPackageManager = env->GetMethodID(context_clz, "getPackageManager",
                                                       "()Landroid/content/pm/PackageManager;");
        // android.content.pm.PackageManager object
        jobject package_manager = env->CallObjectMethod(application, getPackageManager);
        // PackageManager class
        jclass package_manager_clz = env->GetObjectClass(package_manager);
        // getPackageInfo()
        jmethodID getPackageInfo = env->GetMethodID(package_manager_clz, "getPackageInfo",
                                                    "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
        // context.getPackageName()
        jmethodID getPackageName = env->GetMethodID(context_clz, "getPackageName",
                                                    "()Ljava/lang/String;");
        // call getPackageName() and cast from jobject to jstring
        jstring package_name = (jstring) (env->CallObjectMethod(application, getPackageName));
        // PackageInfo object
        jobject package_info = env->CallObjectMethod(package_manager, getPackageInfo, package_name,
                                                     64);
        // class PackageInfo
        jclass package_info_clz = env->GetObjectClass(package_info);
        // field signatures
        jfieldID signatures_field = env->GetFieldID(package_info_clz, "signatures",
                                                    "[Landroid/content/pm/Signature;");
        jobject signatures = env->GetObjectField(package_info, signatures_field);
        jobjectArray signatures_array = (jobjectArray) signatures;
        jobject signature0 = env->GetObjectArrayElement(signatures_array, 0);
        jclass signature_clz = env->GetObjectClass(signature0);

        jmethodID toCharsString = env->GetMethodID(signature_clz, "toCharsString",
                                                   "()Ljava/lang/String;");
        // call toCharsString()
        jstring signature_str = (jstring) (env->CallObjectMethod(signature0, toCharsString));

        // release
        env->DeleteLocalRef(application);
        env->DeleteLocalRef(context_clz);
        env->DeleteLocalRef(package_manager);
        env->DeleteLocalRef(package_manager_clz);
        env->DeleteLocalRef(package_name);
        env->DeleteLocalRef(package_info);
        env->DeleteLocalRef(package_info_clz);
        env->DeleteLocalRef(signatures);
        env->DeleteLocalRef(signature0);
        env->DeleteLocalRef(signature_clz);

        const char *sign = env->GetStringUTFChars(signature_str, NULL);
        if (sign == NULL) {
            LOGE("Software Error : Unsigned application source.");
            break;
        }

        //----------------------------------------------------------------------------------------------------------------------------------
        ret = sign;
        //LOGE("_getSignature : %s \n", ret.c_str());
        //----------------------------------------------------------------------------------------------------------------------------------

        env->ReleaseStringUTFChars(signature_str, sign);
        env->DeleteLocalRef(signature_str);
    } while (false);

    return ret;
}

std::string via::utility::getSignature(JavaVM* vm) {
    JNIEnv* env = NULL;
    if(vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("vm->AttachCurrentThread(&env, NULL)");
        return NULL;
    }
    return _getSignature (vm, env);
}

std::string via::utility::getAppCacheStoragePath() {
    std::string ret = "";
    pid_t pid = getpid();
//    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "process id %d\n", pid);
    char path[64] = { 0 };
    sprintf(path, "/proc/%d/cmdline", pid);
    FILE *cmdline = fopen(path, "r");
    if (cmdline) {
        char application_id[1024] = { 0 };
        fread(application_id, sizeof(application_id), 1, cmdline);
   //     __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "application id %s\n", application_id);
        fclose(cmdline);

        ret = application_id;
        ret = "/sdcard/Android/data/" + ret;
    }
    else {
        ret = "/sdcard/";
    }

    return ret;
}


uint32_t via::utility::getDecDate()
{
    uint32_t ret = 19900101;
    time_t now = time(0);
    struct tm *ltm = localtime(&now);

    if (ltm != NULL) {
        int year = 1900 + ltm->tm_year;
        int mon = 1 + ltm->tm_mon;
        int day = ltm->tm_mday;
        int hour = ltm->tm_hour;
        int min = ltm->tm_min;
        ret = (uint32_t)(year * 10000 + mon * 100 + day);
    }
    return ret;
}

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

#include <sstream>
#include <GLES2/gl2.h>
#include <glm/gtc/type_ptr.hpp>
#include "glModel/image/ImageModel.h"
#include "jni_glModel_ImageModel.h"

#include <android/log.h>
#define  LOG_TAG    "jni_glModel_ImageModel"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace std;
using namespace via::gl;

namespace{
class native_section {
public:
    native_section() {
        mClass_objClass = NULL;
        module = NULL;
    }

    ~native_section() {
        if (module != NULL) {
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
    ImageModel *module;

};
}
/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_viatech_glModel_ImageModel_jni_1create
        (JNIEnv *env, jobject obj)
{
    native_section *nData = new native_section();
    bool isValid = false;

    if(nData != NULL) {
        if(nData->init(env, obj)) {
            nData->module = new ImageModel();
            if (nData->module != NULL) {
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

    if(!isValid && nData != NULL) {
        delete nData;
        nData = NULL;
    }

    return (jlong)nData;
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_init
 * Signature: (JLjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_glModel_ImageModel_jni_1init
        (JNIEnv *env, jobject, jlong moduleAddr, jstring jPath, jstring jImgName)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        ImageModel *module = nData->module;
        if(jPath == nullptr && jImgName == nullptr) {
            ret = (jboolean)module->init();
        }
        else {
            const char *cPath = env->GetStringUTFChars(jPath, NULL);
            const char *cImgName = env->GetStringUTFChars(jImgName, NULL);
            std::string ssPath = cPath;
            std::string ssImgName = cImgName;

            ret = (jboolean)module->init(ssPath, ssImgName);

            if(cPath != NULL) env->ReleaseStringUTFChars(jPath, cPath);
            if(cImgName != NULL) env->ReleaseStringUTFChars(jImgName, cImgName);

        }
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1release
        (JNIEnv *env, jobject, jlong moduleAddr)
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
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_initGL
 * Signature: (JLjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_glModel_ImageModel_jni_1initGL__JLjava_lang_String_2
        (JNIEnv *env, jobject, jlong moduleAddr, jstring jShaderPath)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        ImageModel *module = nData->module;
        const char *cShaderPath = env->GetStringUTFChars(jShaderPath, NULL);
        std::string ssPath = cShaderPath;
        try {
            module->initGL(ssPath);
            ret = JNI_TRUE;
        }
        catch (std::runtime_error e) {
            LOGE("%s", e.what());
        }

        if(cShaderPath != NULL) env->ReleaseStringUTFChars(jShaderPath, cShaderPath);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_initGL
 * Signature: (JLjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_viatech_glModel_ImageModel_jni_1initGL__JLjava_lang_String_2Ljava_lang_String_2
        (JNIEnv *env, jobject, jlong moduleAddr, jstring jVertexShader, jstring jFragShader)
{
    jboolean ret = JNI_FALSE;
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        ImageModel *module = nData->module;
        const char *cVertexShader = env->GetStringUTFChars(jVertexShader, NULL);
        const char *cFragShader = env->GetStringUTFChars(jFragShader, NULL);
        std::string sVertexShader = cVertexShader;
        std::string sFragShader = cFragShader;
        try {
            module->initGL(sVertexShader, sFragShader);
            ret = JNI_TRUE;
        }
        catch (std::runtime_error e) {
            LOGE("%s", e.what());
        }

        if(cVertexShader != NULL) env->ReleaseStringUTFChars(jVertexShader, cVertexShader);
        if(cFragShader != NULL) env->ReleaseStringUTFChars(jFragShader, cFragShader);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
    return ret;
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_setMatrices
 * Signature: (J[F[F[F)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1setMatrices
        (JNIEnv *env, jobject, jlong moduleAddr, jfloatArray jProjectionAry, jfloatArray jViewAry, jfloatArray jModelAry)
{
    native_section *nData = (native_section *)moduleAddr;
    if(nData != NULL) {
        jfloat *cProjectionAry = NULL;
        jfloat *cViewArt = NULL;
        jfloat *cModelAry = NULL;

        do {
            // get a pointer to the array
            cProjectionAry = env->GetFloatArrayElements(jProjectionAry, 0);
            cViewArt = env->GetFloatArrayElements(jViewAry, 0);
            cModelAry = env->GetFloatArrayElements(jModelAry, 0);
            if (cProjectionAry == nullptr || cViewArt == nullptr || cModelAry == nullptr) break;

            glm::mat4 projection, view, model;

            memcpy( glm::value_ptr(projection), cProjectionAry, sizeof(float) *16);
            memcpy( glm::value_ptr(view), cViewArt, sizeof(float) *16);
            memcpy( glm::value_ptr(model), cModelAry, sizeof(float) *16);
            nData->module->setMatrices(projection, view, model);

        } while(false);

        if(cProjectionAry != NULL) env->ReleaseFloatArrayElements(jProjectionAry, cProjectionAry, 0);
        if(cViewArt != NULL) env->ReleaseFloatArrayElements(jViewAry, cViewArt, 0);
        if(cModelAry != NULL) env->ReleaseFloatArrayElements(jModelAry, cModelAry, 0);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_draw
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1draw
        (JNIEnv *, jobject, jlong moduleAddr)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->module->draw();
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_setAlpha
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1setAlpha
        (JNIEnv *, jobject, jlong moduleAddr, jfloat alpha)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->module->setAlpha(alpha);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_setColor
 * Signature: (JFFF)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1setColor
        (JNIEnv *, jobject, jlong moduleAddr, jfloat r, jfloat g, jfloat b)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->module->setColor(r, g, b);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_updateTextCoord
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1updateTextCoord
        (JNIEnv *, jobject, jlong moduleAddr, jfloat x, jfloat y, jfloat width, jfloat height)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->module->updateTextCoord(x, y, width, height);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}

/*
 * Class:     com_viatech_glModel_ImageModel
 * Method:    jni_updateRefTexture
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_viatech_glModel_ImageModel_jni_1updateRefTexture
        (JNIEnv *, jobject, jlong moduleAddr, jint textureId)
{
    native_section *nData = (native_section *)moduleAddr;

    if(nData != NULL) {
        nData->module->updateRefTexture((GLuint)textureId);
    }
    else {
        LOGE("module doesn't created or init fail. in %s", __func__);
    }
}
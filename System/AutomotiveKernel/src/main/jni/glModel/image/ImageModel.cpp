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

#include <iostream>
#include <stdexcept>
#include <sstream>
#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include "glModel/image/ImageModel.h"
#include "exceptions/Exceptions.h"

#include <android/log.h>
#define  LOG_TAG    "BaseShaderProgram"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


using namespace via::gl;
using namespace cv;

const std::string ImageModel::VERTEX_SHADER_NAME = "ImageModel.vsh";
const std::string ImageModel::FRAGMENT_SHADER_NAME = "ImageModel.fsh";

ImageModel::ImageModel()
{
    vboCount = 0;
    b_isInit = false;
    b_isInitGL = false;
    vboAttributeId = NULL;
    vboIndicesId = NULL;
    alpha = 1.0f;
    diffuseColor = glm::vec3(1.0f, 1.0f, 1.0f);

}

ImageModel::~ImageModel()
{
    if (vboAttributeId != NULL) delete vboAttributeId;
    if (vboIndicesId != NULL) delete vboIndicesId;
}

bool ImageModel::init(std::string path, std::string imgName)
{
    try {
        b_isInit = attributeData.load(path, imgName);
    }
    catch (FileNotFoundException e) {
        LOGE("%s", e.what());
        b_isInit = false;
    }
    
    return b_isInit;
}

bool ImageModel::init()
{
    b_isInit = attributeData.create();

    return b_isInit;
}

void ImageModel::release()
{

}

GLuint static CreateTexture_1x1_white()
{
    GLuint ret = 0;
    unsigned char data[] = {255, 255, 255};

    glGenTextures(1, &ret);
    glBindTexture(GL_TEXTURE_2D, ret);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, 0);

    return ret;
}

static GLuint LoadTexture(cv::Mat &img)
{
    GLuint ret = 0;

    if (!img.empty()) {
        //cv::flip(img, img, 0);
        glGenTextures(1, &ret);
        glBindTexture(GL_TEXTURE_2D, ret);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.cols, img.rows, 0, GL_RGBA,  GL_UNSIGNED_BYTE, img.ptr());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    return ret;
}

static GLuint CreateTexture(glm::vec3 &color)
{
    GLuint ret = 0;
    unsigned char data[] = { (uchar)(color.r * 255.0f), (uchar)(color.g * 255.0f), (uchar)(color.b * 255.0f) };

    glGenTextures(1, &ret);
    glBindTexture(GL_TEXTURE_2D, ret);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1, 1, 0, GL_RGB, GL_UNSIGNED_BYTE, data);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glBindTexture(GL_TEXTURE_2D, 0);

    return ret;
}

void ImageModel::initGL(std::string vertex, std::string fragment)
{
    if (!b_isInit) {
        throw ShaderDataNotCreatedException("shader data not created, or init() called before initGL()");
    }

    do {
        std::lock_guard<std::mutex> mLock(glShaderData.mutex);
        if (!glShaderData.isActive) {
            if (!initShaderByString(glShaderData, vertex, fragment)) {
                std::stringstream ss;
                ss << "init shader fail." << " | in " << __func__;
                throw std::runtime_error(ss.str());
            }
        }
    } while (false);

    // buufer data
    updateGLAttribute();

    // load texture file
    Mat img = attributeData.getImage();
    textureData.diffuse = LoadTexture(img);

    b_isInitGL = true;
}

void ImageModel::initGL(std::string shaderPath)
{
    this->initGL(shaderPath, NULL);
}

void ImageModel::initGL(std::string shaderPath, ImageModel *sharedTextureObj)
{
    if (!b_isInit) {
        throw ShaderDataNotCreatedException("shader data not created, or init() called before initGL()");
    }

    // init shader
#ifdef _WIN32
    std::string vPath = shaderPath + "\\" + VERTEX_SHADER_NAME;
    std::string fPath = shaderPath + "\\" + FRAGMENT_SHADER_NAME;
#else
    std::string vPath = shaderPath + "/" + VERTEX_SHADER_NAME;
    std::string fPath = shaderPath + "/" + FRAGMENT_SHADER_NAME;
#endif

    do {
        std::lock_guard<std::mutex> mLock(glShaderData.mutex);
        if (!glShaderData.isActive) {
            if (!initShaderByPath(glShaderData, vPath, fPath)) {
                std::stringstream ss;
                ss << "init shader fail." << " | in " << __func__;
                throw std::runtime_error(ss.str());
            }
        }
    } while (false);

    // buufer data
    updateGLAttribute();

    // load texture file
    if (sharedTextureObj == NULL) {
        Mat img = attributeData.getImage();
        textureData.diffuse = LoadTexture(img);
    }
    else {
        this->textureData = sharedTextureObj->textureData;
    }

    b_isInitGL = true;
}

void ImageModel::initGL(std::string shaderPath, glm::vec3 &color)
{
    if (!b_isInit) {
        throw ShaderDataNotCreatedException("shader data not created, or init() called before initGL()");
    }

    // init shader
#ifdef _WIN32
    std::string vPath = shaderPath + "\\" + VERTEX_SHADER_NAME;
    std::string fPath = shaderPath + "\\" + FRAGMENT_SHADER_NAME;
#else
    std::string vPath = shaderPath + "/" + VERTEX_SHADER_NAME;
    std::string fPath = shaderPath + "/" + FRAGMENT_SHADER_NAME;
#endif

    do {
        std::lock_guard<std::mutex> mLock(glShaderData.mutex);
        if (!glShaderData.isActive) {
            if (!initShaderByPath(glShaderData, vPath, fPath)) {
                std::stringstream ss;
                ss << "init shader fail." << " | in " << __func__;
                throw std::runtime_error(ss.str());
            }
            else {
                LOGI("Init shader success");
            }
        }
    } while (false);

    // buufer data
    updateGLAttribute();

    // load texture file
    textureData.diffuse = CreateTexture(color);

    b_isInitGL = true;
}

void ImageModel::updateGLAttribute()
{
    std::lock_guard<std::mutex> mLock(mutex_Object);

    AttributeDataPermutationType type;
    size_t attributeSize;
    std::vector<ShaderData> *datas;
    attributeData.getAttributeData(&type, &attributeSize, &datas);

    if (vboAttributeId == NULL) {
        vboAttributeId = new GLuint[datas->size()];
        glGenBuffers((GLsizei)datas->size(), vboAttributeId);
    }
    if (vboIndicesId == NULL) {
        vboIndicesId = new GLuint[datas->size()];
        glGenBuffers((GLsizei)datas->size(), vboIndicesId);
    }
    
    for (int i = 0; i < datas->size(); i++) {
       // printf("vboAttributeId[i] %d ... %X\n", vboAttributeId[i], this);
      //  printf("vboIndicesId[i]   %d ... %X\n", vboIndicesId[i], this);
        glBindBuffer(GL_ARRAY_BUFFER, vboAttributeId[i]);
        glBufferData(GL_ARRAY_BUFFER, attributeSize * ((*datas)[i]).attributeDataLength, ((*datas)[i]).attributeData, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId[i]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned int) * ((*datas)[i]).indicesLength, ((*datas)[i]).indices, GL_STATIC_DRAW);
    }
    //printf("-----------------------------------\n");
}

void ImageModel::getBoundingBox(glm::vec3 &minAnchor, glm::vec3 &maxAnchor)
{
    float minX, minY, minZ, maxX, maxY, maxZ;
    attributeData.getBoundingBox(&minX, &minY, &minZ, &maxX, &maxY, &maxZ);
    minAnchor = glm::vec3(minX, minY, minZ);
    maxAnchor = glm::vec3(maxX, maxY, maxZ);
}

void ImageModel::updateTextCoord(float x, float y, float width, float height)
{
    std::lock_guard<std::mutex> mLock(mutex_Object);

    AttributeDataPermutationType type;
    size_t attributeSize;
    std::vector<ShaderData> *datas;
    attributeData.getAttributeData(&type, &attributeSize, &datas);

    if (datas != NULL && datas->size() > 0) {
        void *srcData = (*datas)[0].attributeData;
        if (srcData != NULL) {
            switch (type) {
            case AttributeDataPermutationType::TYPE_V3_T2: {
                AttributeData_V3_T2 * data = (AttributeData_V3_T2 *)(srcData);
                data->textCoord[0] = x;
                data->textCoord[1] = y + height;
                data = (AttributeData_V3_T2 *)((unsigned char *)data + attributeSize);
                data->textCoord[0] = x + width;
                data->textCoord[1] = y + height;
                data = (AttributeData_V3_T2 *)((unsigned char *)data + attributeSize);
                data->textCoord[0] = x;
                data->textCoord[1] = y;
                data = (AttributeData_V3_T2 *)((unsigned char *)data + attributeSize);
                data->textCoord[0] = x + width;
                data->textCoord[1] = y;
            } break;
            default:
                throw AccessUnfinishedOperationException("unhandled data permutation");
                break;
            }

            b_isDataExpired = true;
        }
    }
}

void ImageModel::updateRefTexture(GLuint refTexture)
{
    textureData.diffuse = refTexture;
}

void ImageModel::setAlpha(float alpha)
{
    this->alpha = alpha;
}

void ImageModel::setColor(float r, float g, float b)
{
    diffuseColor.x = r;
    diffuseColor.y = g;
    diffuseColor.z = b;
}

void ImageModel::setMatrices(glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model)
{
    projection_ = projection;
    view_ = view;
    model_ = model;
}
void ImageModel::draw()
{
    if (!this->isInitGL()) return;

    // update data
    if (b_isDataExpired) {
        updateGLAttribute();
        b_isDataExpired = false;
    }

    // rendering
    _draw(glShaderData, projection_, view_, model_);

}

void ImageModel::_draw(gl_ShaderData &shader, glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model)
{
    AttributeDataPermutationType type;
    size_t stepSize;
    std::vector<ShaderData> *attrubuteDatas;
    std::vector<PartialMaterialData> *mtls;
    attributeData.getAttributeData(&type, &stepSize, &attrubuteDatas);
    attributeData.getMaterialData(&mtls);


    glUseProgram(shader.mShaderProgram);

    // render
    GLint uModel = glGetUniformLocation(shader.mShaderProgram, "uModel");
    GLint uView = glGetUniformLocation(shader.mShaderProgram, "uView");
    GLint uProjection = glGetUniformLocation(shader.mShaderProgram, "uProjection");
    glUniformMatrix4fv(uModel, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(uView, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(uProjection, 1, GL_FALSE, glm::value_ptr(projection));


    GLint uMaterials_alpha = glGetUniformLocation(shader.mShaderProgram, "uMaterials.alpha");
    glUniform4f(uMaterials_alpha, diffuseColor.x, diffuseColor.y, diffuseColor.z, alpha);


    GLint aVertex = glGetAttribLocation(shader.mShaderProgram, "aVertex");
    GLint aTextCoord = glGetAttribLocation(shader.mShaderProgram, "aTextCoord");

    for (int ai = 0; ai < attrubuteDatas->size(); ai++) {
        GLint tDiffuseLoc = glGetUniformLocation(shader.mShaderProgram, "uTextures.diffuse");
        glUniform1i(tDiffuseLoc, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureData.diffuse);

        glBindBuffer(GL_ARRAY_BUFFER, vboAttributeId[ai]);
        glEnableVertexAttribArray(aVertex);
        glVertexAttribPointer(aVertex, 3, GL_FLOAT, false, (GLsizei)stepSize, NULL);
        glEnableVertexAttribArray(aTextCoord);
        glVertexAttribPointer(aTextCoord, 2, GL_FLOAT, false, (GLsizei)stepSize, (void *)(sizeof(GLfloat) * 3));

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId[ai]);
        glDrawElements(GL_TRIANGLE_STRIP, (GLsizei)((*attrubuteDatas)[ai]).indicesLength, GL_UNSIGNED_INT, NULL);
    }
    glUseProgram(0);

}
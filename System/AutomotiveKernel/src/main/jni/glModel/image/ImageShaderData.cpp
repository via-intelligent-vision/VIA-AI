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
#include <iostream>
#include <algorithm>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include "exceptions/FileNotFoundException.h"
#include "glModel/image/ImageShaderData.h"

#include <android/log.h>
#define  LOG_TAG    "ImageShaderData"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


// ---------------------------------------------------------------------
using namespace via::gl;
using namespace cv;
// ---------------------------------------------------------------------

bool ImageShaderData::load(std::string path, std::string imgName)
{
    bool ret = false;
#ifdef _WIN32
    std::string fullPath = path + "\\" + imgName;
#else
    std::string fullPath = path +"/" + imgName;
#endif

    Mat img = imread(fullPath, IMREAD_UNCHANGED);
    if (img.empty()) {
        LOGE("FIle %s" ,fullPath.c_str());
        throw FileNotFoundException("File not found.", path, imgName);
    }

    // scale channel to 4 (bgra)
    
    if (img.channels() != 4) {
        Mat alpha = cv::Mat(img.size(), CV_8UC1, Scalar(255));

        std::vector<cv::Mat> matChannels;
        cv::split(img, matChannels);
        switch (matChannels.size()) {
        case 1:
            matChannels.push_back(matChannels[0].clone());  // gray G
            matChannels.push_back(matChannels[0].clone());  // gray R
            matChannels.push_back(alpha.clone());  // gray alpha
            break;
        case 3:
            matChannels.push_back(alpha.clone());  // gray alpha
            break;
        default:
            throw std::runtime_error("Unhandlled channel size");
            break;
        }
        cv::merge(matChannels, imageSrc);
    }
    else {
        imageSrc = img;
    }
    cv::cvtColor(imageSrc, imageSrc, CV_BGRA2RGBA);

    // create attribute data
    ret = create();

    return ret;
}

bool ImageShaderData::create()
{
    bool ret = false;

    // set material
    materialDatas.resize(1);
    materialDatas[0] = defaultMaterial;

    // parse shapes
    shaderDatas.resize(1);
    for (size_t si = 0; si < shaderDatas.size(); si++) {

        // parse meshs
        real_t *pVertex = NULL;
        real_t *pTextCoord = NULL;
        size_t pStep = 0;

        // pre allocated render data
        shaderDatas[si].name = "image";
        shaderDatas[si].attributeDataLength = 4;

        // allocate data
        shaderDatas[si].materialId = 0;
        shaderDatas[si].attributeData = new AttributeData_V3_T2[shaderDatas[si].attributeDataLength];
        pStep = getStepSize(dataPermutationType);
        pVertex = (real_t *)shaderDatas[si].attributeData;
        pTextCoord = (real_t *)((unsigned char *)shaderDatas[si].attributeData + sizeof(real_t) * 3);

//        real_t v[] = { -1, -1, 0,
//                        1, -1, 0,
//                       -1,  1, 0 ,
//                        1,  1, 0 };
        real_t v[] = { -0.9, -0.9, 0,
                       0.9, -0.9, 0,
                       -0.9,  0.9, 0 ,
                       0.9,  0.9, 0 };
        real_t vt[] = { 0, 1,
                        1, 1,
                        0, 0,
                        1, 0 };
        for (int vi = 0; vi < 4; vi++) {
            int vi3 = vi * 3;
            int vi2 = vi * 2;
            pVertex[0] = v[vi3 + 0];
            pVertex[1] = v[vi3 + 1];
            pVertex[2] = v[vi3 + 2];
            pTextCoord[0] = vt[vi2 + 0];
            pTextCoord[1] = vt[vi2 + 1];
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pTextCoord = (real_t *)((unsigned char *)pTextCoord + pStep);
        }

        // allocate index ( triangle strip)
        shaderDatas[si].indicesLength = 4;
        shaderDatas[si].indices = new unsigned int[shaderDatas[si].indicesLength];
        shaderDatas[si].indices[0] = 0;
        shaderDatas[si].indices[1] = 1;
        shaderDatas[si].indices[2] = 2;
        shaderDatas[si].indices[3] = 3;
    }


    // parse shapes
    // parse shapes
    //for (size_t si = 0; si < shaderDatas.size(); si++) {
    //    printf("parse result -------------------------------------------------------\n");
    //    printf("%s\n", shaderDatas[si].name.c_str());

    //    // pre allocated render data
    //    void *srcData = shaderDatas[si].attributeData;
    //    for (int ti = 0; ti < shaderDatas[si].attributeDataLength; ti++) {
    //        switch (this->dataPermutationType) {
    //        case AttributeDataPermutationType::TYPE_V3_T2: {
    //            AttributeData_V3_T2 * data = (AttributeData_V3_T2 *)srcData;
    //            printf("v %f %f %f , c %f %f\n",
    //                data->vertex[0], data->vertex[1], data->vertex[2],
    //                data->textCoord[0], data->textCoord[1]);
    //            srcData = (unsigned char *)srcData + getStepSize(dataPermutationType);
    //        }
    //        break;
    //        }
    //    }
    //}


    bBox_min[0] = bBox_min[1] = bBox_min[2] = -1;
    bBox_max[0] = bBox_max[1] = bBox_max[2] = 1;
    ret = true;

    return ret;
}

void ImageShaderData::getAttributeData(AttributeDataPermutationType *type, size_t *attributeSize, std::vector<ShaderData> **datas) 
{
    if (type == NULL || attributeSize == NULL || datas == NULL) {
        std::stringstream ss;
        ss << "parameter is NULL." << " | in " << __func__;
        throw std::invalid_argument(ss.str());
    }
    else {
        *type = dataPermutationType;
        *attributeSize = dataStepSize;
        *datas = &shaderDatas;
    }
}

void ImageShaderData::getMaterialData(std::vector<PartialMaterialData> **mtls)
{
    if (mtls == NULL) {
        std::stringstream ss;
        ss << "parameter is NULL." << " | in " << __func__;
        throw std::invalid_argument(ss.str());
    }
    else {
        *mtls = &materialDatas;
    }
}

void ImageShaderData::getBoundingBox(float *minX, float *minY, float *minZ, float *maxX, float *maxY, float *maxZ)
{
    if (minX == NULL || minY == NULL || minZ == NULL || maxX == NULL || maxY == NULL || maxZ == NULL) {
        std::stringstream ss;
        ss << "parameter is NULL." << " | in " << __func__ ;
        throw std::invalid_argument(ss.str());
    }
    else {
        *minX = bBox_min[0];
        *minY = bBox_min[1];
        *minZ = bBox_min[2];
        *maxX = bBox_max[0];
        *maxY = bBox_max[1];
        *maxZ = bBox_max[2];
    }
}

std::string ImageShaderData::getImagePath()
{
    return this->imagePath;
}

std::string ImageShaderData::getImageName()
{
    return this->imageName;
}

cv::Mat ImageShaderData::getImage() {
    return this->imageSrc;
}

void ImageShaderData::release() 
{
    for (size_t gi = 0; gi < shaderDatas.size(); gi++) {
        if (shaderDatas[gi].attributeData != NULL) {
            via::gl::releaseData(dataPermutationType, &shaderDatas[gi].attributeData);
        }
    }
    shaderDatas.clear();
}

ImageShaderData::ImageShaderData() 
{
    dataPermutationType = AttributeDataPermutationType::TYPE_V3_T2;
    dataStepSize = getStepSize(dataPermutationType);
    defaultMaterial.name = "default-material";
    defaultMaterial.ambient[0] = 0.0f;
    defaultMaterial.ambient[1] = 0.0f;
    defaultMaterial.ambient[2] = 0.0f;
    defaultMaterial.diffuse[0] = 1.0f;
    defaultMaterial.diffuse[1] = 1.0f;
    defaultMaterial.diffuse[2] = 1.0f;
    defaultMaterial.specular[0] = 1.0f;
    defaultMaterial.specular[1] = 1.0f;
    defaultMaterial.specular[2] = 1.0f;
    defaultMaterial.alpha = 1.0f;
}
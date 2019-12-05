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

#pragma once

#include <string>
#include <vector>
#include "glModel/core/types.h"
#include <opencv2/core.hpp>
// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------

class ImageShaderData {
public:
    ImageShaderData();
    bool load(std::string path, std::string imgName);
    bool create();
    void release();
    void getBoundingBox(float *minX, float *minY, float *minZ, float *maxX, float *maxY, float *maxZ);
    void getAttributeData(AttributeDataPermutationType *type, size_t *stepSize, std::vector<ShaderData> **datas);
    void getMaterialData(std::vector<PartialMaterialData> **mtls);
    std::string getImagePath();
    std::string getImageName();
    cv::Mat getImage();

private:
    std::string imagePath;
    std::string imageName;
    float bBox_min[3];  // bounding box
    float bBox_max[3];
    cv::Mat imageSrc;

    AttributeDataPermutationType dataPermutationType;
    size_t dataStepSize;
    std::vector<ShaderData> shaderDatas;

    PartialMaterialData defaultMaterial;
    std::vector<PartialMaterialData> materialDatas;
};

// ---------------------------------------------------------------------
}   //namespace shader
}   //namespace utils
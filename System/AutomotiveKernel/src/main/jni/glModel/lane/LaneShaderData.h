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
// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------
class LaneParabolaData {
public:
    void copyTo(LaneParabolaData &dst);
    LaneParabolaData& operator=(LaneParabolaData & arg);
    LaneParabolaData();
    LaneParabolaData(double a, double b, double c, double width);
    double value(double v);
    double valueL(double v);
    double valueR(double v);
    double getCurvature(double x = 20 * 100);
    double cA;   // data = cA * v^2 + cB * v + cC;
    double cB;
    double cC;
    double width;
    bool visibility;
};

class LaneShaderData
{
public:
    enum class LaneTypes : unsigned char {
        MainLane,
        LeftLane,
        RightLane
    };
    static const int MODEL_ID_MAIN_LANE = 0;
    static const int MODEL_ID_MAIN_OVERLAPE_LANE = 1;
    static const int MODEL_ID_RIGHT_LANE = 2;
    static const int MODEL_ID_LEFT_LANE = 3;

    LaneShaderData();
    ~LaneShaderData();

    bool generate(const float startZ, const float length, const float y);
    bool update(LaneParabolaData mainLane);
    void release();
    void getBoundingBox(float *minX, float *minY, float *minZ, float *maxX, float *maxY, float *maxZ);
    void getAttributeData(AttributeDataPermutationType *type, size_t *stepSize, std::vector<ShaderData> **datas);
    void getMaterialData(std::vector<PartialMaterialData> **mtls);
    const LaneParabolaData &getLane(LaneTypes type);

private:
    float bBox_min[3];  // bounding box
    float bBox_max[3];

    float laneLength;
    LaneParabolaData lane_Main;
    LaneParabolaData lane_R;
    LaneParabolaData lane_L;

    AttributeDataPermutationType dataPermutationType;
    size_t dataStepSize;
    std::vector<ShaderData> shaderDatas;

    PartialMaterialData defaultMaterial;
    std::vector<PartialMaterialData> materialDatas;
};

// ---------------------------------------------------------------------
}   //namespace shader
}   //namespace utils
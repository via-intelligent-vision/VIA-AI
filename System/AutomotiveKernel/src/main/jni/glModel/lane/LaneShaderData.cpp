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
#include <cmath>
#include <iostream>
#include <algorithm>
#include "glModel/lane/LaneShaderData.h"

// ---------------------------------------------------------------------
using namespace std; 
using namespace via::gl;

// ---------------------------------------------------------------------
template <class T>
T interpolate(T vInMinSpeed, T vInMaxSpeed, T speed, T minSpeed, T maxSpeed) {
    T v;
    if (speed <= minSpeed) {
        v = vInMinSpeed;
    }
    else if (speed >= maxSpeed) {
        v = vInMaxSpeed;
    }
    else {
        v = vInMinSpeed + ((vInMaxSpeed - vInMinSpeed) / (maxSpeed - minSpeed)) * (speed - minSpeed);
    }
    return v;
}

bool LaneShaderData::generate(const float startZ, const float length, const float y)
{
    bool ret = false;
    const int MAX_SLICE = 100;

    if (length < 0) {
        std::stringstream ss;
        ss << "parameter couldn't less than 0." << " | in " << __func__;
        throw std::invalid_argument(ss.str());
    }

    laneLength = length;
    
    // 0 is main lane(base), 1 is main lane(overlap)
    // 3 is left, 2 ls right
    shaderDatas.resize(4);  
    materialDatas.resize(4);


    lane_L = lane_Main;
    lane_L.cC += lane_Main.width * 0.9f;
    lane_R = lane_Main;
    lane_R.cC -= lane_Main.width * 0.9f;

    // calc 1 - main lane(base)
    do {
        ShaderData * shaderData = &shaderDatas[MODEL_ID_MAIN_OVERLAPE_LANE];

        // parse meshs
        real_t *pVertex = NULL;
        real_t *pColor = NULL;
        size_t pStep = 0;

        // pre allocated render data
        shaderData->name = "main lane(base)";
        shaderData->materialId = 0;
        shaderData->attributeDataLength = (MAX_SLICE + 1) * 2;    // 2 is left/right point
        shaderData->attributeData = new AttributeData_V3_C4[shaderData->attributeDataLength];
        pStep = getStepSize(dataPermutationType);
        pVertex = (real_t *)shaderData->attributeData;
        pColor = (real_t *)((unsigned char *)shaderData->attributeData + sizeof(real_t) * 3);

       
        // padding data
        for (int si = 0; si <= MAX_SLICE; si++) {
            float alphaScale = 1.0f;
            if (si < 2)
                alphaScale = interpolate<float>(0.01f, 1.0f, (float)si, 0, 2);
            else
                alphaScale = interpolate<float>(1.0f, 0.01f, (float)si, MAX_SLICE / 2, MAX_SLICE);


            int dz = (int)(startZ + (si / (float)(MAX_SLICE)) * laneLength);
            //pVertex[0] = laneModelData.lane_Main.valueL(dz);
            pVertex[0] = -1.0f;    // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = 0.0f;
            pColor[1] = 1.0f;
            pColor[2] = 1.0f;
            pColor[3] = 0.85f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);

            //pVertex[0] = laneModelData.lane_Main.valueR(dz);
            pVertex[0] = 1.0f;  // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = 0.0f;
            pColor[1] = 1.0f;
            pColor[2] = 1.0f;
            pColor[3] = 0.85f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);
        }

        // padding index
        shaderData->indicesLength = (MAX_SLICE * 2) * 3;
        shaderData->indices = new unsigned int[shaderData->indicesLength];
        int CC = 0; // 0 is center
        for (int si = 0; si < MAX_SLICE; si++) {
            shaderData->indices[CC++] = si * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si +1) * 2 ;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = (si + 1) * 2 + 1;
        }
    } while (false);


    do {
        float CC_0[3] = { 0.015686f, 0.55294f, 0.768627f };
        float CC_1[3] = { 0.023529f, 0.08235f, 0.137254f };
        ShaderData * shaderData = &shaderDatas[MODEL_ID_MAIN_LANE];

        // parse meshs
        real_t *pVertex = NULL;
        real_t *pColor = NULL;
        size_t pStep = 0;

        // pre allocated render data
        shaderData->name = "main lane(base)";
        shaderData->materialId = 0;
        shaderData->attributeDataLength = (MAX_SLICE + 1) * 2;    // 2 is left/right point
        shaderData->attributeData = new AttributeData_V3_C4[shaderData->attributeDataLength];
        pStep = getStepSize(dataPermutationType);
        pVertex = (real_t *)shaderData->attributeData;
        pColor = (real_t *)((unsigned char *)shaderData->attributeData + sizeof(real_t) * 3);

        // padding data
        float tmpWidth = (float)(lane_Main.width);
        lane_Main.width = tmpWidth *0.95f;

        for (int si = 0; si <= MAX_SLICE; si++) {
            float colorScale = (float)si / (float)(MAX_SLICE);
            float alphaScale = 1.0f;
            alphaScale = interpolate<float>(1.0f, 0.01f, (float)si, MAX_SLICE / 2, MAX_SLICE);

            int dz = (int)(startZ * 0.8f + (si / (float)(MAX_SLICE)) * laneLength* 0.8f);

            //pVertex[0] = laneModelData.lane_Main.valueL(dz);
            pVertex[0] = -1.0f;  // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);

            pVertex[0] = 1.0f;   // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);
        }
        lane_Main.width = tmpWidth;

        // padding index
        shaderData->indicesLength = (MAX_SLICE * 2) * 3;
        shaderData->indices = new unsigned int[shaderData->indicesLength];
        int CC = 0; // 0 is center
        for (int si = 0; si < MAX_SLICE; si++) {
            shaderData->indices[CC++] = si * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = (si + 1) * 2 + 1;
        }
    } while (false);


    // calc 2 - lane left
    do {
        float CC_0[3] = { 0.003921f, 0.28627f, 0.603921f };
        float CC_1[3] = { 0.003921f, 0.14901f, 0.207843f };
        ShaderData * shaderData = &shaderDatas[MODEL_ID_LEFT_LANE];

        // parse meshs
        real_t *pVertex = NULL;
        real_t *pColor = NULL;
        size_t pStep = 0;

        // pre allocated render data
        shaderData->name = "main lane(base)";
        shaderData->materialId = 0;
        shaderData->attributeDataLength = (MAX_SLICE + 1) * 2;    // 2 is left/right point
        shaderData->attributeData = new AttributeData_V3_C4[shaderData->attributeDataLength];
        pStep = getStepSize(dataPermutationType);
        pVertex = (real_t *)shaderData->attributeData;
        pColor = (real_t *)((unsigned char *)shaderData->attributeData + sizeof(real_t) * 3);


        // padding data
        for (int si = 0; si <= MAX_SLICE; si++) {
            float colorScale = (float)pow((float)si / (float)(MAX_SLICE), 1.0);
            float alphaScale = 1.0f;
            if (si < 2)
                alphaScale = interpolate<float>(0.01f, 1.0f, (float)si, 0, 2);
            else
                alphaScale = interpolate<float>(1.0f, 0.01f, (float)si, MAX_SLICE / 2, MAX_SLICE);


            int dz = (int)(startZ + (si / (float)(MAX_SLICE)) * laneLength);
            pVertex[0] = -1.0f;  // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);

            pVertex[0] = 1.0f;   // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);
        }

        // padding index
        shaderData->indicesLength = (MAX_SLICE * 2) * 3;
        shaderData->indices = new unsigned int[shaderData->indicesLength];
        int CC = 0; // 0 is center
        for (int si = 0; si < MAX_SLICE; si++) {
            shaderData->indices[CC++] = si * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = (si + 1) * 2 + 1;
        }
    } while (false);


    // calc 3 - lane right
    do {
        float CC_0[3] = { 0.003921f, 0.28627f, 0.603921f };
        float CC_1[3] = { 0.003921f, 0.14901f, 0.207843f };
        ShaderData * shaderData = &shaderDatas[MODEL_ID_RIGHT_LANE];

        // parse meshs
        real_t *pVertex = NULL;
        real_t *pColor = NULL;
        size_t pStep = 0;

        // pre allocated render data
        shaderData->name = "main lane(base)";
        shaderData->materialId = 0;
        shaderData->attributeDataLength = (MAX_SLICE + 1) * 2;    // 2 is left/right point
        shaderData->attributeData = new AttributeData_V3_C4[shaderData->attributeDataLength];
        pStep = getStepSize(dataPermutationType);
        pVertex = (real_t *)shaderData->attributeData;
        pColor = (real_t *)((unsigned char *)shaderData->attributeData + sizeof(real_t) * 3);


        // padding data
        for (int si = 0; si <= MAX_SLICE; si++) {
            float colorScale = (float)si / (float)(MAX_SLICE);
            float alphaScale = 1.0f;
            if (si < 2)
                alphaScale = interpolate<float>(0.01f, 1.0f, (float)si, 0, 2);
            else
                alphaScale = interpolate<float>(1.0f, 0.01f, (float)si, MAX_SLICE / 2, MAX_SLICE);


            int dz = (int)(startZ + (si / (float)(MAX_SLICE)) * laneLength);
            pVertex[0] = -1.0f;  // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);

            pVertex[0] = 1.0f;   // -1 is left, 1 is right,  real x value is calculated bu shader code.
            pVertex[1] = y - 1.0f;
            pVertex[2] = (real_t)dz;
            pColor[0] = CC_0[0] + colorScale * (CC_1[0] - CC_0[0]);
            pColor[1] = CC_0[1] + colorScale * (CC_1[1] - CC_0[1]);
            pColor[2] = CC_0[2] + colorScale * (CC_1[2] - CC_0[2]);
            pColor[3] = 0.5f * alphaScale;
            pVertex = (real_t *)((unsigned char *)pVertex + pStep);
            pColor = (real_t *)((unsigned char *)pColor + pStep);
        }

        // padding index
        shaderData->indicesLength = (MAX_SLICE * 2) * 3;
        shaderData->indices = new unsigned int[shaderData->indicesLength];
        int CC = 0; // 0 is center
        for (int si = 0; si < MAX_SLICE; si++) {
            shaderData->indices[CC++] = si * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = si * 2 + 1;
            shaderData->indices[CC++] = (si + 1) * 2;
            shaderData->indices[CC++] = (si + 1) * 2 + 1;
        }
    } while (false);


    // parse shapes
    //for (size_t si = 0; si < shaderDatas.size(); si++) {
    //    printf("parse result -------------------------------------------------------\n");
    //    printf("%s\n", shaderDatas[si].name.c_str());

    //    // pre allocated render data
    //    void *srcData = shaderDatas[si].attributeData;
    //    for (int ti = 0; ti < shaderDatas[si].attributeDataLength; ti++) {
    //        switch (this->dataPermutationType) {
    //        case AttributeDataPermutationType::TYPE_V3_C4: {
    //            AttributeData_V3_C4 * data = (AttributeData_V3_C4 *)srcData;
    //            printf("v %f %f %f , c %f %f %f \n",
    //                data->vertex[0], data->vertex[1], data->vertex[2],
    //                data->color[0], data->color[1], data->color[2], data->color[3]);
    //            srcData = (unsigned char *)srcData + sizeof(AttributeData_V3_C4);
    //        }
    //        break;
    //        }
    //    }
    //}

    // parse material
    materialDatas.resize(1);
    materialDatas[0].name = defaultMaterial.name;
    materialDatas[0].ambient_TextureName = defaultMaterial.ambient_TextureName;
    materialDatas[0].diffuse_TextureName = defaultMaterial.diffuse_TextureName;
    materialDatas[0].specular_TextureName = defaultMaterial.specular_TextureName;
    materialDatas[0].alpha_TextureName = defaultMaterial.alpha_TextureName;

    for (int vi = 0; vi < 3; vi++) {
        materialDatas[0].ambient[vi] = defaultMaterial.ambient[vi];
        materialDatas[0].diffuse[vi] = defaultMaterial.diffuse[vi];
        materialDatas[0].specular[vi] = defaultMaterial.specular[vi];
    }

    return ret;
}

bool LaneShaderData::update(LaneParabolaData mainLane)
{
    bool ret = false;

    lane_Main.cA = mainLane.cA;
    lane_Main.cB = mainLane.cB;
    lane_Main.cC = mainLane.cC;
    lane_Main.width = mainLane.width;
    lane_L = lane_Main;
    lane_L.cC += lane_Main.width * 0.9f;
    lane_R = lane_Main;
    lane_R.cC -= lane_Main.width * 0.9f;

    return ret;
}

void LaneShaderData::getAttributeData(AttributeDataPermutationType *type, size_t *attributeSize, std::vector<ShaderData> **datas)
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

void LaneShaderData::getMaterialData(std::vector<PartialMaterialData> **mtls)
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

const LaneParabolaData &LaneShaderData::getLane(LaneShaderData::LaneTypes type)
{
    switch (type) {
        case LaneShaderData::LaneTypes::MainLane:
            return lane_Main;
            break;
        case LaneShaderData::LaneTypes::LeftLane:
            return lane_L;
            break;
        case LaneShaderData::LaneTypes::RightLane:
            return lane_R;
    }
}

void LaneShaderData::getBoundingBox(float *minX, float *minY, float *minZ, float *maxX, float *maxY, float *maxZ)
{
    if (minX == NULL || minY == NULL || minZ == NULL || maxX == NULL || maxY == NULL || maxZ == NULL) {
        std::stringstream ss;
        ss << "parameter is NULL." << " | in " << __func__;
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

void LaneShaderData::release()
{
    for (size_t gi = 0; gi < shaderDatas.size(); gi++) {
        if (shaderDatas[gi].attributeData != NULL) {
            via::gl::releaseData(dataPermutationType, &shaderDatas[gi].attributeData);
        }
    }
    shaderDatas.clear();
}

LaneShaderData::LaneShaderData()
{
    dataPermutationType = AttributeDataPermutationType::TYPE_V3_C4;
    dataStepSize = getStepSize(dataPermutationType);
    defaultMaterial.name = "default-material";
    defaultMaterial.ambient[0] = 1.0f;
    defaultMaterial.ambient[1] = 1.0f;
    defaultMaterial.ambient[2] = 1.0f;
    defaultMaterial.diffuse[0] = 1.0f;
    defaultMaterial.diffuse[1] = 1.0f;
    defaultMaterial.diffuse[2] = 1.0f;
    defaultMaterial.specular[0] = 1.0f;
    defaultMaterial.specular[1] = 1.0f;
    defaultMaterial.specular[2] = 1.0f;
}

LaneShaderData::~LaneShaderData()
{
}
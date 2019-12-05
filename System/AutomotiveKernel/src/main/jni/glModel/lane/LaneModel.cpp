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
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include "glModel/lane/LaneModel.h"
#include "exceptions/Exceptions.h"

using namespace via::gl;
using namespace via::tools;

const std::string LaneModel::VERTEX_SHADER_NAME = "LaneModel.vsh";
const std::string LaneModel::FRAGMENT_SHADER_NAME = "LaneModel.fsh";
gl_ShaderData LaneModel::glShaderData;

LaneModel::LaneModel()
{
    b_isCreate = false;
    b_isInitGL = false;
    laneStatus_Main = LaneStatus::NoDetected;
    laneStatus_L = LaneStatus::NoDetected;
    laneStatus_R = LaneStatus::NoDetected;
    vboAttributeId = NULL;
    vboIndicesId = NULL;
    timeFactor.setStep(300, TimeFactor::Mode::INVERT);
}

LaneModel::~LaneModel()
{
    if (vboAttributeId != NULL) delete vboAttributeId;
    if (vboIndicesId != NULL) delete vboIndicesId;
}

bool LaneModel::create(const float startZ, const float length, const float y)
{
    bool ret = false;
    ret = attributeData.generate(startZ, length, y);
    b_isCreate = true;

    return ret;
}

void LaneModel::initGL(std::string shaderPath)
{
    if (!b_isCreate) {
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
    AttributeDataPermutationType type;
    size_t attributeSize;
    std::vector<ShaderData> *datas;
    attributeData.getAttributeData(&type, &attributeSize, &datas);

    vboAttributeId = new GLuint[datas->size()];
    vboIndicesId = new GLuint[datas->size()];
    glGenBuffers((GLsizei)datas->size(), vboAttributeId);
    glGenBuffers((GLsizei)datas->size(), vboIndicesId);

    for (int i = 0; i < datas->size(); i++) {
        glBindBuffer(GL_ARRAY_BUFFER, vboAttributeId[i]);
        glBufferData(GL_ARRAY_BUFFER, attributeSize * ((*datas)[i]).attributeDataLength, ((*datas)[i]).attributeData, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId[i]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned int) * ((*datas)[i]).indicesLength, ((*datas)[i]).indices, GL_STATIC_DRAW);
    }

    b_isInitGL = true;
}

void LaneModel::updateLane(float cA, float cB, float cC, float width)
{
    LaneParabolaData mainLane;
    mainLane.cA = cA;
    mainLane.cB = cB;
    mainLane.cC = cC;
    mainLane.width = width;
    attributeData.update(mainLane);
}

void LaneModel::setLaneStatus(LaneTypes lane, LaneStatus status)
{
    switch (lane) {
    case LaneTypes::MainLane:
        laneStatus_Main = status;
        break;
    case LaneTypes::LeftLane:
        laneStatus_L = status;
        break;
    case LaneTypes::RightLane:
        laneStatus_R = status;
        break;
    }
}

void LaneModel::getBoundingBox(glm::vec3 &minAnchor, glm::vec3 &maxAnchor)
{
    float minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
    attributeData.getBoundingBox(&minX, &minY, &minZ, &maxX, &maxY, &maxZ);
    minAnchor = glm::vec3(minX, minY, minZ);
    maxAnchor = glm::vec3(maxX, maxY, maxZ);
}

void LaneModel::draw(glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model)
{
    _draw(glShaderData, projection, view, model);
}

void LaneModel::_draw(gl_ShaderData &shader, glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model)
{
    AttributeDataPermutationType type;
    size_t stepSize;
    std::vector<ShaderData> *datas;
    std::vector<PartialMaterialData> *mtls;
    
    attributeData.getAttributeData(&type, &stepSize, &datas);
    attributeData.getMaterialData(&mtls);
    const LaneParabolaData &lane_Main = attributeData.getLane(LaneShaderData::LaneTypes::MainLane);
    const LaneParabolaData &lane_L = attributeData.getLane(LaneShaderData::LaneTypes::LeftLane);
    const LaneParabolaData &lane_R = attributeData.getLane(LaneShaderData::LaneTypes::RightLane);



    glUseProgram(shader.mShaderProgram);
    GLint uModel = glGetUniformLocation(shader.mShaderProgram, "uModel");
    GLint uView = glGetUniformLocation(shader.mShaderProgram, "uView");
    GLint uProjection = glGetUniformLocation(shader.mShaderProgram, "uProjection");
    glUniformMatrix4fv(uModel, 1, GL_FALSE, (const GLfloat *)glm::value_ptr(model));
    glUniformMatrix4fv(uView, 1, GL_FALSE, (const GLfloat *)glm::value_ptr(view));
    glUniformMatrix4fv(uProjection, 1, GL_FALSE, (const GLfloat *)glm::value_ptr(projection));
    
    GLint uScale = glGetUniformLocation(shader.mShaderProgram, "uScale");
    GLint aVertex = glGetAttribLocation(shader.mShaderProgram, "aVertex");
    GLint aColor = glGetAttribLocation(shader.mShaderProgram, "aColor");
    glUniform3f(uScale, 1.0f, 1.0f, 1.0f);


    float lightY = 100 + (float)timeFactor.getFactor() * 200;
    timeFactor.update();


    //LaneParabolaData dd;
    //dd.cA = laneModelData.lane_Main.cA;
    //dd.cB = laneModelData.lane_Main.cB;
    //dd.cC = laneModelData.lane_Main.cC;
    //dd.width = laneModelData.lane_Main.width;
    //attributeData.update(dd);

    for (int i = 0; i < datas->size(); i++) {
        GLint uParabola = glGetUniformLocation(shader.mShaderProgram, "laneData.parabola");
        switch (i) {
        case  LaneShaderData::MODEL_ID_MAIN_LANE:
            glUniform4f(uParabola, (GLfloat)lane_Main.cA, (GLfloat)lane_Main.cB, (GLfloat)lane_Main.cC, (GLfloat)(lane_Main.width * 0.95f));
            break; 
        case LaneShaderData::MODEL_ID_MAIN_OVERLAPE_LANE:
            glUniform4f(uParabola, (GLfloat)lane_Main.cA, (GLfloat)lane_Main.cB, (GLfloat)lane_Main.cC, (GLfloat)lane_Main.width);
            break;
        case LaneShaderData::MODEL_ID_LEFT_LANE:
            glUniform4f(uParabola, (GLfloat)lane_L.cA, (GLfloat)lane_L.cB, (GLfloat)lane_L.cC, (GLfloat)lane_L.width);
            break;
        case LaneShaderData::MODEL_ID_RIGHT_LANE:
            glUniform4f(uParabola, (GLfloat)lane_R.cA, (GLfloat)lane_R.cB, (GLfloat)lane_R.cC, (GLfloat)lane_R.width);
            break;
        }

        // buffer material
        int materialId = ((*datas)[i]).materialId;
        PartialMaterialData &mtl = (*mtls)[materialId];
        do {
            GLint matAmbientLoc = glGetUniformLocation(shader.mShaderProgram, "uMaterial.ambient");
            glUniform3f(matAmbientLoc, mtl.ambient[0], mtl.ambient[1], mtl.ambient[2]);
        } while (false);


        GLint uLightPos = glGetUniformLocation(shader.mShaderProgram, "uLightPos");
        GLint uDiffuse = glGetUniformLocation(shader.mShaderProgram, "uMaterial.diffuse");

        switch (i) {
        case LaneShaderData::MODEL_ID_MAIN_LANE:
        case LaneShaderData::MODEL_ID_MAIN_OVERLAPE_LANE:
            switch (laneStatus_Main) {
            case LaneStatus::NoDetected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 0.4f, 0.4f, 0.4f);
                break;
            case LaneStatus::Detected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 1.0f, 1.0f, 1.0f);
                break;
            case LaneStatus::Warning:
                glUniform3f(uLightPos, 0.0f, lightY *2, -100.0f);
                glUniform3f(uDiffuse, 1.0f, 1.0f, 0.0f);
                break;
            }
            break;
        case LaneShaderData::MODEL_ID_LEFT_LANE:
            switch (laneStatus_L) {
            case LaneStatus::NoDetected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 0.1f, 0.1f, 0.1f);
                break;
            case LaneStatus::Detected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 1.0f, 1.0f, 1.0f);
                break;
            case LaneStatus::Warning:
                glUniform3f(uLightPos, 0.0f, lightY, -100.0f);
                glUniform3f(uDiffuse, 1.0f, 0.0f, 0.0f);
                break;
            }
            break; 
        case LaneShaderData::MODEL_ID_RIGHT_LANE:
            switch (laneStatus_R) {
            case LaneStatus::NoDetected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 0.1f, 0.1f, 0.1f);
                break;
            case LaneStatus::Detected:
                glUniform3f(uLightPos, 0.0f, 0.0f, 0.0f);
                glUniform3f(uDiffuse, 1.0f, 1.0f, 1.0f);
                break;
            case LaneStatus::Warning:
                glUniform3f(uLightPos, 0.0f, lightY, -100.0f);
                glUniform3f(uDiffuse, 1.0f, 0.0f, 0.0f);
                break;
            }
            break;
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboAttributeId[i]);
        glEnableVertexAttribArray(aVertex);
        glVertexAttribPointer(aVertex, 3, GL_FLOAT, false, (GLsizei)stepSize, NULL);
        glEnableVertexAttribArray(aColor);
        glVertexAttribPointer(aColor, 4, GL_FLOAT, false, (GLsizei)stepSize, (void *)(sizeof(GLfloat) * 3));

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId[i]);
        glDrawElements(GL_TRIANGLES, (GLsizei)((*datas)[i]).indicesLength, GL_UNSIGNED_INT, NULL);
    }

    //reset 
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    glUseProgram(0);
}
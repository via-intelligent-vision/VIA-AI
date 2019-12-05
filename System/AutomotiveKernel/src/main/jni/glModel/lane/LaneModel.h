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
#include <glm/glm.hpp>
#include "tools/TimeFactor.h"
#include "glModel/core/BaseModel.h"
#include "glModel/core/BaseShaderProgram.h"
#include "LaneShaderData.h"

// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------

class LaneModel : public BaseShaderProgram, public BaseModel
{
public:
    enum class LaneTypes : unsigned char {
        MainLane,
        LeftLane,
        RightLane
    };
    enum class LaneStatus : unsigned char {
        NoDetected,
        Detected,
        Warning,
    };
    LaneModel();
    ~LaneModel();
    
    bool create(const float startZ, const float length, const float y);
    void initGL(std::string shaderPath);
    void draw(glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model);
    void getBoundingBox(glm::vec3 &minAnchor, glm::vec3 &maxAnchor);
    void updateLane(float cA, float cB, float cC, float width);
    void setLaneStatus(LaneTypes lane, LaneStatus status);
    bool isInitGL() { return b_isInitGL; };
    bool isCreated() { return b_isCreate; }


private:
    void _draw(gl_ShaderData &data, glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model);
        
    static const std::string VERTEX_SHADER_NAME;
    static const std::string FRAGMENT_SHADER_NAME;
    static gl_ShaderData glShaderData;
    bool b_isCreate;
    bool b_isInitGL;
    LaneStatus laneStatus_Main;
    LaneStatus laneStatus_L;
    LaneStatus laneStatus_R;
    LaneShaderData attributeData;
    GLuint *vboAttributeId;
    GLuint *vboIndicesId;
    via::tools::TimeFactor timeFactor;
};

// ---------------------------------------------------------------------
}   //namespace shader
}   //namespace utils
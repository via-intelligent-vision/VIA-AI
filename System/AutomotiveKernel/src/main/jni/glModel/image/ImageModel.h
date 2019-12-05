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
#include <mutex>
#include <memory>
#include <glm/glm.hpp>
#include "glModel/core/BaseModel.h"
#include "glModel/core/BaseShaderProgram.h"
#include "glModel/image/ImageShaderData.h"
// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------

class ImageModel : public BaseShaderProgram, public BaseModel
{
public:
    ImageModel();
    ~ImageModel();
    
    bool init(std::string path, std::string imgName);
    bool init();
    void release();
    void initGL(std::string vertex, std::string fragment);
    void initGL(std::string shaderPath);
    void initGL(std::string shaderPath, ImageModel *sharedTextureObj);
    void initGL(std::string shaderPath, glm::vec3 &color);
    void setMatrices(glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model);
    void draw();
    void setAlpha(float alpha);
    void setColor(float r, float g, float b);
    void getBoundingBox(glm::vec3 &minAnchor, glm::vec3 &maxAnchor);
    bool isInitGL() { return b_isInitGL; };
    bool isCreated() { return b_isInit; }
    void updateTextCoord(float x, float y, float width, float height);
    void updateRefTexture(GLuint refTexture);

private:
    std::mutex mutex_Object;
    void _draw(gl_ShaderData &data, glm::mat4 &projection, glm::mat4 &view, glm::mat4 &model);
    void updateGLAttribute();
        
    static const std::string VERTEX_SHADER_NAME;
    static const std::string FRAGMENT_SHADER_NAME;
    gl_ShaderData glShaderData;

    bool b_isInit;
    bool b_isInitGL;
    bool b_isDataExpired;
    float alpha;
    glm::vec3 diffuseColor;
    glm::mat4 projection_;
    glm::mat4 view_;
    glm::mat4 model_;
    ImageShaderData attributeData;
    size_t vboCount;
    GLuint *vboAttributeId;
    GLuint *vboIndicesId;
    gl_TextureIdData textureData;
};

// ---------------------------------------------------------------------
}   //namespace shader
}   //namespace utils
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


#include <mutex>
#ifdef _WIN32
#include <GL/glew.h>
#elif __ANDROID__
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------
class gl_ShaderData {
public:
    std::mutex mutex;
    bool isActive;
    GLuint vs;
    GLuint fs;
    GLuint mShaderProgram;

    gl_ShaderData() {
        isActive = false;
    }
};

class gl_TextureIdData {
public:
    gl_TextureIdData() {
        ambient = 0;
        diffuse = 0;
        specular = 0;
        alpha = 0;
    }

    gl_TextureIdData &operator= (gl_TextureIdData &gl_TextureIdData) {
        this->ambient   = gl_TextureIdData.ambient;
        this->diffuse   = gl_TextureIdData.diffuse;
        this->specular  = gl_TextureIdData.specular;
        this->alpha     = gl_TextureIdData.alpha;

        return *this;
    }

    GLuint ambient;
    GLuint diffuse;
    GLuint specular;
    GLuint alpha;
};

typedef float real_t;

enum class AttributeDataPermutationType : unsigned char {
    TYPE_V3_C4,
    TYPE_V3_T2,
    TYPE_V3_N3_C3_T2,
    TYPE_V3_N3_C3_T2_M1
};

#pragma pack(push, 1)
struct AttributeData_V3_C4 {
    real_t vertex[3];
    real_t color[4];    // r,g,b,a
};
#pragma pack(pop)

#pragma pack(push, 1)
struct AttributeData_V3_T2 {
    real_t vertex[3];
    real_t textCoord[2];
};
#pragma pack(pop)

#pragma pack(push, 1)
struct AttributeData_V3_N3_C3_T2 {
    real_t vertex[3];
    real_t normal[3];
    real_t color[3];
    real_t textCoord[2];
};
#pragma pack(pop)

#pragma pack(push, 1)
struct AttributeData_V3_N3_C3_T2_M1 {
    real_t vertex[3];
    real_t normal[3];
    real_t color[3];
    real_t textCoord[2];
    real_t material;
};
#pragma pack(pop)

inline size_t getStepSize(AttributeDataPermutationType type)
{
    size_t ret = 0;
    switch (type) {
        case AttributeDataPermutationType::TYPE_V3_N3_C3_T2:
            ret = sizeof(AttributeData_V3_N3_C3_T2);
            break;
        case AttributeDataPermutationType::TYPE_V3_N3_C3_T2_M1:
            ret = sizeof(AttributeData_V3_N3_C3_T2_M1);
            break;
        case AttributeDataPermutationType::TYPE_V3_T2:
            ret = sizeof(AttributeData_V3_T2);
            break;
        case AttributeDataPermutationType::TYPE_V3_C4:
            ret = sizeof(AttributeData_V3_C4);
            break;
    }

    return ret;
}

inline void releaseData(AttributeDataPermutationType type, void **data) {
    if(data != NULL && *data != NULL) {
        switch (type) {
            case AttributeDataPermutationType::TYPE_V3_C4:
                delete ((AttributeData_V3_C4 *) (*data));
                break;
            case AttributeDataPermutationType::TYPE_V3_T2:
                delete ((AttributeData_V3_T2 *) (*data));
                break;
            case AttributeDataPermutationType::TYPE_V3_N3_C3_T2:
                delete ((AttributeData_V3_N3_C3_T2 *) (*data));
                break;
            case AttributeDataPermutationType::TYPE_V3_N3_C3_T2_M1:
                delete ((AttributeData_V3_N3_C3_T2_M1 *) (*data));
                break;
        }
        *data = NULL;
    }
}

// Group data
struct ShaderData {
    std::string name;
    size_t attributeDataLength;
    void *attributeData;

    size_t indicesLength;
    unsigned int *indices;

    int materialId;

    bool visibility;

    ShaderData() {
        attributeData = NULL;
        attributeDataLength = 0;
        materialId = -1;
        visibility = true;
    }
};

class PartialMaterialData {
public:
    std::string name;
    real_t ambient[3];
    real_t diffuse[3];
    real_t specular[3];
    real_t alpha;
    std::string ambient_TextureName;     // map_Ka
    std::string diffuse_TextureName;     // map_Kd
    std::string specular_TextureName;    // map_Ks
    std::string alpha_TextureName;      // map_d

    PartialMaterialData() {
    }

    PartialMaterialData &operator= (const PartialMaterialData &src) {
        this->name = src.name;
        this->ambient[0] = src.ambient[0];
        this->ambient[1] = src.ambient[1];
        this->ambient[2] = src.ambient[2];
        this->diffuse[0] = src.diffuse[0];
        this->diffuse[1] = src.diffuse[1];
        this->diffuse[2] = src.diffuse[2];
        this->specular[0] = src.specular[0];
        this->specular[1] = src.specular[1];
        this->specular[2] = src.specular[2];
        this->alpha = src.alpha;
        this->ambient_TextureName = src.ambient_TextureName;
        this->diffuse_TextureName = src.diffuse_TextureName;
        this->specular_TextureName = src.specular_TextureName;
        this->alpha_TextureName = src.alpha_TextureName;

        return *this;
    }
};


// ---------------------------------------------------------------------
} // utils
} // shader
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
#include <mutex>
#include <glm/glm.hpp>
#include "types.h"

// ---------------------------------------------------------------------
namespace via {
namespace gl {
// ---------------------------------------------------------------------
bool loadFile(const char* filename, std::string &string);

class BaseShaderProgram
{
public:
    BaseShaderProgram();
    virtual ~BaseShaderProgram();

    //bool initShaderByPath(gl_ShaderData &data, const char* vname, const char* fname);
    bool initShaderByPath(via::gl::gl_ShaderData &data, std::string &vname, std::string &fname);
    bool initShaderByString(via::gl::gl_ShaderData &data, std::string &vertex, std::string &fragment);
};

// ---------------------------------------------------------------------
}   //namespace shader
}   //namespace utils
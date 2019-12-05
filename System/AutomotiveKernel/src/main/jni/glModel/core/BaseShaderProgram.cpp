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

#include <string>
#include <fstream>
#include <streambuf>
#include <iostream>
#ifdef _WIN32
#include <GL/glew.h>
#elif __ANDROID__
#include <GLES2/gl2.h>
#endif
#include "BaseShaderProgram.h"

#include <android/log.h>
#define  LOG_TAG    "BaseShaderProgram"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::gl;
using namespace std;

bool via::gl::loadFile(const char* filename, std::string &string)
{
    bool ret = false;
    std::ifstream fp(filename);
    if (!fp.is_open()) {
        std::cout << "Open <" << filename << "> error." << std::endl;
    }
    else {
        string = std::string((std::istreambuf_iterator<char>(fp)), std::istreambuf_iterator<char>());
        fp.close();
        ret = true;
    }
    return ret;
}

static unsigned int loadShader(std::string &source, GLenum type)
{
    unsigned int ShaderID;
    ShaderID = glCreateShader(type);

    const char* csource = source.c_str();
    glShaderSource(ShaderID, 1, &csource, NULL);
    glCompileShader(ShaderID);

    char error[1000] = "";
    glGetShaderInfoLog(ShaderID, 1000, NULL, error);
    std::cout << "Complie status : \n" << error << std::endl;

    return ShaderID;
}

bool BaseShaderProgram::initShaderByPath(gl_ShaderData &data, std::string &vname,
                                         std::string &fname)
{
    bool ret = false;
    std::string source;

    cout << "Load vertex shader \n";
    if (loadFile(vname.c_str(), source)) {
        data.vs = loadShader(source, GL_VERTEX_SHADER);
    }

    cout << "Load fragment shader \n";
    if (loadFile(fname.c_str(), source)) {
        //cout << "frag source " << endl << source << endl;
        data.fs = loadShader(source, GL_FRAGMENT_SHADER);
    }
    
    int isSuccess;
    data.mShaderProgram = glCreateProgram();
    glAttachShader(data.mShaderProgram, data.vs);
    glAttachShader(data.mShaderProgram, data.fs);
    glLinkProgram(data.mShaderProgram);
    glGetProgramiv(data.mShaderProgram, GL_LINK_STATUS, &isSuccess);
    if (isSuccess == GL_FALSE) {
        GLchar errorLog[1024] = { 0 };
        glGetProgramInfoLog(data.mShaderProgram, 1024, NULL, errorLog);
        LOGE("error linking program:  %s", errorLog);
    }
    else {
        data.isActive = true;
        ret = true;
        LOGI("Info : link shader program success.");
    }

    return ret;
}


bool BaseShaderProgram::initShaderByString(gl_ShaderData &data, std::string &vertex, std::string &fragment)
{
    bool ret = false;
    cout << "Load vertex shader \n";
    data.vs = loadShader(vertex, GL_VERTEX_SHADER);

    cout << "Load fragment shader \n";
    data.fs = loadShader(fragment, GL_FRAGMENT_SHADER);


    int isSuccess;
    data.mShaderProgram = glCreateProgram();
    glAttachShader(data.mShaderProgram, data.vs);
    glAttachShader(data.mShaderProgram, data.fs);
    glLinkProgram(data.mShaderProgram);
    glGetProgramiv(data.mShaderProgram, GL_LINK_STATUS, &isSuccess);
    if (isSuccess == GL_FALSE) {
        GLchar errorLog[1024] = { 0 };
        glGetProgramInfoLog(data.mShaderProgram, 1024, NULL, errorLog);
        LOGE("error linking program:  %s", errorLog);
    }
    else {
        data.isActive = true;
        ret = true;
        LOGI("Info : link shader program success.");
    }

    return ret;
}


BaseShaderProgram::BaseShaderProgram()
{
}


BaseShaderProgram::~BaseShaderProgram()
{
}
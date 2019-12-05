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

#include "automotiveControl/LongitudePlanner/LongitudePlan.h"

#include <android/log.h>
#define  LOG_TAG    "LongitudePlan"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


// ----------------------------------------------------------------------------------------------------------------------------------------------------
using namespace automotive::longitude;

// ----------------------------------------------------------------------------------------------------------------------------------------------------
LongitudePlan::LongitudePlan()
{
    reset();
}
void LongitudePlan::reset()
{
    desiredSpeed_ = 0.0f;
    timeStamp_ = 0;
    accelType_ = 0.0f;
    isValid_ = false;
    isControllable_ = false;
}
void LongitudePlan::copyTo(LongitudePlan &dst)
{
    dst.desiredSpeed_ = this->desiredSpeed_;
    dst.timeStamp_ = this->timeStamp_;
    dst.isValid_ = this->isValid_;
    dst.isControllable_ = this->isControllable_;
    dst.accelType_ = this->accelType_;
}


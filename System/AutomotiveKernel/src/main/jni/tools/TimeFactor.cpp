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

#include<stdio.h>
#include <math.h>

#ifdef _WIN32
#include <Windows.h>
#else 
#include <sys/time.h>
#endif
#include "tools/TimeFactor.h"

// ---------------------------------------------------------------------
using namespace via::tools;

// ---------------------------------------------------------------------
const float TimeFactor::MAX_FACTOR = 1.0f;
const float TimeFactor::MIN_FACTOR = 0.0f;

// ---------------------------------------------------------------------
double TimeFactor::getms() {
    double ret = 0;
#ifdef _WIN32
    __int64 ctr1 = 0, ctr2 = 0, freq = 0;
    QueryPerformanceCounter((LARGE_INTEGER *)&ctr2);
    QueryPerformanceFrequency((LARGE_INTEGER *)&freq);
    ret = (((ctr2 - ctr1) * 1.0 / freq) * 1000.0);
#else
    struct timeval tv;
    gettimeofday(&tv, NULL);
    ret = (double)(tv.tv_sec) * 1000.0 + (double)(tv.tv_usec) * 0.001;

#endif

    return ret;
}

TimeFactor::TimeFactor()
{
    firstTime = -1;
    timeStep = 1000;
    factor = 0.0;
    delay = 0.0;
    mode = Mode::REPEAT;
}

TimeFactor::~TimeFactor()
{
}

void TimeFactor::setStep(int step_ms, Mode mode, double delay) {
    this->timeStep = step_ms;
    this->mode = mode;
    this->delay = delay;
}

void TimeFactor::update()
{
    if (firstTime < 0) firstTime = getms();
    double f;
    double curTime = getms();
    double diff = curTime - (firstTime + delay);
    if (diff < 0) return;

    if (diff < 0.0001) diff = 0.0001f;    // prevent 0

    switch (this->mode) {
    case Mode::ONCE:
        f = diff / (timeStep);
        if (f >= 1.0f) {
            factor = 1.0f;
        }
        else {
            f = f - floor(f);
            if (factor < f) factor = f;
        }
        break;
    case Mode::REPEAT:
        f = diff / (timeStep);
        factor = f - floor(f);
        break; 
    case Mode::INVERT:
        f = diff / (timeStep * 2);
        f -= floor(f);
        f *= 2.0;

        if (f < 1.0f) {
            factor = f;
        }
        else {
            factor = 2.0 - f;
        }
        break;
    }
    
}

double TimeFactor::getFactor()
{
    return factor;
}
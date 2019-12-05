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

#include <vector>
#include <vBus/CANbusDefines.h>
#include <vBus/Honda/CANDefines.h>

namespace via {
namespace canbus {
namespace honda_crv_2017_bosch {

class CANPacker
{
public:
    static void pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, via::canbus::CANCtx_Rx &canCtx_Rx, via::canbus::CANParams_SteeringControl &param);
    static void pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, via::canbus::CANCtx_Rx &canCtx_Rx, via::canbus::CANParams_LKASHud &param);
    static void pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, via::canbus::CANCtx_Rx &canCtx_Rx, via::canbus::CANParams_ACCHud &param);
    static void pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, via::canbus::CANCtx_Rx &canCtx_Rx, via::canbus::CANParams_WheelButtons &param);
};

}
}
}
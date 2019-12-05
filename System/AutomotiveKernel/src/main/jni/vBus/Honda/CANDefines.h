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

#ifndef VIA_ADAS_CANDEFINES_H
#define VIA_ADAS_CANDEFINES_H

#define STEERING_CONTROL        228     // 2017 (228) , 2016 (404)
#define STEERING_SENSORS        330
#define POWERTRAIN_DATA         380
#define STEER_STATUS            399
#define GEARBOX                 401
#define SCM_BUTTONS             662
#define ACC_HUD                 780
#define LKAS_HUD                829
#define BRAKE_HOLD              232
#define SCM_FEEDBACK			806
#define ACC_CONTROL				479
#define ACC_CONTROL_ON				495
#define VEHICLE_HUD_10hz		772 //0x304
#define BRAKE_COMMAND           506 //0x1FA
#define GAS_COMMAND             512
#define RADAR_HUD               927
#define CAR_SPEED				777
#define BRAKE_MODULE			446
#define STALK_STATUS_2          891
#define WHEEL_SPEEDS            464
#define ENGINE_DATA             344

#endif //VIA_ADAS_CANDEFINES_H

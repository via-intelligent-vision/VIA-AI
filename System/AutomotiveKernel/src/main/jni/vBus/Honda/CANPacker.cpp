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

#include <cstdint>
#include "vBus/Honda/CANPacker.h"

#include <android/log.h>
#define  LOG_TAG    "CANPacker"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::canbus::honda_crv_2017_bosch;
using namespace via::canbus;

static unsigned int honda_checksum(unsigned int address, uint64_t d, int l) 
{
    d >>= ((8 - l) * 8); // remove padding
    d >>= 4; // remove checksum

    int s = 0;
    while (address) { s += (address & 0xF); address >>= 4; }
    while (d) { s += (d & 0xF); d >>= 4; }
    s = 8 - s;
    s &= 0xF;

    return s;
}

#ifdef NNNNN
static void can_packer(CANPackageUnit &result, int bus, int idx, unsigned TEST_ADDRESS)
{

    static unsigned char CC = 0;
    switch (TEST_ADDRESS) {
    

    case ACC_HUD: { // 2017
                      unsigned long PCM_SPEED = 0;
                      unsigned char PCM_GAS = 0;
                      unsigned char CRUISE_SPEED = 0;	// 63mph
                      unsigned char ENABLE_MINI_CAR = 0;
                      unsigned char ACC_ON = 0;
                      unsigned char ACC_PROBLEM = 0;
                      unsigned char FCM_OFF = 0;
                      unsigned char FCM_PROBLEM = 0;
                      unsigned char CRUISE_CONTROL_LABEL = 0x01;
                      unsigned char SET_TO_1 = 0x01;
                      unsigned char SET_TO_X3 = 0x03;
                      unsigned char HUD_LEAD = 0;    //HUD_LEAD   3 "acc_off" ,  2 "solid_car" ,  1 "dashed_car" ,  0 "no_car" ;
                      unsigned char HUD_DISTANCE = 0;

                      unit->mAddress = ACC_HUD;
                      unit->mBusSrc = bus;
                      unit->mDataSize = 8;
                      unit->mData[0] = 0x00;  //ZEROS_BOH
                      unit->mData[1] = 0x00;
                      unit->mData[2] = 0x00;
                      unit->mData[3] = CRUISE_SPEED;
                      unit->mData[4] = (SET_TO_1 << 4) | (FCM_OFF << 3) | (ACC_PROBLEM << 5) | (FCM_PROBLEM << 2);
                      unit->mData[5] = (HUD_DISTANCE << 6) | (HUD_LEAD << 4) | (CRUISE_CONTROL_LABEL);
                      unit->mData[6] = (SET_TO_X3 << 6) | (ACC_ON << 4);
                      //unit->mData[7] = 0xC0 | ((idx << 4) & 0x30);
                      unit->mData[7] = (idx << 4) & 0x30;
    } break;

    

    case SCM_BUTTONS: {
                          int CRUISE_BUTTONS = 2; // 7 "tbd" , 6 "tbd" , 5 "tbd" , 4 "accel_res" , 3 "decel_set" , 2 "cancel" , 1 "main", 0 "none"
                          int CRUISE_SETTING = 0; // 3 "distance_adj" ,  2 "tbd" ,  1 "lkas_button" , 0 "none"
                          unsigned char couldCtl = 0;

                          unit->mAddress = SCM_BUTTONS;
                          unit->mBusSrc = bus;
                          unit->mDataSize = 4;
                          unit->mData[0] = (CRUISE_BUTTONS << 5) | (CRUISE_SETTING) << 2;
                          unit->mData[1] = 0x00;
                          unit->mData[2] = 0x00;
                          unit->mData[3] = (idx << 4) & 0x30;
    }break;

    case BRAKE_HOLD:{
                        unsigned long XMISSION_SPEED = 0;
                        unsigned long COMPUTER_BRAKE = 0;
                        unsigned char COMPUTER_BRAKE_REQUEST = 0;
                        unit->mAddress = BRAKE_HOLD;
                        unit->mBusSrc = bus;
                        unit->mDataSize = 7;
                        unit->mData[0] = XMISSION_SPEED >> 8;
                        unit->mData[1] = (XMISSION_SPEED & 0x0FF);
                        unit->mData[2] = 0x00;
                        unit->mData[3] = (COMPUTER_BRAKE_REQUEST << 5);
                        unit->mData[4] = COMPUTER_BRAKE >> 8;
                        unit->mData[5] = (COMPUTER_BRAKE & 0x0FF);
                        unit->mData[6] = (idx << 4) & 0x30;
    } break;


    case GAS_COMMAND: { // 2016
                          unsigned char ENABLE = 0;
                          unsigned long vGAS_COMMAND = 0;
                          unsigned long vGAS_COMMAND2 = 0;
                          unit->mAddress = GAS_COMMAND;
                          unit->mBusSrc = bus;
                          unit->mDataSize = 6;
                          unit->mData[0] = vGAS_COMMAND >> 8;
                          unit->mData[1] = (vGAS_COMMAND & 0x0FF);
                          unit->mData[2] = vGAS_COMMAND2 >> 8;
                          unit->mData[3] = (vGAS_COMMAND2 & 0x0FF);
                          unit->mData[4] = (ENABLE << 7);
                          unit->mData[5] = (idx << 4) & 0x30;
    }break;

    case BRAKE_COMMAND: { // 2016
                            unsigned long COMPUTER_BRAKE = 0;
                            unsigned char BRAKE_PUMP_REQUEST = (COMPUTER_BRAKE > 0);
                            unsigned char CRUISE_OVERRIDE = 0;
                            unsigned char CRUISE_FAULT_CMD = 0;
                            unsigned char CRUISE_CANCEL_CMD = 0;
                            unsigned char COMPUTER_BRAKE_REQUEST = (COMPUTER_BRAKE > 0);
                            unsigned char SET_ME_0X80 = 0x80;
                            unsigned char BRAKE_LIGHTS = (COMPUTER_BRAKE > 0);
                            unsigned char CHIME = 0;
                            unsigned long FCW = 0;

                            unit->mAddress = BRAKE_COMMAND;
                            unit->mBusSrc = bus;
                            unit->mDataSize = 8;
                            unit->mData[0] = 0x00;
                            unit->mData[1] = 0x00;
                            unit->mData[2] = 0x00;
                            unit->mData[3] = SET_ME_0X80;
                            unit->mData[4] = 0x00;
                            unit->mData[5] = 0x00;
                            unit->mData[6] = 0x00;
                            unit->mData[7] = (idx << 4) & 0x30;
    } break;

    case RADAR_HUD: {
                        unsigned long ACC_ALERTS = 0;
                        unsigned long LEAD_SPEED = 0x1FE;
                        unsigned long LEAD_STATE = 0x7;
                        unsigned long LEAD_DISTANCE = 0x1E;
                        unsigned char SET_TO_1 = 0x01;

                        unit->mAddress = RADAR_HUD;
                        unit->mBusSrc = bus;
                        unit->mDataSize = 8;
                        unit->mData[0] = 0x00;
                        unit->mData[1] = SET_TO_1 << 5;
                        unit->mData[2] = 0x00;
                        unit->mData[3] = 0x00;
                        unit->mData[4] = 0x00;
                        unit->mData[5] = 0x00;
                        unit->mData[6] = 0x00;
                        unit->mData[7] = (idx << 4) & 0x30;
    }
        break;

    default:
        delete unit;
        unit = NULL;
        break;
    }


    // Add honda Checksum
    if (unit != NULL) {
        uint64_t dat = 0;
        dat |= ((uint64_t)unit->mData[7] & 0x0FF);
        dat |= ((uint64_t)unit->mData[6] & 0x0FF) << 8;
        dat |= ((uint64_t)unit->mData[5] & 0x0FF) << 16;
        dat |= ((uint64_t)unit->mData[4] & 0x0FF) << 24;
        dat |= ((uint64_t)unit->mData[3] & 0x0FF) << 32;
        dat |= ((uint64_t)unit->mData[2] & 0x0FF) << 40;
        dat |= ((uint64_t)unit->mData[1] & 0x0FF) << 48;
        dat |= ((uint64_t)unit->mData[0] & 0x0FF) << 56;

        unsigned int checksum = honda_checksum(unit->mAddress, dat, unit->mDataSize);
        unit->mData[unit->mDataSize - 1] |= (unsigned char)(checksum & 0x0F);

        result.push_back(unit);
    }
}
#endif

static void addCheckSum(CANPackageUnit *unit)
{
    // Add honda Checksum
    if(unit != NULL) {
        uint64_t dat = 0;
        dat |= ((uint64_t)unit->mData[7] & 0x0FF);
        dat |= ((uint64_t)unit->mData[6] & 0x0FF) << 8;
        dat |= ((uint64_t)unit->mData[5] & 0x0FF) << 16;
        dat |= ((uint64_t)unit->mData[4] & 0x0FF) << 24;
        dat |= ((uint64_t)unit->mData[3] & 0x0FF) << 32;
        dat |= ((uint64_t)unit->mData[2] & 0x0FF) << 40;
        dat |= ((uint64_t)unit->mData[1] & 0x0FF) << 48;
        dat |= ((uint64_t)unit->mData[0] & 0x0FF) << 56;

        unsigned int checksum = honda_checksum( unit->mAddress, dat, unit->mDataSize);
        unit->mData[unit->mDataSize - 1] |= (unsigned char)(checksum & 0x0F);
    }
}

void CANPacker::pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, CANCtx_Rx &canCtx_Rx, CANParams_SteeringControl &param)
{
    CANPackageUnit unit;
    
    unsigned char STEER_TORQUE_REQUEST = param.steerTorqueRequest;
    unit.mAddress = STEERING_CONTROL;
    unit.mBusSrc = (unsigned char)bus;
    unit.mDataSize = 5;
    if (STEER_TORQUE_REQUEST == 1) {
        unit.mData[0] = (param.steerTorque >> 8) & 0xFF;   //STEER_TORQUE v0
        unit.mData[1] = (param.steerTorque & 0xFF);    //STEER_TORQUE v1
        unit.mData[2] = (STEER_TORQUE_REQUEST << 7);   // STEER_TORQUE_REQUEST
        unit.mData[3] = 0x00;
        unit.mData[4] = (idx << 4) & 0x30;
    }
    else {
        unit.mData[0] = 0x00;   //STEER_TORQUE v0
        unit.mData[1] = 0x00;   //STEER_TORQUE v1
        unit.mData[2] = (STEER_TORQUE_REQUEST << 7);   // STEER_TORQUE_REQUEST
        unit.mData[3] = 0x00;
        unit.mData[4] = (idx << 4) & 0x30;
    }
    addCheckSum(&unit);

    unitList.push_back(unit);
}

void CANPacker::pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, CANCtx_Rx &canCtx_Rx, CANParams_LKASHud &param)
{
    CANPackageUnit unit;
    unsigned char SOLID_LANE = (unsigned char)((param.isLaneDetected) & 0x01);
    unsigned char DASH_LANE = (unsigned char)((~param.isLaneDetected) & 0x01);
    unsigned char BEEP = param.beep ;//(mFrameId_HUD %500 == 0);
    unsigned char STEER_REQUIRE = 0;
    if(param.steering_requeired) STEER_REQUIRE = 1;

    unit.mAddress = LKAS_HUD;
    unit.mBusSrc = bus;
    unit.mDataSize = 5;
    if (canCtx_Rx.canSafetyFeatureParams.isEnabled_LKS) {
        unit.mData[0] = 0x41;
        unit.mData[1] = (SOLID_LANE << 2) | (DASH_LANE << 6) | STEER_REQUIRE;  // solid lane (0x04) , dash lane(0x40)
        unit.mData[2] = BEEP;
        unit.mData[3] = 0x48;
        unit.mData[4] = (unsigned char)((idx << 4) & 0x30);
    }
    else {
        unit.mData[0] = 0x41;
        unit.mData[1] = 0x00;   // solid lane (0x04) , dash lane(0x40)
        unit.mData[2] = 0x00;
        unit.mData[3] = 0x48;
        unit.mData[4] = (unsigned char)((idx << 4) & 0x30);
    }
    addCheckSum(&unit);

    unitList.push_back(unit);
}

void CANPacker::pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, CANCtx_Rx &canCtx_Rx, CANParams_WheelButtons &param)
{
    CANPackageUnit unit;
    unsigned char  CRUISE_BUTTONS = 0; // 7 "tbd" , 6 "tbd" , 5 "tbd" , 4 "accel_res" , 3 "decel_set" , 2 "cancel" , 1 "main", 0 "none"
    unsigned char  CRUISE_SETTING = 0; // 3 "distance_adj" ,  2 "tbd" ,  1 "lkas_button" , 0 "none"
    unsigned char couldCtl = 0;

    if(param.accumlate_)
        CRUISE_BUTTONS = 4;
    else if(param.decelerate_)
        CRUISE_BUTTONS = 3;
    else if(param.cancel_control_)
        CRUISE_BUTTONS = 2;
    else if(param.controller_toggle_)
        CRUISE_BUTTONS = 1;

    unit.mAddress = SCM_BUTTONS;
    unit.mBusSrc = bus;
    unit.mDataSize = 4;
    unit.mData[0] = (CRUISE_BUTTONS << 5) | (CRUISE_SETTING) << 2;
    unit.mData[1] = 0x00;
    unit.mData[2] = 0x00;
    unit.mData[3] = (unsigned char)((idx << 4) & 0x30);

    addCheckSum(&unit);

    unitList.push_back(unit);
}
void CANPacker::pack(std::vector<CANPackageUnit> &unitList, unsigned char bus, int idx, CANCtx_Rx &canCtx_Rx, CANParams_ACCHud &param)
{

}
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

#include <stdio.h>
#include <string.h>
#include <vBus/Honda/CANPaser.h>
#include <android/log.h>

#include <android/log.h>
#define  LOG_TAG    "CANPaser"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::canbus::honda_crv_2017_bosch;

void CANPaser::parse(uint32_t *data, int recvSize, via::canbus::CANCtx_Rx &canCtx_Rx)
{
    // populate message
    unsigned char frameData[16];

    for (int i = 0; i<(recvSize / 0x10); i++) {
        unsigned int canID = 0;
        if (data[i * 4] & 4) {
            // extended
            canID = data[i * 4] >> 3;
        }
        else {
            // normal
            canID = data[i * 4] >> 21;
        }

        int busTime = data[i * 4 + 1] >> 16;
        int len = data[i * 4 + 1] & 0xF;
        int src = (data[i * 4 + 1] >> 4) & 0xff;


        memcpy(frameData, (uint8_t*)&data[i * 4 + 2], len);


        bool pInfo = true;
        static int CC = 0;
        switch (canID) {
            case CAR_SPEED: {
                canCtx_Rx.canSpeedParams.roughSpeed = (float)frameData[2];
            } break;

            case STEERING_SENSORS: {
                short s0 = (short)frameData[0];
                short s1 = (short)frameData[1];
                short data = (short)(s0 << 8 | s1);
                canCtx_Rx.canSteeringSensorParams.steerAngle = (float)(data) * 0.1f;

                s0 = (short)frameData[2];
                s1 = (short)frameData[3];
                data = (short)(s0 << 8 | s1);
                canCtx_Rx.canSteeringSensorParams.steerAngleRate = (float)(data) ;


                char angleOffset = (char)frameData[4];
                canCtx_Rx.canSteeringSensorParams.angleOffset = (float)(angleOffset) * 0.1f;
                //LOGE("canCtx_Rx.canSteeringSensorParams.angleOffset %f", canCtx_Rx.canSteeringSensorParams.angleOffset);
                //LOGE("canCtx_Rx.canSteeringSensorParams.steerAngle  %d ,  %f", data, canCtx_Rx.canSteeringSensorParams.steerAngle );
                //    LOGE("canCtx_Rx.canSteeringSensorParams.steerAngleRate  %d ,  %f", data, canCtx_Rx.canSteeringSensorParams.steerAngleRate );
            } break;

            case WHEEL_SPEEDS: {
                float WHEEL_SPEED_FL = (float)( ((unsigned short)frameData[0] << 7) |               // unit :  raw data
                                              ((unsigned short)frameData[1] >> 1) );
                float WHEEL_SPEED_FR = (float)( ((unsigned short)(frameData[1] & 0x01) << 14) |     // unit :  raw data
                                              ((unsigned short)frameData[1] << 6) |
                                              ((unsigned short)frameData[2] >> 2));
            } break;

            case ENGINE_DATA: {
                float XMISSION_SPEED = (float)((short)frameData[0] << 8 | (short)frameData[1]);    // unit :  raw data
                XMISSION_SPEED = XMISSION_SPEED * 0.002759506f + 0; // m/s
                XMISSION_SPEED *= 3.6; // hm/h
                canCtx_Rx.canSpeedParams.engineSpeed = XMISSION_SPEED;
            } break;

            case GEARBOX: {
                unsigned char g = (frameData[0] & 0x2F);
                //printf("GEAR_SHIFTER [ %d ] \n", g);
            } break;

            case POWERTRAIN_DATA: {
                unsigned char ACC_STATUS = (frameData[4] & 0x40) >> 6;
                //LOGE("[POWERTRAIN_DATA] [ACC_STATUS] %d", ACC_STATUS);
                canCtx_Rx.canSafetyFeatureParams.isGasPressed = (unsigned char)(frameData[4] >> 7);
            } break;

            case STEER_STATUS: {   //STEER_STATUS
                canCtx_Rx.canSteeringSensorParams.steerStatus = frameData[4] >> 4;
                canCtx_Rx.canSteeringSensorParams.steerControlActive = (frameData[4] & 0x08) >> 3;

                short s0 = (short)frameData[0];
                short s1 = (short)frameData[1];
                short v = (short)(s0 << 8 | s1);
                canCtx_Rx.canSteeringSensorParams.steerTorque = v;
            } break;

            case ACC_HUD: {
                //		printf("ACC_HUD bus time %d\n", busTime);
                //	        printf("ACC_HUD src %d\n", src);
                //            for(int i = 0; i < len ; i++) {
                //                printf("ACC_HUD [%d] : %X \n", i, frameData[i]);
                //            }
                //            printf("\n");
                //unsigned int hudDistance = (unsigned int)(frameData[5] & 0xC0) >> 6;
                //unsigned int hudLead     = (unsigned int)(frameData[5] & 0x30) >> 4;
                //printf("[ACC_HUD] [HUD_DISTANCE] [ %d ]  ......... HUD_LEAD [ %d ]    .................. %d \n", hudDistance, hudLead, CC);
                canCtx_Rx.canACCHudParams.cruiseSpeed = (int)frameData[3] & 0xFF;
                canCtx_Rx.canACCHudParams.accOn = (u_char)((int)(frameData[6] >> 4) & 0x01);
            } break;

            case LKAS_HUD: {
                //	printf("src %d\n", src);
                //printf("src [%d]  LKAS_HUD [4] : %X \n", src, frameData[4]);
                //  for(int i = 0; i < len ; i++) {
                //      printf("LKAS_HUD [%d] : %X \n", i, frameData[i]);
                // }
                // printf("\n");

                //LOGE("\t\t\t\t\t\t\t\t[%3d] LKAS_HUD  .... idx %d , data[1] %X , bus time %d\n", src, frameData[4] >> 4, frameData[1], busTime);
            } break;

            case STEERING_CONTROL: {
                //printf("STEERING_CONTROL src %d\n", src);
                //for(int i = 0; i < len ; i++) {
                //	printf("STEERING_CONTROL [%d] : %X \n", i, frameData[i]);
                //}
                //printf("\n");

                //if(src !=1)	printf("\t\t\t\t\t\t\t\t[%3d] STEERING_CONTROL .... idx %d\n", src ,frameData[4] >> 4);
            } break;

            case SCM_FEEDBACK: {
                unsigned char mainON = (unsigned char)((frameData[3] >> 4) & 0x01);
                canCtx_Rx.canSafetyFeatureParams.isControlSystemReady = mainON;
                canCtx_Rx.canSafetyFeatureParams.isEnabled_ACC = mainON && canCtx_Rx.canHealth.controlsAllowed;
                canCtx_Rx.canSafetyFeatureParams.isEnabled_LKS = mainON && canCtx_Rx.canHealth.controlsAllowed;

                canCtx_Rx.canDriverControllers.rightBlinkerOn = (unsigned char)((frameData[3] >> 3) & 0x01);
                canCtx_Rx.canDriverControllers.leftBlinkerOn = (unsigned char)((frameData[3] >> 2) & 0x01);
            } break;

            case SCM_BUTTONS: {
                if(src == 0) {
                    u_char v = (unsigned char) ((frameData[0] >> 5) & 0xFF);
                    canCtx_Rx.canDriverControllers.accelBtnOn_ = (v == 4);
                    canCtx_Rx.canDriverControllers.decelBtnOn_ = (v == 3);
                    canCtx_Rx.canDriverControllers.cancelBtnOn_ = (v == 2);

                    v = (unsigned char) ((frameData[0] >> 2) & 0x03);
                    canCtx_Rx.canDriverControllers.lksBtnOn_ = (v == 1);

                }
            } break;

            case STALK_STATUS_2: {
                unsigned char WIPERS = (unsigned char)(frameData[2] & 0x03);
                canCtx_Rx.canDriverControllers.wiperStatus = WIPERS;

            } break;

            case ACC_CONTROL: {
                unsigned char CONTROL_ON =  (unsigned char)((frameData[2] >> 5) & 0x07);
                //			printf("src %d\n", src);
                //            //printf("MAIN_ON %d\n", MAIN_ON);
                //            for(int i = 0; i < len ; i++) {
                //                printf("ACC_CONTROL [%d] : %X \n", i, frameData[i]);
                //            }
                //            printf("\n");
                //LOGE("[ACC_CONTROL] [CONTROL_ON] %d\n", CONTROL_ON);
            } break;

            case ACC_CONTROL_ON: {
                unsigned char CONTROL_ON =  (unsigned char)((frameData[0] >> 7) & 0x01);
                //			printf("src %d\n", src);
                //            //printf("MAIN_ON %d\n", MAIN_ON);
                //            for(int i = 0; i < len ; i++) {
                //                printf("ACC_CONTROL [%d] : %X \n", i, frameData[i]);
                //            }
                //            printf("\n");
                //LOGE("[ACC_CONTROL_ON] [CONTROL_ON] %d\n", CONTROL_ON);
            } break;

            case BRAKE_COMMAND: {
                //			printf("BRAKE_COMMAND bus %d  , len %d\n",  src, len);
                //            for(int i = 0; i < len ; i++) {
                //                printf("BRAKE_COMMAND [%d] : %X \n", i, frameData[i]);
                //            }
                //            printf("-----------------------------------------------\n");
            } break;

            case GAS_COMMAND: {
                //            printf("GAS_COMMAND bus %d  , len %d\n",  src, len);
                //            for(int i = 0; i < len ; i++) {
                //                printf("GAS_COMMAND [%d] : %X \n", i, frameData[i]);
                //            }
                //            printf("-----------------------------------------------\n");
            } break;

            case RADAR_HUD: {
                //    printf("RADAR_HUD bus %d  , len %d\n",  src, len);
                //     for(int i = 0; i < len ; i++) {
                //        printf("RADAR [%d] : %X \n", i, frameData[i]);
                //    }
                //    printf("-----------------------------------------------\n");
            } break;

            case BRAKE_MODULE: {
                // user brake signal on 0x17C reports applied brake from computer brake on accord
                // and crv, which prevents the usual brake safety from working correctly. these
                // cars have a signal on 0x1BE which only detects user's brake being applied so
                // in these cases, this is used instead.
                // most hondas: 0x17C bit 53
                // accord, crv: 0x1BE bit 4
                //#define IS_USER_BRAKE_MSG(to_push) (!honda_alt_brake_msg ? to_push->RIR>>21 == 0x17C : to_push->RIR>>21 == 0x1BE)
                //#define USER_BRAKE_VALUE(to_push)  (!honda_alt_brake_msg ? to_push->RDHR & 0x200000  : to_push->RDLR & 0x10)
                // exit controls on rising edge of brake press or on brake press when
                // speed > 0
                static unsigned char brake_prev = 0;

                unsigned char brake = (frameData[0] & 0x10) >> 4;
                if (brake && (!(brake_prev) || canCtx_Rx.canSpeedParams.roughSpeed)) {
                    canCtx_Rx.canHealth.controlsAllowed = 0;
                    canCtx_Rx.canSafetyFeatureParams.isBrakePressed = 1;
                }
                else {
                    canCtx_Rx.canSafetyFeatureParams.isBrakePressed = 0;
                }
                brake_prev = brake;
            } break;

            default:
                pInfo = false;
                break;
        }
    }

}
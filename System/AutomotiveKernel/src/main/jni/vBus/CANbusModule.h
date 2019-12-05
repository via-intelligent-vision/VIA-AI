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
#include <thread>
#include <fstream>
#include <memory>
#include <car/CarDefines.h>
#include <vBus/CANbusDefines.h>
#include <vBus/CANDongles.h>
#include <vBus/CANPasers.h>
#include <vBus/CANPackers.h>

namespace via {
namespace canbus {

class CANbusModule
{
public:
    CANbusModule();
    ~CANbusModule();

    bool init(via::car::CarTypes carType, CANDongleTypes dongleType);
    void exec();
    void release();

    bool isInit();
    bool isExit();
    bool isRecording() ;

    CANDongleTypes getDongleType();
    std::string getRecordPath();

    void stopRecord();
    bool startRecord(std::string path, bool appendFile);
    void record(uint32_t *data, int recvSize);

    void setGPS(double latitude, double longitude);

    /**
    Don't call this method directory.
    */
    bool isDongleConnected();
    bool connectDongle(ConnectDataBundle *dataBundle);
    void doDongleInit();
    void doDongleRelease();
    void doDongleCANHealth(CANbusModule *module);
    void doDongleCANRecv(CANbusModule *module);
    void doDongleCANSend(CANbusModule *module);
    
    /** access Tx context
    */
    void Tx_SteeringControl(CANParams_SteeringControl &param);
    void Tx_LKAS_HUD(CANParams_LKASHud &param);
    void Tx_ACC_HUD(CANParams_ACCHud &param);
    void Tx_WheelButtons(CANParams_WheelButtons &param);

    /** access Rx context
    */
    void Rx_CANHealth(CANHealth &param);
    void Rx_SpeedParams(CANParams_Speed &param);
    void Rx_SteeringSensorParams(CANParams_SteeringSensor &param);
    void Rx_SteeringControlParams(CANParams_SteeringControl &param);
    void Rx_LKAS_HUD(CANParams_LKASHud &param);
    void Rx_ACC_HUD(CANParams_ACCHud &param);
    void Rx_SafetyFeature(CANParams_SafetyFeature &param);
    void Rx_DriverControllers(CANParams_DriverControllers &param);

    /** Manual set data
     */
    void manual_CANHealth(CANHealth &param);
    void manual_CANSpeedParams(CANParams_Speed &param);
    void manual_CANSteeringSensorParams(CANParams_SteeringSensor &param);
    void manual_CANSteeringControlParams(CANParams_SteeringControl &param);
    void manual_LKAS_HUD(CANParams_LKASHud &param);
    void manual_ACC_HUD(CANParams_ACCHud &param);
    void manual_SafetyFeature(CANParams_SafetyFeature &param);
    void manual_DriverControllers(CANParams_DriverControllers &param);

private:
    std::mutex recoderMutex;
    via::car::CarTypes carType;
    CANDongleTypes dongleType;

    CANCtx_Rx canCtx_Rx;
    CANCtx_Tx canCtx_Tx;

    double gpsLatitude;
    double gpsLongitude;
    clock_t gpsLastUpdateTime;
    clock_t gpsRefreshTime;

    bool flagExit;
    bool b_IsRecording;
    std::string recordPath;
    std::ofstream recordStream;
    double recordStartTime;

    unsigned int frameId_Tx;
    std::unique_ptr<CANDongle> pCANDongle;
    pthread_t threadHandle_CANSend;
    pthread_t threadHandle_CANRecv;
    pthread_t threadHandle_CANHealth;

};


}
}

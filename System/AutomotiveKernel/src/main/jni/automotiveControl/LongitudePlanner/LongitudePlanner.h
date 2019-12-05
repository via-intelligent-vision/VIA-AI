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

#ifndef VIA_AUTOMOTIVE_LONGITUDEPLANNER_H
#define VIA_AUTOMOTIVE_LONGITUDEPLANNER_H
// ----------------------------------------------------------------------------------------------------------------------------------------------------
#include <memory>
#include "mobile360/CameraCoord/CameraModule.h"
#include <car/CarContext.h>
#include <vBus/CANbusDefines.h>
#include "automotiveControl/LongitudePlanner/LongitudePlan.h"
// ----------------------------------------------------------------------------------------------------------------------------------------------------
namespace automotive {
namespace longitude {

// ----------------------------------------------------------------------------------------------------------------------------------------------------
class LongitudePlanner {
public:
    class Input {
    public:
        Input();
        via::canbus::CANParams_SafetyFeature *canSafetyFeature_;
        via::canbus::CANParams_Speed *canSpeed_;
        via::canbus::CANParams_SteeringSensor *canSteeringSensor_;
        via::canbus::CANParams_ACCHud *canACCHud;
        float driverConfigSpeed_;
    };

    LongitudePlanner();
    ~LongitudePlanner();
    void init(via::car::CarContext *refCarContext, via::camera::CameraModule *refCameraModule);
    bool isInit();
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);

    bool update(LongitudePlanner::Input &input, automotive::longitude::LongitudePlan &result);
    void setCameraModule(via::camera::CameraModule *refCameraModule);

    void stopRecord();
    bool startRecord(std::string path, bool appendFile);
    bool isRecording();
    std::string getRecordPath();

private:
    class DataImpl;
    class ConfigImpl;
    std::unique_ptr<ConfigImpl> cfg_;
    std::unique_ptr<DataImpl> data_;

    void _stopRecord();

};
// ----------------------------------------------------------------------------------------------------------------------------------------------------
}   // namespace longitude
}   // namespace automotive
#endif //VIA_AUTOMOTIVE_LONGITUDEPLANNER_H

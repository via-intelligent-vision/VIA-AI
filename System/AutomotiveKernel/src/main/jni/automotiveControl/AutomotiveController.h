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

#include <vBus/CANbusModule.h>
#include "mobile360/CameraCoord/CameraModule.h"
//#include <sensing/SensingComponent.h>
#include "mobile360/SensingComponent/SensingComponent.h"
#include "automotiveControl/Command/command.h"
#include "automotiveControl/LatitudePlanner/LatitudePlan.h"

// ---------------------------------------------------------------------------------------------------------------------------------------------------
namespace via {
namespace automotive {

// ---------------------------------------------------------------------------------------------------------------------------------------------------
enum class EventLevels : int {
    Level_0,
    Level_1,
    Level_2,
    Level_3,
    Level_N,
    LevelCount, // must be the last one
};

enum class EventTypes : unsigned char {
    // level 0
    SystemDisable,
    SystemDisable_Gas,
    SystemDisable_Brake,
    SystemDisable_InvalidCalibration,

    // level 1
    LaneDeparture,

    // level 2
    LaneDetectFail,
    CurvatureOverControl,

    // level 3
    SystemEnable,
    CalibrationStart,
    CalibrationFail,
    CalibrationSuccess,

    // level 4
    DriverClickAccelBtn,
    DriverClickDecelBtn,
    InvalidManualSpeed,
    SystemNoEnable,

    // level 5
    NoEvent,
};

class EventSlot {
public:
    EventSlot() {
        type = EventTypes::NoEvent;
        time = 0;
        level = EventLevels::Level_N;
    }

    EventSlot &operator= (const EventSlot & src) {
        this->level = src.level;
        this->type = src.type;
        this->time = src.time;
        return *this;
    }

    EventLevels level;
    EventTypes type;
    double time;
};

// ---------------------------------------------------------------------------------------------------------------------------------------------------

class AutomotiveController
{
public:
    AutomotiveController();
    ~AutomotiveController();

    bool init(via::car::CarTypes carType, std::string &cfgPath, std::string &calibExportPath, float cameraInstalledHeight, float cameraToCenterOffset);
    void release();
    const EventSlot runAutoControl();
    void pushCommand(std::unique_ptr<ControllerCommand> &cmd);

    void registerCameraModule(camera::CameraModule *ref);
    void registerCANbusModule(via::canbus::CANbusModule *ref);
    void registerSensingModule_Lane(sensing::SensingComponent *ref);
    void registerSensingModule_ForwardVehicle(sensing::SensingComponent *ref);

    bool isRecording();

    via::automotive::LatitudePlan *getLatitudePlan();

private:
    class ControllerImpl;
    std::unique_ptr<ControllerImpl> pimpl_;

};



}
}



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

#ifndef VIA_AUTOMOTIVE_COMMAND_H
#define VIA_AUTOMOTIVE_COMMAND_H
// ---------------------------------------------------------------------------------------------------------------------------------------------------
#include <stdint.h>
#include <vector>
#include <string>
#include <mutex>
#include <memory>
#include "mobile360/CameraCoord/CameraModule.h"
// ---------------------------------------------------------------------------------------------------------------------------------------------------
namespace via {
namespace automotive {
// ---------------------------------------------------------------------------------------------------------------------------------------------------

class ControllerCommand {
public:
    enum class Types : unsigned char {
        Unknown,
        StartRecord,
        StopRecord,
        StartCameraExtrinsicCalibration,
        StopCameraExtrinsicCalibration,
        SetCruiseSpeed,
    };

    ControllerCommand(ControllerCommand::Types type) {
        type_ = type;
        code_ = 0;
        msg_ = "";
    }

    virtual ~ControllerCommand() {}

    ControllerCommand::Types getType() {return type_; }

    u_int32_t getStatusCode() { return code_; }

    std::string getStatusMsg() { return msg_; }

    double getTime() { return  time_; }

    void setStatus(u_int32_t code, std::string msg) {
        code_ = code;
        msg_ = msg;
    }

    void setTime(double time) { time_ = time; }

private:
    ControllerCommand::Types type_;
    u_int32_t code_;
    std::string msg_;
    double time_;
};

class StartRecordCommand : public ControllerCommand {
public:
    enum class Codes : u_int32_t {
        SUCCESS,
        FAIL
    };
    StartRecordCommand(std::string path, bool appendFile) :
            ControllerCommand(ControllerCommand::Types::StartRecord),
            path_(path),
            appendFile_(appendFile) {}
    std::string getPath() {return path_; }
    bool isAppend() {return appendFile_; }
private:
    std::string path_;
    bool appendFile_;
};

class StopRecordCommand : public ControllerCommand {
public:
    enum class Codes : u_int32_t {
        SUCCESS,
        FAIL
    };

    StopRecordCommand() :
            ControllerCommand(ControllerCommand::Types::StopRecord) {

    }
};

class SetCruiseSpeedCommand : public ControllerCommand {
public:
    enum class Codes : u_int32_t {
        SUCCESS,
        SYSTEM_NOT_INIT,
        ACC_NOT_ENABLE,
        SPEED_OVER_CONTROL,
        SPEED_UNDER_CONTROL,
    };
    SetCruiseSpeedCommand(int speed) :
            ControllerCommand(ControllerCommand::Types::SetCruiseSpeed),
            speed_(speed) {}
    int getSpeed() {return speed_; }
    void setSpeed(int speed) { speed_ = speed;}

private:
    int speed_;
};

class StartCameraExtrinsicCalibrationCommand : public ControllerCommand {
public:
    enum class Codes : u_int32_t {
        SUCCESS,
        NO_SENSING_COMPONENT_VALID,
        SENSING_COMPONENT_NOT_IN_SENSING,
    };
    StartCameraExtrinsicCalibrationCommand(camera::CameraLocationTypes location, float installedHeight, float cameraToCenterOffset) :
            ControllerCommand(ControllerCommand::Types::StartCameraExtrinsicCalibration),
            location_(location),
            installHeight_(installedHeight) ,
            cameraToCenterOffset_ (cameraToCenterOffset) {}

    camera::CameraLocationTypes getLocation() { return location_; }

    float getInstallHeight() { return installHeight_; }

    float getCameraToCenterOffset() { return cameraToCenterOffset_; }

private:
    camera::CameraLocationTypes location_;
    float installHeight_;
    float cameraToCenterOffset_;
};



class CommandList {
public:
    void push_and_move(std::unique_ptr<ControllerCommand> &cmd, double time) {
        std::lock_guard<std::mutex> lock(mutext_);
        cmd->setTime(time);
        cmds_.push_back(std::move(cmd));
    }

    void clear() { cmds_.clear(); }

    size_t size() { return cmds_.size(); }

    std::shared_ptr<ControllerCommand> get(size_t id) {
        if (id < cmds_.size()) {
            return std::shared_ptr<ControllerCommand>(cmds_[id]);
        }
        else {
            return std::shared_ptr<ControllerCommand>(nullptr);
        }
    }

private:
    std::mutex mutext_;
    std::vector<std::shared_ptr<ControllerCommand> > cmds_;
};

class CommandListSwitcher {
public:
    CommandListSwitcher() {
        pProc = &cmdLists_[0];
        pRepo = &cmdLists_[1];
    }

    void lock() {
        mutex_.lock();
    }

    void unlock() {
        mutex_.unlock();
    }

    void swap() {
        pProc->clear();
        CommandList *tmp = pRepo;
        pRepo = pProc;
        pProc = tmp;
    }

    CommandList *getProcList() {
        return pProc;
    }

    CommandList *getRepoList() {
        return pRepo;
    }

private:
    std::mutex mutex_;
    CommandList cmdLists_[2];
    CommandList *pProc;
    CommandList *pRepo;
};


// ---------------------------------------------------------------------------------------------------------------------------------------------------
}
}
// ---------------------------------------------------------------------------------------------------------------------------------------------------
#endif //VIA_AUTOMOTIVE_COMMAND_H

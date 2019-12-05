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

#include <assert.h>
#include <sstream>
#include "vBus/CANDongles.h"
#include "vBus/CANDongles/CANDongle_commaai_whitePanda.h"
#include "vBus/CANDongles/CANDongle_UserManual.h"
#include "vBus/types.h"

#include <android/log.h>
#define  LOG_TAG    "CANDongle"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
// ---------------------------------------------------------------------------------------------------------------------------------------------------------
using namespace via::canbus;


// ---------------------------------------------------------------------------------------------------------------------------------------------------------
CANDongle::CANDongle(CANDongleTypes type)
{
    std::stringstream errStream;

    switch(type) {
        case CANDongleTypes::commaai_WhitePanda:
            pimpl = std::move(std::unique_ptr<CANDongleInterface> (new CANDongle_commaai_whitePanda()));
            break;
        case CANDongleTypes::UserManual:
            pimpl = std::move(std::unique_ptr<CANDongleInterface> (new CANDongle_UserManual()));
            break;
        default:
            errStream << "Unhandled dongle type " << (int)type;
            throw std::runtime_error(errStream.str());
    }
}

void CANDongle::init()
{
    pimpl->init();
}
bool CANDongle::connect(ConnectDataBundle *connectData)
{
    assert(connectData != NULL);
    bool ret = false;
    switch (pimpl->getTypes()) {
        case CANDongleTypes::commaai_WhitePanda:
            ret = pimpl->connect((DevFileDataBundle *)connectData);
            break;
        case CANDongleTypes::UserManual:
        default:
            ret = pimpl->connect(NULL);
            break;
    }

    return ret;
}
bool CANDongle::isConnect()
{
    return pimpl->isConnect();
}
void CANDongle::doCANHealth(CANHealth &helth, int timeout)
{
    pimpl->doCANHealth(helth, timeout);
}
int CANDongle::doCANRecv(unsigned char *data, int recvSize, int timeout)
{
    return pimpl->doCANRecv(data, recvSize, timeout);
}
void CANDongle::doCANSend(std::vector<CANPackageUnit> &dataList, int timeout)
{
    pimpl->doCANSend(dataList, timeout);
}
void CANDongle::release()
{
    pimpl->release();
}


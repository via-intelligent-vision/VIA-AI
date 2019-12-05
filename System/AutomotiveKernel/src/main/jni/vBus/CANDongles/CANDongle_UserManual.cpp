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

#include <vBus/CANDongles/CANDongle_UserManual.h>

using namespace via::canbus;


CANDongle_UserManual::CANDongle_UserManual() :
        CANDongleInterface(CANDongleTypes::UserManual) {

}

void CANDongle_UserManual::init()
{

}

bool CANDongle_UserManual::connect(void *connectData)
{
    return true;
}

bool CANDongle_UserManual::isConnect()
{
    return true;
}

void CANDongle_UserManual::doCANHealth(CANHealth &helth, int timeout)
{

}

int CANDongle_UserManual::doCANRecv(unsigned char *data, int recvSize, int timeout)
{
    return 0;
}

void CANDongle_UserManual::doCANSend(std::vector<CANPackageUnit> &dataList, int timeout)
{

}

void CANDongle_UserManual::release()
{

}
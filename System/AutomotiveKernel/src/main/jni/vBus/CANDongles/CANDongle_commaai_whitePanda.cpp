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

#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include <libusb1.0/libusb.h>
#include <vBus/CANDongles/CANDongle_commaai_whitePanda.h>

#include <android/log.h>
#define  LOG_TAG    "CANDongle_commaai_whitePanda"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::canbus;

class CANDongle_commaai_whitePanda::WhitePandaDongleImpl
{
public:
    WhitePandaDongleImpl();
    void init();
    bool connect(int natvieDevFileDescriptor);
    bool isConnect();
    void doCANHealth(CANHealth &helth, int timeout);
    int doCANRecv(unsigned char *data, int recvSize, int timeout);
    void doCANSend(std::vector<CANPackageUnit> &dataList, int timeout);
    void release();

private:

    void handle_usb_issue(int err, const char func[]);
    uint32_t *sendBuffer;
    int sendBufferSize;

    struct libusb_context *usbCtx;
    struct libusb_device_handle *usbDevHandle;
    pthread_mutex_t mutex_Dongle;
    CANDongleStatus dongleStatus;
    bool flagAbortConnect;
};




#define SAFETY_NOOUTPUT  0
#define SAFETY_HONDA 1
#define SAFETY_TOYOTA 2
#define SAFETY_ELM327 0xE327
#define SAFETY_GM 3
#define SAFETY_HONDA_BOSCH 4
#define SAFETY_FORD 5
#define SAFETY_CADILLAC 6
#define SAFETY_TOYOTA_NOLIMITS 0x1336
#define SAFETY_ALLOUTPUT 0x1337
#define SAFETY_ELM327 0xE327

CANDongle_commaai_whitePanda::WhitePandaDongleImpl::WhitePandaDongleImpl()
{
    usbCtx = NULL;
    usbDevHandle = NULL;
    sendBuffer = NULL;
    sendBufferSize = 32 * 0x10;
    pthread_mutex_init(&mutex_Dongle, NULL);
    dongleStatus = CANDongleStatus::Idle;
    flagAbortConnect = false;
}

void CANDongle_commaai_whitePanda::WhitePandaDongleImpl::init()
{
    // init libusb
    int err;
    err = libusb_init(&usbCtx);
    LOGE("libusb_init(&usbCtx);  %d\n", err);
    assert(err == 0);
    //libusb_set_debug(usbCtx, 3);
    dongleStatus = CANDongleStatus::Init;


    libusb_device **list = NULL;
    ssize_t count = libusb_get_device_list(usbCtx, &list);
    if (count < 0) {
        LOGE("no usb devices found\n");
    }
    else {
        for (size_t idx = 0; idx < count; ++idx) {
            libusb_device *device = list[idx];
            libusb_device_descriptor desc = {0};

            int rc = libusb_get_device_descriptor(device, &desc);
            LOGE("Vendor:Device = %04x:%04x\n", desc.idVendor, desc.idProduct);
        }
    }
}

void CANDongle_commaai_whitePanda::WhitePandaDongleImpl::release()
{
    LOGE("Dongle Release ......");
    flagAbortConnect = true;
    while(dongleStatus == CANDongleStatus::Connecting || dongleStatus == CANDongleStatus::Connected) {
        usleep(100);
    }
    LOGE("DongleStatus ...... ConnectAborted");
    flagAbortConnect = false;
    dongleStatus = CANDongleStatus::Release;

    if(usbDevHandle != NULL) libusb_close(usbDevHandle);
    if(usbCtx != NULL) libusb_exit(usbCtx);
    if(sendBuffer != NULL) delete[] sendBuffer;
    LOGE("Dongle Release ...... finish");
}

bool CANDongle_commaai_whitePanda::WhitePandaDongleImpl::isConnect()
{
    return (dongleStatus == CANDongleStatus::Connected);
}

bool CANDongle_commaai_whitePanda::WhitePandaDongleImpl::connect(int natvieDevFileDescriptor)
{
    const int TIMEOUT = 0;
    LOGE("attempting to connect panda , device fd %d\n", natvieDevFileDescriptor);

    dongleStatus = CANDongleStatus::Connecting;
    do {
        int err = 0;
        unsigned char is_pigeon[1] = { 0 };

        LOGD("p0\n");
        err = libusb_wrap_fd(usbCtx, natvieDevFileDescriptor, &usbDevHandle);
        //usbDevHandle = libusb_open_device_with_vid_pid(usbCtx, 0xbbaa, 0xddcc);
        if (usbDevHandle == NULL) {
            LOGE("err  %d\n", err);
            break;
        }

        int cc;
        libusb_get_configuration(usbDevHandle, &cc);
        LOGD("current configuration %d\n", cc);


        LOGD("p1, address %ld\n", (long)usbDevHandle);
        err = libusb_set_configuration(usbDevHandle, 1);
        if (err != 0) {
            LOGE("err  %d\n", err);
            break;
        }

        LOGE("p2\n");
        err = libusb_claim_interface(usbDevHandle, 0);
        if (err != 0) {
            LOGE("err  %d\n", err);
            break;
        }

        LOGE("p3\n");
        /*if (loopback_can) {
            libusb_control_transfer(usbDevHandle, 0xc0, 0xe5, 1, 0, NULL, 0, TIMEOUT);
        }
        else {
            libusb_control_transfer(usbDevHandle, 0xc0, 0xe5, 0, 0, NULL, 0, TIMEOUT);
        }*/
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xe5, 0, 0, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        // power off ESP
        LOGE("p3 ESP\n");
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xd9, 0, 0, NULL, 0, TIMEOUT);
        //libusb_control_transfer(usbDevHandle, 0xc0, 0xd9, 1, 0, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        // power on charging (may trigger a reconnection, should be okay)
        LOGE("p4\n");
#ifndef __x86_64__
        //err = libusb_control_transfer(usbDevHandle, 0xc0, 0xe6, 1, 0, NULL, 0, TIMEOUT);
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xe6, 0, 0, NULL, 0, TIMEOUT);
#else
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xe6, 0, 0, NULL, 0, TIMEOUT);
            printf("not enabling charging on x86_64\n");
#endif

        // no output is the default
        LOGE("Honda p5");
        // set if long_control is allowed by openpilot. Hardcoded to True for now
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xdf, 1, 0, NULL, 0, TIMEOUT);

        //libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_NOOUTPUT, 0, NULL, 0, TIMEOUT);
        //libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_HONDA, 0, NULL, 0, TIMEOUT);
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_HONDA_BOSCH, 1, NULL, 0, TIMEOUT);
        // libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_ALLOUTPUT, 0, NULL, 0, TIMEOUT);
        //libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_TOYOTA, 0, NULL, 0, TIMEOUT);
        //libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_ELM327, 0, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }


        // clear all Rx ring buffer
        LOGE("clear all Rx ring buffer");
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xf1, 0xFFFF, 0, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        // clear all Tx ring buffer
        LOGE("lear all Tx ring buffer ");
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xf1, 0x0, 0, NULL, 0, TIMEOUT);
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xf1, 0x1, 0, NULL, 0, TIMEOUT);
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xf1, 0x2, 0, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        libusb_control_transfer(usbDevHandle, 0xc0, 0xc1, 0, 0, is_pigeon, 1, TIMEOUT);


        // set fan speed
        //uint16_t target_fan_speed = 16384;
        //libusb_control_transfer(usbDevHandle, 0xc0, 0xd3, target_fan_speed, 0, NULL, 0, TIMEOUT);


        //pthread_mutex_lock(&usb_lock);
        // set in the mutex to avoid race
        uint16_t safetyParam = 1; // Accord and CRV 5G use an alternate user brake msg
        LOGE("set safetyParam ");
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xdc, SAFETY_HONDA_BOSCH, safetyParam, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        //pthread_mutex_unlock(&usb_lock);

        // Reset forwarding
        LOGE("Reset forwarding ");
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xdd, 1, 0xFF, NULL, 0, TIMEOUT);
        if (err < 0) { LOGE("err  %d\n", err);  break; }
        err = libusb_control_transfer(usbDevHandle, 0xc0, 0xdd, 2, 0xFF, NULL, 0, TIMEOUT);
        if (err < 0) { LOGE("err  %d\n", err);  break; }
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xdd, 1, 0xFF, NULL, 0, TIMEOUT);
        if (err < 0) { LOGE("err  %d\n", err);  break; }
        err = libusb_control_transfer(usbDevHandle, 0x40, 0xdd, 2, 0xFF, NULL, 0, TIMEOUT);
        if (err < 0) { LOGE("err  %d\n", err);  break; }

        // set forwarding
        LOGE("set forwarding ");
        libusb_control_transfer(usbDevHandle, 0xc0, 0xdd, 2, 1, NULL, 0, TIMEOUT);
       // libusb_control_transfer(usbDevHandle, 0xc0, 0xdd, 1, 2, NULL, 0, TIMEOUT);
        if (err < 0) {
            LOGE("err  %d\n", err);
            break;
        }

        dongleStatus = CANDongleStatus::Connected;
        LOGE("connected to dongle\n");
    } while (false);

    if(dongleStatus != CANDongleStatus::Connected) {
        LOGE("Fail to connect ...... %d\n", (int)dongleStatus);
    }
    if(flagAbortConnect == true) {
        dongleStatus = CANDongleStatus::ConnectAborted;
    }

    return (dongleStatus == CANDongleStatus::Connected);
}

void CANDongle_commaai_whitePanda::WhitePandaDongleImpl::doCANHealth(CANHealth &dstHealth, int timeout)
{
    // copied from board/main.c
    struct __attribute__((packed)) {
        uint32_t voltage_pkt;
        uint32_t current_pkt;
        uint32_t can_send_errs_pkt;
        uint32_t can_fwd_errs_pkt;
        uint32_t gmlan_send_errs_pkt;
        uint8_t started_pkt;
        uint8_t controls_allowed;
        uint8_t gas_interceptor_detected_pkt;
        uint8_t car_harness_status_pkt;
    }  health;
//    struct __attribute__((packed)) health{
//        uint32_t voltage;
//        uint32_t current;
//        uint8_t started;
//        uint8_t controls_allowed;
//        uint8_t gas_interceptor_detected;
//        uint8_t started_signal_detected;
//        uint8_t started_alt;
//    } health;

    // recv from board
    pthread_mutex_lock(&mutex_Dongle);
    int cnt;
    do {
        cnt = libusb_control_transfer(usbDevHandle, 0xc0, 0xd2, 0, 0, (unsigned char*)&health, sizeof(health), timeout);
        if (cnt != sizeof(health)) {
            handle_usb_issue(cnt, __func__);
            break;
        }
        if(flagAbortConnect) {
            dongleStatus = CANDongleStatus::ConnectAborted;
            break;
        }
    } while (cnt != sizeof(health));

    pthread_mutex_unlock(&mutex_Dongle);

    dstHealth.controlsAllowed = health.controls_allowed;
}

int CANDongle_commaai_whitePanda::WhitePandaDongleImpl::doCANRecv(unsigned char *data, int recvSize, int timeout)
{
    int recv;

    pthread_mutex_lock(&mutex_Dongle);
    int err;
    do {
        err = libusb_bulk_transfer(usbDevHandle, 0x81, (uint8_t*)data, recvSize, &recv, timeout);
        if (err != 0) {
            handle_usb_issue(err, __func__);
            break;
        }
        if (err == -8) { printf("overflow got 0x%x \n", recv); };
        // timeout is okay to exit, recv still happened
        if (err == -7) { break; }
        if(flagAbortConnect) {
            dongleStatus = CANDongleStatus::ConnectAborted;
            break;
        }
    } while (err != 0);

    pthread_mutex_unlock(&mutex_Dongle);

    return recv;
}

void CANDongle_commaai_whitePanda::WhitePandaDongleImpl::doCANSend(std::vector<CANPackageUnit> &dataList, int timeout)
{
    int msg_count = (int)dataList.size();
    if (sendBuffer == NULL) {
        sendBuffer = (uint32_t*)malloc(sendBufferSize);
    }
    if (sendBufferSize < ((int)dataList.size() * sizeof(CANPackageUnit))) {
        delete[] sendBuffer;
        sendBufferSize = ((int)dataList.size() * sizeof(CANPackageUnit));
        sendBuffer = (uint32_t*)malloc(sendBufferSize);
    }
    memset(sendBuffer, 0, msg_count * 0x10);

    //LOGE("canTx %d", msg_count);
    for (int i = 0; i < msg_count; i++) {
        CANPackageUnit *cmsg = &dataList[i];
        if (cmsg->mAddress >= 0x800) {
            sendBuffer[i * 4] = (cmsg->mAddress << 3) | 5;  // extended
        }
        else {
            sendBuffer[i * 4] = (cmsg->mAddress << 21) | 1; // normal
        }
        //LOGE("    Tx Addr %d", cmsg->mAddress);

        assert(cmsg->mDataSize <= 8);
        sendBuffer[i * 4 + 1] = cmsg->mDataSize | (cmsg->mBusSrc << 4);
        memcpy(&sendBuffer[i * 4 + 2], cmsg->mData, cmsg->mDataSize);
    }


    // send to board
    int err;
    int sentSize;
    pthread_mutex_lock(&mutex_Dongle);

    do {
        err = libusb_bulk_transfer(usbDevHandle, 3, (uint8_t*)sendBuffer, msg_count * 0x10, &sentSize, timeout);
        if (err != 0 || ((msg_count * 0x10) != sentSize)) {
            handle_usb_issue(err, __func__);
            break;
        }
        if(flagAbortConnect) {
            dongleStatus = CANDongleStatus::ConnectAborted;
            break;
        }
    } while (err != 0);

    pthread_mutex_unlock(&mutex_Dongle);
}

void CANDongle_commaai_whitePanda::WhitePandaDongleImpl::handle_usb_issue(int err, const char func[])
{
    LOGE("usb error %d \"%s\" in %s \n", err, libusb_strerror((enum libusb_error)err), func);
    if (err == -4) {
        LOGE("lost connection\n");
    }
    dongleStatus = CANDongleStatus::ConnectAborted;
}


// ---------------------------------------------------------------------------------------------------------------------------------------------------------
// about  CANDongle_commaai_whitePanda
// ---------------------------------------------------------------------------------------------------------------------------------------------------------
CANDongle_commaai_whitePanda::CANDongle_commaai_whitePanda():
        CANDongleInterface(CANDongleTypes::commaai_WhitePanda)
{
    pimpl = std::move(std::unique_ptr<WhitePandaDongleImpl> (new WhitePandaDongleImpl()));
}

void CANDongle_commaai_whitePanda::init()
{
    pimpl->init();
}

bool CANDongle_commaai_whitePanda::connect(void *connectData)
{
    return pimpl->connect(((DevFileDataBundle *)connectData)->nativeDevFileDescriptor);
}

bool CANDongle_commaai_whitePanda::isConnect()
{
    return  pimpl->isConnect();
}

void CANDongle_commaai_whitePanda::doCANHealth(CANHealth &health, int timeout)
{
    pimpl->doCANHealth(health, timeout);
}

int CANDongle_commaai_whitePanda::doCANRecv(unsigned char *data, int recvSize, int timeout)
{
    return pimpl->doCANRecv(data, recvSize, timeout);
}

void CANDongle_commaai_whitePanda::doCANSend(std::vector<CANPackageUnit> &dataList, int timeout)
{
    pimpl->doCANSend(dataList, timeout);
}

void CANDongle_commaai_whitePanda::release()
{
    pimpl->release();
}

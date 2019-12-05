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

#include "capture.h"

#include "util.h"
#include "yuv.h"
#include "video_device.h"
#include <assert.h>
#include <fcntl.h>
#include <errno.h>
#include <linux/videodev2.h>


char* pWatermarkImage = NULL;
int watermarkWidth, watermarkHeight = 0;

int capture_start(int fd) {
    enum v4l2_buf_type type;

    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if(-1 == xioctl(fd, VIDIOC_STREAMON, &type)) {
        return errnoexit("VIDIOC_STREAMON");
    }

    return SUCCESS_LOCAL;
}

int start_capture(int fd) {
    unsigned int i;
    enum v4l2_buf_type type;

    for(i = 0; i < BUFFER_COUNT; ++i) {
        struct v4l2_buffer buf;
        CLEAR(buf);
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = i;

        if(-1 == xioctl(fd, VIDIOC_QBUF, &buf)) {
            return errnoexit("VIDIOC_QBUF");
        }
    }

    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if(-1 == xioctl(fd, VIDIOC_STREAMON, &type)) {
        return errnoexit("VIDIOC_STREAMON");
    }

    return SUCCESS_LOCAL;
}

int read_frame(int fd, buffer* frame_buffers, int width, int height,
        int* rgb_buffer, int* y_buffer) {
    struct v4l2_buffer buf;
    CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;

    if(-1 == xioctl(fd, VIDIOC_DQBUF, &buf)) {
        switch(errno) {
            case EAGAIN:
                return 0;
            case EIO:
            default:
                return errnoexit("VIDIOC_DQBUF");
        }
    }

    assert(buf.index < BUFFER_COUNT);
//    yuyv422_to_argb(frame_buffers[buf.index].start, width, height, rgb_buffer,
//            y_buffer);

    NV21TOARGB(frame_buffers[buf.index].start, rgb_buffer, width, height);

    if(-1 == xioctl(fd, VIDIOC_QBUF, &buf)) {
        return errnoexit("VIDIOC_QBUF");
    }

    return 1;
}

jboolean bfirst = JNI_TRUE;

int read_next_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer) {
    struct v4l2_buffer buf;
    CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;

    if(-1 == xioctl(fd, VIDIOC_DQBUF, &buf)) {
        switch(errno) {
            case EAGAIN:
                return 0;
            case EIO:
            default:
                return errnoexit("VIDIOC_DQBUF");
        }
    }

    assert(buf.index < BUFFER_COUNT);
    LOGE("BufferIndex:%d",buf.index);
    int srcWidth = 720;
    int srcHeight = 504;
    unsigned char *pSrc = frame_buffers[buf.index].start;

    if(dstSurface!=NULL) {
        unsigned char *pDstBuffer = dstSurface;
//    memcpy(pDstBuffer, pSrc, srcWidth*srcHeight*3/2);
        int ySize = srcWidth * srcHeight;

        memcpy(pDstBuffer, pSrc, ySize);

        int uvSize = ySize / 2;
        int uSize = uvSize / 2;

        memcpy(pDstBuffer + ySize, pSrc + ySize + 1, uvSize - 1);
        unsigned char *nvcur = pSrc + ySize;
        unsigned char *yuvcur = pDstBuffer + ySize + 1;
        int i = 0;
        while (i < uSize) {
            (*yuvcur) = (*nvcur);
            yuvcur += 2;
            nvcur += 2;
            ++i;
        }
    }

    if(dstBuffer!=NULL) {
        memcpy(dstBuffer, pSrc, srcWidth*srcHeight*3/2);
    }

    if(bfirst==JNI_TRUE) {
        bfirst = JNI_FALSE;
    } else {
        if (-1 == xioctl(fd, VIDIOC_QBUF, &buf)) {
            return errnoexit("VIDIOC_QBUF");
        }
    }

    return 1;
}

int stop_capturing(int fd) {
    enum v4l2_buf_type type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if(-1 != fd && -1 == xioctl(fd, VIDIOC_STREAMOFF, &type)) {
        return errnoexit("VIDIOC_STREAMOFF");
    }

    return SUCCESS_LOCAL;
}

void process_camera(int fd, buffer* frame_buffers, int width,
        int height, int* rgb_buffer, int* ybuf) {
    if(fd == -1) {
        return;
    }

    for(;;) {
        fd_set fds;
        FD_ZERO(&fds);
        FD_SET(fd, &fds);

        struct timeval tv;
        tv.tv_sec = 2;
        tv.tv_usec = 0;

        int result = select(fd + 1, &fds, NULL, NULL, &tv);
        if(-1 == result) {
            if(EINTR == errno) {
                continue;
            }
            errnoexit("select");
        } else if(0 == result) {
            LOGE("select timeout");
        }

        if(read_frame(fd, frame_buffers, width, height, rgb_buffer, ybuf) == 1) {
            break;
        }
    }
}


void process_next_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer) {
    if(fd == -1) {
        return;
    }

    for(;;) {
        fd_set fds;
        FD_ZERO(&fds);
        FD_SET(fd, &fds);

        struct timeval tv;
        tv.tv_sec = 2;
        tv.tv_usec = 0;

        int result = select(fd + 1, &fds, NULL, NULL, &tv);
        if(-1 == result) {
            if(EINTR == errno) {
                continue;
            }
            errnoexit("select");
        } else if(0 == result) {
            LOGE("select timeout");
        }

        if(read_next_frame(fd, frame_buffers, dstSurface, dstBuffer) == 1) {
            break;
        }
    }
}


void dequeue_next_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer, int w, int h) {
    if(fd == -1) {
        return;
    }

    for(;;) {
        fd_set fds;
        FD_ZERO(&fds);
        FD_SET(fd, &fds);

        struct timeval tv;
        tv.tv_sec = 2;
        tv.tv_usec = 0;

        int result = select(fd + 1, &fds, NULL, NULL, &tv);
        if(-1 == result) {
            if(EINTR == errno) {
                continue;
            }
            errnoexit("select");
        } else if(0 == result) {
            LOGE("select timeout");
        }

        if(dequeue_frame(fd, frame_buffers, dstSurface, dstBuffer, w, h) == 1) {
            break;
        }
    }
}


void stop_camera(int* fd, int* rgb_buffer, int* y_buffer) {
    stop_capturing(*fd);
    uninit_device();
    close_device(fd);

    if(rgb_buffer) {
        free(rgb_buffer);
    }

    if(y_buffer) {
        free(y_buffer);
    }
}

int dequeue_last_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer, int w, int h) {
    struct v4l2_buffer buf;
    struct v4l2_buffer buf_tmp;

    CLEAR(buf_tmp);
    buf_tmp.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf_tmp.memory = V4L2_MEMORY_MMAP;

    buf.index = -1;

    while(-1 != xioctl(fd, VIDIOC_DQBUF, &buf_tmp)) {
        if(buf.index!=-1) {
            xioctl(fd, VIDIOC_QBUF, &buf);
        }

        buf = buf_tmp;
    }
    if(buf.index==-1) return -1;

    assert(buf.index < BUFFER_COUNT);
    LOGE("BufferIndex:%d",buf.index);
    int srcWidth = w;
    int srcHeight = h;
    unsigned char *pSrc = frame_buffers[buf.index].start;

    if(dstSurface!=NULL) {
        unsigned char *pDstBuffer = dstSurface;
        int ySize = srcWidth * srcHeight;

        memcpy(pDstBuffer, pSrc, ySize);

        int uvSize = ySize / 2;
        int uSize = uvSize / 2;

        memcpy(pDstBuffer + ySize, pSrc + ySize + 1, uvSize - 1);
        unsigned char *nvcur = pSrc + ySize;
        unsigned char *yuvcur = pDstBuffer + ySize + 1;
        int i = 0;
        while (i < uSize) {
            (*yuvcur) = (*nvcur);
            yuvcur += 2;
            nvcur += 2;
            ++i;
        }
    }

    if(dstBuffer!=NULL) {
        memcpy(dstBuffer, pSrc, srcWidth*srcHeight*3/2);
    }

    return buf.index;
}

void mix_watermark(char *pSrcImage, int srcWidth, int srcHeight, char *pWaterImage, int waterWidth,
                   int waterHeight, float watermarkAlpha) {

    float srcAlpha = 1-watermarkAlpha;

    char* dstTmp = pSrcImage;
    char* pWatermarkTmp = pWaterImage;

    int jumpHeight = 0;
    int jumpWidth = srcWidth - waterWidth;

    dstTmp += jumpHeight*srcWidth;
    dstTmp += jumpWidth;
    for(int h=0;h<waterHeight;h++) {
        for (int w = 0; w < waterWidth; w++) {
            (*(dstTmp+w)) = (*(dstTmp+w))*srcAlpha + (*(pWatermarkTmp+w))*watermarkAlpha;
        }
        dstTmp += srcWidth;
        pWatermarkTmp += watermarkWidth;
    }

    //UV
    dstTmp = pSrcImage+srcHeight*srcWidth;
    pWatermarkTmp = pWaterImage+waterWidth*waterHeight;
    jumpHeight = 0;
    jumpWidth = srcWidth - waterWidth;
    dstTmp += jumpHeight*srcWidth;
    dstTmp += jumpWidth;
    for(int h=0;h<waterHeight/2;h++) {
        for (int w = 0; w < waterWidth; w++) {
            (*(dstTmp+w)) = (*(dstTmp+w))*srcAlpha + (*(pWatermarkTmp+w))*watermarkAlpha;
        }
        dstTmp += srcWidth;
        pWatermarkTmp += watermarkWidth;
    }
}
long timeStamp = -1;

int dequeue_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer, int w, int h) {
    struct v4l2_buffer buf;
    CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;

    if(-1 == xioctl(fd, VIDIOC_DQBUF, &buf)) {
        switch(errno) {
            case EAGAIN:
                return -1;
            case EIO:
            default:
                return -1;
        }
    }

    assert(buf.index < BUFFER_COUNT);
    long bufTimeStamp = buf.timestamp.tv_sec*1000 + buf.timestamp.tv_usec/1000;
//    LOGE("TEST dequeue frame Index:%d, TimeStamp:%ld",buf.index,bufTimeStamp);
    int srcWidth = w;
    int srcHeight = h;
    unsigned char *pSrc = frame_buffers[buf.index].start;

    // show on screen
    if(dstSurface!=NULL) {
        unsigned char *pDstBuffer = dstSurface;
        int ySize = srcWidth * srcHeight;

        memcpy(pDstBuffer, pSrc, ySize);

        int uvSize = ySize / 2;
        int uSize = uvSize / 2;

        memcpy(pDstBuffer + ySize, pSrc + ySize + 1, uvSize - 1);
        unsigned char *nvcur = pSrc + ySize;
        unsigned char *yuvcur = pDstBuffer + ySize + 1;
        int i = 0;
        while (i < uSize) {
            (*yuvcur) = (*nvcur);
            yuvcur += 2;
            nvcur += 2;
            ++i;
        }
    }
    // maybe encode input source
    if(dstBuffer!=NULL) {
        memcpy(dstBuffer, pSrc, srcWidth*srcHeight*3/2);

        if(pWatermarkImage!=NULL)
            mix_watermark(dstBuffer,srcWidth, srcHeight, pWatermarkImage, watermarkWidth, watermarkHeight, 0.5);
    }

    return buf.index;
}

int queue_frame(int fd, int i) {
    struct v4l2_buffer buf;
    CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;
    buf.index = i;
//    LOGE("TEST queue frame index:%d",i);
    if (-1 == xioctl(fd, VIDIOC_QBUF, &buf)) {
        return errnoexit("VIDIOC_QBUF");
    }
}


void set_watermark(char *pImage, int w, int h) {
    watermarkWidth = w;
    watermarkHeight = h;
    int size = watermarkWidth*watermarkHeight*3/2;
    pWatermarkImage = (char*) calloc(size,sizeof(char));
    memcpy(pWatermarkImage, pImage, size);
}
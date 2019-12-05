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

#ifndef __CAPTURE_H__
#define __CAPTURE_H__

#include <jni.h>
#include <stdio.h>

#include "video_device.h"

/* Private: Begins capturing video frames from a previously initialized device.
 *
 * The buffers in FRAME_BUFFERS are handed off to the device.
 *
 * fd - a valid file descriptor to the device.
 *
 * Returns SUCCESS_LOCAL if no errors, otherwise ERROR_LOCAL.
 */
int start_capture(int fd);

/* Private: Read a single frame of video from the device into a buffer.
 *
 * The resulting image is stored in RGBA format across two buffers, rgb_buffer
 * and y_buffer.
 *
 * fd - a valid file descriptor pointing to the camera device.
 * frame_buffers - memory mapped buffers that contain the image from the device.
 * width - the width of the image.
 * height - the height of the image.
 * rgb_buffer - output buffer for RGB data.
 * y_buffer - output buffer for alpha (Y) data.
 *
 * Returns SUCCESS_LOCAL if no errors, otherwise ERROR_LOCAL.
 */
int read_frame(int fd, buffer* frame_buffers, int width, int height,
        int* rgb_buffer, int* y_buffer);


int read_next_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer);

int/*buffer index*/ dequeue_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer, int w, int h);
int/*buffer index*/ dequeue_last_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer, int w, int h);

int queue_frame(int fd, int bufIndex);


void set_watermark(char *pImage, int width, int height);
/* Private: Unconfigure the video device for capturing.
 *
 * Returns SUCCESS_LOCAL if no errors, otherwise ERROR_LOCAL.
 */
int stop_capturing(int fd);

/* Private: Request a frame of video from the device to be output into the rgb
 * and y buffers.
 *
 * If the descriptor is not valid, no frame will be read.
 *
 * fd - a valid file descriptor pointing to the camera device.
 * frame_buffers - memory mapped buffers that contain the image from the device.
 * width - the width of the image.
 * height - the height of the image.
 * rgb_buffer - output buffer for RGB data.
 * y_buffer - output buffer for alpha (Y) data.
 */
void process_camera(int fd, buffer* frame_buffers, int width,
        int height, int* rgb_buffer, int* ybuf);


void process_next_frame(int fd, buffer* frame_buffers, unsigned char* dstSurface, unsigned char* dstBuffer);
/* Private: Stop capturing, uninitialize the device and free all memory. */
void stop_camera(int* fd, int* rgb_buffer, int* y_buffer);

#endif // __CAPTURE_H__

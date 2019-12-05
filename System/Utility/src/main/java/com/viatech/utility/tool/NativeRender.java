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

package com.viatech.utility.tool;

import android.view.Surface;

import java.nio.ByteBuffer;

public class NativeRender {
    static {
        System.loadLibrary("viautility");
    }

    public static void init() {

    }
    public static final int TOP_HALF = 0;
    public static final int BOTTON_HALF = 1;

    public static native void renderingToSurface(Surface s, byte[] pixelBytes, int offset, int width, int height, int size);
    public static native void renderingToSurface2(Surface s, ByteBuffer pixelBytes, int offset, int width, int height, int size , int colorFormat, int stride);
    public static native void renderingPtToSurface(Surface s, long pt, int width, int height, int size , int colorFormat, int stride);
    public static native void renderingTopOrBottonHalfToSurface(Surface s, ByteBuffer pixelBytes, int offset, int type, int width, int height, int size, int colorFormat, int stride);
    public static native long getPointerFromByteBuffer(ByteBuffer b, int offset);
    public static native void renderingYUV(Surface s, ByteBuffer y, ByteBuffer u, ByteBuffer v, int y_stride, int u_stride, int v_stride, int y_rowStride, int u_rowStride, int v_rowStride, int width, int height);
    public static native void renderingYUVTopOrBottonHalf(Surface s, ByteBuffer y, ByteBuffer u, ByteBuffer v, int y_stride, int u_stride, int v_stride, int y_rowStride, int u_rowStride, int v_rowStride, int type, int width, int height);
    public static native long allocateMemory(int size);
    public static native void copyImageAccordingPitch(ByteBuffer b, int offset, int src_rowStride, long pt, int width, int height, int pitch);
    public static native void releaseMemory(long pt);
    public static native long copyImageYToDestBuffer(byte[] src, int src_w, int src_h, ByteBuffer dest, int x, int y, int dest_w, int dest_h);
    public static native long newLookupTable(int src_length, int dest_length);
    public static native void deleteLookupTable(long pTable);
    public static native long copyImageYScaleToDestBuffer(byte[] src, int src_w, int src_h, ByteBuffer dest, int dest_w, int dest_h, long pTableW, long pTableY);
    public static native long copyYV12ToByteBufferNV12(ByteBuffer src, int cropW, int cropH, int W, int H, ByteBuffer dest);
    public static native long copyYV12ToByteBufferNV21(ByteBuffer src, int cropW, int cropH, int W, int H, ByteBuffer dest);

    public static void renderSurface(Surface s, ByteBuffer pixelBytes, int offset, int width, int height, int size, int colorFormat, int stride) {
        if(pixelBytes.isDirect()) {
            renderingToSurface2(s,pixelBytes,offset,width,height,size,colorFormat,stride);
        } else {
            byte[] b = new byte[pixelBytes.remaining()];
            pixelBytes.get(b);
            renderingToSurface(s,b,offset,width,height,size);
        }
    }
}

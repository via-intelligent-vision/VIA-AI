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

package com.viatech.utility.gles;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;


import com.viatech.utility.video.AvcEncoder;
import com.viatech.utility.video.VIARecorder;

import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class WindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {
    public static final int Encode = 1;
    public static final int Preview = 2;
    private EGLSurface mEGLPreviewSurface;
    private EGLSurface mEGLEncodeSurface;
    private EGL10 egl10 = (EGL10) EGLContext.getEGL();

    private Surface mEncodeSurface = null;
    private VIARecorder mVIARecorder = null;

    public EGLDisplay d = null;
    public EGLConfig c = null;

    private final String TAG = this.getClass().getName();

    public VIARecorder getRecorder() {
        return mVIARecorder;
    }


    public EGLSurface createEGLSurface(Surface s) {
        EGL10 egl = (EGL10) EGLContext.getEGL();

        return egl.eglCreateWindowSurface(d,c,s,null);
    }

    public void setEncodeSurface(Surface s) {
        mEncodeSurface = s;
        mEGLEncodeSurface = createEGLSurface(mEncodeSurface);
//        Log.d("HANK", "setEncodeSurface: ");
    }


    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                          EGLConfig config, Object nativeWindow) {
        EGLSurface result = null;
        d = display;
        c = config;
        try {
            mEGLPreviewSurface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "eglCreateWindowSurface (native)", e);
        }
        // this return will trigger Renderer
        result = mEGLPreviewSurface;
        return result;
    }

    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }

    public void makeCurrent(EGLContext context, int c){
        if(c == Encode) {
            if(mEGLEncodeSurface!=null) {
                egl10.eglMakeCurrent(egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY),
                        mEGLEncodeSurface, mEGLEncodeSurface, context);
            }
        }
        else if(c == Preview){
            egl10.eglMakeCurrent(egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY),
                    mEGLPreviewSurface, mEGLPreviewSurface,context );
        }
    }

    public void makeCurrent(EGLContext context, EGLSurface s){
            egl10.eglMakeCurrent(egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY),
                    s, s,context );
    }

    public void swapBuffers(EGLSurface s){
        egl10.eglSwapBuffers(egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), s);
    }

    public void swapBuffers(){
            egl10.eglSwapBuffers(egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), mEGLEncodeSurface);
    }
}
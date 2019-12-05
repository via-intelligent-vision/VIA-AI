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


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

import com.viatech.utility.video.VIARecorder;

import java.awt.font.TextAttribute;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("ViewConstructor")
public class VIASurfaceView extends GLSurfaceView {

    public AdavanceRenderer mRenderer;
    private static final int	EGL_RECORDABLE_ANDROID	= 0x3142;

    public void setVIARecorder(VIARecorder recorder) {
        mRenderer.setVIARecorder(recorder);
    }

    public enum View {
        Full(-1),
        Front(0),
        Left(1),
        Back(2),
        Right(3);
        int index;
        View(int v) {
            index = v;
        }
        public int getIndex() {
            return index;
        }
    }

    static View mCurrentView = View.Full;
    public static void setRenderView(View v) {
        mCurrentView = v;
    }

    public VIASurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer = new AdavanceRenderer(context);
        if(true) {
            setEGLConfigChooser(new EGLConfigChooser() {
                @Override
                public EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eglDisplay) {
                    final int[] attribList = {EGL14.EGL_RED_SIZE, 8, //
                            EGL14.EGL_GREEN_SIZE, 8, //
                            EGL14.EGL_BLUE_SIZE, 8, //
                            EGL14.EGL_ALPHA_SIZE, 8, //
                            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //
                            EGL14.EGL_NONE, 0, //
                            EGL14.EGL_NONE};

                    attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
                    attribList[attribList.length - 2] = 1;

                    int[] configNum = new int[1];
                    EGLConfig config = null;
                    egl10.eglChooseConfig(eglDisplay, attribList, null, 0, configNum);
                    int num = configNum[0];
                    if (num != 0) {
                        EGLConfig[] configs = new EGLConfig[num];
                        egl10.eglChooseConfig(eglDisplay, attribList, configs, num, configNum);
                        config = configs[0];
                    }
                    return config;
                }
            });
        }
        setEGLContextFactory(mRenderer.mContextFactory);
        setEGLWindowSurfaceFactory(mRenderer.mWindowSurfaceFactory);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void setCallback(Callback c) {
        mRenderer.setCallback(c);
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {


        super.onPause();
    }

    public boolean isViewExist(int viewID)
    {
        if(mRenderer == null) {
            return false;
        }
        else
            return mRenderer.isViewExist(viewID);
    }

    public void addView(int viewID)
    {
        if(isViewExist(viewID)) {
            //throw new IllegalArgumentException("Try to add duplicated ID : " + viewID);
        }
        else {
            mRenderer.addView(viewID);
        }
    }

    public void setViewVisibility(int viewID, boolean isVisible)
    {
        if(!isViewExist(viewID)) {
            throw new IllegalArgumentException("Try to access unregistered view ID : " + viewID);
        }
        else {
            CameraViewModel model = mRenderer.getViewModel(viewID);
            if(model == null) {
                throw new IllegalArgumentException("Try to use uncreated model in view ID : " + viewID);
            }
            else {
                model.setVisibility(isVisible);
            }
        }
    }

    public void setViewTextCoord(int viewID,
                                   float textCoord0_x, float textCoord0_y,
                                   float textCoord1_x, float textCoord1_y,
                                   float textCoord2_x, float textCoord2_y,
                                   float textCoord3_x, float textCoord3_y)
    {
        if(!isViewExist(viewID)) {
            throw new IllegalArgumentException("Try to access unregistered view ID : " + viewID);
        }
        else {
            CameraViewModel model = mRenderer.getViewModel(viewID);
            if(model == null) {
                throw new IllegalArgumentException("Try to use uncreated model in view ID : " + viewID);
            }
            else {
                model.setImgTextCoord(textCoord0_x, textCoord0_y,
                                        textCoord1_x, textCoord1_y,
                                        textCoord2_x, textCoord2_y,
                                        textCoord3_x, textCoord3_y);
            }
        }
    }

    public void setViewPosition(int viewID, float imgX, float imgY, float imgWidth, float imgHeight, boolean isGLCoordinate)
    {
        if(!isViewExist(viewID)) {
            throw new IllegalArgumentException("Try to access unregistered view ID : " + viewID);
        }
        else {
            CameraViewModel model = mRenderer.getViewModel(viewID);
            if(model == null) {
                throw new IllegalArgumentException("Try to use uncreated model in view ID : " + viewID);
            }
            else {
                model.setImgPosition(imgX, imgY, imgWidth, imgHeight, isGLCoordinate);
            }
        }
    }


    public interface Callback {
        void onSurfaceTextureAvailable(SurfaceTexture[] surfaceTextures);
        void onDraw(int mainTextureId);
    }

    private static class AdavanceRenderer implements GLSurfaceView.Renderer {
        private static String TAG = "AdavanceRenderer";

        private static Callback mCallback = null;


        final int TextureSize = 1;
        private int[] textures = new int[TextureSize];
        SurfaceTexture[] mSurfaceTextures = new SurfaceTexture[TextureSize];
        boolean bInit = false;
        private int mTextureID;

        int mWidth = 1920;
        int mHeight = 1080;
        Object lock = new Object();

        private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

        public WindowSurfaceFactory mWindowSurfaceFactory = new WindowSurfaceFactory();
        public ContextFactory mContextFactory = new ContextFactory();
        public SparseArray<CameraViewModel> mCustomImageModelMap = new SparseArray<>();; // Id, Model
        public SingleEGLImageModel mSingleEGLImageModel = null;

        public VIARecorder mVIARecorder = null;


        public AdavanceRenderer(Context context) {

        }

        public void setCallback(Callback callback) {
            synchronized (lock) {
                mCallback = callback;
                if(bInit) {
                    mCallback.onSurfaceTextureAvailable(mSurfaceTextures);
                }
            }
        }

        @Override
        public void onDrawFrame(GL10 glUnused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glViewport(0,0,mWidth,mHeight);

            mSurfaceTextures[0].updateTexImage();


            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);

            for(int mi = 0; mi < mCustomImageModelMap.size() ; mi++) {
                int key = mCustomImageModelMap.keyAt(mi);
                mCustomImageModelMap.get(key).draw(mCurrentView.getIndex(), true);
            }

            if(mVIARecorder!=null) {
                if(!mVIARecorder.bSurfaceGetted) {
                    Surface surface = mVIARecorder.getInputSurface();
                    mWindowSurfaceFactory.setEncodeSurface(surface);
                }

                if(!mVIARecorder.isStarted()) {
                    return;
                }

                if(!mVIARecorder.needEncode()) {
                    return;
                }

                mWindowSurfaceFactory.makeCurrent(mContextFactory.getContext(), WindowSurfaceFactory.Encode);

                int encodeWidth = mVIARecorder.getWidth();
                int encodeHeight = mVIARecorder.getHeight();
                boolean isFourInOneVideo = mVIARecorder.isFourInOneRecord();
                if(isFourInOneVideo) {
                    GLES20.glViewport(0, encodeHeight / 2, encodeWidth / 2, encodeHeight / 2);
                    this.mSingleEGLImageModel.draw(0, true, true, false);
                    GLES20.glViewport(encodeWidth / 2, encodeHeight / 2, encodeWidth / 2, encodeHeight / 2);
                    this.mSingleEGLImageModel.draw(1, true, true, false);

                    GLES20.glViewport(0, 0, encodeWidth / 2, encodeHeight / 2);
                    this.mSingleEGLImageModel.draw(2, true, true, false);
                    GLES20.glViewport(encodeWidth / 2, 0, encodeWidth / 2, encodeHeight / 2);
                    this.mSingleEGLImageModel.draw(3, true, true, false);
                } else {
                    GLES20.glViewport(0,0,encodeWidth, encodeHeight);
                    this.mSingleEGLImageModel.draw(0,false,false,false);
                }
                mWindowSurfaceFactory.swapBuffers();
                mWindowSurfaceFactory.makeCurrent(mContextFactory.getContext(), WindowSurfaceFactory.Preview);
            }
        }

        public void setVIARecorder(VIARecorder recorder) {
            mVIARecorder = recorder;
        }

        @Override
        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        public boolean isViewExist(int viewID)
        {
            return (mCustomImageModelMap.get(viewID) != null);
        }

        public void addView(int viewID)
        {
            mCustomImageModelMap.put(viewID, new CameraViewModel());
        }

        public CameraViewModel getViewModel(int viewID)
        {
            return mCustomImageModelMap.get(viewID);
        }

        @Override
        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            synchronized (lock) {
                GLES20.glGenTextures(TextureSize, textures, 0);
                for (int i = 0; i < TextureSize; i++) {

                    mTextureID = textures[i];
                    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
                    GLUtility.checkGlError("glBindTexture mTextureID");

                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                    GLUtility.checkGlError("glTexParameterf");

                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLUtility.checkGlError("glTexParameterf");

                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLUtility.checkGlError("glTexParameterf");

                    GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                    GLUtility.checkGlError("glTexParameterf");

                    mSurfaceTextures[i] = new SurfaceTexture(mTextureID);
                }

                mSingleEGLImageModel = new SingleEGLImageModel();

                if (mCallback != null) mCallback.onSurfaceTextureAvailable(mSurfaceTextures);
                bInit = true;
            }
        }
    }
}
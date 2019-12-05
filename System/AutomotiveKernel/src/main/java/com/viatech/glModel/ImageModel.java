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

package com.viatech.glModel;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ImageModel {
    private final static String TAG = "ImageModel";
    private static boolean mLibraryLoadStatus = false;
    static {
        System.loadLibrary("VIA-AI");
    }

    private long mNatvieObjAddress = 0;
    private boolean mIsInitGL = false;
    private boolean mIsCreated = false;
    private String mVertexShader ="";
    private String mFragShader ="";;

    private native long jni_create();
    public ImageModel() {
        mNatvieObjAddress = jni_create();
    }

    private native boolean jni_init(long address, String path, String imgName);
    public boolean init(@NonNull String path, @NonNull String imgName) {
        mIsCreated = jni_init(mNatvieObjAddress, path, imgName);
        return mIsCreated;
    }

    private String getShaderCode(Context context, String assetsFilePath) {
        String ret = null;
        AssetManager assetManager = context.getResources().getAssets();

        try {
            // copy data.
            StringBuilder sb = new StringBuilder();
            InputStream is = assetManager.open(assetsFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
            ret = sb.toString();
        }
        catch (Exception e) {
            Log.d(TAG, "ERROR: " + e.toString());
        }

        return  ret;
    }

    public boolean init(Context context) {
        mIsCreated = jni_init(mNatvieObjAddress, null, null);

        // restore shader files to internal storage.
        if(mIsCreated) {
            mVertexShader = getShaderCode(context, "shader/ImageModel.vsh");
            mFragShader = getShaderCode(context, "shader/ImageModel.fsh");
        }
        return mIsCreated;
    }

    private native void jni_release(long address);
    public void release() {
        jni_release(mNatvieObjAddress);
        mNatvieObjAddress = 0;
        mIsCreated = false;
        mIsInitGL = false;
    }

    private native boolean jni_initGL(long address, @NonNull String shaderPath);
    private native boolean jni_initGL(long address, @NonNull String vertexShader, @NonNull String fragShader);
    public void initGL() {
        // exprot shader to internal path
        mIsInitGL = jni_initGL(mNatvieObjAddress, mVertexShader, mFragShader);
    }

    private native void jni_setMatrices(long address, float []projection, float []view, float []model);
    public void setMatrices( @NonNull float []projection,  @NonNull float []view,  @NonNull float []model) {
        if(projection.length != 16 || view.length != 16 || model.length != 16) {
            throw new IllegalArgumentException("input matrix muse be 4x4 matrix");
        }
        jni_setMatrices(mNatvieObjAddress, projection, view, model);
    }

    private native void jni_draw(long address);
    public void draw() {
        jni_draw(mNatvieObjAddress);
    }

    private native void jni_setAlpha(long address, float alpha);
    public void setAlpha(float alpha) {
        if(mIsCreated) {
            jni_setAlpha(mNatvieObjAddress, alpha);
        }
    }

    private native void jni_setColor(long address, float r, float g, float b);
    public void setColor(float r, float g, float b) {
        if(mIsCreated) {
            jni_setColor(mNatvieObjAddress, r, g, b);
        }
    }

    public boolean isInitGL() {
        return mIsInitGL;
    }

    public boolean isCreated() {
        return mIsCreated;
    }

    private native void jni_updateTextCoord(long address, float x, float y, float width, float height);
    public void updateTextCoord(float x, float y, float width, float height) {
        if(mIsCreated && mIsInitGL) {
            jni_updateTextCoord(mNatvieObjAddress, x, y, width, height);
        }
    }

    private native void jni_updateRefTexture(long address, int refTextureId);
    public void updateRefTexture(int refTextureId) {
        if(mIsCreated && mIsInitGL) {
            jni_updateRefTexture(mNatvieObjAddress, refTextureId);
        }
    }
}

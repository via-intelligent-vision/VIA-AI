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

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 @brief This model is used to render image, and the rendering position is asigned by user.
 @note The corrdinate of setSource is based on the "Regular Image" coordinate.
 * */
public class CameraViewModel
{
    private final String TAG = this.getClass().getName();

    private int mProgram;
    private boolean mNeedUpdate = true;
    private volatile boolean mIsModelGenerated = false;
    private volatile boolean mIsRendering = false;
    private final int X_SLICE = 2;
    private final int Y_SLICE = 2;
    private ShortBuffer mIndexBuffer;

    private float mPosition[];
    private float mTextureCoordinate[];
    private short mIndex[];

    // VBO
    private int[] mPositionVBO = new int[1];
    private int[] mTextureCoordinateVBO = new int[1];
    private int[] mIndexVBO = new int[1];

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec2 aTexCoord;" +
        "varying vec2 vTexCoord;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  vTexCoord = aTexCoord;" +
        "}";

    private final String fragmentShaderCode =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;" +
        "varying vec2 vTexCoord;" +
        "uniform samplerExternalOES sTexture;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = texture2D(sTexture, vTexCoord);" +
        "}";

    private float[] mMvpMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float mImageAlpha = 1.0f;


    public CameraViewModel()
    {
        mPosition = new float [X_SLICE * Y_SLICE * 3];
        mTextureCoordinate = new float [X_SLICE * Y_SLICE * 2];


        mIndex = new short[(Y_SLICE -1) * (X_SLICE -1) * 2 * 3];

        int cc = 0;
        for(int dy = 0 ; dy < Y_SLICE - 1; dy++) {
            int ii = dy * X_SLICE;
            for(int dx = 0 ; dx < X_SLICE - 1; dx++) {
                mIndex[cc++] = (short)ii;
                mIndex[cc++] = (short)(ii +1);
                mIndex[cc++] = (short)(ii + Y_SLICE);
                mIndex[cc++] = (short)(ii +1);
                mIndex[cc++] = (short)(ii + Y_SLICE + 1);
                mIndex[cc++] = (short)(ii + Y_SLICE);
                ii++;
            }
        }

        final int BYTES_PER_SHORT = 2;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mIndex.length * BYTES_PER_SHORT);
        byteBuffer.order(ByteOrder.nativeOrder());
        mIndexBuffer = byteBuffer.asShortBuffer();
        mIndexBuffer.put(mIndex);
        mIndexBuffer.position(0);


        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, -1, 1);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.multiplyMM(mMvpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

    }

    /**
     @brief Update image texture coordinate, Note, the coordinate of input image is based on "Image Coordinate", top-left is (0, 0)
       Input Image Coordinate :
      (0, 0)
        --------------------------------------
        |         p0 ------------ p1
        |          |                  |
        |       p3 ------------ p2
       |
     */
    public void setImgTextCoord(float textCoord0_x, float textCoord0_y,
                                float textCoord1_x, float textCoord1_y,
                                float textCoord2_x, float textCoord2_y,
                                float textCoord3_x, float textCoord3_y)
    {
        float invX = 1.0f / (float)(X_SLICE -1);
        float invY = 1.0f / (float)(Y_SLICE -1);
        int cc = 0;
        for(int dy = 0 ; dy < Y_SLICE; dy++) {
            for(int dx = 0 ; dx < X_SLICE; dx++) {
                float tx = textCoord0_x + (textCoord2_x - textCoord0_x) * dx * invX;
                float ty = textCoord0_y + (textCoord2_y - textCoord0_y) * dy * invY;
                mTextureCoordinate[cc++] = tx;
                mTextureCoordinate[cc++] = ty;
            }
        }

        mNeedUpdate = true;
    }

    /**
        @brief Update image vertex, Note, the coordinate of input image is based on "Image Coordinate"
        CustomImageModel will conver to gl coordinate automatically when @param isGLCoordinate disabled .
       */
    public void setImgPosition(float imgX, float imgY, float imgWidth, float imgHeight, boolean isGLCoordinate)
    {
        if(!isGLCoordinate) {
            imgX = imgX * 2.0f - 1.0f;
            imgY = 1.0f - imgY * 2.0f;
            imgWidth *= 2.0f;
            imgHeight *= 2.0f;
        }

        float invX = 1.0f / (float)(X_SLICE -1);
        float invY = 1.0f / (float)(Y_SLICE -1);
        int cc = 0;
        for(int dy = 0 ; dy < Y_SLICE; dy++) {
            for(int dx = 0 ; dx < X_SLICE; dx++) {
                float tx = imgX + imgWidth * dx * invX;
                float ty = imgY - imgHeight * dy * invY;
                mPosition[cc++] = tx;
                mPosition[cc++] = ty;
                mPosition[cc++] = -0.5f;
            }
        }

        mNeedUpdate = true;
    }

    public void setVisibility(boolean isVisible)
    {
        mIsRendering = isVisible;
    }

    private void checkAndInitGL()
    {
        if(mIsModelGenerated == false) {
            FloatBuffer vertexBuffer;
            FloatBuffer textureCoordinateBuffer;

            // VBO
            GLES20.glGenBuffers(1, mPositionVBO, 0);
            GLUtility.checkGlError("glGenBuffers");

            // Initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(mPosition.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(mPosition);
            vertexBuffer.position(0);

            // VBO buffer vertex data
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mPosition.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");



            // VBO buffer texture coord data
            bb = ByteBuffer.allocateDirect(mTextureCoordinate.length * 4);
            bb.order(ByteOrder.nativeOrder());
            textureCoordinateBuffer = bb.asFloatBuffer();
            textureCoordinateBuffer.put(mTextureCoordinate);
            textureCoordinateBuffer.position(0);

            GLES20.glGenBuffers(1, mTextureCoordinateVBO, 0);
            GLUtility.checkGlError("glGenBuffers");

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextureCoordinate.length * 4, textureCoordinateBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");


            // VBO buffer index
            GLES20.glGenBuffers(1, mIndexVBO, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexVBO[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndex.length * 2, mIndexBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");



            // prepare shaders and OpenGL program
            int vertexShader = GLUtility.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = GLUtility.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            mProgram = GLES20.glCreateProgram();
            GLUtility.checkGlError("glCreateProgram");
            GLES20.glAttachShader(mProgram, vertexShader);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glLinkProgram(mProgram);
            GLUtility.checkGlError("glLinkProgram");

            mIsModelGenerated = true;
        }
    }

    private void checkDataUpdate()
    {
        if(mNeedUpdate) {
            // VBO update vertex coordinate
            ByteBuffer bb = ByteBuffer.allocateDirect(mPosition.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(mPosition);
            vertexBuffer.position(0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mPosition.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");


            // VBO update texture coordinate
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            bb = ByteBuffer.allocateDirect(mTextureCoordinate.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer textureCoordinateBuffer = bb.asFloatBuffer();
            textureCoordinateBuffer.put(mTextureCoordinate);
            textureCoordinateBuffer.position(0);

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextureCoordinate.length * 4, textureCoordinateBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");

            mNeedUpdate = false;
        }
    }

    public void draw(int index, boolean fourInOne)
    {
        if(!mIsRendering) return;
        // Check is opengl init or not.
        checkAndInitGL();

        // Check is data updated or not.
        checkDataUpdate();

        GLES20.glUseProgram(this.mProgram);
        GLUtility.checkGlError("glUseProgram");


        // Set the face rotation
        GLES20.glFrontFace(GLES20.GL_CW);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionVBO[0]);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,0, 0);


        // Get handle to mTextureCoordinate coordinates location
        int mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false,0, 0);


        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMvpMatrix, 0);


        // Get handle to mTextureObject locations
        int mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexVBO[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndex.length, GLES20.GL_UNSIGNED_SHORT, 0);
        GLUtility.checkGlError("glDrawElements");

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}

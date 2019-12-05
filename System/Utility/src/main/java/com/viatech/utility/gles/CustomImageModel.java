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

/**
 @brief This model is used to render image, and the rendering position is asigned by user.
 @note The corrdinate of setSource is based on the "Regular Image" coordinate.
 * */
public class CustomImageModel
{
    private final String TAG = this.getClass().getName();

    private int mProgram;
    private int mProgram_Luminance;
    private boolean mNeedUpdate = true;
    private volatile boolean mIsModelGenerated = false;
    private volatile boolean mIsRendering = false;

    private float mPosition[] =
    {
        -1.0f,  -1.0f, -0.5f,   // bottom left
        -1.0f,  1.0f, -0.5f,    // top left
        1.0f,  -1.0f, -0.5f,    // bottom right
        1.0f,   1.0f, -0.5f     // top right
    };

    private float mTextureCoordinate[] =
    {
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };

    // VBO
    private int[] mPositionVBO = new int[1];
    private int[] mTextureCoordinateVBO = new int[1];

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

    private final String fragmentShaderCode_Luminance =
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;" +
        "uniform samplerExternalOES sTexture;"+
        "uniform vec4 vColor;" +
        "varying vec2 vTexCoord;" +

        "void main() {" +
        "  vec4 color = texture2D(sTexture, vTexCoord);" +
        "  color.r = 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;" +
        "  color.g = color.r;" +
        "  color.b = color.r;" +
        "  color.a = 1.0;" +
        "  gl_FragColor = color;" +
        "}";
    private float[] mMvpMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float mImageAlpha = 1.0f;


    public CustomImageModel()
    {

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

        mTextureCoordinate[0] = textCoord3_x;   // map to gl  bottom left vertex
        mTextureCoordinate[1] = textCoord3_y;

        mTextureCoordinate[2] = textCoord0_x;   // map to gl  top left vertex
        mTextureCoordinate[3] = textCoord0_y;

        mTextureCoordinate[4] = textCoord2_x;   // map to gl  bottom right vertex
        mTextureCoordinate[5] = textCoord2_y;

        mTextureCoordinate[6] = textCoord1_x;   // map to gl  top right vertex
        mTextureCoordinate[7] = textCoord1_y;

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
        mPosition[0] = imgX;                // to gl bottom left  coordinate
        mPosition[1] = imgY - imgHeight;
        mPosition[2] = -0.5f;

        mPosition[3] = imgX;                // to gl top left coordinate
        mPosition[4] = imgY;
        mPosition[5] = -0.5f;

        mPosition[6] = imgX + imgWidth;     // to gl bottom right coordinate
        mPosition[7] = imgY - imgHeight;
        mPosition[8] = -0.5f;

        mPosition[9] = imgX + imgWidth;     // to gl top right coordinate
        mPosition[10] = imgY;
        mPosition[11] = -0.5f;

        mNeedUpdate = true;
    }

    public void setVisibility(boolean isVisible)
    {
        mIsRendering = isVisible;
    }

    private void checkAndInitGL()
    {
        if(mIsModelGenerated == false) {
            // Add dummy surface for debug

            FloatBuffer vertexBuffer;
            FloatBuffer textureCoordinateBuffer;

            // VBO
            GLES20.glGenBuffers(1, mPositionVBO, 0);
            GLUtility.checkGlError("glGenBuffers");

            GLES20.glGenBuffers(1, mTextureCoordinateVBO, 0);
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

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mTextureCoordinate.length * 4, textureCoordinateBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");


            // prepare shaders and OpenGL program
            int vertexShader = GLUtility.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = GLUtility.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            int fragmentShader_Luminance = GLUtility.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode_Luminance);

            mProgram = GLES20.glCreateProgram();
            GLUtility.checkGlError("glCreateProgram");
            GLES20.glAttachShader(mProgram, vertexShader);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glLinkProgram(mProgram);
            GLUtility.checkGlError("glLinkProgram");

            mProgram_Luminance = GLES20.glCreateProgram();
            GLUtility.checkGlError("glCreateProgram");
            GLES20.glAttachShader(mProgram_Luminance, vertexShader);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glAttachShader(mProgram_Luminance, fragmentShader_Luminance);
            GLUtility.checkGlError("glAttachShader");
            GLES20.glLinkProgram(mProgram_Luminance);
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
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
            GLUtility.checkGlError("glBindBuffer");

            bb = ByteBuffer.allocateDirect(mTextureCoordinate.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer textureCoordinateBuffer = bb.asFloatBuffer();
            textureCoordinateBuffer.put(mTextureCoordinate);
            textureCoordinateBuffer.position(0);

            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mTextureCoordinate.length * 4, textureCoordinateBuffer, GLES20.GL_STATIC_DRAW);
            GLUtility.checkGlError("glBufferData");

            mNeedUpdate = false;
        }
    }

    public void draw(int index, boolean fourInOne, boolean luminance)
    {
        if(mIsRendering == false) return;
        // Check is opengl init or not.
        checkAndInitGL();

        // Check is data updated or not.
        checkDataUpdate();

        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, -1, 1);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.multiplyMM(mMvpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        if (luminance == true) {
            GLES20.glUseProgram(this.mProgram_Luminance);
            GLUtility.checkGlError("glUseProgram");
        }
        else  {
            GLES20.glUseProgram(this.mProgram);
            GLUtility.checkGlError("glUseProgram");
        }

        // Set the face rotation
        GLES20.glFrontFace(GLES20.GL_CW);
        GLUtility.checkGlError("glFrontFace");

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLUtility.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLUtility.checkGlError("glEnableVertexAttribArray");

        // Bind VBO Position
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionVBO[0]);
        GLUtility.checkGlError("glBindBuffer");

        GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, 0);
        GLUtility.checkGlError("glVertexAttribPointer");

        // Get handle to mTextureCoordinate coordinates location
        int mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLUtility.checkGlError("glGetAttribLocation");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLUtility.checkGlError("glEnableVertexAttribArray");

        // Bind VBO Texture Coordinate
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinateVBO[0]);
        GLUtility.checkGlError("glBindBuffer");


        GLES20.glVertexAttribPointer(
                mTexCoordHandle, 2,
                GLES20.GL_FLOAT, false,
                0, 0);
        GLUtility.checkGlError("glVertexAttribPointer");

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLUtility.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMvpMatrix, 0);
        GLUtility.checkGlError("glUniformMatrix4fv");

        // Get handle to mTextureObject locations
        int mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
        GLUtility.checkGlError("glGetUniformLocation");

        // Set the sampler mTextureCoordinate unit to 0, where we have saved the mTextureCoordinate.
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLUtility.checkGlError("glUniform1i");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosition.length / 3);
        GLUtility.checkGlError("glDrawArrays");

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLUtility.checkGlError("glDisableVertexAttribArray");

        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        GLUtility.checkGlError("glDisableVertexAttribArray");
    }
}

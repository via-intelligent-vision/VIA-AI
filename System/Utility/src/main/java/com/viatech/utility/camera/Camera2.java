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

package com.viatech.utility.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;

import com.viatech.utility.tool.NativeRender;
import com.viatech.utility.video.VIARecorder;

import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.content.Context.CAMERA_SERVICE;

@TargetApi(21)
public class Camera2 extends VIACamera{
    Context mContext = null;
    ImageReader mImageReader;
    ImageReader mFakeJpegReader;
    CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    CameraCaptureSession mCaptureSession;
    CaptureRequest mPreviewRequest;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    SurfaceTexture mSurfaceTexture = null;
    boolean bImageReaderEnable = false;
    Surface mSurface = null;
    CameraManager manager;
    String cameraId = "";
    final static String TAG = "Camera2";
    boolean bRecord = false;
    VIARecorder mVIARecorder = null;
    int mWidth = 0;
    int mHeight = 0;
    Surface mEncodeSurface = null;
    SurfaceView mDisplaySurfaceView = null;
    SurfaceTexture mDisplaySurfaceTexture = null;

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void loop(boolean f) {

    }

    @Override
    public void close() {
        if(mVIARecorder!=null) {
            mVIARecorder.stop();
            mVIARecorder = null;
        }
        bRecord = false;
        release();
    }

    int mRecordBitrate = 0;
    int mRecordFPS = 0;
    int mRecordPerodicTime = 0;
    String mRecordPath = "";
    VIARecorder.FileListener mRecordFileListener = null;
    Surface mOtherSurface = null;

    public void setOtherSurface(Surface surface) {
        mOtherSurface = surface;
    }
    @Override
    public void enableRecord(String path,int bitrate, int fps, int  perodicTimeInSec, VIARecorder.FileListener fileListener) {
        bRecord = true;
        mRecordPath = path;
        mRecordBitrate = bitrate;
        mRecordFPS = fps;
        mRecordPerodicTime = perodicTimeInSec;
        mRecordFileListener = fileListener;
    }

    protected Camera2(Context c, int id, int width, int height , SurfaceView surfaceView) {
        manager = (CameraManager) c.getSystemService(CAMERA_SERVICE);
        mContext = c;
        startBackgroundThread();
        cameraId = id + "";
        mWidth = width;
        mHeight = height;
        mDisplaySurfaceView = surfaceView;
    }

    protected Camera2(Context c, int id, int width, int height , SurfaceTexture surfaceTexture) {
        manager = (CameraManager) c.getSystemService(CAMERA_SERVICE);
        mContext = c;
        startBackgroundThread();
        cameraId = id + "";
        mWidth = width;
        mHeight = height;
        mDisplaySurfaceTexture = surfaceTexture;
    }


    VIACamera.Callback mCallback = null;

    @Override
    public void setCallback(VIACamera.Callback c) {
        if(c==null)
            bImageReaderEnable = false;
        else
            bImageReaderEnable = true;

        mCallback = c;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened( CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected( CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError( CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    @TargetApi(21)
    public void start() {

        try {
            manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(21)
    private void createCameraPreviewSession() {
        try {
            if(mDisplaySurfaceView!=null) {
                mSurface = mDisplaySurfaceView.getHolder().getSurface();
            }

            if(mDisplaySurfaceTexture!=null) {
                mDisplaySurfaceTexture.setDefaultBufferSize(mWidth,mHeight);
                mSurface = new Surface(mDisplaySurfaceTexture);
            }
            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> surfaceList = new ArrayList<>();

            if(mSurface!=null) {
                mPreviewRequestBuilder.addTarget(mSurface);
                surfaceList.add(mSurface);
            }

            if(mOtherSurface!=null) {
                mPreviewRequestBuilder.addTarget(mOtherSurface);
                surfaceList.add(mOtherSurface);
            }

            if(bImageReaderEnable) {
                mImageReader = ImageReader.newInstance(mWidth, mHeight,
                        ImageFormat.YUV_420_888, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);
                surfaceList.add(mImageReader.getSurface());
                mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            }

            /*
                Special Path, need to set jpeg imagereader such that camera HAL can be received real size
             */

            if(true) {
                mFakeJpegReader = ImageReader.newInstance(mWidth, mHeight,
                        ImageFormat.JPEG, /*maxImages*/2);
                surfaceList.add(mFakeJpegReader.getSurface());
            }

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(surfaceList,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured( CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                 CameraCaptureSession cameraCaptureSession) {
//                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    boolean bDebug = false;
    Image borrowedImage = null;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized (mLock) {
                if (borrowedImage == null) {
                    Image i = reader.acquireNextImage();
                    borrowedImage = i;
                    if (bDebug) {
                        Log.d(TAG, "==== OnImageReady ====");
                        Log.d(TAG, "Size:" + i.getWidth() + "x" + i.getHeight());
                        Log.d(TAG, "Format:" + i.getFormat());
                        Log.d(TAG, "#Planes:" + i.getPlanes().length);
                        Log.d(TAG, "Y-Plane Pixel Stride:" + i.getPlanes()[0].getPixelStride());
                        Log.d(TAG, "Y-Planes Row Stride" + i.getPlanes()[0].getRowStride());
                        Log.d(TAG, "Y(i[0]) Start Address:" + NativeRender.getPointerFromByteBuffer(i.getPlanes()[0].getBuffer(), 0));
                        Log.d(TAG, "U(i[0]) Start Address:" + NativeRender.getPointerFromByteBuffer(i.getPlanes()[1].getBuffer(), 0));
                        Log.d(TAG, "V(i[0]) Start Address:" + NativeRender.getPointerFromByteBuffer(i.getPlanes()[2].getBuffer(), 0));
                        Log.d(TAG, "======================");
                    }

                    if (mCallback != null) mCallback.onFrameReady();
                }
            }
        }
    };

    private void releaseCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
//        if (null != mCameraDevice) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    public void release() {
        if(mBackgroundThread!=null) stopBackgroundThread();
        releaseCamera();
    }

    private Object mLock= new Object();

    @Override
    public void queueBuffer() {
        synchronized (mLock) {
            borrowedImage.close();
            borrowedImage = null;
        }
    }

    @Override
    public long dequeueBufferAndGetPointer() {
        if(borrowedImage!=null)
            return NativeRender.getPointerFromByteBuffer(borrowedImage.getPlanes()[0].getBuffer(),0);
        else
            return -1;
    }

    @Override
    public Image dequeueBuffer() {
        return borrowedImage;
    }

    @Override
    public ByteBuffer dequeueBufferAndGetByteBuffer() {
        if(borrowedImage!=null)
            return borrowedImage.getPlanes()[0].getBuffer();
        else
            return null;
    }

    /**
     * Comparator based on area of the given {@link Size} objects.
     */
    @TargetApi(21)
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}

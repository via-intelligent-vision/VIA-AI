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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;

import com.viatech.utility.tool.Helper;

import java.io.File;

import static android.content.Context.CAMERA_SERVICE;

public abstract class VIACamera implements VIACameraImpl {
    public enum MODE {
        Native, //FirstAutoMobile Version, it can't compatible on all platform
        Camera, // Using CameraHAL,
        Camera2, // Using CameraHAL2 , API version >= 21
        FakeCameraGPU,
    }


    @TargetApi(21)
    public static int queryNumberOfCamera(Context context) {
        CameraManager manager =
                (CameraManager)context.getSystemService(CAMERA_SERVICE);
        int number = 0;
        try {
            number = manager.getCameraIdList().length;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return number;
    }

    @TargetApi(21)
    public static Size[] querySupportResolution(Context context, int index) {
        CameraManager manager =
                (CameraManager)context.getSystemService(CAMERA_SERVICE);
        try {
            CameraCharacteristics chars
                    = manager.getCameraCharacteristics(index+"");

            StreamConfigurationMap configurationMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            return configurationMap.getOutputSizes(ImageFormat.YUV_420_888);

        } catch(CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }


//    public enum FAKE_MODE {
//        CPURender,
//        GPURender,
//    }

    public static VIACamera create(MODE mode,String videoPath, SurfaceView displayView) throws Exception {
        switch (mode) {
            case FakeCameraGPU:
                File f = new File(videoPath);
                if(f.exists() && f.canRead()) {
                    return new FakeCameraGPU(videoPath, displayView);
                } else {
                    throw new Exception(videoPath+" is not exist or can not read!");
                }
            default:
                throw new Exception("This is FakeCameraGPU Constructor, If you want to go normal Camera path, please using create(MODE mode, Context context, int index, int width, int height, SurfaceView displayView)");
        }
    }

    public static VIACamera create(MODE mode,String videoPath, SurfaceTexture surfaceTextureForDisplay) throws Exception {
        switch (mode) {
            case FakeCameraGPU:
                File f = new File(videoPath);
                if(f.exists() && f.canRead()) {
                    return new FakeCameraGPU(videoPath, surfaceTextureForDisplay);
                } else {
                    throw new Exception(videoPath+" is not exist or can not read!");
                }
            default:
                throw new Exception("This is FakeCameraGPU Constructor, If you want to go normal Camera path, please using create(MODE mode, Context context, int index, int width, int height, SurfaceView displayView)");
        }
    }


    public static VIACamera create(MODE mode, Context context, int index, int width, int height, SurfaceView displayView) throws Exception {
        switch (mode) {
            case Camera:
                return new Camera(context,index,width,height, displayView);
            case Native:
                File nodeFile = new File("/dev/video"+index);
                if(!nodeFile.exists()) {
                    if(nodeFile.canWrite() && nodeFile.canRead()) {
                        return new NativeCamera(context,"/dev/video"+index,width,height, displayView);
                    }
                }
                throw new Exception("/dev/video"+index+" is not exist or not read/write");

            case Camera2:
                if(Helper.isUpperThanAPI21())
                    return new Camera2(context,index,width,height, displayView);
                else
                    throw new Exception("Camera2 need API Version >= 21");
            case FakeCameraGPU:
                throw new Exception("FakeCamera need input filePath in constructor");
            default:
                return null;
        }
    }


    public static VIACamera create(MODE mode, Context context, int index, int width, int height, SurfaceTexture surfaceTexturForDisplay) throws Exception {
        switch (mode) {
            case Camera:
                return new Camera(context,index,width,height, surfaceTexturForDisplay);
            case Native:
                File nodeFile = new File("/dev/video"+index);
                if(!nodeFile.exists()) {
                    if(nodeFile.canWrite() && nodeFile.canRead()) {
                        throw new Exception("NativeCam Need to use SurfaceView to display");
                    }
                }
                throw new Exception("/dev/video"+index+" is not exist or not read/write");

            case Camera2:
                if(Helper.isUpperThanAPI21())
                    return new Camera2(context,index,width,height, surfaceTexturForDisplay);
                else
                    throw new Exception("Camera2 need API Version >= 21");
            case FakeCameraGPU:
                throw new Exception("FakeCamera need input filePath in constructor");
            default:
                return null;
        }
    }

    public interface Callback {
        void onFrameReady();
        void onEOS();
    }
}

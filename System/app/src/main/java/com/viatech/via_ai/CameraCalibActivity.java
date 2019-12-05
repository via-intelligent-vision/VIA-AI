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

package com.viatech.via_ai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.OnBoomListener;
import com.viatech.camera.CameraCalibrator;
import com.viatech.glModel.ImageModel;
import com.viatech.media.FrameFormat;
import com.viatech.utility.camera.FakeCameraGPU;
import com.viatech.utility.camera.VIACamera;
import com.viatech.utility.gles.VIASurfaceView;
import com.viatech.via_ai.Media.CameraPermutation;
import com.viatech.via_ai.Media.EventSpeaker;
import com.viatech.via_ai.System.Helper;
import com.viatech.via_ai.System.SystemEvents;
import com.viatech.via_ai.UI.LoadingWrapper;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.nio.ByteBuffer;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CameraCalibActivity extends Activity {
    final static String TAG = "VIA_AdvanceDashCam";
    private Context mContext = this;

    public enum SystemStatus {
        Init,
        Detection,
        Calibrating,
        Preview,
        Pause
    }

    // Camera View Id set , these id are used to specify view number of VIASurfaceView.
    private static final int mCameraViewId_F = 0;
    private int prevHintId = 0;

    // Camera setttings
    private volatile boolean bIsCameraPermissionGuaranteed = false; // as true if permission guaranteed
    private volatile boolean bIsCameraSurfaceViewCreated = false; // as true if surfaceview created.
    private int mFrameWidth = 1280;
    private int mFrameHeight = 720;
    private int mCameraDeviceIdentify = 0;
    private VIACamera mCamera;
    private VIACamera.MODE mCameraMode = VIACamera.MODE.Camera;
    private CameraPermutation mCameraPermutation = CameraPermutation.Camera1_In_Frame;
    private VIASurfaceView mUI_CameraView;
    private String mVideoPath = "/sdcard/VIA_ADAS/F_fo-20170718_104639.mp4";
    private SurfaceTexture[] mSurfaceTextures;
    private volatile Object mMutex_OnCameraAccess = new Object();

    // Camera Calibrator
    private long prevFetchTime = System.nanoTime();
    private CameraCalibrator mCamCalibrator;
    private Thread mCalibThread = null;

    // UI Part
    private ConstraintLayout mUI_Layout;
    private LoadingWrapper mUI_LoadingLayout;
    private RotateLoading mUI_CountProgress;
    private TextView mUI_CountPercentage;
    private TextView mUI_CalibrationInfo;
    private TextView mUI_ErrorHint;
    private EditText mUI_BoardWidth;
    private EditText mUI_BoardHeight;
    private EditText mUI_GridSize;
    private ImageView mUI_Save;

    // System status
    private volatile SystemStatus mSystemStatus = SystemStatus.Init;
    private volatile SystemStatus mPrevSystemStatus = SystemStatus.Init;
    private volatile boolean bIsAnyPermissionDenied = false;
    private InitAsyncTask mInitAsyncTask = null;
    private Helper mHelper;


    private void parseActivityParameterBundle() {
        Intent parameterIntent = getIntent();
        if (parameterIntent != null) {
            Bundle parameterBundle = parameterIntent.getExtras();
            if (parameterBundle != null) {

                // parse camera mode
                VIACamera.MODE cameraMode = (VIACamera.MODE) parameterBundle.get(getResources().getString(R.string.key_camera_mode));
                if (cameraMode != null) {
                    mCameraMode = cameraMode;

                }

                Integer cameraID = (Integer) parameterBundle.get(getResources().getString(R.string.key_camera_id));
                if (cameraID != null) {
                    mCameraDeviceIdentify = cameraID;
                }

                // parse video file name
                if (mCameraMode == VIACamera.MODE.FakeCameraGPU) {
                    String filePath = (String) parameterBundle.get(getResources().getString(R.string.key_file_path));
                    String fileName = (String) parameterBundle.get(getResources().getString(R.string.key_file_name));
                    if (filePath != null && fileName != null && filePath.length() > 0 && fileName.length() > 0) {
                        mVideoPath = filePath + "/" + fileName;
                    }
                }

                // parse camera permutation
                CameraPermutation cameraPermutation = (CameraPermutation) parameterBundle.get(getResources().getString(R.string.key_camera_permutation));
                if (cameraPermutation != null) {
                    mCameraPermutation = cameraPermutation;
                }

                // parse frame size
                Integer frameWidth = (Integer) parameterBundle.get(getResources().getString(R.string.key_frame_width));
                Integer frameHeight = (Integer) parameterBundle.get(getResources().getString(R.string.key_frame_height));
                if (frameWidth != null && frameHeight != null && frameWidth > 0 && frameHeight > 0) {
                    mFrameWidth = frameWidth;
                    mFrameHeight = frameHeight;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MultiDex.install(this);

        mHelper = new Helper();
        mHelper.setupAutoHideSystemUI(this);

        // Parse bundle & get data
        parseActivityParameterBundle();

        // set view by system mode.
        setContentView(R.layout.activity_camera_calib);

        // Setup UI
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bIsAnyPermissionDenied) {
            if (!checkAllPermission()) {
                Log.e(TAG, "checkAllPermission Fail");
                CameraCalibActivityPermissionsDispatcher.onNeedPermissionsWithCheck(CameraCalibActivity.this);
            } else {
                // change system mode
                if (mPrevSystemStatus == SystemStatus.Calibrating)
                    setSystemStatus(SystemStatus.Calibrating);
                else {
                    setSystemStatus(SystemStatus.Init);
                }

                if(mInitAsyncTask == null) {
                    mInitAsyncTask = new InitAsyncTask();
                    mInitAsyncTask.execute();
                }
                else {
                    resumeCamera();
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // clear system mode
        setSystemStatus(SystemStatus.Pause);

        pauseCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseCamera();

        releaseCamCalibrator();
    }

    private void setSystemStatus(SystemStatus mode) {
        synchronized (this) {
            mPrevSystemStatus = mSystemStatus;
            mSystemStatus = mode;
        }
    }

    private SystemStatus getSystemStatus() {
        return mSystemStatus;
    }

    /**
     @brief AsyncTask to init system
     */
    private class InitAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // apply UI scenario
            setUIScenario(mCameraPermutation, UIScenarioTypes.CameraInit);

            EventSpeaker.getInstance().pushEvent(SystemEvents.SystemInit);
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            // Wait all permission.
            this.waitPermissions();


            // init camera surface view
            setupCameraSurfaceView();

            // apply view scenario
            setSurfaceViewScenario(mCameraPermutation, SurfaceViewScenarioTypes.CameraInit);

            // wait camera surface view create finish.
            this.waitSurfaceViewCreated();

            // start camera
            startCamera(mSurfaceTextures);

            // init camera calibrator
            initCamCalibrator();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void a) {
            super.onPostExecute(a);

            setSystemStatus(SystemStatus.Detection);

            // set view scenario to detection
            setSurfaceViewScenario(mCameraPermutation, SurfaceViewScenarioTypes.Detection);
            setUIScenario(mCameraPermutation, UIScenarioTypes.Detection);

        }

        private void waitPermissions() {
            while(!bIsCameraPermissionGuaranteed) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void waitSurfaceViewCreated() {
            while(!bIsCameraSurfaceViewCreated) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void waitUserCameraPreview(int time) {
            try {
                Thread.sleep(time); // Delay 2s to check all camera view.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     @brief setup & find application UI
     */
    private void setupUI()
    {
        // Find all ui
        mUI_Layout = findViewById(R.id.DummyLayout);
        mUI_LoadingLayout =  findViewById(R.id.LoadingLayout);
        mUI_CameraView = findViewById(R.id.CameraDisplayView);
        mUI_CountProgress = findViewById(R.id.CountProgress);
        mUI_CountPercentage = findViewById(R.id.CountPercentage);
        mUI_CalibrationInfo = findViewById(R.id.CalibrationInfo);
        mUI_ErrorHint = findViewById(R.id.ErrorHint);
        mUI_BoardWidth = findViewById(R.id.BoardWidth);
        mUI_BoardHeight = findViewById(R.id.BoardHeight);
        mUI_GridSize = findViewById(R.id.GridSize);
        mUI_Save = findViewById(R.id.Save);

        mUI_CountProgress.start();
        mUI_CountPercentage.setText("0%");
        mUI_Save.setEnabled(false);


        mUI_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if(mCamCalibrator != null && mCamCalibrator.isCalibrated())
                {
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.DIR_SELECT;
                    properties.root = new File("/");
                    properties.error_dir = new File("/storage/emulated/0/");
                    properties.offset = new File("/storage/emulated/0/");

                    FilePickerDialog dialog = new FilePickerDialog(CameraCalibActivity.this, properties);
                    dialog.setTitle("Select a folder to save result.");
                    dialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {

                            //  Show a dialog to set camera name
                            final String exprotPath = files[0];

                            AlertDialog.Builder builder = new AlertDialog.Builder(CameraCalibActivity.this);
                            builder.setTitle("Camera Name");
                            final EditText input = new EditText(CameraCalibActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String cameraName = input.getText().toString();

                                    final boolean result = mCamCalibrator.save(cameraName, exprotPath);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(result) {
                                                Toast.makeText(CameraCalibActivity.this, "[O] Save to " + exprotPath , Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(CameraCalibActivity.this, "[X] Save to " + exprotPath , Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    });
                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    dialog.show();
                }
                /*else {
                    Toast.makeText(CameraCalibActivity.this, "Calibration not finish yet" , Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }

    /** Init gl models
     */
    void initCamCalibrator() {
        if(mCamCalibrator == null) {
            mCamCalibrator = new CameraCalibrator();
        }
        mCamCalibrator.init(9, 6, 2.9f, 2.9f);
    }

    void releaseCamCalibrator() {
        if(mCamCalibrator != null) {
            mCamCalibrator.release();
        }
        mCamCalibrator = null;
    }

    void startCalibration() {
        if(mCamCalibrator != null) {
            setSystemStatus(SystemStatus.Calibrating);

            if(mUI_LoadingLayout != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mUI_LoadingLayout.setLoadNote("Calibrating ....");
                        mUI_LoadingLayout.show();
                    }
                });
            }


            if (mCalibThread != null) {
                try {
                    mCalibThread.interrupt();
                    mCalibThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mCalibThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    double rms = mCamCalibrator.calibrate();
                    float [] camera = mCamCalibrator.getCameraMatrix();
                    float [] dist = mCamCalibrator.getDistCoeff();

                    String info ="Calibration Information:\n";
                    info += "rms :" + rms + "\n";
                    info += "frame size : " + mCamCalibrator.getCalibSize().toString() + "\n";
                    info += "camera matrix : { \n";
                    for(int i = 0 ; i < camera.length ; i++) {
                        if(i % 3 == 0) info += "        ";
                        info += Float.toString(camera[i]) + "    ";
                        if((i +1) % 3 == 0) info +="\n";
                    }
                    info += "    }\n";

                    info += "distortion Coeffs : { \n";
                    for(int i = 0 ; i < dist.length ; i++) {
                        info += "        " + Float.toString(dist[i]);
                    }
                    info += "    }\n";


                    final String finalInfo = info;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUI_LoadingLayout.hide();
                            mUI_CalibrationInfo.setText(finalInfo);
                            mUI_CalibrationInfo.setVisibility(View.VISIBLE);
                            mUI_Save.setEnabled(true);
                        }
                    });
                }
            });
            mCalibThread.start();
        }
    }

    private void setupCameraSurfaceView()
    {
        // init surface view
        mUI_CameraView.setRenderView(VIASurfaceView.View.Full);
        mUI_CameraView.setCallback(new VIASurfaceView.Callback() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture[] surfaceTextures) {
                mSurfaceTextures = surfaceTextures;
                bIsCameraSurfaceViewCreated = true;
            }

            @Override
            public void onDraw(int mainTextureId) {

            }
        });

        // Add views (initialize view)
        switch (mCameraPermutation) {
            case Camera1_In_Frame:
                mUI_CameraView.addView(mCameraViewId_F);
                mUI_CameraView.setViewVisibility(mCameraViewId_F, false);
                break;
            default:
                break;
        }
    }


    /** about UI ScenarioTypes
        DON''T access openGL surfaceview in these function, use setUIScenario replaced
     */
    public enum UIScenarioTypes
    {
        CameraInit,
        Detection,
    }

    private void setUIScenario_1in1(UIScenarioTypes viewScenario)
    {
        // TODO : Same as 4in1_4x1 now.
        // TODO: Refine this ,and ADAS Manager
        switch(viewScenario) {
            case CameraInit:
                switch(mCameraPermutation) {
                    case Camera1_In_Frame:
                        mUI_LoadingLayout.setLoadNote("Camera Permutation : 1-in-1");
                        break;
                    case Camera2_In_Frame_1x2:
                        mUI_LoadingLayout.setLoadNote("Camera Permutation : 2-in-1 (1x2)");
                        break;
                    case Camera4_In_Frame_2x2:
                        mUI_LoadingLayout.setLoadNote("Camera Permutation : 4-in-1 (2x2)");
                        break;
                    case Camera4_In_Frame_1x4:
                        mUI_LoadingLayout.setLoadNote("Camera Permutation : 4-in-1 (1x4)");
                        break;
                }
                mUI_LoadingLayout.setVisibility(View.VISIBLE);
                mUI_LoadingLayout.show();
                break;

            case Detection:
                mUI_LoadingLayout.hide();
                break;
            default:
                break;
        }
    }

    private void setUIScenario(CameraPermutation cameraPermutation, UIScenarioTypes uiScenario)
    {
        switch(cameraPermutation) {
            case Camera1_In_Frame:
                setUIScenario_1in1(uiScenario);
                break;
        }
    }

    /** about SurfaceViewScenarioTypes
        DON''T access UI visibility in these function, use setUIScenario replaced
       */
    public enum SurfaceViewScenarioTypes
    {
        CameraInit,
        Detection,
    }

    private void setSurfaceViewScenario_1in1(SurfaceViewScenarioTypes viewScenario)
    {
        switch(viewScenario) {
            case CameraInit:
                mUI_CameraView.setViewVisibility(mCameraViewId_F, true);
                mUI_CameraView.setViewPosition(mCameraViewId_F, 0.05f, 0.05f, 0.9f, 0.9f, false);
                mUI_CameraView.setViewTextCoord(mCameraViewId_F, 0.0f, 0.0f,  1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
                break;

            case Detection:
                mUI_CameraView.setViewVisibility(mCameraViewId_F, true);
                mUI_CameraView.setViewPosition(mCameraViewId_F, 0.0f, 0.0f, 1.0f, 1.0f, false);
                mUI_CameraView.setViewTextCoord(mCameraViewId_F, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,0.0f, 1.0f);
                break;

            default:
                break;
        }
    }

    private void setSurfaceViewScenario(CameraPermutation cameraPermutation, SurfaceViewScenarioTypes viewScenario)
    {
        switch(cameraPermutation) {
            case Camera1_In_Frame:
                setSurfaceViewScenario_1in1(viewScenario);
                break;
        }
    }

    private boolean checkInputValid() {
        //check input value valid
        final String regexStr = "^[0-9]*$";
        boolean isValid = false;
        EditText errEdit = null;

        do {
            if (mUI_BoardWidth.getText().length() == 0 || !mUI_BoardWidth.getText().toString().trim().matches(regexStr)) {
                errEdit = mUI_BoardWidth;
                break;
            }

            if (mUI_BoardHeight.getText().length() ==0 || !mUI_BoardHeight.getText().toString().trim().matches(regexStr)) {
                errEdit = mUI_BoardHeight;
                break;
            }

            try{
                if(mUI_GridSize.getText().length() > 0) {
                    Float.parseFloat(mUI_GridSize.getText().toString());
                    isValid = true;
                }
                else {
                    errEdit = mUI_GridSize;
                }
            }
            catch(NumberFormatException e){
                errEdit = mUI_GridSize;
                break;
            }

        } while(false);


        if(errEdit != null && prevHintId != errEdit.getId()) {
            final EditText finalErrEdit = errEdit;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConstraintSet set = new ConstraintSet();
                    set.clone(mUI_Layout);
                    set.clear(mUI_ErrorHint.getId(), ConstraintSet.TOP);
                    set.connect(mUI_ErrorHint.getId(), ConstraintSet.TOP, finalErrEdit.getId(), ConstraintSet.TOP, 0);
                    set.applyTo(mUI_Layout);

                    mUI_ErrorHint.setVisibility(View.VISIBLE);
                }
            });
            prevHintId = errEdit.getId();
        }
        else if(isValid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUI_ErrorHint.setVisibility(View.INVISIBLE);
                }
            });
            prevHintId = 0;
        }

        return isValid;
    }


    VIACamera.Callback mFrameCallback = new VIACamera.Callback() {
        @Override
        public void onEOS() {
            // on video end , release camera.
            if(getSystemStatus() != SystemStatus.Pause) {
                releaseCamera();
            }
        }

        @Override
        public void onFrameReady() {
            synchronized (mMutex_OnCameraAccess) {
                if(mCamera == null) return;

                boolean isValid = checkInputValid();

                // In detection mode, notify ADAS thread to lock resource and wait locking finish.
                if (isValid && getSystemStatus() == SystemStatus.Detection && mCamCalibrator != null) {
                    boolean isFound = false;
                    Image frame = null;
                    ByteBuffer buffer = null;
                    double timeDiff = (System.nanoTime() - prevFetchTime) / 1e6;

                    switch (mCameraMode) {
                        case Camera2:
                            frame = mCamera.dequeueBuffer();
                            // TODO : Add findPattern for camera2
                            break;
                        case Native:
                        case Camera:
                        case FakeCameraGPU:
                            buffer = mCamera.dequeueBufferAndGetByteBuffer();
                            if(buffer != null && timeDiff > 200) {
                                isFound = mCamCalibrator.findPattern(FrameFormat.NV12, buffer, mFrameWidth, mFrameHeight);
                                prevFetchTime = System.nanoTime();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUI_CountPercentage.setText( mCamCalibrator.getRepositoryRatio() + "%");
                                    }
                                });

                                if(isFound) {
                                    mUI_CameraView.setViewPosition(mCameraViewId_F, 0.05f, 0.05f, 0.9f, 0.9f, false);
                                }
                                else {
                                    mUI_CameraView.setViewPosition(mCameraViewId_F, 0.0f, 0.0f, 1.0f, 1.0f, false);
                                }

                                // start calibration
                                if(mCamCalibrator.getRepositoryRatio() >= 100){
                                    startCalibration();
                                }
                            }
                            break;
                    }

                }

                mCamera.queueBuffer();
            }
        }
    };

    public void startCamera(final SurfaceTexture[] surfaceTextures) {

        try {
            if(mCameraMode.equals(VIACamera.MODE.FakeCameraGPU)) {
                mCamera = VIACamera.create(mCameraMode, mVideoPath, surfaceTextures[0]);
                mCamera.loop(true);
            } else {
                mCamera = VIACamera.create(mCameraMode, CameraCalibActivity.this, mCameraDeviceIdentify, mFrameWidth, mFrameHeight, surfaceTextures[0]);
            }
            mCamera.setCallback(mFrameCallback);
            mCamera.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseCamera() {
        if(mCamera != null) {
            mCamera.pause();
        }
    }

    private void resumeCamera() {
        if(mCamera != null) {
             mCamera.resume();
        }
    }

    private void releaseCamera() {
        synchronized (mMutex_OnCameraAccess) {
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
        }
    }


    /*
        Callback functions of requesting permissions
     */
    private boolean checkAllPermission() {
        String permissions[] = new String [] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
        };
        boolean granted = true;

        for(int i = 0 ; granted == true && i < permissions.length ; i++) {
            if (ContextCompat.checkSelfPermission(CameraCalibActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if(granted) {
            bIsCameraPermissionGuaranteed = true;
        }

        return granted;
    }


    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeedPermissions() {
        Log.d(TAG,"onNeedPermissions");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraCalibActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        Log.d(TAG,"onRequestPermissionsResult");
        for(int i=0;i<permissions.length;i++) {
            Log.d(TAG,permissions[i]+","+grantResults[i]);
            if(permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                // Application has mCamera permission then start mCamera
                //startCamera(mSurfaceTextures);
                //startCamera();
                bIsCameraPermissionGuaranteed = true;
            }
        }
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onShowRationale(final PermissionRequest request) {
        request.proceed();
        Log.d(TAG,"onShowRationale");
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onPermissionDenied() {
        bIsAnyPermissionDenied = true;

        if(bIsAnyPermissionDenied) {
            mUI_LoadingLayout.setLoadNote("");
        }

        Spanned dialogText;
        dialogText = Html.fromHtml("Some necessary permission is denied, please allow all permissions. <br>"+
                "If \"<b>Never ask again</b>\" button is clicked, please enable the permissions with Android Setting->Applications->VIA_ADAS->Permisssions.");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Error);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(dialogText);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        Log.d(TAG,"onNeverAskAgain");

    }

}

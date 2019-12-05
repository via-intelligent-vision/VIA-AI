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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.OnBoomListener;

import com.viatech.adas.webservices.AWS.S3.S3DataTypes;
import com.viatech.adas.webservices.WebService;
import com.viatech.automotive.AutomotiveController;
import com.viatech.automotive.event.EventSlot;
import com.viatech.automotive.event.EventTypes;
import com.viatech.camera.CameraLocationTypes;
import com.viatech.camera.CameraModule;
import com.viatech.car.CarTypes;
import com.viatech.media.FrameFormat;
import com.viatech.resource.ResourceManager;
import com.viatech.sensing.DetectorTypes;
import com.viatech.sensing.SensingModule;
import com.viatech.sensing.SensingSamples;
import com.viatech.utility.camera.FakeCameraGPU;
import com.viatech.utility.camera.VIACamera;
import com.viatech.utility.gles.VIASurfaceView;
import com.viatech.utility.video.VIARecorder;
import com.viatech.vBus.CANDongleTypes;
import com.viatech.vBus.CANbusModule;
import com.viatech.via_ai.Location.LocationGPS;
import com.viatech.via_ai.Media.CameraPermutation;
import com.viatech.via_ai.Media.EventSpeaker;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.System.Helper;
import com.viatech.via_ai.System.SystemEvents;
import com.viatech.via_ai.UI.ADASEffectOverlayView;
import com.viatech.via_ai.UI.BatteryStatusView;
import com.viatech.via_ai.UI.CalibrationErrorView;
import com.viatech.via_ai.UI.DrawSensingSamples;
import com.viatech.via_ai.UI.LoadingWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends Activity {
    final static String TAG = "VIA_AdvanceDashCam";
    private Context mContext = this;

    public enum SystemStatus {
        Init,
        Detection,
        Calibration,
        Setting,
        Pause
    }

    // Camera View Id set , these id are used to specify view number of VIASurfaceView.
    private static final int mCameraViewId_F = 0;

    // Camera setttings
    private volatile boolean bIsAllPermissionGuaranteed = false; // as true if permission guaranteed
    private volatile boolean bIsCameraSurfaceViewCreated = false; // as true if surfaceview created.
    private int mFrameWidth = 1280;
    private int mFrameHeight = 720;
    private int mCameraDeviceIdentify = 0;
    private VIACamera mCamera;
    private VIACamera.MODE mCameraMode = VIACamera.MODE.Camera2;
    private VIARecorder mVIARecorder = null;
    private CameraPermutation mCameraPermutation = CameraPermutation.Camera1_In_Frame;
    private VIASurfaceView mCameraDisplayView;
    private ADASEffectOverlayView mADASEffectOverlayView;
    private String mVideoPath = "/sdcard/VIA_ADAS/F_fo-20170718_104639.mp4";
    private String mRecordPath = "/sdcard/VIA_ADAS/record/";
    private SurfaceTexture[] mSurfaceTextures;

    final static long REMAIN_SIZE = 200 * 1024 * 1024L;

    private volatile Object mMutex_OnCameraAccess = new Object();

    // ADAS
    private String mConfigPath = "/sdcard/VIA_ADAS/";
    private String mConfigName = "LDWSConfig_1024x576_20170629_TW.xml";
    private String mConfigFullPath;
    private String mConfigExportFullPath;
    private ResourceManager mResourceManager = null;

    // Sensing module
    private CameraModule mCameraModule_Front = null;
    private Rect mSensingRoi_Front = null;
    private SensingModule mSensingModule_LDWS = null;
    private SensingTask_LDWS mSensingTask_LDWS = null;

    // Automotive Controller
    private AutomotiveController mAutomotiveController = null;
    private Thread mThread_AutomotiveController = null;

    // CANbus module;
    private CANbusModule mCANbusModule = null;
    private Thread mThread_CANbusModule = null;

    // UI Part
    private LoadingWrapper mUI_LoadingLayout;
    private BoomMenuButton mUI_BMB;
    private ImageView mUI_BusStatus;
    private ImageView mUI_LKSStatus;
    private ImageView mUI_ACCStatus;
    private AppCompatTextView mUI_VehicleSpeed;
    private BatteryStatusView mUI_BatteryStatus;
    private CalibrationErrorView mUI_CalibrationErrorView;

    // System status
    private volatile SystemStatus mSystemStatus = SystemStatus.Init;
    private volatile SystemStatus mPrevSystemStatus = SystemStatus.Init;
    private volatile boolean bIsAnyPermissionDenied = false;
    private InitAsyncTask mInitAsyncTask;
    private volatile long mFrameSupplierPauseId = -1;
    private Helper mHelper;

    // location
    private LocationGPS mLocationGPS;


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

                // parse config
                String configPath = (String) parameterBundle.get(getResources().getString(R.string.key_config_path));
                String configName = (String) parameterBundle.get(getResources().getString(R.string.key_config_name));
                if (configPath != null && configName != null && configPath.length() > 0 && configName.length() > 0) {
                    mConfigPath = configPath;
                    mConfigName = configName;
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
        mConfigExportFullPath = mConfigPath + "/" + mConfigName;
        mConfigFullPath = mConfigPath + "/" + mConfigName;
        mRecordPath = Preferences.getInstance().getFrameRecordData().getRecordPath();


        // set view by system mode.
        setContentView(R.layout.activity_main);

        // Setup UI
        setupUI();
    }

    @Override
    public void onBackPressed() {

    }

    private void startBatteryMonitor() {
        if(mUI_BatteryStatus != null) {
            mUI_BatteryStatus.start();
        }
    }
    private void stopBatteryMonitor() {
        if(mUI_BatteryStatus != null) {
            mUI_BatteryStatus.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bIsAnyPermissionDenied) {
            if (!checkAllPermission()) {
                Log.e(TAG, "checkAllPermission Fail");
                MainActivityPermissionsDispatcher.onNeedPermissionsWithCheck(MainActivity.this);
            } else {
                // change system mode
                if (mPrevSystemStatus == SystemStatus.Calibration)
                    setSystemStatus(SystemStatus.Calibration);
                else {
                    setSystemStatus(SystemStatus.Init);
                }
                DrawSensingSamples.loadData(this);

                mInitAsyncTask = new InitAsyncTask();
                mInitAsyncTask.execute();
            }
        }


        if(mServer == null) {
            Intent mIntent = new Intent(this, WebService.class);
            bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        if (bIsAllPermissionGuaranteed) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        // clear system mode
        setSystemStatus(SystemStatus.Pause);

        releaseAutomotiveController();

        releaseSensingModules();

        releaseCameraModules();

        releaseCamera();

        releaseLocation();

        releaseCANbusModule();

        stopBatteryMonitor();

        if(mServer != null) {
            unbindService(mConnection);
            mServer = null;
        }
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private WebService mServer = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebService.LocalBinder mLocalBinder = (WebService.LocalBinder)service;
            mServer = mLocalBinder.getServiceInstance();
            //  Toast.makeText(MainActivity.this, "Service is connected " + mServer.getTime(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServer = null;
            //   Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }

    };



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

            startBatteryMonitor();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            // Wait all permission.
            this.waitPermissions();


            // Init canbus module (must init before sensing modules)
            initCANbusModule();

            // init camera surface view
            setupCameraSurfaceView();

            // apply view scenario
            setSurfaceViewScenario(mCameraPermutation, SurfaceViewScenarioTypes.CameraInit);

            // wait camera surface view create finish.
            this.waitSurfaceViewCreated();

            // start camera
            startCamera(mSurfaceTextures);

            // Delay 2s to check all camera view (for the preview purpose in n-in-1 camera mode).
            waitUserCameraPreview(500);

            // Init ADAS camera moduels (it's different to Am,must init before sensing modules)
            initADASCameraModules();

            // Init sensing modules ADAS
            initSensingModules();

            // Init AutomotiveController
            initAutomotiveController();

            // Init location
            initLocation();

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

            // Start vehiclebus thread
            startCANbusModule();

            // Start Detection thread
            startSensingModules();

            // Start AutomotiveController thread
            startAutomotiveController();

            // Start location
            startLocation();

            if(mCameraModule_Front != null && !mCameraModule_Front.isStable()) {
                EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationError);
                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.NON_CALIBRATED);
            }
            else {
                EventSpeaker.getInstance().pushEvent(SystemEvents.SensingSystemStart);
                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.CALIBRATED);
            }
        }

        private void waitPermissions() {
            while(!bIsAllPermissionGuaranteed) {
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

    private void initADASCameraModules()
    {
        Log.i(TAG, "initADASCameraModules");
        mCameraModule_Front = new CameraModule(Preferences.getInstance().getFrameSourceData().getCameraTypes(0),
                CameraLocationTypes.Front,
                Preferences.getInstance().getFrameSourceData().getCameraCalibPath(0),
                mConfigFullPath);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, Preferences.getInstance().getFrameSourceData().getCameraTypes(0).toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void releaseCameraModules()
    {
        Log.i(TAG, "releaseCameraModules");
        if(mCameraModule_Front != null) {
            mCameraModule_Front.release();
        }
    }

    private void initAutomotiveController()
    {
        Log.i(TAG, "initAutomotiveController");
        Log.i(TAG, "calib H : " +
                Preferences.getInstance().getFrameSourceData().getCameraInstallHeight(0) + ", To center " +
                Preferences.getInstance().getFrameSourceData().getCameraToCenterOffset(0));
        try {
            if(mAutomotiveController == null) {
                String ctlCfgPath= Preferences.getInstance().getConfigurationData().getAutomotiveCfgPath();
                String sensingCfgPath = Preferences.getInstance().getConfigurationData().getSensingCfgPath();
                String calibExportPath = sensingCfgPath.substring(0, sensingCfgPath.lastIndexOf("/"));

                mAutomotiveController = new AutomotiveController(CarTypes.HONDA_CRV_2017_BOSCH, ctlCfgPath, calibExportPath, Preferences.getInstance().getFrameSourceData().getCameraInstallHeight(0), 5);
                mAutomotiveController.registerCANbusModule(mCANbusModule);
                mAutomotiveController.registerSensingModule_Lane(mSensingModule_LDWS);
                mAutomotiveController.registerCameraModule(mCameraModule_Front);

                mAutomotiveController.setOnEventChangeListener(new AutomotiveController.OnEventChangeListener() {
                    @Override
                    public void onEventChange(EventSlot lastEvent, final EventSlot curEvent) {
                        switch (curEvent.getType()) {
                            case SystemDisable:
                            case SystemDisable_Gas:
                            case SystemDisable_Brake:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.AutomaticSystemDisable);
                                break;
                            case SystemDisable_InvalidCalibration:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationError);
                                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.NON_CALIBRATED);
                                break;
                            case LaneDeparture:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.LaneDeparture);
                                break;
                            case LaneDetectFail:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.LaneMissing);
                                break;
                            case CurvatureOverControl:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.CurvatureOverControl);
                                break;
                            case SystemEnable:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.AutomaticSystemEnable);
                                break;
                            case CalibrationStart:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationStart);
                                // copy new file
                                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.CALIBRATING);
                                break;
                            case CalibrationFail:
                                //EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationFail);
                                EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationSuccess);
                                Preferences.getInstance().getConfigurationData().setSensingCfgPath(mAutomotiveController.getCalibExportPath());
                                Preferences.getInstance().save(mContext);
                                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.NON_CALIBRATED);
                                break;
                            case CalibrationSuccess:
                                EventSpeaker.getInstance().pushEvent(SystemEvents.CalibrationSuccess);
                                Preferences.getInstance().getConfigurationData().setSensingCfgPath(mAutomotiveController.getCalibExportPath());
                                Preferences.getInstance().save(mContext);
                                mUI_CalibrationErrorView.setMode(CalibrationErrorView.Mode.CALIBRATED);
                                break;
                            case DriverClickAccelBtn:

                                break;
                            case DriverClickDecelBtn:

                                break;
                            case NoEvent:
                                break;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(curEvent.getType() != EventTypes.NoEvent) {
                                    Toast.makeText(MainActivity.this, curEvent.getType().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

            }
        } catch (final IOException e) {
            if(mAutomotiveController != null) {
                mAutomotiveController.release();
                mAutomotiveController = null;
            }
            e.printStackTrace();

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Info);
                    dialogBuilder.setTitle("Information");
                    dialogBuilder.setMessage(e.getMessage());
                    dialogBuilder.setPositiveButton("OK", null);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    alertDialog.show();
                    Helper helper = new Helper();
                    helper.setupAutoHideSystemUI(alertDialog.getWindow());
                }
            });

        }
    }

    private void startAutomotiveController() {
        Log.i(TAG, "startAutomotiveController");
        if(mAutomotiveController != null) {
            mThread_AutomotiveController = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread currentThread = Thread.currentThread();

                    while (mThread_AutomotiveController == currentThread && getSystemStatus() != SystemStatus.Pause) {
                        mAutomotiveController.runAutoControl();

                        mADASEffectOverlayView.setLatitudePlan(mAutomotiveController.getLatitudePlan());

                        try {
                            Thread.sleep(5);    // 100 Hz
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mThread_AutomotiveController.start();
        }
    }

    private void releaseAutomotiveController() {
        Log.i(TAG, "releaseAutomotiveController");
        if(mThread_AutomotiveController != null) {
            try {
                mThread_AutomotiveController.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(mAutomotiveController != null) {
            mAutomotiveController.release();
            mAutomotiveController = null;
        }
    }

    private void initLocation() {
        Log.i(TAG, "initLocation");
        mLocationGPS = new LocationGPS(this);
        mLocationGPS.init();
        mLocationGPS.setOnLocationChangedListener(new LocationGPS.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(mCANbusModule != null) mCANbusModule.setGPS(location.getLatitude(), location.getLongitude());
            }
        });

    }

    private void startLocation() {
        Log.i(TAG, "startLocation");
        if(mLocationGPS != null) {
            mLocationGPS.start();
        }
    }

    private void releaseLocation() {
        Log.i(TAG, "releaseLocation");
        if(mLocationGPS != null) {
            mLocationGPS.stop();
            mLocationGPS = null;
        }
    }

    /** Init CANbus module
     */
    private void initCANbusModule() {
        Log.i(TAG, "initCANbusModule");
        if(mCANbusModule == null && Preferences.getInstance().getVehicleData().isEnableVehiclebus()) {
            mCANbusModule = new CANbusModule(this);
            if (mCANbusModule.init(Preferences.getInstance().getVehicleData().getCarType(), CANDongleTypes.commaai_WhitePanda)) {
                Log.i(TAG, "mCANbusModule.init finish");
            }
            else {
                Log.e(TAG, "mCANbusModule.init fail");
            }

        }
    }

    private void startCANbusModule() {
        Log.i(TAG, "startCANbusModule");
        if(mCANbusModule != null) {
            mThread_CANbusModule = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Note:  call exec() will block the thread
                    mCANbusModule.exec(33);
                }
            });
            mThread_CANbusModule.setPriority(Thread.MAX_PRIORITY);  // NOTE : CANbus module need MAX_PRIORITY to keep frequency.
            mThread_CANbusModule.start();
        }
    }

    private void releaseCANbusModule() {
        Log.i(TAG, "releaseCANbusModule");
        if(mCANbusModule != null) {
            if(mCANbusModule.isRecording()) {
                mCANbusModule.stopRecord();
            }
            mCANbusModule.release();
            mCANbusModule = null;
        }
    }

    /** Init Sensing
        1. This step will init and apply resource to adas modules)
        2. This resource such as SLD model, FCWS cascade model, FCWS_DL model must set in this functions.
         */
    private void initSensingModules()
    {
        if(mSensingModule_LDWS != null) return;

        // --------------------------------------------------------------------------------
        // 0. Set detect ROI for ADAS function.
        int imgWidth ,imgHeight;
        switch (mCameraPermutation) {
            case Camera1_In_Frame:
                imgWidth = mFrameWidth ;
                imgHeight = mFrameHeight;
                mSensingRoi_Front = new Rect(0, 0, imgWidth, imgHeight);
                break;
            case Camera2_In_Frame_1x2:

                break;
            case Camera3_In_Frame_1x3:
                imgWidth = mFrameWidth / 3;
                imgHeight = mFrameHeight;
                mSensingRoi_Front = new Rect(0, 0, imgWidth, imgHeight);
                break;
            case Camera4_In_Frame_2x2:
                imgWidth = mFrameWidth / 2;
                imgHeight = mFrameHeight / 2;
                mSensingRoi_Front = new Rect(0, 0, imgWidth, imgHeight);
                break;
            case Camera4_In_Frame_1x4:
                imgWidth = mFrameWidth / 4;
                imgHeight = mFrameHeight;
                mSensingRoi_Front = new Rect(0, 0, imgWidth, imgHeight);
                break;
        }

        // --------------------------------------------------------------------------------
        // 1. Set Detectors for ADAS function.
        if(Preferences.getInstance().getLaneDepartureWarningData().isEnable()  || Preferences.getInstance().getSpeedLimitWarningData().isEnable()) {
            EnumSet<DetectorTypes> set = EnumSet.noneOf(DetectorTypes.class);

            if(Preferences.getInstance().getLaneDepartureWarningData().isEnable()) {
                set.add(DetectorTypes.LDWS);
            }
            if(Preferences.getInstance().getSpeedLimitWarningData().isEnable()) {
                set.add(DetectorTypes.SLD);
                set.add(DetectorTypes.Weather);
            }

            if(Preferences.getInstance().getTrafficSignalData().isEnable()) {
                set.add(DetectorTypes.TLD);
            }
            mSensingModule_LDWS = new SensingModule(set, mCANbusModule, mCameraModule_Front);
            mSensingModule_LDWS.setCallback(new SensingModule.Callbacks() {
                @Override
                public void onCalibrationFinish(boolean success) {
                    Log.i(TAG, "onCalibrationFinish " + success);
                    // On enable automotiveController, it will handle cnofig restoration on calibration finish.
                    //mSensingModule_LDWS.restoreConfiguration(Preferences.getInstance().getConfigurationData().getSensingCfgPath());
                }
            });
        }



        // --------------------------------------------------------------------------------
        // 2. Init ADAS
        if(mSensingModule_LDWS != null) {
            mSensingModule_LDWS.init(this, mConfigFullPath);
        }

        // --------------------------------------------------------------------------------
        // 3. Init sensing tasks
        if(Preferences.getInstance().getLaneDepartureWarningData().isEnable() && mSensingTask_LDWS == null) {
            mSensingTask_LDWS = new SensingTask_LDWS();
            mSensingTask_LDWS.init(mSensingModule_LDWS);
        }
    }

    private void startSensingModules()
    {
        Log.i(TAG, "startSensingModules");
        // Start Detection thread
        if (mSensingTask_LDWS != null) {
            mSensingTask_LDWS.start(Thread.MAX_PRIORITY);
        }
    }

    private void releaseSensingModules()
    {
        Log.i(TAG, "releaseSensingModules");
        // release thread
        if(mSensingTask_LDWS != null) {
            mSensingTask_LDWS.release();
            mSensingTask_LDWS = null;
        }
    }

    /**
     @brief setup & find application UI
     */
    private void setupUI()
    {
        // Find all ui
        mUI_LoadingLayout =  findViewById(R.id.layoutActivityAdas_LoadingLayout);
        mCameraDisplayView = findViewById(R.id.layoutActivityAdas_CameraDisplayView);
        mADASEffectOverlayView = findViewById(R.id.layoutActivityAdas_AdasEffectView);
        mUI_BMB = findViewById(R.id.layoutActivityAdas_BMB);
        mUI_BusStatus = findViewById(R.id.BusStatus);
        mUI_LKSStatus = findViewById(R.id.LKSStatus);
        mUI_ACCStatus = findViewById(R.id.ACCStatus);
        mUI_BatteryStatus = findViewById(R.id.BatteryStatus);
        mUI_CalibrationErrorView = findViewById(R.id.CalibrationErrorView);
        mUI_VehicleSpeed = findViewById(R.id.VehicleSpeed);


        AssetManager am = this.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am, String.format(Locale.US, "font/%s", "cursedtimerulil.ttf"));
        mUI_VehicleSpeed.setTypeface(typeface);

        // Build BMB
        mUI_BMB.addBuilder(new HamButton.Builder()
                .normalImageRes(R.drawable.exit_btn_type_c)
                .ellipsize(TextUtils.TruncateAt.MARQUEE)
                .normalColor(Color.rgb(255, 255, 255))
                .pieceColor(Color.argb(0, 255, 255, 255))
                .normalText("Exit System")
                .normalTextColor(Color.rgb(125, 125, 125))
                .subNormalText("Exit system immediately, all controller will be closed.")
                .subNormalTextColor(Color.rgb(125, 125, 125))
                );
        mUI_BMB.addBuilder(new HamButton.Builder()
                .normalImageRes(R.drawable.autocalibration_btn_type_c)
                .normalColor(Color.rgb(255, 255, 255))
                .pieceColor(Color.argb(0, 255, 255, 255))
                .normalText("Auto Calibration")
                .normalTextColor(Color.rgb(125, 125, 125))
                .subNormalText("Run auto calibration, it will take few times to calibrate system.")
                .subNormalTextColor(Color.rgb(125, 125, 125))
        );

        mUI_BMB.setNormalColor(Color.argb(0, 0, 0, 0));
        mUI_BMB.setHighlightedColor(Color.argb(0, 0, 0, 0));
        mUI_BMB.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                switch(index) {
                    case 0:     // Exit
                        ((Activity)mContext).finish();
                        break;
                    case 1:
                        if(mAutomotiveController != null) {
                            mAutomotiveController.runCameraCalibration(CameraLocationTypes.Front, 145.0f, 5.0f);
                        }
                        break;
                }
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });

    }

    private void setupCameraSurfaceView()
    {
        // init surface view
        mCameraDisplayView.setRenderView(VIASurfaceView.View.Full);
        mCameraDisplayView.setCallback(new VIASurfaceView.Callback() {
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
                mCameraDisplayView.addView(mCameraViewId_F);
                mCameraDisplayView.setViewVisibility(mCameraViewId_F, false);
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

                mADASEffectOverlayView.setVisibility(View.GONE);
                mUI_BMB.setVisibility(View.GONE);
                break;

            case Detection:
                mUI_LoadingLayout.hide();
                mADASEffectOverlayView.setVisibility(View.VISIBLE);
                mUI_BMB.setVisibility(View.VISIBLE);
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
                mCameraDisplayView.setViewVisibility(mCameraViewId_F, true);
                mCameraDisplayView.setViewPosition(mCameraViewId_F, 0.05f, 0.05f, 0.9f, 0.9f, false);
                mCameraDisplayView.setViewTextCoord(mCameraViewId_F, 0.0f, 0.0f,  1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
                break;

            case Detection:
                mCameraDisplayView.setViewVisibility(mCameraViewId_F, true);
                mCameraDisplayView.setViewPosition(mCameraViewId_F, 0.0f, 0.0f, 1.0f, 1.0f, false);
                mCameraDisplayView.setViewTextCoord(mCameraViewId_F,0.0f, 0.0f,1.0f,0.0f, 1.0f, 1.0f,0.0f, 1.0f);
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


    private int prevBusImgID = 0;
    private int prevLKSImgID = 0;
    private int prevACCImgID = 0;
    private void updateUI_AutomotiveController() {
        do {
            int carSpeed = 0;
            boolean isControlSystemReady = false;
            boolean isLKSEnabled = false;
            boolean isSteerControllable = false;
            boolean isSteerOverControl = false;
            boolean isACCEnabled = false;
            boolean isCANDongleActive = false;
            boolean isRightBlikerOn = false;
            boolean isLeftBlikerOn = false;
            byte wriperStats ;

            if(mCANbusModule != null) {
                carSpeed = (int)mCANbusModule.getSpeed().roughSpeed;
                isControlSystemReady  = mCANbusModule.getSafetyFeature().isControlSystemReady;
                isLKSEnabled = mCANbusModule.getSafetyFeature().isEnabled_LKS;
                isACCEnabled = mCANbusModule.getSafetyFeature().isEnabled_ACC;
                isCANDongleActive = mCANbusModule.isConnected();
                isRightBlikerOn = mCANbusModule.getDriverControllers().rightBlinkerOn;
                isLeftBlikerOn = mCANbusModule.getDriverControllers().leftBlinkerOn;
                wriperStats = mCANbusModule.getDriverControllers().wiperStatus;
            }

            if(mAutomotiveController != null) {
                isSteerControllable = mAutomotiveController.getLatitudePlan().isSteerControllable;
                isSteerOverControl = mAutomotiveController.getLatitudePlan().isSteerOverControl;
            }

            do {
                final int ImgID ;
                if (isCANDongleActive && isControlSystemReady) {
                    ImgID = R.drawable.icon_small_bus_connect;
                } else if (isCANDongleActive) {
                    ImgID = R.drawable.icon_small_bus_connect;
                } else {
                    ImgID = R.drawable.icon_small_bus_disconnect;
                }

                if (ImgID != prevBusImgID) {
                    if (mUI_BusStatus != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUI_BusStatus.setImageResource(ImgID);
                            }
                        });
                    }
                    prevBusImgID = ImgID;
                }
            } while(false);


            do {
                final int ImgID;
                if (isLKSEnabled && isSteerControllable) {
                    ImgID = R.drawable.icon_small_lks_on;
                } else {
                    ImgID = R.drawable.icon_small_lks_off;
                }
                if (ImgID != prevLKSImgID) {
                    if (mUI_LKSStatus != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUI_LKSStatus.setImageResource(ImgID);
                            }
                        });
                    }
                    prevLKSImgID = ImgID;
                }
            } while(false);

            do {
                final int ImgID;
                if (isACCEnabled) {
                    ImgID = R.drawable.icon_small_acc_on;
                } else {
                    ImgID = R.drawable.icon_small_acc_off;
                }
                if (ImgID != prevACCImgID) {
                    if (mUI_ACCStatus != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUI_ACCStatus.setImageResource(ImgID);
                            }
                        });
                    }
                    prevACCImgID = ImgID;
                }
            } while(false);


            mUI_VehicleSpeed.setText(Integer.toString(carSpeed));

        } while(false);
    }

    /**
     @brief SensingTasks , these task is used to handle thread for ADAS Sensing module.
     */
    private abstract class SensingTask implements Runnable {
        private volatile Thread mThread;
        private volatile boolean mIsActive;
        private SensingModule mSensingModule;

        public abstract boolean sensing();
        public abstract void postResult();

        private SensingTask() {
            mThread = null;
            mIsActive = false;
            mSensingModule = null;
        }

        public void init(SensingModule module) {
            mSensingModule = module;
        }

        public boolean isActive() {
            return mIsActive;
        }

        public void start(int newPriority) {
            if(mThread != null) {
                Log.i(TAG, "release previous task ....");
                release();
            }

            mIsActive = true;
            mThread = new Thread(this);
            mThread.setPriority(newPriority);
            mThread.start();
        }

        public void release() {
            mIsActive = false;
            try {
                if(mThread != null) mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public SensingModule getSensingModule() {
            return mSensingModule;
        }

        @Override
        public void run() {
            //while (mThread == currentThread && getSystemStatus() != SystemStatus.Pause) {
            while (isActive()) {
                if(sensing()) {
                    postResult();
                }
                else {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class SensingTask_LDWS extends SensingTask {
        @Override
        public boolean sensing() {
            boolean ret = false;

            SensingModule module = this.getSensingModule();
            if(module != null) {
                // detect (if no frame buffered, it will return immediately)
                module.detect(false);
                ret = true;
            }
            return ret;
        }

        @Override
        public void postResult() {
            // post process
            SensingModule module = this.getSensingModule();
            if(module != null) {
                if (module.isDetectorActive(DetectorTypes.LDWS)) {
                    SensingSamples.LaneDetectSample sample = (SensingSamples.LaneDetectSample) module.getSensingSample(SensingSamples.SampleTypes.LaneDetectSample);
                    mADASEffectOverlayView.setSensingSample(module, sample);
                }

                if (module.isDetectorActive(DetectorTypes.Weather)) {
                    SensingSamples.EnvironmentSample sample = (SensingSamples.EnvironmentSample) module.getSensingSample(SensingSamples.SampleTypes.EnvironmentSample);
                    mADASEffectOverlayView.setSensingSample(module, sample);
                }

                if (module.isDetectorActive(DetectorTypes.SLD)) {
                    SensingSamples.SpeedLimitDetectSample sample = (SensingSamples.SpeedLimitDetectSample) module.getSensingSample(SensingSamples.SampleTypes.SpeedLimitDetectSample);
                    mADASEffectOverlayView.setSensingSample(module, sample);
                }

                if (module.isDetectorActive(DetectorTypes.TLD)) {
                    SensingSamples.TrafficLightSample sample = (SensingSamples.TrafficLightSample) module.getSensingSample(SensingSamples.SampleTypes.TrafficLightDetectSample);
                    mADASEffectOverlayView.setSensingSample(module, sample);
                }

            }
        }
    }

    private int mFPS_FrameCounter = 0;  // the value to count

    VIACamera.Callback mFrameCallback = new VIACamera.Callback() {
        @Override
        public void onEOS() {
            // on video end , release camera.
            // in pnpause, don't release again.
            if(getSystemStatus() != SystemStatus.Pause) {
                releaseCamera();
            }
        }

        @Override
        public void onFrameReady() {
            synchronized (mMutex_OnCameraAccess) {
                if(mCamera == null) return;

                // In detection mode, notify ADAS thread to lock resource and wait locking finish.
                if (getSystemStatus() == SystemStatus.Detection) {
                    Image frame = null;
                    long framePtr = 0;
                    switch (mCameraMode) {
                        case Camera2:
                            frame = mCamera.dequeueBuffer();

                            if (mSensingModule_LDWS != null && frame != null && frame.getPlanes() != null) {
                                Image.Plane yPlane = frame.getPlanes()[0];
                                Image.Plane uPlane = frame.getPlanes()[1];
                                Image.Plane vPlane = frame.getPlanes()[2];
                                mSensingModule_LDWS.bufferFrame(yPlane.getBuffer(), uPlane.getBuffer(), vPlane.getBuffer(),
                                        frame.getWidth(), frame.getHeight(),
                                        yPlane.getRowStride(), uPlane.getRowStride(), vPlane.getRowStride(),
                                        yPlane.getPixelStride(), uPlane.getPixelStride(), vPlane.getPixelStride(),
                                        mSensingRoi_Front.left, mSensingRoi_Front.top, mSensingRoi_Front.width(), mSensingRoi_Front.height());
                            }

                            break;
                        case Native:
                        case Camera:
                        case FakeCameraGPU:
                            framePtr = mCamera.dequeueBufferAndGetPointer();
                            if(framePtr != 0) {
                                if (mSensingModule_LDWS != null) {
                                    mSensingModule_LDWS.bufferFrame(framePtr, FrameFormat.NV12, mFrameWidth, mFrameHeight, mSensingRoi_Front.left, mSensingRoi_Front.top, mSensingRoi_Front.width(), mSensingRoi_Front.height());
                                }
                            }
                            break;
                    }


                    // Refresh UI
                    if(mCANbusModule != null) {
                        mADASEffectOverlayView.setCANParams_SteeringSensor(mCANbusModule.getSteeringSensor());
                        mADASEffectOverlayView.setCANParams_SafetyFeature(mCANbusModule.getSafetyFeature());
                    }
                    mADASEffectOverlayView.postInvalidate();
                    updateUI_AutomotiveController();
                }

                mCamera.queueBuffer();

                mFPS_FrameCounter++;
            }
        }
    };

    public void startCamera() {

        try {
            if(mCameraMode.equals(VIACamera.MODE.FakeCameraGPU)) {
                mCamera = VIACamera.create(mCameraMode, mVideoPath, mCameraDisplayView);

            } else {
                mCamera = VIACamera.create(mCameraMode, MainActivity.this, mCameraDeviceIdentify, mFrameWidth, mFrameHeight, mCameraDisplayView);
            }
            mCamera.setCallback(mFrameCallback);
           // mCamera.enableRecord(mRecordPath,mRecordBitrate,mRecordFPS,mRecordPerodicTimeInSec,mRecordFileListener);

            mCamera.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCamera(final SurfaceTexture[] surfaceTextures) {

        try {
            if(mCameraMode.equals(VIACamera.MODE.FakeCameraGPU)) {
                mCamera = VIACamera.create(mCameraMode, mVideoPath, surfaceTextures[0]);
                mCamera.loop(true);
            } else {
                mCamera = VIACamera.create(mCameraMode, MainActivity.this, mCameraDeviceIdentify, mFrameWidth, mFrameHeight, surfaceTextures[0]);
            }
            mCamera.setCallback(mFrameCallback);

            // check record status
            if(Preferences.getInstance().getFrameRecordData().isRecordEnable() && mCameraMode != VIACamera.MODE.FakeCameraGPU) {
                int bitrate = Preferences.getInstance().getFrameRecordData().getEncodeBitRate();
                switch(Preferences.getInstance().getFrameSourceData().getCameraPermutation()) {
                    case Camera1_In_Frame:
                        Log.i(TAG, "Enable 1in1 (1x1) record");
                        mVIARecorder = new VIARecorder(mRecordPath,
                                "record-",
                                mFrameWidth, mFrameHeight,
                                bitrate,
                                30 + 5,
                                Preferences.getInstance().getFrameRecordData().getRecorInterval_s() * 60,
                                VIARecorder.Mode.Surface);
                        break;
                    default:
                        mVIARecorder = new VIARecorder(mRecordPath, "record-", mFrameWidth, mFrameHeight, bitrate, 30 + 5, 60 * 3, VIARecorder.Mode.Surface);
                        break;
                }

                mVIARecorder.start();
                mCameraDisplayView.setVIARecorder(mVIARecorder);
                mVIARecorder.addFileListener(mRecordFileListener);
            }

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

    public long getCurrentPosition() {
        long currentPosition = 0;
        if(mCamera != null) {
            currentPosition =  ((FakeCameraGPU)mCamera).getCurrentPosition();
        }
        return currentPosition;
    }

    private void releaseCamera() {
        Log.i(TAG, "releaseCamera");
        synchronized (mMutex_OnCameraAccess) {
            if (mCamera != null) {
                Log.i(TAG, "close camera");
                mCamera.close();
                mCamera = null;
            }

            if(mVIARecorder != null && Preferences.getInstance().getFrameRecordData().isRecordEnable()) {
                Log.i(TAG, "stop recoder");
                mVIARecorder.stop();
                mVIARecorder = null;
            }
        }
        Log.i(TAG, "ok");

    }

    VIARecorder.FileListener mRecordFileListener = new VIARecorder.FileListener() {
        @Override
        public void OnFileComplete(String filePath) {
            mSingleFileSize = (new File(filePath)).length();
            if(!bRemoverWorking) {
                bRemoverWorking = true;
                new Thread(removerTask).start();
            }

//            if(mAutomotiveController != null) {
//                if(mAutomotiveController.isRecording()) mAutomotiveController.stopRecord();
//            }

            if(mServer != null) {
                String prefix = filePath.substring(filePath.lastIndexOf("/")+1);
                prefix = prefix.replace(".mp4", "-") ;
                if(prefix.length() > 4) {
                    if(mServer.isLogin() && mServer.isNeedUploadData()) {
                        try {
                            mServer.uploadFile(S3DataTypes.Video, filePath, null);
                            mServer.uploadFile(S3DataTypes.SensingConfig, Preferences.getInstance().getConfigurationData().getSensingCfgPath(), prefix);
                            mServer.uploadFile(S3DataTypes.AutomotiveConfig, Preferences.getInstance().getConfigurationData().getAutomotiveCfgPath(), prefix);

                            if (mCANbusModule != null && mCANbusModule.isRecording()) {
                                mServer.uploadFile(S3DataTypes.VehicleBus, mCANbusModule.getRecordPath(), null);
                            }
                        } catch (final Exception e) {
                            ((Activity)mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                }
            }

            if(mCANbusModule != null && mCANbusModule.isRecording()) {
                mCANbusModule.stopRecord();
            }
        }

        @Override
        public void OnFileCreate(String filePath) {
            String fName = filePath.substring(filePath.lastIndexOf("/") +1);
            String restorPath = filePath.substring(0, filePath.lastIndexOf("/"));

//            if(mAutomotiveController != null) {
//                if(mAutomotiveController.isRecording()) mAutomotiveController.stopRecord();
//                mAutomotiveController.startRecord(restorPath , fName + ".dana", true);
//            }

            if(mCANbusModule != null) {
                if(mCANbusModule.isRecording()) mCANbusModule.stopRecord();
                mCANbusModule.startRecord(restorPath, fName + ".bus", true);
            }
        }
    };

    boolean bRemoverWorking = false;

    long mSingleFileSize = 0;
    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    Object lock = new Object();
    Runnable removerTask = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                bRemoverWorking = true;
                File folder = new File(mRecordPath);
                Long folderSize = folderSize(folder);
                long usableSpace = folder.getUsableSpace();
                long totalSpace = folder.getTotalSpace();
                Log.d(TAG, "====== Remover Task ======");
                Log.d(TAG, "path  : " + mRecordPath);
                Log.d(TAG, "total : " + totalSpace);
                Log.d(TAG, "usable: " + usableSpace);
                Log.d(TAG, "folderSize: " + folderSize);
                // we need to remain usableSpace = 5 * Single File Size
                if (mSingleFileSize == 0) {
                    Log.d(TAG, "single file size = 0, something wrong!");
                    return;
                }
                File[] files = folder.listFiles();
                Arrays.sort(files, new Comparator() {
                    public int compare(Object o1, Object o2) {

                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return +1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });

                if(usableSpace<=REMAIN_SIZE && files.length!=0) {
                    for (File f : files) {
                        if (f.getName().contains(".mp4")) {
                            Log.d(TAG, "Remove File " + f.getName());
                            if (f.delete()) {
                                Log.d(TAG, "Remove File " + f.getName() + ": Success");

                                // try to delete relate csv file
                                File fcsv = new File(f.getAbsolutePath().replace(".mp4",".csv"));
                                if(fcsv.exists()) fcsv.delete();

                            } else {
                                Log.d(TAG, "Remove File " + f.getName() + ": Fail");
                            }


                            usableSpace = folder.getUsableSpace();
                            if (usableSpace>REMAIN_SIZE) {
                                Log.d(TAG, "===== Remover Task Done =====");
                                bRemoverWorking = false;
                                return;
                            }
                        }
                    }
                    usableSpace = folder.getUsableSpace();
                    if(usableSpace<=REMAIN_SIZE) {
                        Log.d(TAG,"The usable space is not enough!!!");
                    }
                }

                Log.d(TAG, "===== Remover Task Done =====");
                bRemoverWorking = false;
            }
        }
    };

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
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if(granted) {
            bIsAllPermissionGuaranteed = true;
        }

        return granted;
    }

    /*
        Callback functions of requesting permissions
     */
    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeedPermissions() {
        Log.d(TAG,"onNeedPermissions");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        Log.d(TAG,"onRequestPermissionsResult");
        for(int i=0;i<permissions.length;i++) {
            Log.d(TAG,permissions[i]+","+grantResults[i]);
            if(permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                // Application has mCamera permission then start mCamera
                //startCamera(mSurfaceTextures);
                //startCamera();
                bIsAllPermissionGuaranteed = true;
            }
        }
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onShowRationale(final PermissionRequest request) {
        request.proceed();
        Log.d(TAG,"onShowRationale");
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
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

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        Log.d(TAG,"onNeverAskAgain");

    }

}

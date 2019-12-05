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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.viatech.adas.webservices.WebService;
import com.viatech.camera.CameraTypes;
import com.viatech.utility.camera.VIACamera;
import com.viatech.via_ai.Media.CameraPermutation;
import com.viatech.via_ai.Media.EventSpeaker;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.System.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class StartMenuActivity extends Activity {

    // Tag
    private final String TAG = "StartMenuActivity";
    private final String TAG_mp4 = "mp4";
    private final String html_space4 = "&nbsp;&nbsp;&nbsp;&nbsp;";

    // Context
    private Activity mActivity = this;
    private Context mContext = this;
    private Helper mHelper;

    // UI
    private ImageButton mUI_ExitSystem;
    private ImageButton mUI_Settings;
    private ImageButton mUI_SystemStart;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startmenu);
        Preferences.getInstance().load(mContext);

        mHelper = new Helper();
        mHelper.setupAutoHideSystemUI(this);

//          // Keep code for debug.
//        int numberOfCamera = VIACamera.queryNumberOfCamera(this);
//        Log.d("HANK", "===== Number Of Camera : "+numberOfCamera+" =====");
//        for(int i=0; i<numberOfCamera;i++) {
//            Size[] sizes = VIACamera.querySupportResolution(this,i);
//            Log.d("HANK", "=== Number Of [ " + i + " ] Resolution ===");
//            if(null!=sizes) {
//                for(Size s: sizes) {
//                    Log.d("HANK", "== "+s.getWidth()+"x"+s.getHeight()+" ==");
//                }
//            }
//        }

        setupUI();

        if(!hasPermission()) {
            requestPermission();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Preferences.getInstance().load(mContext);

        if(Preferences.getInstance().getAudioData().isEnableTTS() || Preferences.getInstance().getAudioData().isEnableBeep()) {
            if (!EventSpeaker.getInstance().isInit()) {
                EventSpeaker.getInstance().init(mContext, new EventSpeaker.OnEventChangeListener() {
                    @Override
                    public void postStartActivityForResult(Intent intent, int requestCode) {
                        startActivityForResult(intent, requestCode);
                    }

                    @Override
                    public void postTTSInstallActionRequest(Intent intent) {
                        startActivity(intent);
                    }
                }, Preferences.getInstance().getAudioData().isEnableTTS(), Preferences.getInstance().getAudioData().isEnableBeep());
            }
        }

        // reset event status\
        if(!mUI_ExitSystem.isEnabled()) mUI_ExitSystem.setEnabled(true);
        if(!mUI_Settings.isEnabled()) mUI_Settings.setEnabled(true);
        if(!mUI_SystemStart.isEnabled()) mUI_SystemStart.setEnabled(true);

        // check configuration is valid or not.
        if(hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            checkConfig(false);
        }



        if(mServer == null) {
            Intent mIntent = new Intent(this, WebService.class);
            startService(mIntent);
            bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        }
        else {
            Toast.makeText(StartMenuActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
        }
    }


    private WebService mServer = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebService.LocalBinder mLocalBinder = (WebService.LocalBinder)service;
            mServer = mLocalBinder.getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServer = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }

    };

    @Override
    protected void onPause()
    {
        super.onPause();

        if(mServer != null) {
            unbindService(mConnection);
            mServer = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventSpeaker.getInstance().release();

//        if(Preferences.getInstance().getModuleToggle(Preferences.ModuleToggleTypes.ModuleToggle_PreferencesSaveRestart) == false) {
//            android.os.Process.killProcess(android.os.Process.myPid());
//        }
//        else {
//            Preferences.getInstance().setModuleToggle(Preferences.ModuleToggleTypes.ModuleToggle_PreferencesSaveRestart, false);
//        }
    }

    @Override
    public void onBackPressed() {
        // your code.
    }

    // request permission when first open
    private static final int PERMISSIONS_REQUEST_CODE = FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                  checkSelfPermission(PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_READ_STORAGE) ||
               shouldShowRequestPermissionRationale(PERMISSION_WRITE_STORAGE)) {
                Toast.makeText(this,"R/W storage permission are required for this application", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[] {PERMISSION_READ_STORAGE, PERMISSION_WRITE_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    //Add this method to show Dialog when the required permission has been granted to the app.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(StartMenuActivity.this,"Permission is Required for getting list of files",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /***
     @brief Check previoud camera setting valid or not?
     @param cameraId requested camera ID
     @param cameraResSize requested cameraSize
     @param notShowInfo disable alertDialog to show of information status,  (Error, Warning keep to show.)
     */
    private boolean checkCameraSetting(int cameraId, Size cameraResSize, boolean notShowInfo)
    {
        final String html_space4 = "&nbsp;&nbsp;&nbsp;&nbsp;";

        boolean isValid = false;
        boolean isCameraIndexValid = false;
        int numberOfCamera = VIACamera.queryNumberOfCamera(this);
        Size[] cameraSupportResolutionList = null;

        do {
            // Check camera index inside range of camera list.
            if(cameraId < 0 || cameraId >= numberOfCamera) break;

            try {
                cameraSupportResolutionList = VIACamera.querySupportResolution(this, cameraId);
                isCameraIndexValid = true;
            } catch (IllegalArgumentException e) {
                isCameraIndexValid = false;
            }

            if (isCameraIndexValid && cameraSupportResolutionList != null) {
                for (Size s : cameraSupportResolutionList) {
                    if (s.getWidth() == cameraResSize.getWidth() && s.getHeight() == cameraResSize.getHeight()) {
                        isValid = true;
                        break;
                    }
                }
            }
        } while(false);

        // post message
        if(!isValid && !notShowInfo) {
            Spanned dialogText;
            if(!isCameraIndexValid) {
                dialogText = Html.fromHtml("<b>Camera setting is unavailable. [ Camera Index out of Devices list. ] </b><br>"+
                                                html_space4 + "Request Camera ID [ " + cameraId + " ]<br>" +
                                                html_space4 + "Request resolution  [ " + cameraResSize.getWidth() + "x" + cameraResSize.getHeight() + " ]<br>" +
                                                "<br>" +
                                                html_space4 + "<b>Support Camera Device Count : [ " + numberOfCamera + " ]<b>");
            }
            else {
                String str = "<b>Camera setting is unavailable. [ Unsupported resolution in this Camera. ] </b><br>" +
                        html_space4 + "Request Camera ID [ " + cameraId + " ]<br>" +
                        html_space4 + "Request Resolution  [ " + cameraResSize.getWidth() + "x" + cameraResSize.getHeight() + " ]<br>" +
                        "<br>" +
                        html_space4 + "Support Resolutions  : <br>";
                if(cameraSupportResolutionList != null) {
                    for(Size s: cameraSupportResolutionList) {
                        str += html_space4 + html_space4 + " > " + s.getWidth() + "x" + s.getHeight() + "<br>";
                    }
                }
                else {
                    str += html_space4 + html_space4 + "No resolution supported in this devices";
                }

                dialogText = Html.fromHtml(str);
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Error);
            dialogBuilder.setTitle("Error");
            dialogBuilder.setMessage(dialogText);
            dialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            alertDialog.show();

            Helper helper = new Helper();
            helper.setupAutoHideSystemUI(alertDialog.getWindow());
        }

        return isValid;
    }

    private class ConfigTag {
        public String mPath;
        public String mTag;
        public String mResPath;
        public ConfigTag(String tag, String path, String defaultAssest) {
            mTag = tag;
            mPath = path;
            mResPath = defaultAssest;
        }

    };
    /***
        @brief Check the configuration is exist ? or writable ?
        @param notShowInfo disable alertDialog to show of information status,  (Error, Warning keep to show.)
     */
    private boolean checkConfig(boolean notShowInfo)
    {
        boolean isValid = true;
        List<ConfigTag> cList = new ArrayList<>();
        ArrayList<ConfigTag> restoreList = new ArrayList<>();
        ArrayList<ConfigTag> errorList = new ArrayList<>();
        cList.add(new ConfigTag("Sensing Config", Preferences.getInstance().getConfigurationData().getSensingCfgPath(), "sensingConfig.xml"));
        cList.add(new ConfigTag("Automotive Config", Preferences.getInstance().getConfigurationData().getAutomotiveCfgPath(), "automotiveConfig.xml"));
        if(Preferences.getInstance().getFrameSourceData().getCameraTypes(0) == CameraTypes.Custom) {
            cList.add(new ConfigTag("Camera Calibration Config", Preferences.getInstance().getFrameSourceData().getCameraCalibPath(0), null));
        }

        String htmlText = "";
        for(int li = 0 ; li < cList.size(); li++) {
            String configPath = cList.get(li).mPath;
            String defaultAsset = cList.get(li).mResPath;
            String newConfigs = null;

            if (!isFileExist(configPath)) {
                if(defaultAsset != null) {
                    newConfigs = getDefaultConfigs(defaultAsset);
                    restoreList.add(new ConfigTag(cList.get(li).mTag, configPath, newConfigs));
                }
                else {
                    errorList.add(new ConfigTag(cList.get(li).mTag, configPath, newConfigs));
                }
            }
            else if (!isConfigWritable(configPath)) {
                // restore to internal sotrage.
                String configName = configPath.substring(configPath.lastIndexOf("/") + 1);
                final String restorePath = mContext.getApplicationContext().getFilesDir().toString();
                if (restorePath.endsWith("/"))
                    newConfigs = restorePath + configName;
                else
                    newConfigs = restorePath + "/" + configName;

                try {
                    File srcF = new File(configPath);
                    File dstF = new File(newConfigs);
                    FileInputStream inStream = new FileInputStream(srcF);
                    FileOutputStream outStream = new FileOutputStream(dstF);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
                    restoreList.add(new ConfigTag(cList.get(li).mTag, configPath, newConfigs));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    errorList.add(new ConfigTag(cList.get(li).mTag, configPath, null));
                } catch (IOException e) {
                    e.printStackTrace();
                    errorList.add(new ConfigTag(cList.get(li).mTag, configPath, null));
                }
            }
            else {
                newConfigs = configPath;
            }

            if(newConfigs != null) {
                switch (li) {
                    case 0:
                        // TODO : Add configure format verification.
                        Preferences.getInstance().getConfigurationData().setSensingCfgPath(newConfigs);
                        Preferences.getInstance().save(this);
                        break;
                    case 1:
                        // TODO : Add configure format verification.
                        Preferences.getInstance().getConfigurationData().setAutomotiveCfgPath(newConfigs);
                        Preferences.getInstance().save(this);
                        break;
                    case 2:
                        // TODO : Add configure format verification.
                        Preferences.getInstance().getFrameSourceData().setCameraCalibPath(0, newConfigs);
                        Preferences.getInstance().save(this);
                        break;
                }
            }
            else {
                isValid = false;
            }
        }

        if(isValid) {
            // Show dialog(Information) or not
            if (!notShowInfo && restoreList.size() > 0) {
                String html = "<b>* Clean configuration :</b> <br>";
                for (int i = 0; i < restoreList.size(); i++) {
                    if(restoreList.get(i).mResPath == null) {
                        html += html_space4 + restoreList.get(i).mTag + " : No configuration found]<br> ";
                    }
                    else {
                        html += html_space4 + restoreList.get(i).mTag + " : resotre [" + restoreList.get(i).mPath + "] to [" + restoreList.get(i).mResPath + "]<br> ";
                    }
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Info);
                dialogBuilder.setTitle("Information");
                dialogBuilder.setMessage(Html.fromHtml(html));
                dialogBuilder.setPositiveButton("OK", null);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                alertDialog.show();
                Helper helper = new Helper();
                helper.setupAutoHideSystemUI(alertDialog.getWindow());
            }
        }
        else {
            // Show dialog(Information) or not
            if (!notShowInfo && errorList.size() > 0) {
                String html = "<b>* Error :</b> <br> Fail to restore or access configure files : <br>";
                for (int i = 0; i < errorList.size(); i++) {
                    html += html_space4 + errorList.get(i).mTag + "<br>";
                }

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Error);
                dialogBuilder.setTitle("Information");
                dialogBuilder.setMessage(Html.fromHtml(html));
                dialogBuilder.setPositiveButton("OK", null);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                alertDialog.show();
                Helper helper = new Helper();
                helper.setupAutoHideSystemUI(alertDialog.getWindow());
            }
        }

        return isValid;
    }

    private boolean checkCameraCalib()
    {
        Preferences.FrameSourceData frameData = Preferences.getInstance().getFrameSourceData();
        int camCount = frameData.getCameraCount();
        boolean isValid_ = false;

        for(int ci = 0 ; ci < camCount ; ci++) {
            boolean isValid = false;
            String errMsg ="";

            do {
                if (frameData.getCameraTypes(ci) == CameraTypes.Custom && !isFileExist(frameData.getCameraCalibPath(ci))) {
                    errMsg = "camera calibration file not exist.";
                    break;
                }
                if (frameData.getCameraTypes(ci) == CameraTypes.Custom && !isFileExist(frameData.getCameraCalibPath(ci))) {
                    errMsg = "the camera data in camera calibration file is different from ADAS configuration.";
                    break;
                }
                isValid = true;
            } while(false);

            isValid_ &= isValid;
        }

        return isValid_;
    }

    private String getDefaultConfigs(String assetDefaultFileName) {
        final String restorePath = mContext.getApplicationContext().getFilesDir().toString();
        String ret = null;
        AssetManager assetManager = mContext.getResources().getAssets();

        try {
            String fPath = restorePath + "/" + assetDefaultFileName;
            InputStream inputStream = assetManager.open(assetDefaultFileName);
            File file = new File(fPath);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length = 0;
            while((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            ret = fPath;
        } catch (Exception e) {
            Log.d(TAG, "ERROR: " + e.toString());
        }

        return ret;
    }

    private boolean isFileExist(String configPath)
    {
        boolean ret = false;


        if(configPath != null && configPath.length() > 0) {
            File file = new File(configPath);
            if (file.exists()) ret = true;
        }
        return ret;
    }

    private boolean isConfigWritable(String configPath)
    {
        boolean ret = false;

        if(configPath.length() > 0) {
            File file = new File(configPath);
            if (file.exists() && file.canWrite()) ret = true;
        }
        return ret;
    }

    private void setupUI()
    {
        mUI_ExitSystem = (ImageButton) findViewById(R.id.layoutActivityStartMenu_ImageButton_Exit);
        mUI_Settings = (ImageButton) findViewById(R.id.layoutActivityStartMenu_ImageButton_Settings);
        mUI_SystemStart = findViewById(R.id.layoutActivityStartMenu_ImageView_SystemStart);

        mUI_ExitSystem.setOnClickListener(mOnClickListener_Icons);
        mUI_Settings.setOnClickListener(mOnClickListener_Icons);
        mUI_SystemStart.setOnClickListener(mOnClickListener_Icons);
    }

    private void startAdasActivity(@Nullable final Bundle bundle)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                intent = new Intent(mActivity, MainActivity.class);

                if(bundle != null) intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    public void postToast(final Context context, final String text, final int duration) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    private Bundle checkVideoInformation(VIACamera.MODE cameraMode, String framePath, boolean notShowInfo)
    {
        Bundle bundle = new Bundle();
        Resources resources = getResources();

        switch(cameraMode) {
            case Camera:
            case Camera2:
            case Native:
                bundle.putSerializable(resources.getString(R.string.key_camera_permutation), CameraPermutation.Camera1_In_Frame);
                bundle.putInt(resources.getString(R.string.key_frame_width), -1);
                bundle.putInt(resources.getString(R.string.key_frame_height), -1);
                break;
            case FakeCameraGPU:
                {
                    MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
                    CameraPermutation permutation = Preferences.getInstance().getFrameSourceData().getCameraPermutation();
                    int prefFrameWidth = Preferences.getInstance().getFrameSourceData().getCameraSourceWidth();
                    int prefFrameHeight = Preferences.getInstance().getFrameSourceData().getCameraSourceHeight();

                    try {
                        retriever.setDataSource(framePath);
                        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        boolean isValid = false;

                        switch(permutation) {
                            case Camera1_In_Frame:
                                isValid = true;
                                break;
                            case Camera2_In_Frame_1x2:
                                if ((width == 2048 && height == 576) || (width == 2560 && height == 720)) {
                                    isValid = true;
                                }
                                break;
                            case Camera3_In_Frame_1x3:
                                if ((width == 3072 && height == 576) || (width == 3840 && height == 720)) {
                                    isValid = true;
                                }
                                break;
                            case Camera4_In_Frame_2x2:
                                if ((width == 2048 && height == 1152) || (width == 2560 && height == 1440)) {
                                    isValid = true;
                                }
                                break;
                            case Camera4_In_Frame_1x4:
                                if ((width == 5120 && height == 720) || (width == 4096 && height == 576)) {
                                    isValid = true;
                                }
                                break;
                        }

                        if(isValid) {
                            bundle.putSerializable(resources.getString(R.string.key_camera_permutation), permutation);
                            bundle.putInt(resources.getString(R.string.key_frame_width), width);
                            bundle.putInt(resources.getString(R.string.key_frame_height), height);
                        }
                        else {
                            bundle = null;

                            if(!notShowInfo) {
                                final String html_space4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
                                Spanned dialogText;
                                String str = "<b>Unsupported camera permutation in this video. </b><br>" +
                                        html_space4 + "Video Resolution  [ " + width + "x" + height + " ]<br>" + "<br>" +
                                        html_space4 + "Setting Required Permutation  : <br> [ " + permutation.toString() + " ]";
                                dialogText = Html.fromHtml(str);

                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle_Error);
                                dialogBuilder.setTitle("Error");
                                dialogBuilder.setMessage(dialogText);
                                dialogBuilder.setPositiveButton("OK", null);
                                AlertDialog alertDialog = dialogBuilder.create();
                                alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                                alertDialog.show();
                                Helper helper = new Helper();
                                helper.setupAutoHideSystemUI(alertDialog.getWindow());
                            }
                        }
                        retriever.release();
                    }
                    catch (IllegalArgumentException e) {
                        Log.e("checkCameraPermutation", "IllegalArgumentException " + e.getMessage());
                    }
                }
                break;
        }
        return bundle;
    }


    View.OnClickListener mOnClickListener_Icons = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            // rule for disable multiple click on button.
            String configFullPath = Preferences.getInstance().getConfigurationData().getSensingCfgPath();
            String configPath, configName;
            Bundle bundle;
            Resources resources = getResources();
            Preferences.FrameSourceData pFrameData = Preferences.getInstance().getFrameSourceData();

            // Check config
            boolean isConfigValid;
            boolean isFrameSupplierValid;

            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setEnabled(false);
                }
            });


            switch(view.getId()) {
                case R.id.layoutActivityStartMenu_ImageButton_Settings:
                    //mContext.startActivity(new Intent(mContext, SettingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    mContext.startActivity(new Intent(mContext, SettingActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
                case R.id.layoutActivityStartMenu_ImageButton_Exit:
                    finishAffinity();
                    break;
                case R.id.layoutActivityStartMenu_ImageView_SystemStart:
                    mUI_SystemStart.setEnabled(false);
                    isConfigValid = checkConfig(false);
                    if(Preferences.getInstance().getFrameSourceData().isUseCamera()) {
                        isFrameSupplierValid = checkCameraSetting(pFrameData.getCameraID(), new Size(pFrameData.getCameraSourceWidth(), pFrameData.getCameraSourceHeight()), false);
                    }
                    else {
                        isFrameSupplierValid = true;
                    }

                    if(isConfigValid && isFrameSupplierValid) {
                        boolean isValid = false;
                        String msg = "";

                        do {
                            configPath = configFullPath.substring(0, configFullPath.lastIndexOf("/"));
                            configName = configFullPath.substring(configFullPath.lastIndexOf("/"));
                            bundle = new Bundle();
                            if(Preferences.getInstance().getFrameSourceData().isUseCamera()) {
                                bundle.putSerializable(resources.getString(R.string.key_camera_mode), VIACamera.MODE.Camera);
                            }
                            else {
                                String vPath = Preferences.getInstance().getFrameSourceData().getVideoPath();
                                if(!isFileExist(vPath)) {
                                    msg = "Video not exist " + vPath;
                                    break;
                                }
                                String fileName = vPath.substring(vPath.lastIndexOf("/") + 1);
                                String filePath = vPath.substring(0, vPath.lastIndexOf("/"));
                                bundle.putSerializable(resources.getString(R.string.key_camera_mode), VIACamera.MODE.FakeCameraGPU);
                                bundle.putString(resources.getString(R.string.key_file_path), filePath);
                                bundle.putString(resources.getString(R.string.key_file_name), fileName);
                            }
                            bundle.putSerializable(resources.getString(R.string.key_camera_permutation), Preferences.getInstance().getFrameSourceData().getCameraPermutation());
                            bundle.putInt(resources.getString(R.string.key_frame_width), Preferences.getInstance().getFrameSourceData().getCameraSourceWidth());
                            bundle.putInt(resources.getString(R.string.key_frame_height), Preferences.getInstance().getFrameSourceData().getCameraSourceHeight());
                            bundle.putString(resources.getString(R.string.key_config_path), configPath);
                            bundle.putString(resources.getString(R.string.key_config_name), configName);
                            isValid = true;
                        } while(false);

                        if(isValid) {
                            startAdasActivity(bundle);
                        }
                        else {
                            postToast(mContext, msg, Toast.LENGTH_SHORT);
                            mUI_SystemStart.setEnabled(true);
                        }
                    }
                    else {
                        mUI_SystemStart.setEnabled(true);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        EventSpeaker.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @brief This class is used to handel all listener about FileDialog.
       */
    class FileDialogListeners implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener, DialogSelectionListener
    {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            // unlock click event
            //mEventStatus_FileDialogDimissed.toggle(true);
        }

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            // unlock click event
            //mEventStatus_FileDialogDimissed.toggle(true);
        }

        @Override
        public void onSelectedFilePaths(String[] files) {
            if(files != null && files.length > 0) {
                String fileFullPath =  files[0];

                if(fileFullPath.endsWith(TAG_mp4)) {
                    String fileName = fileFullPath.substring(fileFullPath.lastIndexOf("/") + 1);
                    String configFullPath = Preferences.getInstance().getConfigurationData().getSensingCfgPath();

                    // Check config, in this step, infoAlert doesn't need to show.
                    boolean isConfigValid = checkConfig(false);

                    if(isConfigValid) {
                        configFullPath = Preferences.getInstance().getConfigurationData().getSensingCfgPath();
                        String configPath = configFullPath.substring(0, configFullPath.lastIndexOf("/"));
                        String configName = configFullPath.substring(configFullPath.lastIndexOf("/"));

                        // Start activity
                        String filePath = fileFullPath.substring(0, fileFullPath.lastIndexOf("/"));
                        Bundle videoInfo = checkVideoInformation(VIACamera.MODE.FakeCameraGPU, fileFullPath, false);

                        if(videoInfo != null) {
                            Bundle bundle = new Bundle();
                            Resources resources = getResources();
                            bundle.putSerializable(resources.getString(R.string.key_camera_mode), VIACamera.MODE.FakeCameraGPU);
                            bundle.putSerializable(resources.getString(R.string.key_camera_permutation), videoInfo.getSerializable(getResources().getString(R.string.key_camera_permutation)));
                            bundle.putInt(resources.getString(R.string.key_camera_id), Preferences.getInstance().getFrameSourceData().getCameraID());
                            bundle.putInt(resources.getString(R.string.key_frame_width), videoInfo.getInt(getResources().getString(R.string.key_frame_width)));
                            bundle.putInt(resources.getString(R.string.key_frame_height), videoInfo.getInt(getResources().getString(R.string.key_frame_height)));
                            bundle.putString(resources.getString(R.string.key_file_path), filePath);
                            bundle.putString(resources.getString(R.string.key_file_name), fileName);
                            bundle.putString(resources.getString(R.string.key_config_path), configPath);
                            bundle.putString(resources.getString(R.string.key_config_name), configName);
                            startAdasActivity(bundle);
                        }

                    }
                }
                else {
                    postToast(mContext, "File format error, only accpet mp4 file.", Toast.LENGTH_LONG);
                }
            }
        }
    };


    /**
     @brief This class is used to specify binary event status,
    @note Such as button click event, if we want to disable multiple click of button, EventStatus is a simple way to handled this feature.
                1. In initialize, define one EventStatus and  set status,
                2. In runtime, check status before we do anything when button click, and toggle EventStatus when event changed.
     */
    private class EventStatus
    {
        private volatile boolean mStatus = false;

        public EventStatus(boolean status) {
            mStatus = status;
        }

        public synchronized boolean isValid() {
            return mStatus;
        }

        public synchronized void toggle(boolean status) {
            mStatus = status;
        }
    }

}

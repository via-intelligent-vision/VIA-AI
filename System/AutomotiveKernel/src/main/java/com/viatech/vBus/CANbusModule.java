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

package com.viatech.vBus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.viatech.car.CarTypes;

import java.util.HashMap;

public class CANbusModule {
    /**
     @brief  Method to check native library load or not
     */
    private final static String TAG = "CANbusModule";

    private static boolean mLibraryLoadStatus = false;

    static {
        try {
            System.loadLibrary("VIA-AI");
            mLibraryLoadStatus = true;
        }
        catch(java.lang.UnsatisfiedLinkError e) {
            Log.e(TAG, "Fail to load library ... " + e.getMessage());
            mLibraryLoadStatus = false;
        }
    }

    private static boolean isNativeLibraryLoaded() {
        return mLibraryLoadStatus;
    }

    private void checkNativeLibraryStatus() {
        if(!mLibraryLoadStatus) {
            throw new IllegalStateException("Native library load faild");
        }
    }

    /**
        @brief Refresh Task to update canbus data or retry connect.
        */
    private class RefreshTask implements Runnable {
        private int refTime;
        private volatile boolean isExit;
        private CANbusModule refCANbusModule;

        public RefreshTask(CANbusModule refCANbusModule, int refTime) {
            this.refTime = refTime;
            this.isExit = false;
            this.refCANbusModule = refCANbusModule;
        }

        public void release() {
            isExit = true;
        }

        @Override
        public void run() {
            while(!isExit) {
                // if connected, refresh data
                //Log.e(TAG, "this.refCANbusModule.isConnected() " + this.refCANbusModule.isConnected());
                if( this.refCANbusModule.isConnected()) {
                    //Log.e(TAG, "this.refCANbusModule.refreshData();");
                    this.refCANbusModule.refreshData();

                    try {
                        //Log.e(TAG, "RefreshTask Sleep ... " + refTime);
                        Thread.sleep(refTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // otherwise, retry connect with 100ms
                else {
                    switch(this.refCANbusModule.getCANDongleType()) {
                        case commaai_WhitePanda:
                            //Log.e(TAG, "this.refCANbusModule.retryUSBConnect ");
                            this.refCANbusModule.retryUSBConnect(48042, 56780); //0xbbaa, 0xddcc
                            break;
                        default:
                            break;
                    }

                    try {
                        //Log.e(TAG, "RefreshTask Sleep ... " + 1000);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     @brief Constructor of CANbusModule
     */
    private Context mContext;
    private CarTypes mCarType;
    private CANDongleTypes mDongleType;

    public CANbusModule(Context context)
    {
        mContext = context;
        mCarType = CarTypes.Unknown;
        mDongleType = CANDongleTypes.Unknown;
        mNatvieAddress = 0;
        mThread_RefreshTask = null;
        data_CANHealth = new CANbusData.CANHealth();
        data_Speed = new CANbusData.CANParams_Speed();
        data_SteeringSensor = new CANbusData.CANParams_SteeringSensor();
        data_SteeringControl = new CANbusData.CANParams_SteeringControl();
        data_LKASHud = new CANbusData.CANParams_LKASHud();
        data_ACCHud = new CANbusData.CANParams_ACCHud();
        data_SafetyFeature = new CANbusData.CANParams_SafetyFeature();
        data_DriverControllers = new CANbusData.CANParams_DriverControllers();
    }

    /**
        @brief  USB devices, (for native libusb)
       */
    private volatile boolean mIsWaitPermission = false;
    private UsbManager mUsbManager = null;
    private UsbDevice mGrantedUsbDevice = null;
    private UsbDeviceConnection mGrantedUsbConnection = null ;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mUsbPendingIntent = null;

    /**
     @brief Native address of this obj,  release() is need  at the release of this object,
     */
    private long mNatvieAddress = 0;

    private native long jni_getModuleNativeAddress(long addr);
    public long getModuleNativeAddress()
    {
        return jni_getModuleNativeAddress(mNatvieAddress);
    }

    /**
        @brief Refresh timer, used to update data with specify time step and retry connect.
      */
    private Thread mThread_RefreshTask = null;
    private RefreshTask mRefreshTask = null;

    private native long jni_init(int carType, int dongleType);
    public boolean init(CarTypes carType, CANDongleTypes dongleType) {
        checkNativeLibraryStatus();
        if(mNatvieAddress != 0) {
            Log.i(TAG, "Previous module detected, release first....");
            release();
        }

        mCarType = carType;
        mDongleType = dongleType;
        mNatvieAddress = jni_init(carType.getIndex(), dongleType.getIndex());
        return (mNatvieAddress != 0);
    }


    private final BroadcastReceiver mBrocastReceiver_USBPermission = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e(TAG, "onReceive !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.e(TAG, "ACTION_USB_PERMISSION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            mGrantedUsbDevice = usbDevice;
                            Log.e(TAG, "Get Permission  for device " + usbDevice +
                                    " , VID " + usbDevice.getVendorId() +
                                    " , PID " + usbDevice.getProductId() +
                                    " *****************************************");
                            try {
                                mGrantedUsbConnection = mUsbManager.openDevice(mGrantedUsbDevice);
                                if (mGrantedUsbConnection != null) {
                                    Log.e(TAG, "mUsbManager.openDevic   OK !!!!!!!!!!!!!!!!!!!");

                                    if(connect(mGrantedUsbConnection.getFileDescriptor()) == false) {
                                        Log.e(TAG, "try to connect USB devices fail");
                                    }
                                    else {
                                        Log.e(TAG, "try to connect USB devices success");
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "mUsbManager.openDevic Fail " + e.getMessage());
                            }
                        }
                    } else {
                        mGrantedUsbDevice = null;
                        Log.e(TAG, "Permission denied for device " + usbDevice +
                                " , VID " + usbDevice.getVendorId() +
                                " , PID " + usbDevice.getProductId()+
                                " *****************************************");
                    }
                    mIsWaitPermission = false;
                }
            }
        }
    };


    public boolean retryUSBConnect(int deviceVID, int devicePID)
    {
        boolean ret = false;
        Log.i(TAG, "retryUSBConnect deviceVID " + deviceVID + " , devicePID " + devicePID );

        // Check data first.
        if(mUsbManager == null) {
            mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        }
        if(mUsbPendingIntent == null) {
            mUsbPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            mContext.registerReceiver(mBrocastReceiver_USBPermission, filter);
        }

        // Try to find usb devices.
        if(mUsbManager != null) {
            // Find device to require permission
            if(mGrantedUsbConnection == null && mIsWaitPermission == false) {
                HashMap<String, UsbDevice> map = mUsbManager.getDeviceList();
                for (String key : map.keySet()) {
                    UsbDevice usbDevice = map.get(key);

                    Log.e(TAG, "USB devices VID " + usbDevice.getVendorId()  + " , PID " + usbDevice.getProductId());
                    if (usbDevice.getVendorId() == deviceVID && usbDevice.getProductId() == devicePID) {
                        Log.e(TAG, "Got Target devices,   mIsWaitPermission " + mIsWaitPermission);
                        if (mUsbManager.hasPermission(usbDevice)) {
                            Log.e(TAG, "has Permission , no requestPermission" );
                            mGrantedUsbDevice =  usbDevice;
                            mIsWaitPermission = false;

                            Log.e(TAG, "Try open Devices" );
                            try {
                                mGrantedUsbConnection = mUsbManager.openDevice(mGrantedUsbDevice);
                                if (mGrantedUsbConnection != null) {
                                    Log.e(TAG, "mUsbManager.openDevic   OK !!!!!!!!!!!!!!!!!!!");

                                    if(this.connect(mGrantedUsbConnection.getFileDescriptor()) == false) {
                                        Log.e(TAG, "try to connect USB devices fail");
                                    }
                                    else {
                                        Log.e(TAG, "try to connect USB devices success");
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "mUsbManager.openDevic Fail " + e.getMessage());
                            }
                        }
                        else {
                            Log.e(TAG, "no Permission , requestPermission" );
                            mUsbManager.requestPermission(usbDevice, mUsbPendingIntent);
                            mIsWaitPermission = true;
                        }
                        break;
                    }
                }
            }

            // if previous connect is lost.
//            if(mGrantedUsbConnection != null && this.isConnected() == false && mIsWaitPermission == false) {
//                Log.e(TAG, "Detect connection lost !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
//                mGrantedUsbConnection.close();
//                mGrantedUsbDevice = null;
//                mGrantedUsbConnection = null;
//            }



//            if(mGrantedUsbDevice != null) {
//                if(mGrantedUsbConnection != null) {
//                    // try to connect USB devices
//                    Log.e(TAG, "try to connect USB devices !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                    if(this.connect(mGrantedUsbConnection.getFileDescriptor()) == false) {
//                        mGrantedUsbConnection.close();
//                        Log.e(TAG, "try to connect USB devices fail");
//                    }
//                    else {
//
//                        Log.e(TAG, "try to connect USB devices success");
//                    }
//                    mIsWaitPermission = false;
//                }
//                else {
//                    Log.e(TAG, "mUsbManager.openDevice(mGrantedUsbDevice) fail");
//                }
//
//            }

//            if(mGrantedUsbDevice != null) {
//                do {
//                    if (mUsbManager.hasPermission(mGrantedUsbDevice)) {
//                        Log.e("LibUsb", "mGrantedUsbDevice usbManger.hasPermission");
//                        if(mGrantedUsbConnection != null) {
//                            // try to connect USB devices
//                            Log.e(TAG, "try to connect USB devices !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                            if(this.connect(mGrantedUsbConnection.getFileDescriptor()) == false) {
//                                mGrantedUsbConnection.close();
//                                Log.e(TAG, "try to connect USB devices fail");
//                            }
//                            else {
//
//                                Log.e(TAG, "try to connect USB devices success");
//                            }
//                            mIsWaitPermission = false;
//                        }
//                        else {
//                            Log.e(TAG, "mUsbManager.openDevice(mGrantedUsbDevice) fail");
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        Log.e("LibUsb", "mGrantedUsbDevice no Permission");
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } while(true);
//
//            }
        }

        return ret;
    }

    private native void jni_release(long addr);
    public void release() {
        Log.e(TAG, "release");
        if(mRefreshTask != null) {
            mRefreshTask.release();
            try {
                mThread_RefreshTask.join();
                mThread_RefreshTask = null;
                mRefreshTask = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        jni_release(mNatvieAddress);
        mNatvieAddress = 0;
        Log.e(TAG, "release finish");
    }

    private native void jni_exec(long addr);
    public void exec(int refms) {
        checkNativeLibraryStatus();

        //TODO: if module is executed, throw exception

        // create refreshData timer
        mRefreshTask = new RefreshTask(this, refms);
        mThread_RefreshTask = new Thread(mRefreshTask);
        mThread_RefreshTask.start();

        // Block after this call.
        jni_exec(mNatvieAddress);

    }

    private native boolean jni_isConnected(long addr);
    public boolean isConnected() {
        checkNativeLibraryStatus();

        return jni_isConnected(mNatvieAddress);
    }

    private native boolean jni_connect(long addr, int natvieDevFileDescriptor);
    public boolean connect(int natvieDevFileDescriptor) {
        checkNativeLibraryStatus();

        return jni_connect(mNatvieAddress,  natvieDevFileDescriptor);
    }

    /**
        Refresh data from JNI, this function is called by JNI.
    */
    native void jni_refresh(long addr);
    private void refreshData() {
        checkNativeLibraryStatus();
        jni_refresh(mNatvieAddress);
    }

    /**
     * Push data to JNI, this function will update specified data to JNI
        */
    public void pushData() {

    }



    boolean b_IsRecording = false;
    String recordPath = "";
    public boolean isRecording() {
        return b_IsRecording;
    }

    public String getRecordPath() {
        return recordPath;
    }

    private native void jni_stopRecord(long addr);
    public void stopRecord() {
        synchronized (this) {
            jni_stopRecord(mNatvieAddress);
            b_IsRecording = false;
        }
    }

    private native boolean jni_startRecord(long addr, String path, boolean appendFile);
    public void startRecord(String path, String name, boolean appendFile) {
        synchronized (this) {
            recordPath = path + "/" + name;
            b_IsRecording = jni_startRecord(mNatvieAddress, recordPath, appendFile);
        }
    }

    /**
        Access Module data
    */
    public CarTypes getCarType() {
        return mCarType;
    }

    public CANDongleTypes getCANDongleType() {
        return mDongleType;
    }

    /**
        CAN bus datas (readbale)
      */
    private CANbusData.CANHealth data_CANHealth;
    private CANbusData.CANParams_Speed data_Speed;
    private CANbusData.CANParams_SteeringSensor data_SteeringSensor;
    private CANbusData.CANParams_SteeringControl data_SteeringControl;
    private CANbusData.CANParams_LKASHud data_LKASHud;
    private CANbusData.CANParams_ACCHud data_ACCHud;
    private CANbusData.CANParams_SafetyFeature data_SafetyFeature;
    private CANbusData.CANParams_DriverControllers data_DriverControllers;


    private native void jni_setGPS(long addr, double latitude, double longitude);
    public void setGPS(double latitude, double longitude) {
        jni_setGPS(mNatvieAddress, latitude, longitude);
    }

    /**
      Manual CANbus data setting.
     */
    private native void jni_manualCANData_CANHealth(long addr, boolean controls_allowed, boolean dongleConnected);
    public void manualCANData_CANHealth(boolean controls_allowed, boolean dongleConnected) {
        jni_manualCANData_CANHealth(mNatvieAddress,  controls_allowed, dongleConnected);
    }

    private native void jni_manualCANdata_Speed(long addr, float roughSpeed, float engineSpeed, float wheelSpeed_FrontLeft, float wheelSpeed_FrontRight, float wheelSpeed_RearLeft, float wheelSpeed_RearRight);
    public void manualCANdata_Speed(float roughSpeed, float engineSpeed, float wheelSpeed_FrontLeft, float wheelSpeed_FrontRight, float wheelSpeed_RearLeft, float wheelSpeed_RearRight) {
        jni_manualCANdata_Speed(mNatvieAddress, roughSpeed, engineSpeed, wheelSpeed_FrontLeft, wheelSpeed_FrontRight, wheelSpeed_RearLeft, wheelSpeed_RearRight);
    }

    private native void jni_manualCANdata_SteeringSensor(long addr, float steerAngle, float steerAngleRate, byte steerStatus, boolean steerControlActive);
    public void manualCANdata_SteeringSensor(float steerAngle, float steerAngleRate, byte steerStatus, boolean steerControlActive) {
        jni_manualCANdata_SteeringSensor(mNatvieAddress, steerAngle, steerAngleRate, steerStatus, steerControlActive);
    }

    private native void jni_manualCANdata_DriverControllers(long addr, boolean leftBlinkerOn, boolean rightBlinkerOn, byte wiperStatus);
    public void manualCANdata_DriverControllers(boolean leftBlinkerOn, boolean rightBlinkerOn, byte wiperStatus) {
        jni_manualCANdata_DriverControllers(mNatvieAddress, leftBlinkerOn, rightBlinkerOn, wiperStatus);
    }

    /**
     @brief CAN bus configure functions, call by native. don't proguard function which stated setCANdata_
     */
    private void setCANData_CANHealth(boolean controls_allowed, boolean dongleConnected) {
        data_CANHealth.controls_allowed = controls_allowed;
        data_CANHealth.dongleConnected = dongleConnected;
    }

    private void setCANdata_Speed(float roughSpeed, float engineSpeed, float wheelSpeed_FrontLeft, float wheelSpeed_FrontRight, float wheelSpeed_RearLeft, float wheelSpeed_RearRight) {
        data_Speed.roughSpeed = roughSpeed;
        data_Speed.engineSpeed = engineSpeed;
        data_Speed.wheelSpeed_FrontLeft = wheelSpeed_FrontLeft;
        data_Speed.wheelSpeed_FrontRight = wheelSpeed_FrontRight;
        data_Speed.wheelSpeed_RearLeft = wheelSpeed_RearLeft;
        data_Speed.wheelSpeed_RearRight = wheelSpeed_RearRight;
    }

    private void setCANdata_SteeringSensor(float steerAngle, float steerAngleRate, byte steerStatus, boolean steerControlActive) {
        data_SteeringSensor.steerAngle = steerAngle;
        data_SteeringSensor.steerAngleRate = steerAngleRate;
        data_SteeringSensor.steerStatus = steerStatus;
        data_SteeringSensor.steerControlActive = steerControlActive;
    }

    private void setCANdata_SteeringControl(boolean steerTorqueRequest, long steerTorque) {
        data_SteeringControl.steerTorqueRequest = steerTorqueRequest;
        data_SteeringControl.steerTorque = steerTorque;
    }

    private void setCANdata_LKASHud(byte isLaneDetected, byte laneType, byte beep) {
        data_LKASHud.isLaneDetected = isLaneDetected;
        data_LKASHud.laneType = laneType;
        data_LKASHud.beep = beep;
    }

    private void setCANdata_ACCHud(boolean accOn, int cruiseSpeed) {
        data_ACCHud.accOn = accOn;
        data_ACCHud.cruiseSpeed = cruiseSpeed;
    }

    private void setCANdata_SafetyFeature(boolean isControlSystemReady, boolean enableACC, boolean enableLKS,
                                         boolean isBrakePressed, boolean isGasPressed) {
        data_SafetyFeature.isControlSystemReady = isControlSystemReady;
        data_SafetyFeature.isEnabled_ACC = enableACC;
        data_SafetyFeature.isEnabled_LKS = enableLKS;
        data_SafetyFeature.isBrakePressed = isBrakePressed;
        data_SafetyFeature.isGasPressed = isGasPressed;
    }

    private void setCANdata_DriverControllers(boolean leftBlinkerOn, boolean rightBlinkerOn, byte wiperStatus)
    {
        data_DriverControllers.leftBlinkerOn = leftBlinkerOn;
        data_DriverControllers.rightBlinkerOn = rightBlinkerOn;
        data_DriverControllers.wiperStatus = wiperStatus;
    }

    public CANbusData.CANHealth getCANHealth() {
        return data_CANHealth;
    }

    public CANbusData.CANParams_Speed getSpeed() {
        return data_Speed;
    }

    public CANbusData.CANParams_SteeringSensor getSteeringSensor() {
        return data_SteeringSensor;
    }

    public CANbusData.CANParams_SteeringControl getSteeringControl() {
        return data_SteeringControl;
    }

    public CANbusData.CANParams_LKASHud getLKASHud() {
        return data_LKASHud;
    }

    public CANbusData.CANParams_ACCHud getACCHud() {
        return data_ACCHud;
    }

    public CANbusData.CANParams_SafetyFeature getSafetyFeature() {
        return data_SafetyFeature;
    }

    public CANbusData.CANParams_DriverControllers getDriverControllers() {
        return data_DriverControllers;
    }

}

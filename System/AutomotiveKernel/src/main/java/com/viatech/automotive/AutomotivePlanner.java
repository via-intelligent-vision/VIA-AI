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

package com.viatech.automotive;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.viatech.camera.CameraID;
import com.viatech.camera.CameraLocationTypes;
import com.viatech.camera.CameraModule;
import com.viatech.camera.CameraTypes;
import com.viatech.car.CarTypes;
import com.viatech.exception.IllegalTaskAccessException;
import com.viatech.media.FrameFormat;
import com.viatech.resource.ResourceManager;
import com.viatech.resource.RuntimeLoadableDataTypes;
import com.viatech.sensing.DetectorTypes;
import com.viatech.sensing.SensingModule;
import com.viatech.sensing.SensingSamples;
import com.viatech.vBus.CANDongleTypes;
import com.viatech.vBus.CANbusModule;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * AutomotivePlanner is a integration planner to handle {@link SensingModule}, {@link CameraModule}, {@link CANbusModule}.
 * This class provides a task system to handle sensing process in ADAS, and save the time of programmer to handle thread process.
 * <p>
 * To program, follow these steps :
 *<ul>
 * <li>
 * <b>Create a {@link AutomotivePlanner} object, and configure camera, sensing task, and task relation.</b><p>
 * The following example create a {@link AutomotivePlanner} object, and configure a {@link CameraModule} specified by id 0, attaching 2 sensing task (task-0 for FCWS , task-1 for LDWS), and associate task-0 and task-1 by task requirement , see {@link #setTaskRelation setTaskRelation}.
 * <pre>
 * {@code
 *     AutomotivePlanner planner = new AutomotivePlanner(mContext);
 *     planner.setCamera(CameraID.Camera_0, CameraTypes.Sharp_Module_OV10640_Fov50, CameraLocationTypes.Front, mConfigFullPath);
 *     planner.setSensingTask(SensingTaskID.SensingTask_0, DetectorTypes.FCWS, CameraID.Camera_0, mSensingRoi_Front, mConfigFullPath);
 *     planner.setSensingTask(SensingTaskID.SensingTask_1, DetectorTypes.LDWS, CameraID.Camera_0, mSensingRoi_Front, mConfigFullPath);
 *     planner.setTaskRelation(SensingTaskID.SensingTask_0, SensingTaskID.SensingTask_1, DetectorTypes.LDWS);
 *     planner.setTaskRelation(SensingTaskID.SensingTask_1, SensingTaskID.SensingTask_0, DetectorTypes.FCWS);
 * }
 *</pre>
 *<p></><b>Note : Function {@link #setCamera setCamera}, {@link #setSensingTask setSensingTask}, {@link #setTaskRelation setTaskRelation} must be called before {@link #init()}.</b><p>
 *  </li>
 *
 * <li>
 * <b></>Init and active the planner.</b> <p>
 *  The following example init and  active the planner. In {@link #init()} planner will create and handle resource of {@link SensingModule}, but planner doesn't do any sensing or decision until {@link #active()} called.
 * One situation is programmer could call {@link #init()} in {@link android.app.Activity#onCreate(Bundle)}, and call {@link #active()} in {@link Activity#onResume()}.
 * <pre>
 * {@code
 *     planner.init();
 *     planner.active(Thread.MAX_PRIORITY);}
 *  </pre>
 * The process time of {@link #init()} is based on the count of task and the detectors attached on task. To avoid android main thread freeze and cause ANR(application not response). Use {@link android.os.AsyncTask AsyncTask} is recommended if {@link #init()} spend time more than expected.
 * <p>
 * </li>
 *
 *<li>
 * <b>Buffer image frame to planner.</b><p>
 * AutomotivePlanner support multiple interface named as {@link #bufferFrame bufferFrame} to buffer image frame into internal sensing modules for sensing.
 * In each buffer operation, task-id is necessary to specify the receiver task, and the ROI (region of interesting) is also necessary to resample input frame as sub frame if need. <br>
 * Take {@link #bufferFrame(SensingTaskID, Image, int, int, int, int)} for example: <br>
 *  <ol>
 * <li>Specify {@link SensingTaskID} to buffer frame</li>
 * <li>Store image data into {@link android.media.Image android.media.Image} object, moreover, these interface supply different frame source : {@link #bufferFrame(SensingTaskID, Image, int, int, int, int) bufferFrame}, {@link #bufferFrame(SensingTaskID, FrameFormat, long, int, int, int, int, int, int) bufferFrame}, {@link #bufferFrame(SensingTaskID, ByteBuffer, ByteBuffer, ByteBuffer, int, int, int, int, int, int, int, int, int, int, int, int) bufferFrame} </li>
 * <li>Set the ROI in  input frame, for example, an input image (2560x720) is a horizontal concatenated image by two image(1280x720). The left image with ROI(x=0, y=0, width=1280, height=720) is buffer into task-0,  and the right image with ROI(x=1280, y=0, width=1280, height=720) is buffer into task-1. </li>
 *</ol>
 * <pre>
 * {@code
 *    planner.bufferFrame(SensingTaskID.SensingTask_0, image, 0, 0, 1280, 720);
 *    planner.bufferFrame(SensingTaskID.SensingTask_1, image, 1280, 0, 1280, 720);
 * } </pre>
 *</li>
 *
 *<li>
 * <b>Buffer CANbus data to planner.</b><p>
 * CANBus module is set as passive mode in AutomotivePlanner, programmer must handle canbus data by them self,
 * and buffer CANbus data via interface {@link #manualCAN_DriverControllers(boolean, boolean, byte) manualCAN_DriverControllers},
 * {@link #manualCAN_Speed(float) manualCAN_Speed}, {@link #manualCAN_SteeringSensor(float) manualCAN_SteeringSensor}.<br>
 * The following example set car speed as 70km/h, and turn left blinker on, and set wiper status to 0 (not enable).
 *  <pre>
 * {@code
 *    planner.manualCAN_Speed(70);
 *    planner.manualCAN_DriverControllers(true,  false, (byte)0);
 * } </pre>
 *</li>
 *
  *<li>
 * <b>Get result of planner.</b><p>
 * To get the detection result of SesingPlanner, {@link #getSensingSample(SensingTaskID, SensingSamples.SampleTypes) getSensingSample} supply a interface to get detection result in each task.
 * Programmer specify task id and the result type of detector, an object {@link com.via.adas.sensing.SensingSamples.SensingSample} will return. The {@link com.via.adas.sensing.SensingSamples.SensingSample} is a base class of all sensing samples,
 *  and it could be casted as another sample in {@link SensingSamples}, such as {@link com.via.adas.sensing.SensingSamples.LaneDetectSample}, {@link com.via.adas.sensing.SensingSamples.VehicleDetectSample} ... <br>
 * The following example get lane detector sample from task 1, and get forward vehicle detector sample from task 1.
 *  <pre>
 * {@code
 *    SensingSamples.LaneDetectSample laneSample = (SensingSamples.LaneDetectSample)mAutomotivePlanner.getSensingSample(SensingTaskID.SensingTask_1, SensingSamples.SampleTypes.LaneDetectSample);
 *    SensingSamples.VehicleDetectSample vehicleSample = (SensingSamples.VehicleDetectSample)mAutomotivePlanner.getSensingSample(SensingTaskID.SensingTask_0, SensingSamples.SampleTypes.VehicleDetectSample);
 * } </pre>
 * Note : {@link com.via.adas.ui.SensingRenderingView} supply a view object to rendering detector result.<br>
 *</li>
 *
 *<li>
 * <b>Release planner.</b><p>
 * {@link AutomotivePlanner} will allocate native resource after {@link #init()}, to release these resource, {@link #release()} is a necessary operation in the ned of program.
 *</li>
 *
 *</ul><p>
 */

public class AutomotivePlanner {
    private String TAG = AutomotivePlanner.class.getName();

    private final int SENSING_TASK_COUNT = 5;
    private final int CAMERA_COUNT = 4;

    private class SensingUnit {
        public SensingTask task;
        public SensingModule module;
        public EnumSet<DetectorTypes> detectorSet;
        public CameraID cameraID;
        public Rect frameROI;
        public String configPath;

        public SensingUnit() {
            release();
        }

        public void release() {
            if(task != null) task.release();
            task = null;
            module = null;
            detectorSet = null;
            frameROI = null;
            cameraID = CameraID.Camera_0;
            configPath = "";
        }
    }

    private class CameraUnit {
        public CameraModule module;
        public CameraTypes type;
        public CameraLocationTypes location;
        public String intrinsicPath;
        public String extrinsicPath;

        public CameraUnit() {
            reset();
        }

        public void reset() {
            module = null;
            type = CameraTypes.Unknown;
            location = CameraLocationTypes.Unknown;
            intrinsicPath = "";
            extrinsicPath = "";
        }
    }

    private class TaskRelationUnit {
        public SensingTaskID srcTaskID;
        public SensingTaskID relatedTaskID;
        public DetectorTypes relatedDetector;

        TaskRelationUnit() {
            srcTaskID = null;
            relatedTaskID = null;
            relatedDetector = null;
        }
    }

    private Context mContext;
    private CANbusModule mCANbusModule;
    private ArrayList<SensingUnit> mSensingUnitList;
    private ArrayList<CameraUnit> mCameraUnitList;
    private ArrayList<TaskRelationUnit> mTaskRelationUnitList;

    /**
     *  Create an new AutomotivePlanner.
     *
     *  @param context A Context of the application package implementing this class.
     */
    public AutomotivePlanner(@NonNull Context context) {
        mContext = context;
        mSensingUnitList = new ArrayList<>();
        mCameraUnitList = new ArrayList<>();
        mTaskRelationUnitList = new ArrayList<>();
        mCANbusModule = null;

        for(int mi = 0; mi < SENSING_TASK_COUNT; mi++) {
            mSensingUnitList.add(new SensingUnit());
        }

        for(int mi = 0; mi < CAMERA_COUNT; mi++) {
            mCameraUnitList.add(new CameraUnit());
        }
    }

    /**
     *  Init AutomotivePlanner and create camera module, sensing modules. In this step, some native resource will create and must be release by <b>{@link #release()}. </b><br>
     *  Once {@link #init()}  called, setting functions ({@link #setCamera setCamera}, {@link #setSensingTask setSensingTask} , {@link #setTaskRelation setTaskRelation}) will throw {@link IllegalTaskAccessException} to discard operation,
     *  therefore, be sure to all setting functions is configured before {@link #init()}.
     */
    public void init() {

        initCameraModules();

        // TODO : Set unknown canbus module now , for manual setting.
        initCANbusModule(CarTypes.Unknown, CANDongleTypes.UserManual);

        initSensingModules();
        initSensingTasks();
        initTaskRelationUnitList();
    }

    /**
     *  Active all sensing task & CANBus module attached in this planner.
     *
     *  @param priority Priority setting, defined as Thread.MAX_PRIORITY,  Thread.MIN_PRIORITY, Thread.NORM_PRIORITY
        */
    public void active(int priority) {
        startCANbusModule();

        for(int si = 0; si < mSensingUnitList.size() ; si++) {
            startSensingModules(mSensingUnitList.get(si), priority);
        }
    }

    /**
     *  Active specified sensing task
     */
    private boolean active(@NonNull SensingTaskID taskID, int newPriority) {
        return startSensingModules(mSensingUnitList.get(taskID.ordinal()), newPriority);
    }

    /**
     *  Return the active status of specified task,
     *  @param taskID request task id.
     *  @return true in active, otherwise the false will return.
     */
    public boolean isTaskActive(@NonNull SensingTaskID taskID) {
        boolean ret = false;
        if(mSensingUnitList != null) {
            SensingUnit unit = mSensingUnitList.get(taskID.ordinal());
            if(unit != null) ret = unit.task.isActive();
        }

        return ret;
    }

    /**
     *  Stop and release the planner, and release all native resource allocated in this object. <b>Be sure to call this function in the release stage of program</b>
      */
    public void release() {
        releaseSensingModules();

        releaseCANbusModule();

        releaseCameraModules();

        Log.i(TAG, "release finish");
    }

    // ---------------------------------------------------------------------------------------------
    // about SensingModule / SensingTask
    // ---------------------------------------------------------------------------------------------
    /** Attach a camera to planner. Some detector depend on  camera configuration to promote accuracy of detection, such as LDW. FCW.
     * Therefore, if system use camera module listed on {@link CameraTypes}, it's recommend to set camera to planner.
     * @param dstCamera specify one {@link CameraID} to task this operation, if one camera id is set multiple time, it will keep last configuration.
     * @param type camera vendor type listed on {@link CameraTypes}.
     * @param  location  installation position on vehicle, and listed on {@link CameraLocationTypes}.
     *  @param configPath configuration path in file storage.
     */
    public void setCamera(@NonNull CameraID dstCamera, @NonNull CameraTypes type, @NonNull CameraLocationTypes location,
                          @NonNull String intrinsicPath, @NonNull String extrinsicPath )
    {
        CameraUnit unit = mCameraUnitList.get(dstCamera.ordinal());
        unit.type = type;
        unit.location = location;
        unit.intrinsicPath = intrinsicPath;
        unit.extrinsicPath = extrinsicPath;
    }

    private void initCameraModules()
    {
        for(int i = 0; i < mCameraUnitList.size(); i++) {
            CameraUnit unit = mCameraUnitList.get(i);
            if(unit.type != null) {
                unit.module = new CameraModule(unit.type, unit.location, unit.intrinsicPath, unit.extrinsicPath);
            }
        }
    }

    private void releaseCameraModules()
    {
        for(int i = 0; i < mCameraUnitList.size(); i++) {
            CameraUnit unit = mCameraUnitList.get(i);
            if(unit.module != null) {
                Log.i(TAG, "releaseCameraModules ... " +  i);
                unit.module.release();
            }
        }

        for(int i = 0; i < mCameraUnitList.size(); i++) {
            CameraUnit unit = mCameraUnitList.get(i);
            unit.reset();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // about  CANbus module
    // ---------------------------------------------------------------------------------------------
    /**   Init This step will init canbus modules)
    TODO : This class is incompatible with libVehicleMonitiring
     */
    private void initCANbusModule(CarTypes carType, CANDongleTypes dongleType) {
        if(mCANbusModule == null) {
            mCANbusModule = new CANbusModule(mContext);
            if (mCANbusModule.init(carType, dongleType) == true) {
                Log.i(TAG, "mCANbusModule.init finish");
            }
            else {
                Log.e(TAG, "mCANbusModule.init fail");
            }
        }
    }

    private void startCANbusModule() {
//        if(mCANbusModule != null) {
//            mThread_CANbusModule = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    //Note:  call exec() will block the thread
//                    mCANbusModule.exec(33);
//                }
//            });
//            mThread_CANbusModule.setPriority(Thread.MAX_PRIORITY);  // NOTE : CANbus module need MAX_PRIORITY to keep frequency.
//            mThread_CANbusModule.start();
//        }
    }

    private void releaseCANbusModule() {
        Log.i(TAG, "releaseCANbusModule");
        if(mCANbusModule != null) {
            mCANbusModule.release();
            mCANbusModule = null;
        }
    }

    /**
     * Manual CANBus configuration for speed information.
     * @param roughSpeed speed of vehicle, unit : km/h
     */
    public void manualCAN_Speed(float roughSpeed) {
        if(mCANbusModule != null) {
            mCANbusModule.manualCANdata_Speed(roughSpeed, roughSpeed, roughSpeed, roughSpeed, roughSpeed, roughSpeed);
        }
    }

    /**
     * Manual CANBus configuration for steer angngle
     * @param steerAngle steer angle, unit : deg.
     */
    public void manualCAN_SteeringSensor(float steerAngle) {
        if(mCANbusModule != null) {
            mCANbusModule.manualCANdata_SteeringSensor(steerAngle, 0.0f, (byte) 0, false);
        }
    }

    /**
     * Manual CANBus configuration for driver controller unit. such as blinker, wiper,
     * @param leftBlinkerOn true if blinker on, or false for blinker off.
     * @param rightBlinkerOn true if blinker on, or false for blinker off.
     * @param wiperStatus wiper status, 0 : wiper off, 1 : wiper-on-LowSpeed, 2 : wiper-on-FastSpeed.
     */
    public void manualCAN_DriverControllers(boolean leftBlinkerOn, boolean rightBlinkerOn, byte wiperStatus) {
        if(mCANbusModule != null) {
            mCANbusModule.manualCANdata_DriverControllers(leftBlinkerOn, rightBlinkerOn, wiperStatus);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // about SensingModule / SensingTask
    // ---------------------------------------------------------------------------------------------
    /**
     *  Attach sensing task into specified task ID. there 2 rule for this function: <p>
     *      1. Call setSensingTask() before {@link #init()} or it will throw exception {@link com.via.adas.Exception.IllegalTaskAccessException} <p>
     *      2. Only last task will attach in same task ID. <br>
     * @param missionID specify one {@link SensingTaskID} to task this operation, if one task id is set multiple time, it will keep last configuration.
     * @param detectorSet sensing detectors attached in this task. it could be a set of {@link DetectorTypes}.
     * @param referenceCamera set the reference camera id, the camera if set by {@link #setCamera setCamera)}.
     * @param roi set the detection roi (region of interesting) on frame supplied from <code>referenceCamera</code>.
     *           e.x. camera id-0 buffer an image (2560x720) which is a horizontal concatenated image by two image(1280x720).
     *            The left image with ROI(x=0, y=0, width=1280, height=720) ,  and the right image with ROI(x=1280, y=0, width=1280, height=720) ,
     * @param configPath is the configuration path in file storage. this path couldn't be NULL.
     * <br>
     * @exception IllegalTaskAccessException will return if {@link #init()} is called before this function.
     */
    public void setSensingTask(@NonNull SensingTaskID missionID, @NonNull EnumSet<DetectorTypes> detectorSet, CameraID referenceCamera, Rect roi, String configPath)
    {
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(unit.task != null) {
            throw new IllegalTaskAccessException("Specified Task " + missionID.toString() + " is active or in used.");
        }
        else {
            unit.detectorSet = detectorSet;
            unit.cameraID = referenceCamera;
            unit.frameROI = roi;
            unit.configPath = configPath;
        }
    }

    /**
     *  Attach sensing task into specified task ID. there 2 rule for this function: <p>
     *      1. Call setSensingTask() before {@link #init()} or it will throw exception {@link com.via.adas.Exception.IllegalTaskAccessException} <p>
     *      2. Only last task will attach in same task ID. <br>
     * @param missionID specify one {@link SensingTaskID} to task this operation, if one task id is set multiple time, it will keep last configuration.
     * @param detector sensing detector attached in this task. multiple detector support in {@link #setSensingTask(SensingTaskID, EnumSet, CameraID, Rect, String) setSensingTask}.
     * @param referenceCamera set the reference camera id, the camera if set by {@link #setCamera setCamera)}.
     * @param roi set the detection roi (region of interesting) on frame supplied from <code>referenceCamera</code>.
     *           e.x. camera id-0 buffer an image (2560x720) which is a horizontal concatenated image by two image(1280x720).
     *            The left image with ROI(x=0, y=0, width=1280, height=720) ,  and the right image with ROI(x=1280, y=0, width=1280, height=720) ,
     * @param configPath is the configuration path in file storage. this path couldn't be NULL.
     * <br>
     * @exception IllegalTaskAccessException will return if {@link #init()} is called before this function.
     */
    public void setSensingTask(@NonNull SensingTaskID missionID, @NonNull DetectorTypes detector, CameraID referenceCamera, Rect roi, String configPath)
    {
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(unit.task != null) {
            throw new IllegalTaskAccessException("Specified Task " + missionID.toString() + " is active or in used.");
        }
        else {
            unit.detectorSet = EnumSet.of(detector);
            unit.cameraID = referenceCamera;
            unit.frameROI = roi;
            unit.configPath = configPath;
        }
    }

    /**
     *  Some sensing detectors have relation dependence on each other to increase sensing accuracy.
     *  For LDW/FCW as example, LDW need the result of FCW to filter object in image, FCW need the result of LDW to identify forward object on the lane.
     * With these relation, system could apply precise detection algorithm on detector.<br>
     * These relations don't mandatory rule, without these setting, system will use default value to do sensing task. Only last task will attach in same source task ID.<br>
     * The following list shows the rule of detector dependence: <br><br>
     * <ul>
     * <li>LDWS  depend on FCW</li>
     * <li>FCW  depend on  LDW</li>
     * <li>BSD  no dependence</li>
     * <li>SLD  no dependence</li>
     * </ul><br>
     * The following example show the relation setting of task-0 (FCW) and task-1 (FCW).
     * <pre>
     * {@code
     *     planner.setTaskRelation(SensingTaskID.SensingTask_0, SensingTaskID.SensingTask_1, DetectorTypes.LDWS); // task-0 (FCW) depend on task-1 (LDW)
     *     planner.setTaskRelation(SensingTaskID.SensingTask_1, SensingTaskID.SensingTask_0, DetectorTypes.FCWS); // task-1 (LDW) depend on task-0 (FCW)
     * }
     *</pre>
     * @param srcTaskID is the source task which is to require related task id.
     * @param relatedTaskID is the related task id.
     *  @param relatedDetector denote as the require detector of source task..
     *
     *  @exception IllegalTaskAccessException will return if {@link #init()} is called before this function.
     */
    public void setTaskRelation(@NonNull SensingTaskID srcTaskID, @NonNull SensingTaskID relatedTaskID, @NonNull DetectorTypes relatedDetector)
    {
        SensingUnit srcUnit = mSensingUnitList.get(srcTaskID.ordinal());
        SensingUnit relUnit = mSensingUnitList.get(relatedTaskID.ordinal());
        if(srcUnit.task != null) {
            throw new IllegalTaskAccessException("Specified Task " + srcTaskID.toString() + " is active or in used.");
        }
        else if(relUnit.task != null) {
            throw new IllegalTaskAccessException("Specified Task " + relatedTaskID.toString() + " is active or in used.");
        }
        else {
            TaskRelationUnit unit = new TaskRelationUnit();
            unit.srcTaskID = srcTaskID;
            unit.relatedTaskID = relatedTaskID;
            unit.relatedDetector = relatedDetector;
            mTaskRelationUnitList.add(unit);
        }
    }

    /**
     *  Buffer image frame to task.
     * @param missionID buffer target task.
     * @param frame is the object of {@link android.media.Image}.
     * @param roiX the x of roi.
     * @param roiY the y of roi.
     * @param roiWidth the width of roi.
     * @param roiHeight the height of roi.
     * @return true will return if operation success, otherwise, false will return. if the task is inactive, this operation will return false.
     * @exception IllegalArgumentException will throw if the channel of frame not 3.
     */
    public boolean bufferFrame(@NonNull SensingTaskID missionID, @NonNull Image frame, int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;

        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(frame != null && unit != null && unit.module != null && unit.task != null) {
            if(frame.getPlanes().length == 3) { // YUV or RGB
                Image.Plane yPlane = frame.getPlanes()[0];
                Image.Plane uPlane = frame.getPlanes()[1];
                Image.Plane vPlane = frame.getPlanes()[2];

                // TODO : check frame format is support or not.
                boolean isFormatValid = true;

                // buffer frane
                if(isFormatValid) {
                    ret = unit.module.bufferFrame(yPlane.getBuffer(), uPlane.getBuffer(), vPlane.getBuffer(),
                            frame.getWidth(), frame.getHeight(),
                            yPlane.getRowStride(), uPlane.getRowStride(), vPlane.getRowStride(),
                            yPlane.getPixelStride(), uPlane.getPixelStride(), vPlane.getPixelStride(),
                            roiX, roiY, roiWidth, roiHeight);
                    unit.task.notifyFrameReady();
                }
            }
            else {
                throw new IllegalArgumentException("The channel of source frame must be 3 , current is " + frame.getPlanes().length);
            }
        }
        return ret;
    }


    /**
     *  Buffer image frame to task.
     * @param missionID buffer target task.
     * @param frame is the object of {@link android.graphics.Bitmap}.
     * @param roiX the x of roi.
     * @param roiY the y of roi.
     * @param roiWidth the width of roi.
     * @param roiHeight the height of roi.
     * @return true will return if operation success, otherwise, false will return. if the task is inactive, this operation will return false.
     * @exception IllegalArgumentException will throw if the channel of frame not 3.
     */
    public boolean bufferFrame(@NonNull SensingTaskID missionID, @NonNull Bitmap frame, int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;

        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(frame != null && unit != null && unit.module != null && unit.task != null) {
            // buffer frane
            ret = unit.module.bufferFrame(frame, roiX, roiY, roiWidth, roiHeight);
            unit.task.notifyFrameReady();
        }
        return ret;
    }

    /**
     * Buffer image frame to task. <br>
     * Support formae list : <br>
     * <ul>
     * <li>{@link FrameFormat#NV12} : O</li>
     * <li>{@link FrameFormat#NV21} : X</li>
     * <li>{@link FrameFormat#BGR888} : X</li>
     * <li>{@link FrameFormat#RGB888} : X</li>
     * <li>{@link FrameFormat#ARGB8888} : O</li>
     * <li>{@link FrameFormat#Unknown} : X</li>
     * </ul><br>
     * @param missionID  buffer target task.
     * @param fmt frame format list on {@link FrameFormat} , see above support list in this function, unsupported format will throw exception {@link IllegalArgumentException}.
     * @param dataBuffer direct byte address of frame buffer ,
     * @param frameWidth is frame height.
     * @param frameHeight is frame height.
     * @param roiX the x of roi.
     * @param roiY the y of roi.
     * @param roiWidth the width of roi.
     * @param roiHeight the height of roi.
     * @return true will return if operation success, otherwise, false will return. if the task is inactive, this operation will return false.
     */
    public boolean bufferFrame(@NonNull SensingTaskID missionID, @NonNull FrameFormat fmt, @NonNull ByteBuffer dataBuffer, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());

        if(unit != null && unit.module != null && unit.task != null) {
            ret = unit.module.bufferFrame(dataBuffer, fmt, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
            if(ret == true) {
                unit.task.notifyFrameReady();
            }
        }

        return ret;
    }


    /**
     * Buffer image frame to task. <br>
     * Support formae list : <br>
     * <ul>
     * <li>{@link FrameFormat#NV12} : O</li>
     * <li>{@link FrameFormat#NV21} : X</li>
     * <li>{@link FrameFormat#BGR888} : X</li>
     * <li>{@link FrameFormat#RGB888} : X</li>
     * <li>{@link FrameFormat#ARGB8888} : O</li>
     * <li>{@link FrameFormat#Unknown} : X</li>
     * </ul><br>
     * @param missionID  buffer target task.
     * @param fmt frame format list on {@link FrameFormat} , see above support list in this function, unsupported format will throw exception {@link IllegalArgumentException}.
     * @param bufferNativeAddress native address of frame buffer ,
     * @param frameWidth is frame height.
     * @param frameHeight is frame height.
     * @param roiX the x of roi.
     * @param roiY the y of roi.
     * @param roiWidth the width of roi.
     * @param roiHeight the height of roi.
     * @return true will return if operation success, otherwise, false will return. if the task is inactive, this operation will return false.
     */
    public boolean bufferFrame(@NonNull SensingTaskID missionID, @NonNull FrameFormat fmt, @NonNull long bufferNativeAddress, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());

        if(unit != null && unit.module != null && unit.task != null) {
            ret = unit.module.bufferFrame(bufferNativeAddress, fmt, frameWidth, frameHeight, roiX, roiY, roiWidth, roiHeight);
            if(ret == true) {
                unit.task.notifyFrameReady();
            }
        }

        return ret;
    }


    /**
     * Buffer image frame to task. <br>
     *  This interface is deprecated. use {@link #bufferFrame(SensingTaskID, Image, int, int, int, int)} , {@link #bufferFrame(SensingTaskID, FrameFormat, long, int, int, int, int, int, int)} replaced.
     * @param missionID  buffer target task.
     * @param yPlaneBuffer direct byte buffer object of Y plane.
     * @param uPlaneBuffer direct byte buffer object of U plane.
     * @param vPlaneBuffer direct byte buffer object of V plane.
     * @param frameWidth is frame width.
     * @param frameHeight is frame height.
     * @param yStepStride the step stride of Y plane
     * @param uStepStride the step stride of U plane
     * @param vStepStride the step stride of V plane
     * @param yPixelStride the pixel stride of Y plane
     * @param uPixelStride the pixel stride of  U plane
     * @param vPixelStride the pixel stride of V plane
     * @param roiX the x of roi.
     * @param roiY the y of roi.
     * @param roiWidth the width of roi.
     * @param roiHeight the height of roi.
     * @return true will return if operation success, otherwise, false will return. if the task is inactive, this operation will return false.
     */
    @Deprecated
    public boolean bufferFrame(@NonNull SensingTaskID missionID,
                               @NonNull ByteBuffer yPlaneBuffer, @NonNull ByteBuffer uPlaneBuffer, @NonNull ByteBuffer vPlaneBuffer,
                               int frameWidth, int frameHeight,
                               int yStepStride, int uStepStride, int vStepStride,
                               int yPixelStride, int uPixelStride, int vPixelStride,
                               int roiX, int roiY, int roiWidth, int roiHeight) {
        boolean ret = false;
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(unit != null && unit.module != null && unit.task != null) {
            ret = unit.module.bufferFrame(yPlaneBuffer, uPlaneBuffer, vPlaneBuffer,
                    frameWidth, frameHeight,
                    yStepStride, uStepStride, vStepStride,
                    yPixelStride, uPixelStride, vPixelStride,
                    roiX, roiY, roiWidth, roiHeight);
            unit.task.notifyFrameReady();
        }
        return ret;
    }

    /**
     * Get the detector sample from task, this function will return last sample of task.
     * Programmer need <b>cast the return object to correct sample type with type parameter</b>. <br>e.x. the object required by {@link com.via.adas.sensing.SensingSamples.SampleTypes#LaneDetectSample} must be castes to {@link com.via.adas.sensing.SensingSamples.LaneDetectSample}<br>
     *  The following list shows the cast rule. <br><br>
     * <ul>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#LaneDetectSample} cast to -> {@link com.via.adas.sensing.SensingSamples.LaneDetectSample}</li>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#VehicleDetectSample} cast to  -> {@link com.via.adas.sensing.SensingSamples.VehicleDetectSample}</li>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#SpeedLimitDetectSample} cast to  -> {@link com.via.adas.sensing.SensingSamples.SpeedLimitDetectSample}</li>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#BlindSpotDetectSample} cast to  -> {@link com.via.adas.sensing.SensingSamples.BlindSpotDetectSample}</li>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#ObjectDetectSample} cast to  -> {@link com.via.adas.sensing.SensingSamples.ObjectDetectSample}</li>
     * <li>type as {@link com.via.adas.sensing.SensingSamples.SampleTypes#EnvironmentSample} cast to  -> {@link com.via.adas.sensing.SensingSamples.EnvironmentSample}</li>
     * </ul><br>
     *  The following list shows the way to get LaneDetection sample from task0.<br>
     *  <pre>
     *  {@code
     *     SensingSamples.LaneDetectSample laneSample = (SensingSamples.LaneDetectSample)automotivePlanner.getSensingSample(SensingTaskID.SensingTask_0, SensingSamples.SampleTypes.LaneDetectSample);
     * }<pre>
     * @param missionID task id to acquire data.
     * @param type acquire type of data sample.
     * @return a {@link SensingSamples} object will return. Note above cast rule of return object.
     */
    public SensingSamples.SensingSample getSensingSample(@NonNull SensingTaskID missionID, @NonNull SensingSamples.SampleTypes type) {
        SensingSamples.SensingSample ret = null;
        SensingUnit unit = mSensingUnitList.get(missionID.ordinal());
        if(unit != null && unit.module != null) {
            ret = unit.module.getSensingSample(type);
        }
        return  ret;
    }

    /**  Init SensingModule / SensingTask
     1. This step will init and apply resource to adas modules)
     2. This resource such as SLD model, FCWS cascade model, FCWS_DL model must set in this functions.
     */
    private void initSensingModules() {

        // 1. Create sensing modules
        for(int si = 0; si < mSensingUnitList.size() ; si++) {
            SensingUnit unit = mSensingUnitList.get(si);
            if(unit != null && unit.detectorSet != null) {
                unit.module = new SensingModule(unit.detectorSet, mCANbusModule, mCameraUnitList.get(unit.cameraID.ordinal()).module);
            }
        }

        // 2. Set resource of sensing modules
        final String restorePath = mContext.getApplicationContext().getFilesDir().toString();
        ResourceManager mResourceManager = new ResourceManager(mContext);

        for(int si = 0; si < mSensingUnitList.size() ; si++) {
            SensingUnit unit = mSensingUnitList.get(si);
            if(unit != null && unit.module != null) {
                if(unit.module.isDetectorActive(DetectorTypes.FCWS)) {
                    try {
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_CascadeModel, restorePath);
                        String resourceName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_CascadeModel);
                        unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_CascadeModel, restorePath, resourceName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(unit.module.isDetectorActive(DetectorTypes.Weather)) {
                    try {
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.Weather_ClassifyModel, restorePath);
                        String resourceName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.Weather_ClassifyModel);

                        unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.Weather_ClassifyModel, restorePath, resourceName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(unit.module.isDetectorActive(DetectorTypes.SLD)) {
                    try {
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Label, restorePath);
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Model, restorePath);
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_Proto, restorePath);
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_NightModel, restorePath);
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.SLD_PrefetchModel, restorePath);

                        String modelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Model);
                        String protoName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Proto);
                        String labelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_Label);
                        String nightModelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_NightModel);
                        String prefetchModelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.SLD_PrefetchModel);

                        if (modelName != null && protoName != null && labelName != null && nightModelName != null && prefetchModelName != null) {
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Label, restorePath, labelName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Model, restorePath, modelName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_Proto, restorePath, protoName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_NightModel, restorePath, labelName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.SLD_PrefetchModel, restorePath, labelName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(unit.module.isDetectorActive(DetectorTypes.FCWS_DL)) {
                    try {
                        String nativeLibraryDir = mContext.getApplicationInfo().nativeLibraryDir;
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_DL_Model, restorePath);
                        mResourceManager.restoreResource(RuntimeLoadableDataTypes.FCWS_DL_Label, restorePath);

                        String modelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_DL_Model);
                        String labelName = mResourceManager.getResourceName(RuntimeLoadableDataTypes.FCWS_DL_Label);

                        if(modelName != null && labelName != null) {
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_Model, restorePath, modelName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_Label, restorePath, labelName);
                            unit.module.setRuntimeLoadableData(RuntimeLoadableDataTypes.FCWS_DL_DSPLib, nativeLibraryDir, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 3. Init ADAS
        for(int si = 0; si < mSensingUnitList.size() ; si++) {
            SensingUnit unit = mSensingUnitList.get(si);
            if (unit != null && unit.module != null) {
                unit.module.init(mContext, unit.configPath);
            }
        }
    }

    private void initSensingTasks() {
        for(int mi = 0; mi < SENSING_TASK_COUNT; mi++) {
            SensingUnit unit = mSensingUnitList.get(mi);
            unit.task = new SensingTask(mi);
            unit.task.setSensingModule(unit.module);
        }
    }

    private void initTaskRelationUnitList() {
        for(int ui = 0; ui < mTaskRelationUnitList.size(); ui++) {
            TaskRelationUnit unit = mTaskRelationUnitList.get(ui);

            SensingUnit srcUnit = mSensingUnitList.get(unit.srcTaskID.ordinal());
            SensingUnit relUnit = mSensingUnitList.get(unit.relatedTaskID.ordinal());
            if (srcUnit.module != null && relUnit.module != null) {
                srcUnit.module.registerRelatedModule(relUnit.module, unit.relatedDetector);
            }
        }
    }

    private boolean startSensingModules(@NonNull SensingUnit unit, int newPriority) {
        // Start Detection thread
        boolean ret = false;
        if (unit != null && unit.task != null) {
            ret = unit.task.start(newPriority);
        }
        return ret;
    }

    private void releaseSensingModules() {

        for(int si = 0; si < mSensingUnitList.size() ; si++) {
            SensingUnit unit = mSensingUnitList.get(si);
            if (unit != null && unit.task != null) {
                //unit.task.release();
                Log.i(TAG, "releaseSensingModules " + si);
                unit.release();
            }
        }
    }

}

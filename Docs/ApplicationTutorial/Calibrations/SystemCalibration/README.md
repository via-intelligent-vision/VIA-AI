System Calibration
=====


<b>Make sure [Camera calibration](../CameraCalibration/README.md) completed and config camera calibration path in Setting.</b>

VIA-AI support on-the-fly system calibration, this operations will calibrate VIA-AI system automatically when driving on freeway, following step shows the system calibration steps:

1. When camera calibration finish, VIA-AI system need calibrated by system calibration.

2. Please mount the phone, and connect phone to comma.ai panda & comma.ai giraffe.

3. Launch VIA-AI dashcam application, click <b>START</b> button to open system, on system init finish, please check the CANBus status lamps is enable or not (shown as following image). If the CANBus status lamps is disabled, please following the [Trouble Shooting](#Trouble-Shooting)<br>
    
    | Panda Online              | Panda Offline             |
    | ------------------------- | ------------------------- |
    | ![](./panda_offline.png)  |  ![](./panda_online.png)  |

4. In system first launch, "Non Calibrated system" will show and hint user to calibrate system. Please drive to freeway and launch system calibration by following table<br>

    | Make                 | Model                    | Supported Package    | How to enable auto calibration                                                                         |
    | ---------------------| -------------------------| ---------------------| ------------------------------------------------------------------------------------------------------ |
    | Honda                | CR-V 2017-19             | Honda Sensing        | Click the LKS button on steering wheel 3 times<br> ![](./honda_bosch2017_steeringwheel_auto_calib.png) |


5. When system calibration enabled, Please keep vehicle driving on the center of lane and speed over 75km/h, don't change the lane if possible, system calibration will finish in 30s ~ 2minute..<br>

    | System not Calibrated | Calibrating            |  Calibration Finished |
    | --------------------- | ---------------------- | --------------------- |
    | ![](./non_calib.png)  | ![](./calibrating.png) | ![](./calib_fin.png)  |


6. When calibration finish, you could find the way to enable LKS & ACC by following table. 

    | Make                 | Model                    | Supported Package    | How to enable ACC & LKS                                                                                  |
    | ---------------------| -------------------------| ---------------------| -------------------------------------------------------------------------------------------------------- |
    | Honda                | CR-V 2017-19             | Honda Sensing        | Click the RES+ or -SET button on steering wheel<br> ![](./honda_bosch2017_steeringwheel_enable_sys.png)  |

7. When system enabled, please focus on the driving, VIA-AI is an assistance system and couldn't replace the driver. <br> 
   If LKS system will departure from lane or snaking, please re-calibrate system.

8. LKS & ACC system will stop when step on brake/gas. 


Trouble Shooting
==========

* When comma.ai Panda connected, CANBus status lamp is disable, please try the following scenario：
    * Close VIA-AI dashcam app first, remove Panda from Giraffe & phone, and connect to giraffe again then the phone, if panda detected correctly, VIA-AI dashcam app  will launch automatically
    * lose VIA-AI dashcam app first, remove and reconnect OTG cable, if panda detected correctly, VIA-AI dashcam app  will launch automatically
    * <b>!!Warning!! </b> When VIA dashcam app launched and detection system enabled, DON't remove the connection between phone and panda/giraffe, or it will cause the unexceptable potential danger.




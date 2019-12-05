Camera Calibration
=====

A part algorithm of VIA-AI is based on opencv camera model. To launch system correctly, please calibration camera by following steps:

1. Download opencv camera calibration [Pattern](https://docs.opencv.org/2.4/_downloads/pattern.png), print pattern by A4 and create a calibration board.<br>
    ![](./opencv_chessboard.png) 
    
2. Launch "VIA-AI Camera calibration" application, and input the board information : board Width, board Height and grid size (cm). In above opencv calibration board, the board width is 9, board height is 6.<br>
   Application will detect calibration board after all input finish.<br>
    ![](./calibration_parameters.png) 
    
3. Move the camera or calibration board, let calibration board located in different place and angle in camera view, as shown below<br>
   Keep this step until progress 100% (left-top), don't keep in same place or angle too much time.
    ![](./calibration_samples.png) 
    
4. When progress 100%, system will start camera calibration process, this process will take some times (2~5 minute depend on phone's capability). <br>
   Please keep phone awake until calibration finish, close phone or pause application will stop the calibration process.<br>
    ![](./calibration_result.png) 
    
5. Click save button and save calibration data in to storage when calibration finish, VIA-AI dashcam app need this configuration to launch system.

6. For VIA-AI dashcam application, click Setting, choose Camera Settings-> configure Camera Module as <b>Custom Calibration</b> <br>
   Click "Camera Calibration Configuration Path" to choose configuration path in step 5, and the camera calibration step is finish.<br>
    ![](./calibration_setting.png) 
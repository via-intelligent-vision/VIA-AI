VIA-AI : Open Source Driver Assistance System
=====

[VIA-AI](https://www.viatech.com/tw/) is an open source driver assistance app, supports Adaptive Cruise Control (ACC), Lane Keeping Assist System (LKAS), and CAN Bus integration functions. It enables the development of driver assistance capabilities for selected vehicles using a modern Android smartphone and hardware connectors. 


Table of Contents
=====

* [Requisites](#Requisites)
    - [Android Requirements](#Android-Requirements)
    - [Hardware Requirements](#Hardware-Requirements)
* [Supported Devices](#Supported-Devices)
* [Supported Cars](#Supported-Cars)
* [Applications](#Applications)
* [Getting Started](#Getting-Started)
* [Directory structure](#directory-structure)
* [Help and Crash Repor](#Suppoer-and-Crash-Report)
* [Issue List](#Issue-List)
* [License](#License)


## Requisites

#### Hardware Requirements
* [comma.ai White Panda](https://comma.ai/shop/products/panda-obd-ii-dongle)
* [comma.ai Giraffe](https://comma.ai/shop/products/giraffe)   
  * Firmware version: v1.4.2-DEV-519e39e4
  * How to update firmware ?: [panda link](https://github.com/commaai/panda/tree/master/board)
* Smartphone mounts
  * Camera installation guide. [link]()
* USB OTG hub/adapter for USB type-C or micro-A  


#### Android Requirements
* Android 8.0 or higher


## Supported Devices

| Vendor      | Model         | Connector      |
| ----------- | ------------- | -------------- |
| HTC         | U11+          | Type-C         |
| HTC         | U12+          | Type-C         |
| GOOGLE      | Pixel2        | Type-C         |


## Supported Cars
Thanks the contribution of comma.ai & community, the part of vehicle control logic & CNABus DBC are referenced from [openpilot](https://github.com/commaai/openpilot)  

| Make                 | Model                    | Supported Package    | Lateral | Longitudinal   | No Accel Below   | No Steer Below | Giraffe           |
| ---------------------| -------------------------| ---------------------| --------| ---------------| -----------------| ---------------|-------------------|
| Honda                | CR-V 2017-19             | Honda Sensing        | Yes     | Stock          | 0mph             | 12mph          | Honda Bosch       |


## Applications
| App Name                       | Purpose                                                               | Permission Requirements                                                                                                                                 |
| ------------------------------ | --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| VIA-AI                         | A dashcam app supports ACC, LKAS, video/CANBus record.<sup>1</sup>  | CAMERA, INTERNET, <br>ACCESS_NETWORK_STATE, <br>READ_EXTERNAL_STORAGE, <br>WRITE_EXTERNAL_STORAGE, <br>ACCESS_COARSE_LOCATION, <br>ACCESS_FINE_LOCATION |
| VIA-AI Setting                 | About system settings, user management....                            | INTERNET, <br>ACCESS_NETWORK_STATE, <br>READ_EXTERNAL_STORAGE, <br>WRITE_EXTERNAL_STORAGE                                                               |
| VIA-AI Camera Calibration      | Simple camera calibration app based on openCV                         | CAMERA,  READ_EXTERNAL_STORAGE, <br>WRITE_EXTERNAL_STORAGE                                                                                              |

<sup>1</sup> Only video data will be record.<br>


## Getting Started
Before driving, Please review the [requisites](#Requisites) and following installation guides to calibrate & configure VIA-AI system. 

1. [Giraffe/Smartphone Mounts Installation Guide](./Docs/InstallationGuide/README.md)
    * Installing Giraffe & Panda 
    * Installing smartphone mounts
   
2. [Application Tutorial](./Docs/ApplicationTutorial/README.md)
    * First launch
    * System calibration
    * User interfaces

        

## Directory structure
    .
    ├── Docs                    # The documents
    └── System                  # Android Source code about VIA-AI
        ├── app                 # Main system, setting, camera calibration.
        ├── AutomotiveKernel    # Control algorithm about LKS/ACC.
        ├── Utility             # Android utilities for camera.
        ├── VIA_Libs            # VIA private library : web server, sensing module.



## Help and Crash Report
Contact Us : via_ai@via.com.tw


## Issue List

| Issue                 |                                                                                                                                 |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| Panda connection fail | Smartphone couldn't identify device when panda connecting, or need to try different connection scenario between Panda, OTG hub and phone to find device. |
| System charging issue | Some phone models couldn't charging from panda by OTG hub |



## Disclaimer & Privacy Sstatement
* [Disclaimer](./Docs/Disclaimer_zh_tw_20190902.pdf)
* [Privacy Sstatement](./Docs/PrivacyStatement_zh_tw_20190902.pdf)

## License
MIT License

Copyright (c) 2019 VIA, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
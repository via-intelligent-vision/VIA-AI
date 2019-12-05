System UI
=====

![](./main_ui.png)


* CANBus status (Panda Status) : Enabled if Panda dongle connected.

    | Panda Online                       | Panda Offline                         |
    | ---------------------------------- | ------------------------------------- |
    | ![](./icon_small_bus_connect.png)  |  ![](./icon_small_bus_disconnect.png) |

* LKS status : Enabled if driver enable system.
    * LKS will disabled when speed lower/over the vehicle lateral control limitation. [link](../../../../README.md)
    * LKS also disabled when driver take over the steering wheel.
    * When step on the brake/gas pedal, LKS will disable automatically.
    
    | LKS Enable                    | LKS Disable                     |
    | ----------------------------- | ------------------------------- |
    | ![](./icon_small_lks_on.png)  |  ![](./icon_small_lks_off.png)  |

* ACC status : Enabled if driver enable system.
    *  When step on the brake/gas pedal, LKS will disable automatically.
    
    | ACC Enable                    | ACC Disable                     |
    | ----------------------------- | ------------------------------- |
    | ![](./icon_small_acc_on.png)  |  ![](./icon_small_acc_off.png)  |

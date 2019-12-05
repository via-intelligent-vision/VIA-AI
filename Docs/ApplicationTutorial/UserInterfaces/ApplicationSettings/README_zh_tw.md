
Application Settings
=====

Application Setting包含了各項系統參數, 包括Camera, Recorder, ADAS, User, System等.<br>
![](./setting_header.png)


各項配置項目請參考設定內的說明, 以下提出幾項需特別注意的項目

* <b>Camera Settings</b>
  1. <b>Frame Source</b><br>
    目前Frame source提供Camera與Video 2種配置, 若選擇 Video則必需在Playback Video Path內選播放的錄影檔路徑.<br><br>
    <b>注意</b> : 目前Playback模式並不提供CANBus資料同步回放功能, 僅提供影像＆感測算法預覽功能<br>
    ![](./frame_source.png)

  2. <b>Camera Module</b><br>
    Camera Module欄位主要配置手機Camera的校正資訊, 如果您的手機型號位於預設列表中, 則可以考慮使用預設列表提供的相機校正參數.<br>
    若您的手機不再列表中或是您想使用自己校正後的相機資訊, 請將Camera Mode配置為</b>"Custom Calibration"</b>, 並於下列Camera Calibration Configuration Path中設定您的鏡頭校正檔案路徑.<br><br>
    <b>注意:</b>鏡頭校正需透過VIA-AI Camera Calibration來產生, 使用其他軟體產生的鏡頭校正檔可能產生參數存取錯誤或者是相機座標轉換發生問題<br>
    ![](./camera_module.png)
    
  3. <b>Camera Install Height & Offset between vehicle center line </b><br>
    Camera Install Height主要描述的是手機鏡頭至地面的<b>垂直高度</b>.<br>
    Offset between vehicle center line主要描述的是手機鏡頭至車輛中心線的<b>水平距離</b>.<br>
    以上2個參數請依據實際安裝情形調整, 單位均為公分<br>

     |  Parameter                     | Schematic Diagram                |  
     | ------------------------------ | -------------------------------- | 
     | ![](./camera_install_info.png) | ![](./camera_install_sample.png) |
     
* <b>Recorder Settings</b>
  1. 若需啟動Video Record, 請設定Video Record Status為Enable, 並設定儲存路徑
  2. Vehicle Bus data 需先啟動Video-Record後才可決定是否Enable, 否則都是處於Disable狀態

* <b>ADAS Settings</b>
  1. <b>Regular Settings</b><br> 
    內部包含了Sensing & Automotive相關的預設參數檔, 如果沒有特殊實驗需求, 請勿修改路徑 <br>
    * Sensing Configuration : 設定Sensing相關的參數檔, 以讓車道檢測, 前車檢測等ADAS系統使用
    * Automotive Configuration : 設定LKS, ACC等Controller相關參數

  2. <b>Audio Setting</b><br>
    * Text-To-Speech : 是否啟用google TTS提示訊息
    * Beep Alarm : 是否使用手機預設提示音進行提示


* <b>User</b>
  細節請參考[User Managerment](../UserManagement/README.md)


* <b>User</b>
  顯示系統版本號等資訊




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

package com.viatech.resource;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.viatech.resource.RuntimeLoadableDataTypes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class ResourceManager {
    final String TAG = this.getClass().getName();

    // Data
    private Context mContext;
    private HashMap<RuntimeLoadableDataTypes, String > mAllocatedResourceList;

    public ResourceManager(Context context)
    {
        mContext = context;
        mAllocatedResourceList = new HashMap<>();
    }

    public void deleteRestoreResource(RuntimeLoadableDataTypes resourceType)
    {
        String storePath = mAllocatedResourceList.get(resourceType);
        if(storePath != null) {
            File file = new File(storePath);
            if(file.exists()) {
                file.delete();
            }
            mAllocatedResourceList.remove(resourceType);
        }
    }

    public String getResourcePath(RuntimeLoadableDataTypes resource)
    {
        return mAllocatedResourceList.get(resource);
    }

    public String getResourceName(RuntimeLoadableDataTypes resource) {
        String ret = null;
        String path = getResourcePath(resource);
        if(path != null) {
            ret = path.substring(path.lastIndexOf("/") + 1);
        }
        return ret;
    }

    public boolean restoreResource(RuntimeLoadableDataTypes resource, String restorPath) throws Exception {
        boolean ret = false;

        // check restore path is directory
        File dir = new File(restorPath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        if(!dir.exists() || !dir.isDirectory()) {
            throw new Exception("Error restor path [" + restorPath + " ], it must be a directory");
        }

        // restore resource group
        switch (resource) {
            case FCWS_CascadeModel:
                ret = restoreConfigurations(resource, "FCWS/pos_5195_neg_9314_Haar_w28h28_20181018.xml", restorPath);
                break;
            case FCWS_DL_Model:
                //ret = restoreConfigurations(resource, "FCWS_DL/mobilenet_ssd.dlc", restorPath);   // raw DLC file
                ret = restoreConfigurations(resource, "FCWS_DL/color_texture.vdm", restorPath); // protected DLC file
                break;
            case FCWS_DL_Label:
                ret = restoreConfigurations(resource, "FCWS_DL/mobilenet_ssd.txt", restorPath);
                break;
            case SLD_Model:
                ret = restoreConfigurations(resource, "SLD/SLD_Gray_CombineChar.caffemodel", restorPath);
                break;
            case SLD_Proto:
                ret = restoreConfigurations(resource, "SLD/SLD_Gray_CombineChar.prototxt", restorPath);
                break;
            case SLD_Label:
                ret = restoreConfigurations(resource, "SLD/SLD_Gray_CombineChar_label.txt", restorPath);
                break;
            case SLD_NightModel:
                ret = restoreConfigurations(resource, "SLD/SLD_Gray_Night_CombineChar.caffemodel", restorPath);
                break;
            case SLD_PrefetchModel:
                ret = restoreConfigurations(resource, "SLD/SLD_PrefetchModel.xml", restorPath);
                break;
            case Weather_ClassifyModel:
                ret = restoreConfigurations(resource, "Weather/WeatherClassifier.xml", restorPath);
                break;
            case TLD_Pattern_1:
                ret = restoreConfigurations(resource, "TLD/tmpRed_8.png", restorPath);
                break;
            case TLD_Pattern_2:
                ret = restoreConfigurations(resource, "TLD/tmpGn_1.png", restorPath);
                break;
            case TLD_Pattern_3:
                ret = restoreConfigurations(resource, "TLD/tmpRed_8.png", restorPath);
                break;
            default:
                ret = false;
                break;
        }

        return ret;
    }

    private boolean restoreConfigurations(RuntimeLoadableDataTypes resource, String assetFilePath, String restorePath)
    {
        boolean ret = false;
        String assetFileName = assetFilePath.substring(assetFilePath.lastIndexOf("/") + 1);

        // Remove previous restored resource
        String prevStorePath = mAllocatedResourceList.get(resource);
        if(prevStorePath == null) {
            deleteRestoreResource(resource);
        }

        // Copy resource to restore path
        do {
            AssetManager assetManager = mContext.getResources().getAssets();

            try {
                InputStream inputStream = assetManager.open(assetFilePath);
                File file = new File(restorePath + "/" + assetFileName);
                OutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length = 0;
                while((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer,0,length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                // Add path to list
                mAllocatedResourceList.put(resource, restorePath + "/" + assetFileName);

                ret = true;
            } catch (Exception e) {
                Log.d(TAG, "ERROR: " + e.toString());
            }
        } while(false);

        return ret;
    }
}

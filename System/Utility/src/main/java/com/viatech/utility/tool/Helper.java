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

package com.viatech.utility.tool;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.IOException;

public class Helper {

    public static boolean isUpperThanAPI21() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return true;
        } else {
            return false;
        }
    }


    public static String getAPKFilepath(Context context) {
        // Get the path
        String apkFilePath = null;
        ApplicationInfo appInfo = null;
        PackageManager packMgmr = context.getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();
        try {
            appInfo = packMgmr.getApplicationInfo(packageName, 0);
            apkFilePath = appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return apkFilePath;
    }
    public static void findAPKFile(String filepath, Context context) {
        String apkFilepath = getAPKFilepath(context);

        // Get the offset and length for the file: theUrl, that is in your
        // assets folder
        AssetManager assetManager = context.getAssets();
        try {

            AssetFileDescriptor assFD = assetManager.openFd(filepath);
            if (assFD != null) {
                long offset = assFD.getStartOffset();
                long fileSize = assFD.getLength();





                assFD.close();

                // **** offset and fileSize are the offset and size
                // **** in bytes of the asset inside the APK
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

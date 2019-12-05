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

package com.viatech.via_ai.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.viatech.via_ai.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExternalStoragePickDialog extends Dialog implements AdapterView.OnItemClickListener, DialogInterface.OnDismissListener
{
    public interface OnEventChangeListener {
        void onItemClick(ExternalStoragePickDialog dialog, String storageName, String accesaPath);
        void onDismiss(ExternalStoragePickDialog dialog);
    }

    private Context mContext;
    private List<HashMap<String , String>> mStoragePathList;    // StorageName, AccessPath
    private OnEventChangeListener mOnEventChangeListener;
    private ListView mUI_StorageList;

    private final String mKey_StorageName = "name";
    private final String mKey_AccessPath = "path";
    private final String mKey_Path_sdcard = "/sdcard";
    private final String mKey_Path_emulated_internal = "/storage/emulated/0";
    private final String mKey_Path_storage = "/storage";

    public ExternalStoragePickDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ExternalStoragePickDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected ExternalStoragePickDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(@NonNull Context context) {
        mContext = context;
        mOnEventChangeListener = null;
        mStoragePathList = new ArrayList<>();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_external_storage_pick_dialog);

        setupUI();
    }

    private void setupUI()
    {
        mUI_StorageList = findViewById(R.id.layoutExternalStoragePickDialog_StorageList);

    }

    @Override
    public void show() {
        File[] fs = ContextCompat.getExternalFilesDirs(mContext, null);
        mStoragePathList.clear();

        if(fs.length == 0) {
            HashMap<String , String> hashMap = new HashMap<>();
            hashMap.put(mKey_StorageName , "Non");
            hashMap.put(mKey_AccessPath , "No external storage found");
            mStoragePathList.add(hashMap);
        }
        else {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].exists() && fs[i].canRead() && fs[i].canWrite()) {
                    String accessPath = fs[i].getPath();
                    String storageName;

                    if(accessPath.startsWith(mKey_Path_storage)) {
                        storageName = accessPath.substring(mKey_Path_sdcard.length() + 2,  accessPath.indexOf("/Android/data"));
                    }
                    else {
                        storageName = accessPath;
                    }


                    HashMap<String , String> hashMap = new HashMap<>();
                    hashMap.put(mKey_StorageName , storageName);
                    hashMap.put(mKey_AccessPath , accessPath);
                    mStoragePathList.add(hashMap);
                }
            }
        }

        ListAdapter adapter = new SimpleAdapter(mContext, mStoragePathList, android.R.layout.simple_list_item_2,
                new String[] {mKey_StorageName, mKey_AccessPath},
                new int[]{android.R.id.text1 , android.R.id.text2});

        mUI_StorageList.setAdapter(adapter);
        mUI_StorageList.setOnItemClickListener(this);

        // Change size
        Window window = ((Activity)mContext).getWindow();
        if(window != null) {
            Rect displayRectangle = new Rect();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            this.getWindow().setLayout((int) (displayRectangle.width() * 0.8f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        super.setOnDismissListener(this);
        super.show();
    }

    public void setOnEventChangeListener(OnEventChangeListener listener) {
        mOnEventChangeListener = listener;
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        throw new UnsupportedOperationException(this.getClass().getName() + " disable this function, use ExternalStoragePickDialog.OnEventChangeListener replaced");
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if(mOnEventChangeListener != null) {
            mOnEventChangeListener.onDismiss(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        HashMap<String , String> storage = mStoragePathList.get(position);

        if(mOnEventChangeListener != null) {
            mOnEventChangeListener.onItemClick(this, storage.get(mKey_StorageName), storage.get(mKey_AccessPath));
        }
        dismiss();
    }
}

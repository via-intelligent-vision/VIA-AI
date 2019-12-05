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

package com.viatech.via_ai.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.R;

import java.io.File;

import static com.viatech.via_ai.Preferences.Utils.postFileDialog;

public class FileDialogPreference extends Preference {
    private String mPathValue = "";
    private Context mContext;
    private View mView = null;
    private Integer mBkgColor = null;
    private String mFileExtension =".xml";
    private boolean b_FileSelect = true;

    public FileDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public FileDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FileDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FileDialogPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        final Preference thiz = this;
        this.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                postFileDialog(mContext, thiz, mFileExtension, b_FileSelect);
                return false;
            }
        });

    }

    public void setPath(String path) {
        boolean isValid = false;
        mPathValue = path;

        do {
            if(path == null || path == "") break;

            if(isFileExist(path)) {
                this.setSummary("    Path : " + path);
                persistString((String) path);
                isValid = true;
            }
        } while(false);

        if(!isValid) {
            persistString("");
            if(b_FileSelect) {
                this.setSummary("    File : " + path + " ... not exist or permission denied. Click to select a new <" + mFileExtension + "> file.");
            }
            else {
                this.setSummary("    Folder : " + path + " ... not exist or permission denied. Click to select a new folder.");
            }

            if(isEnabled()) {
                this.setBackgroundColor(Color.rgb(170, 30, 30));
            }
            else {
                this.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        else {
            this.setBackgroundColor(Color.TRANSPARENT);
        }

        if(mView != null) {
            super.onBindView(mView);    // use base class's oBindView to avoid recursive.
        }
    }

    public void setDialog(boolean isFileSelector, String fileExtension) {
        b_FileSelect = isFileSelector;
        mFileExtension = fileExtension;
    }

    public void setBackgroundColor(@ColorInt int color) {
        mBkgColor = color;
        _setBackgroundColor(mBkgColor);
    }

    private void _setBackgroundColor(@ColorInt int color) {
        if(mView != null) {
            if(isEnabled()) {
                mView.setBackgroundColor(color);
            }
            else{
                mView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    protected View onCreateView( ViewGroup parent ) {
        super.onCreateView(parent);
        final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.layout_preference_file_dialog, parent, false);
        mView = layout;
        if(mBkgColor != null) this.setBackgroundColor(mBkgColor);

        setPath(this.getPersistedString(""));

        return layout;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindView(View view) {
        setPath(mPathValue);
        super.onBindView(view);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            mPathValue = this.getPersistedString(mPathValue);
        } else {
            // Set default state from the XML attribute
            persistString((String) defaultValue);
            if(defaultValue != null) mPathValue = (String)defaultValue;
        }
        setPath(mPathValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(mBkgColor != null) this._setBackgroundColor(mBkgColor);
    }

    public String getPathValue() {
        return mPathValue;
    }

    private boolean isFileExist(String path)  {
        boolean ret = false;

        if(path.length() > 0) {
            File file = new File(path);
            if (file.exists() && file.canWrite() && file.canRead()) ret = true;
        }
        return ret;
    }
}

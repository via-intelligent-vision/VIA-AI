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
import android.preference.Preference;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.R;


public class TextPreference extends Preference {
    private String mTextValue = "";
    private Context mContext;
    private View mView = null;
    private Integer mBkgColor = null;

    public TextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public TextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

    }

    public void setBackgroundColor(@ColorInt int color) {
        if(mView != null) {
            mView.setBackgroundColor(color);
        }
        mBkgColor = color;
    }

    public String getText() {
        return mTextValue;
    }

    public void setText(String value) {
        mTextValue = value;
        persistString(mTextValue);

        setSummary("    " + mTextValue);

        if(mView != null) {
            super.onBindView(mView);    // use base class's oBindView to avoid recursive.
        }
    }

    @Override
    protected View onCreateView( ViewGroup parent ) {
        super.onCreateView(parent);
        final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.layout_textpreference, parent, false);
        mView = layout;
        if(mBkgColor != null) mView.setBackgroundColor(mBkgColor);

        return layout;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindView(View view) {
        setText(mTextValue);
        super.onBindView(view);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            mTextValue = this.getPersistedString(mTextValue);
        } else {
            // Set default state from the XML attribute
            persistString((String) defaultValue);
        }
    }

}

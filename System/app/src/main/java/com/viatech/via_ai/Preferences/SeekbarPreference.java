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

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Looper;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.viatech.via_ai.R;

import java.text.DecimalFormat;

public class SeekbarPreference  extends Preference {
    private Context mContext;
    private View mView;
    private ImageView mUI_ContentImage;
    private SeekBar mUI_Seekbar;
    private Boolean b_ShowImg;
    private TextView mUI_Summary;
    private float mMin;
    private float mMax;
    private float mStep;
    private String mUnit;
    private int mBarValue;
    private DecimalFormat mBarToDecimal;

    public SeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) throws IllegalAccessException {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public SeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr) throws IllegalAccessException {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SeekbarPreference(Context context, AttributeSet attrs) throws IllegalAccessException {
        super(context, attrs);
        init(context, attrs);
    }

    public SeekbarPreference(Context context) throws IllegalAccessException {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) throws IllegalAccessException {
        mContext = context;
        b_ShowImg = null;
        mMin = 0.0f;
        mMax = 100.0f;
        mStep = 1.0f;
        mBarValue = 0;
        mUnit = "";

        // parse attrs
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference, 0, 0);
        try {
            mMin = ta.getFloat(R.styleable.SeekbarPreference_min, 0.0f);
            mMax = ta.getFloat(R.styleable.SeekbarPreference_max, 100.0f);
            mStep = ta.getFloat(R.styleable.SeekbarPreference_step, 1.0f);
            mUnit = ta.getString(R.styleable.SeekbarPreference_unit);
        } finally {
            ta.recycle();
        }

        if((mMax < mMin)) {
            throw new IllegalAccessException("Error seekbar value :min " + mMin + ",max:" + mMax + " ,step:" + mStep);
        }

        mBarToDecimal = new DecimalFormat("#.##");

    }


    @Override
    protected View onCreateView(ViewGroup parent ) {
        super.onCreateView(parent);
        final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.layout_preference_seekbar, parent, false);

        mView = layout;
        mUI_ContentImage = mView.findViewById(R.id.contentImage);
        mUI_Seekbar = mView.findViewById(R.id.seekbar);
        mUI_Summary = mView.findViewById(android.R.id.summary);

        if(mUI_ContentImage != null) {
            if(b_ShowImg != null)
                setPreviewVisibility(b_ShowImg);
            else {
                setPreviewVisibility(false);
            }
        }

        if(mUI_Seekbar != null) {
            int barMax = (int)((mMax - mMin) / mStep);
            mUI_Seekbar.setMax(barMax);
            mUI_Seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mBarValue = progress;
                    updateUI(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    setSeekValue(mBarValue);
                    setSummary(mBarToDecimal.format(getValue()) + mUnit);
                }
            });
        }

        this.setSummary(mBarToDecimal.format(getValue()) + mUnit);

        setSeekValue(getBarValue(Float.parseFloat(this.getPersistedString("0"))));

        return layout;
    }

    @Override
    protected void onBindView(View view) {
        updateUI(mBarValue);
        super.onBindView(view);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            mBarValue = getBarValue(Float.parseFloat(this.getPersistedString(Integer.toString(mBarValue))));
        } else {
            // Set default state from the XML attribute
            if(defaultValue != null) mBarValue = Integer.parseInt((String)defaultValue);

        }

        setSeekValue(mBarValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setPreviewVisibility(isEnabled());
    }

    public void setSeekValue(int value) {
        if(mUI_Seekbar != null) {
            mUI_Seekbar.setProgress(value);
        }

        persistString(mBarToDecimal.format(getValue()));

        // update UI
        updateUI(value);


        if(mView != null) {
            super.onBindView(mView);    // use base class's oBindView to avoid recursive.
        }
    }

    public float getValue() {
        return mMin + ((float)mBarValue * mStep);
    }


    public int getBarValue(float dValue) {
        return (int) ((dValue - mMin) / mStep);
    }


    public void updateUI(final int barValue) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            float pValue = getValue();

            // set summary
            if(mUI_Summary != null) {
                mUI_Summary.setText(mBarToDecimal.format(pValue) + mUnit);
            }
        }
        else {
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI(barValue);
                }
            });
        }

    }

    public void setPreviewVisibility(final boolean show) {
        b_ShowImg = show;
        if(mUI_ContentImage != null) {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                if (b_ShowImg) {
                    mUI_ContentImage.setVisibility(View.VISIBLE);
                } else {
                    mUI_ContentImage.setVisibility(View.GONE);
                }
            }
            else {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPreviewVisibility(show);
                    }
                });
            }
        }
    }
}

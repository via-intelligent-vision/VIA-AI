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

package com.viatech.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.viatech.automotivekernel.R;

public class SpeedLimitView extends RelativeLayout {

    // Context
    private Context mContext;

    // Data
    private int mSpeedLimit;

    // UI
    private AppCompatTextView mUI_TextView_SpeedLimit;

    // Functions
    public SpeedLimitView(Context context) {
        super(context);
        init(context, null);
    }

    public SpeedLimitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context ,attrs);
    }

    public SpeedLimitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SpeedLimitView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        refreshUI();
    }

    void init(Context context, AttributeSet attrs)
    {
        mContext = context;
        mSpeedLimit = 110;


        // parse attrs
        if(attrs != null) {
            TypedArray typeAry = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpeedLimitView,0, 0);
            if(typeAry != null) {
                try {
                    mSpeedLimit = typeAry.getInteger(R.styleable.SpeedLimitView_speedLimit, 110);
                } finally {
                    typeAry.recycle();
                }
            }
        }


        setupLayout();
    }

    private void setupLayout()
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.speed_limit_view, this, true);

        mUI_TextView_SpeedLimit =  this.findViewById(R.id.SpeedLimitValueText);
        mUI_TextView_SpeedLimit.setText(Integer.toString(mSpeedLimit));
    }

    private void refreshUI()
    {
        mUI_TextView_SpeedLimit.setText(Integer.toString(mSpeedLimit));
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
    }

    private void postRefreshUI()
    {
        post(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        });

    }

    public void setSpeedLimitValue(int speedLimit)
    {
        mSpeedLimit = speedLimit;
        postRefreshUI();
    }

    public int getSpeedLimitValue()
    {
        return mSpeedLimit;
    }
}

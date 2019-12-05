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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.viatech.via_ai.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryStatusView extends ConstraintLayout {

    private Context mContext;
    private AppCompatTextView mUI_BatteryPercentage;
    private ImageView mUI_ChargingImg;
    private ImageView mUI_BatteryImg;
    private Timer mUI_BatteryTimer;
    private int mId_BatteryImg;
    private int mId_ChargingImgVisibility;
    private int mBatteryPercentage;
    private boolean mIsCharging;

    public BatteryStatusView(Context context) {
        super(context);
        init(context);
    }

    public BatteryStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BatteryStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_battery_status_view, this, true);

        mContext = context;
        mIsCharging = false;
        mBatteryPercentage = 0;
        mUI_BatteryPercentage = findViewById(R.id.BatteryPercentage);
        mUI_BatteryImg = findViewById(R.id.BatteryImg);
        mUI_ChargingImg = findViewById(R.id.ChargingImg);

        this.setClickable(false);

        AssetManager am = context.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am, String.format(Locale.US, "font/%s", "cursedtimerulil.ttf"));
        mUI_BatteryPercentage.setTypeface(typeface);
    }

    public void updateBatteryStatus(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;


        Integer status = null;
        if (batteryStatus != null) {
            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            mIsCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);

        }

        mBatteryPercentage = (int)((level / (float) scale) *100);
    }

    private void updateUI() {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            updateBatteryStatus(mContext);


            if (mUI_BatteryPercentage != null) {
                mUI_BatteryPercentage.setText(Integer.toString(mBatteryPercentage) + "%");
            }

            if (mUI_BatteryImg != null) {
                int id;
                if (mBatteryPercentage >= 75) {
                    id = R.drawable.icon_battery_100;
                } else if (mBatteryPercentage >= 50) {
                    id = R.drawable.icon_battery_75;
                } else if (mBatteryPercentage >= 25) {
                    id = R.drawable.icon_battery_50;
                } else {
                    id = R.drawable.icon_battery_25;
                }
                if (id != mId_BatteryImg) {
                    mId_BatteryImg = id;
                    mUI_BatteryImg.setImageResource(mId_BatteryImg);
                }
            }

            if (mUI_ChargingImg != null) {
                int id;
                if (mIsCharging) {
                    id = VISIBLE;
                } else {
                    id = INVISIBLE;
                }
                if (id != mId_ChargingImgVisibility) {
                    mId_ChargingImgVisibility = id;
                    mUI_ChargingImg.setVisibility(mId_ChargingImgVisibility);
                }
            }
        }
        else {
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
        }
    }

    public void start() {
        if(mUI_BatteryTimer == null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    updateUI();
                }
            };
            mUI_BatteryTimer = new Timer();
            mUI_BatteryTimer.schedule(task, 0,2500);
        }
    }

    public void stop() {
        if(mUI_BatteryTimer != null) {
            mUI_BatteryTimer.cancel();
            mUI_BatteryTimer = null;
        }
    }
}

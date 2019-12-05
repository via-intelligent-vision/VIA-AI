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

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.viatech.automotivekernel.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ConciseView extends ConstraintLayout {

    public enum ConciseStyleTypes {
        Unknown(-1),
        TypeA (0),
        TypeB(1),
        ;

        private int uId;

        private ConciseStyleTypes(int id) {
            uId = id;
        }

        public static ConciseStyleTypes getType(int uId) {
            for (ConciseStyleTypes f : values()) {
                if (f.uId == uId) return f;
            }
            return Unknown;
        }
    };

    public enum LaneDepartureType {
        Unknow(-1),
        NoDetected (0),
        Regular (1),
        Departure_Left (2),
        Departure_Right (3);

        private int uId;

        private LaneDepartureType(int id) {
            uId = id;
        }

        public static LaneDepartureType getType(int uId) {
            for (LaneDepartureType f : values()) {
                if (f.uId == uId) return f;
            }
            return NoDetected;
        }
    };

    public enum ForwardObjectTypes {
        NoObject (0),
        Car (3),
        Pedestrian(5),
        Rider(22),
        ;
        private int uId;

        private ForwardObjectTypes(int id) {
            uId = id;
        }

        public static ForwardObjectTypes getType(int uId) {
            for (ForwardObjectTypes f : values()) {
                if (f.uId == uId) return f;
            }
            return NoObject;
        }
    }

    public enum ForwardReactionTypes {
        Unknown(0),
        Remind (1),
        Warning (2),
        Urgent (3),
        ;
        private int uId;

        private ForwardReactionTypes(int id) {
            uId = id;
        }

        public static ForwardReactionTypes getType(int uId) {
            for (ForwardReactionTypes f : values()) {
                if (f.uId == uId) return f;
            }
            return Unknown;
        }
    }


    public enum UnitsOfMeasurement {
        Metric (0, "Km/h"),
        Imperial (1, "mph"),
        ;
        private int uId;
        private String unitString;

        private UnitsOfMeasurement(int id, String unit) {
            uId = id;
            unitString = unit;
        }

        public static UnitsOfMeasurement getType(int uId) {
            for (UnitsOfMeasurement f : values()) {
                if (f.uId == uId) return f;
            }
            return Metric;
        }

        public String getUnitString() {
            return unitString;
        }
    }

    public enum DirectionLightType {
        Left,
        Right,
        No;
    }

    public enum WeatherType {
        Unknown,
        Sun,
        Night,
        Raining,
    }

    private enum UI_InternalID {
        mID_Unknow(-1),

        mID_Custom_SpeedLimit (3);

        private int mIndex = -1;

        UI_InternalID(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }

        public static UI_InternalID getType(int id) {
            for (int i = 0; i < UI_InternalID.values().length; i++) {
                if (id == UI_InternalID.values()[i].getIndex()) {
                    return UI_InternalID.values()[i];
                }
            }

            return mID_Unknow;
        }
    };

    // Context
    private Context mContext = null;
    private ConstraintLayout mView = this;

    // UI Component
    private AppCompatTextView mUI_TextView_VehicleSpeed = null;
    private AppCompatTextView mUI_TextView_ReactionTime = null;
    private TextView mUI_TextView_ReactionTimeUnit = null;
    private TextView mUI_TextView_VehicleBusInfo = null;
    private ImageView mUI_ImageView_ForwardVehicle = null;
    private ImageView mUI_ImageView_SystemVehicle = null;
    private ImageView mUI_ImageView_Lane = null;
    private ImageView mUI_ImageView_DirectionLight_L = null;
    private ImageView mUI_ImageView_DirectionLight_R = null;
    private ImageView mUI_ImageView_Weather = null;
    private SpeedLimitView mUI_SpeedLimitView = null;
    private AppCompatTextView mUI_SpeedLimitViewUnit = null;
    private AppCompatTextView mUI_SpeedUnit = null;
    private AlphaAnimation mUI_AlphaAnimation_Lane;
    private AlphaAnimation mUI_AlphaAnimation_DirectionLight_L;
    private AlphaAnimation mUI_AlphaAnimation_DirectionLight_R;
    private Timer mTimer_SpeedLimitVisibility = null;

    // Data
    private boolean mVehicleBusConnected;
    private int mVehicleSpeedValue;
    private int mPrevSpeedLimitValue;
    private int mSpeedLimitValue;
    private volatile int mSpeedLimitDisplaySecond;
    private double mReactionTime;
    private ConciseStyleTypes mConciseStyleTypes;
    private LaneDepartureType mPrevLaneDepartureType;
    private LaneDepartureType mLaneDepartureType;
    private ForwardObjectTypes mForwardObjectType;
    private ForwardReactionTypes mForwardReactionType;
    private ForwardObjectTypes mPrevForwardObjectType;
    private ForwardReactionTypes mPrevForwardReactionType;
    private DirectionLightType mDirectionLightType;
    private boolean mVisibility_ReactionTime;
    private boolean mVisibility_SpeedLimit;
    private WeatherType mWeatherType;   // Sun(1), Night(2), Raining(4),
    private UnitsOfMeasurement mUintsOfMeasurment;

    // Flag
    private volatile boolean mFlag_UpdateVehicleBusConnection;
    private volatile boolean mFlag_UpdateSpeedLimit;
    private volatile boolean mFlag_UpdateVehicle;
    private volatile boolean mFlag_UpdateVehicleSpeed;
    private volatile boolean mFlag_UpdateLaneMarking;
    private volatile boolean mFlag_UpdateReactionTime;
    private volatile boolean mFlag_UpdateDirectionLight;
    private volatile boolean mFlag_UpdateWeather;

    // Functions
    public ConciseView(Context context) {
        super(context);
        init(context, null);
    }

    public ConciseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ConciseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        mContext = context;

        mVehicleBusConnected = false;
        mVehicleSpeedValue = 0;
        mSpeedLimitValue = 0;
        mSpeedLimitDisplaySecond = 0;
        mPrevSpeedLimitValue = 0;
        mReactionTime = 0.0f;
        mConciseStyleTypes = ConciseStyleTypes.TypeA;
        mLaneDepartureType = LaneDepartureType.NoDetected;
        mPrevLaneDepartureType = LaneDepartureType.Unknow;
        mForwardObjectType = ForwardObjectTypes.NoObject;
        mPrevForwardObjectType = ForwardObjectTypes.NoObject;
        mForwardReactionType = ForwardReactionTypes.Unknown;
        mPrevForwardReactionType = ForwardReactionTypes.Unknown;;
        mDirectionLightType = DirectionLightType.No;
        mWeatherType = WeatherType.Unknown;
        mUintsOfMeasurment = UnitsOfMeasurement.Metric;
        mFlag_UpdateSpeedLimit = true;
        mFlag_UpdateVehicle = true;
        mFlag_UpdateVehicleSpeed = true;
        mFlag_UpdateLaneMarking = true;
        mFlag_UpdateReactionTime = true;
        mFlag_UpdateDirectionLight = true;
        mFlag_UpdateVehicleBusConnection = true;
        mFlag_UpdateWeather = true;

        // parse attrs
        if(attrs != null) {
            TypedArray typeAry = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ConciseView,0, 0);
            if(typeAry != null) {
                try {
                    mVehicleBusConnected = typeAry.getBoolean(R.styleable.ConciseView_vehicle_bus_connected, false);
                    mVehicleSpeedValue = typeAry.getInteger(R.styleable.ConciseView_vehicle_speed, 0);
                    mSpeedLimitValue = typeAry.getInteger(R.styleable.ConciseView_speed_limit, 0);
                    mReactionTime = typeAry.getFloat(R.styleable.ConciseView_reaction_time, 0);
                    mConciseStyleTypes = ConciseStyleTypes.getType(typeAry.getInteger(R.styleable.ConciseView_concise_style, 0));
                    mLaneDepartureType = LaneDepartureType.getType(typeAry.getInteger(R.styleable.ConciseView_lane_departure_type, 0));
                    mVisibility_SpeedLimit = typeAry.getBoolean(R.styleable.ConciseView_speed_limit_visibility, false);
                    mVisibility_ReactionTime = typeAry.getBoolean(R.styleable.ConciseView_reaction_time_visibility, false);
                    mForwardObjectType = ForwardObjectTypes.getType(typeAry.getInteger(R.styleable.ConciseView_forward_object, 0));
                    mForwardReactionType = ForwardReactionTypes.getType(typeAry.getInteger(R.styleable.ConciseView_forward_reaction, 0));
                    mUintsOfMeasurment = UnitsOfMeasurement.getType(typeAry.getInteger(R.styleable.ConciseView_units_of_measurement, 0));
                } finally {
                    typeAry.recycle();
                }
            }
        }

        // inflate UI
        switch(mConciseStyleTypes) {
            case TypeA:
                inflate(context, R.layout.adas_conciseview_type_a, this);
                break;
            case TypeB:
                inflate(context, R.layout.adas_conciseview_type_b, this);
                break;
        }

        // dispose UI
        setupUI();

        refreshUI();
    }

    private void setupUI()
    {
        mUI_TextView_VehicleSpeed = this.findViewById(R.id.VehicleSpeed);
        mUI_TextView_ReactionTime = this.findViewById(R.id.ReactionTime);
        mUI_TextView_ReactionTimeUnit = this.findViewById(R.id.ReactopnTimeUnit);
        mUI_TextView_VehicleBusInfo = this.findViewById(R.id.VehicleBusInfo);
        mUI_ImageView_ForwardVehicle = (ImageView) this.findViewById(R.id.ForwardVehicleImage);
        mUI_ImageView_SystemVehicle = (ImageView) this.findViewById(R.id.SystemVehicleImage);
        mUI_ImageView_Lane = (ImageView) this.findViewById(R.id.LaneDetectionImage);
        mUI_SpeedLimitView = (SpeedLimitView) this.findViewById(R.id.SpeedLimit);
        mUI_SpeedLimitViewUnit = this.findViewById(R.id.SpeedLimitUnit);
        mUI_ImageView_DirectionLight_L = (ImageView) this.findViewById(R.id.DirectionLight_Left);
        mUI_ImageView_DirectionLight_R = (ImageView) this.findViewById(R.id.DirectionLight_Right);
        mUI_ImageView_Weather = (ImageView) this.findViewById(R.id.WeatherInfo);
        mUI_SpeedUnit = this.findViewById(R.id.SpeedUnit);

        // set Id
        if(mUI_SpeedLimitView != null) {
            mUI_SpeedLimitView.setId(UI_InternalID.mID_Custom_SpeedLimit.getIndex());
        }

        // config
        if(mUI_ImageView_ForwardVehicle != null) {
            mUI_ImageView_ForwardVehicle.setVisibility(INVISIBLE);
        }

        if(mUI_SpeedUnit != null) mUI_SpeedUnit.setText(mUintsOfMeasurment.getUnitString());
        if(mUI_SpeedLimitViewUnit != null) mUI_SpeedLimitViewUnit.setText(mUintsOfMeasurment.getUnitString());

        // config font
        AssetManager am = mContext.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am, String.format(Locale.US, "font/%s", "cursedtimerulil.ttf"));
        if(mUI_SpeedLimitViewUnit != null) mUI_SpeedLimitViewUnit.setTypeface(typeface);
        if(mUI_TextView_ReactionTime != null) mUI_TextView_ReactionTime.setTypeface(typeface);
        if(mUI_TextView_ReactionTimeUnit != null) mUI_TextView_ReactionTimeUnit.setTypeface(typeface);
        if(mUI_SpeedUnit != null) mUI_SpeedUnit.setTypeface(typeface);
        if(mUI_TextView_VehicleSpeed != null) mUI_TextView_VehicleSpeed.setTypeface(typeface);


    }

    private void refreshUI()
    {
        // Vehicle Bus Connect status
        if(mFlag_UpdateVehicleBusConnection) {
            if(mUI_TextView_VehicleBusInfo != null) {
                if (mVehicleBusConnected) {
                    mUI_TextView_VehicleBusInfo.setVisibility(INVISIBLE);
                } else {
                    mUI_TextView_VehicleBusInfo.setVisibility(VISIBLE);
                }
            }
            mFlag_UpdateVehicleBusConnection = false;
        }

        // Lane Departure WarningVehicle
        if(mFlag_UpdateLaneMarking) {
            Guideline vehicleGuideline_L = (Guideline) findViewById(R.id.vSystemVehicle_Left);
            Guideline vehicleGuideline_R = (Guideline) findViewById(R.id.vSystemVehicle_Right);
            if(mUI_ImageView_Lane != null && vehicleGuideline_L != null && vehicleGuideline_R != null) {
                ConstraintLayout.LayoutParams vehicleGuidelineParam_L = (ConstraintLayout.LayoutParams) vehicleGuideline_L.getLayoutParams();
                ConstraintLayout.LayoutParams vehicleGuidelineParam_R = (ConstraintLayout.LayoutParams) vehicleGuideline_R.getLayoutParams();

                // Update image
                if (mLaneDepartureType != mPrevLaneDepartureType) {
                    switch (mLaneDepartureType) {
                        case NoDetected:
                            mUI_ImageView_Lane.setImageResource(R.drawable.adas_conciseview_lane_no_detected);
                            vehicleGuidelineParam_L.guidePercent = 0.28f;
                            vehicleGuidelineParam_R.guidePercent = 0.72f;
                            vehicleGuideline_L.setLayoutParams(vehicleGuidelineParam_L);
                            vehicleGuideline_R.setLayoutParams(vehicleGuidelineParam_R);
                            break;
                        case Regular:
                            mUI_ImageView_Lane.setImageResource(R.drawable.adas_conciseview_lane_detected);
                            vehicleGuidelineParam_L.guidePercent = 0.28f;
                            vehicleGuidelineParam_R.guidePercent = 0.72f;
                            vehicleGuideline_L.setLayoutParams(vehicleGuidelineParam_L);
                            vehicleGuideline_R.setLayoutParams(vehicleGuidelineParam_R);
                            break;
                        case Departure_Left:
                            mUI_ImageView_Lane.setImageResource(R.drawable.adas_conciseview_lane_left_departure);
                            vehicleGuidelineParam_L.guidePercent = 0.28f;
                            vehicleGuidelineParam_R.guidePercent = 0.5f;
                            vehicleGuideline_L.setLayoutParams(vehicleGuidelineParam_L);
                            vehicleGuideline_R.setLayoutParams(vehicleGuidelineParam_R);
                            break;
                        case Departure_Right:
                            mUI_ImageView_Lane.setImageResource(R.drawable.adas_conciseview_lane_right_departure);
                            vehicleGuidelineParam_L.guidePercent = 0.5f;
                            vehicleGuidelineParam_R.guidePercent = 0.72f;
                            vehicleGuideline_L.setLayoutParams(vehicleGuidelineParam_L);
                            vehicleGuideline_R.setLayoutParams(vehicleGuidelineParam_R);
                            break;
                    }
                }
                mPrevLaneDepartureType = mLaneDepartureType;

                // process animation
                if (mLaneDepartureType == LaneDepartureType.Departure_Left || mLaneDepartureType == LaneDepartureType.Departure_Right) {
                    if (mUI_AlphaAnimation_Lane == null || mUI_AlphaAnimation_Lane.hasEnded()) {
                        mUI_ImageView_Lane.clearAnimation();
                        mUI_AlphaAnimation_Lane = new AlphaAnimation(1.0f, 0.1f);
                        mUI_AlphaAnimation_Lane.setDuration(150);
                        mUI_AlphaAnimation_Lane.setRepeatCount(4);
                        mUI_AlphaAnimation_Lane.setRepeatMode(Animation.RESTART);
                        mUI_ImageView_Lane.setAnimation(mUI_AlphaAnimation_Lane);
                    }
                    if (!mUI_AlphaAnimation_Lane.hasStarted()) {
                        mUI_AlphaAnimation_Lane.start();
                    }
                }
            }
            mFlag_UpdateLaneMarking = false;
        }

        // Vehicle WarningVehicle Type
        if(mFlag_UpdateVehicle) {
            if(mUI_ImageView_ForwardVehicle != null) {
                if ((mForwardObjectType != mPrevForwardObjectType) || (mForwardReactionType != mPrevForwardReactionType)) {
                    switch (mForwardObjectType) {
                        case Car:
                            mUI_ImageView_ForwardVehicle.setVisibility(VISIBLE);
                            switch (mForwardReactionType) {
                                case Urgent:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_vehicle_urgent);
                                    break;
                                case Warning:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_vehicle_warning);
                                    break;
                                default:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_vehicle_normal);
                                    break;
                            }
                            break;
                        case Pedestrian:
                            mUI_ImageView_ForwardVehicle.setVisibility(VISIBLE);
                            switch (mForwardReactionType) {
                                case Urgent:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_pedestrian_urgent);
                                    break;
                                case Warning:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_pedestrian_warning);
                                    break;
                                default:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_pedestrian_normal);
                                    break;
                            }
                            break;
                        case Rider:
                            mUI_ImageView_ForwardVehicle.setVisibility(VISIBLE);
                            switch (mForwardReactionType) {
                                case Urgent:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_motorcycle_urgent);
                                    break;
                                case Warning:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_motorcycle_warning);
                                    break;
                                default:
                                    mUI_ImageView_ForwardVehicle.setImageResource(R.drawable.adas_conciseview_forward_motorcycle_normal);
                                    break;
                            }
                            break;
                        case NoObject:
                        default:
                            mUI_ImageView_ForwardVehicle.setVisibility(INVISIBLE);
                            break;
                    }
                }
            }
            mPrevForwardObjectType = mForwardObjectType;
            mPrevForwardReactionType = mForwardReactionType;
            mFlag_UpdateVehicle = false;
        }

        // Reaction Time WarningVehicle
        if(mFlag_UpdateReactionTime) {
            if(mUI_TextView_ReactionTime != null && mUI_TextView_ReactionTimeUnit != null) {
                switch (mForwardReactionType) {
                    case Remind:
                    case Unknown:
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(0, 176, 80));
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(0, 176, 80));
                        break;
                    case Warning:
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(255, 125, 0));
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(255, 125, 0));
                        break;
                    case Urgent:
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(255, 0, 0));
                        mUI_TextView_ReactionTime.setTextColor(Color.rgb(255, 0, 0));
                        break;
                }

                mUI_TextView_ReactionTime.setText(String.format("%.1f", mReactionTime));
                if (mVisibility_ReactionTime) {
                    mUI_TextView_ReactionTime.setVisibility(VISIBLE);
                    //mUI_TextView_ReactionTimeUnit.setVisibility(VISIBLE);
                    //mUI_TextView_ReactionTimeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, mUI_TextView_ReactionTime.getTextSize() *0.7f);
                } else {
                    mUI_TextView_ReactionTime.setVisibility(GONE);
                    //mUI_TextView_ReactionTimeUnit.setVisibility(GONE);
                }
            }
            mFlag_UpdateReactionTime = false;
        }

        // Vehicle Speed
        if(mFlag_UpdateVehicleSpeed) {
            if(mUI_TextView_VehicleSpeed != null) {
                mUI_TextView_VehicleSpeed.setText(Integer.toString(mVehicleSpeedValue));
            }
            mFlag_UpdateVehicleSpeed = false;
        }

        // Speed Limit
        if(mFlag_UpdateSpeedLimit) {
           if(mUI_SpeedLimitView != null && mUI_SpeedLimitViewUnit != null) {
                mUI_SpeedLimitView.setSpeedLimitValue(mSpeedLimitValue);
                mPrevSpeedLimitValue = mSpeedLimitValue;

                if (mVisibility_SpeedLimit) {
                    mUI_SpeedLimitView.setVisibility(VISIBLE);
                    mUI_SpeedLimitViewUnit.setVisibility(VISIBLE);
                } else {
                    mUI_SpeedLimitView.setVisibility(GONE);
                    mUI_SpeedLimitViewUnit.setVisibility(GONE);
                }
            }
            mFlag_UpdateSpeedLimit = false;
        }

        // Direction Light
        if(mFlag_UpdateDirectionLight) {
            if (mUI_ImageView_DirectionLight_L != null && mUI_ImageView_DirectionLight_R != null) {
                switch (mDirectionLightType) {
                    case No:
                        if (mUI_AlphaAnimation_DirectionLight_L != null && mUI_AlphaAnimation_DirectionLight_L.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_L.cancel();
                            mUI_ImageView_DirectionLight_L.clearAnimation();
                        }
                        if (mUI_AlphaAnimation_DirectionLight_R != null && mUI_AlphaAnimation_DirectionLight_R.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_R.cancel();
                            mUI_ImageView_DirectionLight_R.clearAnimation();
                        }
                        mUI_ImageView_DirectionLight_L.setVisibility(INVISIBLE);
                        mUI_ImageView_DirectionLight_R.setVisibility(INVISIBLE);
                        break;
                    case Left:
                        if (mUI_AlphaAnimation_DirectionLight_L == null || mUI_AlphaAnimation_DirectionLight_L.hasEnded()) {
                            mUI_AlphaAnimation_DirectionLight_L = new AlphaAnimation(1.0f, 0.3f);
                            mUI_AlphaAnimation_DirectionLight_L.setDuration(150);
                            mUI_AlphaAnimation_DirectionLight_L.setRepeatCount(2);
                            mUI_AlphaAnimation_DirectionLight_L.setRepeatMode(Animation.RESTART);
                            if (mUI_ImageView_DirectionLight_L != null) {
                                mUI_ImageView_DirectionLight_L.clearAnimation();
                                mUI_ImageView_DirectionLight_L.setAnimation(mUI_AlphaAnimation_DirectionLight_L);
                            }
                        }
                        if (!mUI_AlphaAnimation_DirectionLight_L.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_L.start();
                        }
                        if (mUI_AlphaAnimation_DirectionLight_R != null && mUI_AlphaAnimation_DirectionLight_R.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_R.cancel();
                            mUI_ImageView_DirectionLight_R.clearAnimation();

                        }
                        mUI_ImageView_DirectionLight_L.setVisibility(VISIBLE);
                        mUI_ImageView_DirectionLight_R.setVisibility(INVISIBLE);
                        break;
                    case Right:
                        if (mUI_AlphaAnimation_DirectionLight_R == null || mUI_AlphaAnimation_DirectionLight_R.hasEnded()) {
                            mUI_AlphaAnimation_DirectionLight_R = new AlphaAnimation(1.0f, 0.3f);
                            mUI_AlphaAnimation_DirectionLight_R.setDuration(150);
                            mUI_AlphaAnimation_DirectionLight_R.setRepeatCount(2);
                            mUI_AlphaAnimation_DirectionLight_R.setRepeatMode(Animation.RESTART);
                            if (mUI_ImageView_DirectionLight_R != null) {
                                mUI_ImageView_DirectionLight_R.clearAnimation();
                                mUI_ImageView_DirectionLight_R.setAnimation(mUI_AlphaAnimation_DirectionLight_R);
                            }
                        }
                        if (!mUI_AlphaAnimation_DirectionLight_R.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_R.start();
                        }
                        if (mUI_AlphaAnimation_DirectionLight_L != null && mUI_AlphaAnimation_DirectionLight_L.hasStarted()) {
                            mUI_AlphaAnimation_DirectionLight_L.cancel();
                            mUI_ImageView_DirectionLight_L.clearAnimation();
                        }
                        mUI_ImageView_DirectionLight_L.setVisibility(INVISIBLE);
                        mUI_ImageView_DirectionLight_R.setVisibility(VISIBLE);
                        break;
                }
            }
            mFlag_UpdateDirectionLight = false;
        }

        if(mFlag_UpdateWeather) {
            if(mUI_ImageView_Weather != null) {
                switch (mWeatherType) {
                    case Sun:
                        mUI_ImageView_Weather.setImageResource(R.drawable.weather_sun);
                        break;
                    case Night:
                        mUI_ImageView_Weather.setImageResource(R.drawable.weather_night);
                        break;
                    case Raining:
                        // TODO : Add raining
                        break;
                    default:
                        mUI_ImageView_Weather.setImageResource(R.drawable.weather_sun);
                        break;
                }
            }
            mFlag_UpdateWeather = false;
        }
    }

    private void postRefreshUI()
    {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        });
    }

    public void setWeatherInfo(WeatherType wtype)
    {
        if(mFlag_UpdateWeather == false) {
            mWeatherType = wtype;
            mFlag_UpdateWeather = true;
            postRefreshUI();
        }
    }

    public void setVehicleBusConnection(boolean isConnect)
    {
        if(mFlag_UpdateVehicleBusConnection == false) {
            mVehicleBusConnected = isConnect;
            mFlag_UpdateVehicleBusConnection = true;
            postRefreshUI();
        }
    }
    // the units of "Set Vehicle Speed" is using km/h, it will convert to mph if needed
    public void setVehicleSpeed(int vehicleSpeed)
    {
        if(mFlag_UpdateVehicleSpeed == false) {
            mVehicleSpeedValue = vehicleSpeed;
            if(mUintsOfMeasurment.equals(UnitsOfMeasurement.Imperial)) {
                mVehicleSpeedValue = (int)(vehicleSpeed*0.621371);
            }

            mFlag_UpdateVehicleSpeed = true;
            postRefreshUI();
        }
    }

    public void setForwardObjectStatus(ForwardObjectTypes objType, ForwardReactionTypes reactionType, double reactionTime, boolean showReactionTime)
    {
        if(mFlag_UpdateVehicle == false && mFlag_UpdateReactionTime == false) {
            mForwardObjectType = objType;
            mForwardReactionType = reactionType;
            mReactionTime = reactionTime;
            mVisibility_ReactionTime = showReactionTime;
            mFlag_UpdateVehicle = true;
            mFlag_UpdateReactionTime = true;
            postRefreshUI();
        }
    }

    public void setLaneDepartureWarning(LaneDepartureType laneDepartureType)
    {
        if(mFlag_UpdateLaneMarking == false) {
            mLaneDepartureType = laneDepartureType;
            mFlag_UpdateLaneMarking = true;
            postRefreshUI();
        }
    }

    public void setSpeedLimitValue(int speedLimit, int displaySecond, boolean visibility)
    {
        if(mFlag_UpdateSpeedLimit == false) {
            mFlag_UpdateSpeedLimit = true;
            mSpeedLimitValue = speedLimit;
            mSpeedLimitDisplaySecond = displaySecond;
            mVisibility_SpeedLimit = visibility;

            if(mTimer_SpeedLimitVisibility == null) {
                mTimer_SpeedLimitVisibility = new Timer(true);
                mTimer_SpeedLimitVisibility.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(mSpeedLimitDisplaySecond <= 0) {
                            mVisibility_SpeedLimit = false;
                            mSpeedLimitDisplaySecond = 0;
                        }
                        else {
                            mSpeedLimitDisplaySecond--;
                            mVisibility_SpeedLimit = true;
                        }
                        mFlag_UpdateSpeedLimit = true;
                        postRefreshUI();
                    }
                }, 0, 1000);

            }

            postRefreshUI();
        }
    }

    public void setDirectionLight(DirectionLightType type)
    {
        if(mFlag_UpdateDirectionLight == false) {
            mDirectionLightType = type;
            mFlag_UpdateDirectionLight = true;
            postRefreshUI();
        }
    }

    public void setTypeface(final Typeface type) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mUI_TextView_VehicleBusInfo != null) mUI_TextView_VehicleBusInfo.setTypeface(type);
                if(mUI_SpeedLimitViewUnit != null) mUI_SpeedLimitViewUnit.setTypeface(type);
                if(mUI_TextView_VehicleSpeed != null) mUI_TextView_VehicleSpeed.setTypeface(type);
                if(mUI_TextView_ReactionTime != null) mUI_TextView_ReactionTime.setTypeface(type);
                if(mUI_TextView_ReactionTimeUnit != null) mUI_TextView_ReactionTimeUnit.setTypeface(type);
            }
        });
    }

    public void setUnitsOfMeasurement(final UnitsOfMeasurement unit) {
        mUintsOfMeasurment = unit;
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mUI_SpeedUnit != null) mUI_SpeedUnit.setText(mUintsOfMeasurment.getUnitString());
                if(mUI_SpeedLimitViewUnit != null) mUI_SpeedLimitViewUnit.setText(mUintsOfMeasurment.getUnitString());
            }
        });
    }

    public boolean getVehicleBusConnectionStatus()
    {
        return mVehicleBusConnected;
    }

    public int getVehicleSpeedValue()
    {
        return mVehicleSpeedValue;
    }

    public double getReactionTime()
    {
        return mReactionTime;
    }

    public ForwardReactionTypes getForwardReactionType()
    {
        return mForwardReactionType;
    }

    public ForwardObjectTypes getForwardObjectType()
    {
        return mForwardObjectType;
    }

    public boolean getReactionTimeVisibility()
    {
        return mVisibility_ReactionTime;
    }

    public LaneDepartureType getLaneDepartureType()
    {
        return mLaneDepartureType;
    }

    public int getSpeedLimitValue()
    {
        return mSpeedLimitValue;
    }

    public  boolean getSpeedLimitVisibility()
    {
        return mVisibility_SpeedLimit;
    }

    public DirectionLightType getDirectionLightType()
    {
        return mDirectionLightType;
    }
}

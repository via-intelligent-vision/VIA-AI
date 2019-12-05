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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import android.util.SparseArray;

import com.viatech.automotive.LatitudePlanner.LatitudePlan;
import com.viatech.sensing.SensingSamples;
import com.viatech.vBus.CANbusData;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by hankwu on 3/23/17.
 */

public class DrawSensingSamples {
    private static Bitmap mWatermark = null;
    private static SparseArray<String> mLabelIndex = null;

    private static Bitmap mIcon_LockonRegular_OutSide = null;
    private static Bitmap mIcon_LockonRegular_InSide = null;
    private static Bitmap mIcon_LockonWarning_OutSide = null;
    private static Bitmap mIcon_LockonWarning_InSide = null;
    private static Bitmap mIcon_AccLockonOutSide_1 = null;
    private static Bitmap mIcon_AccLockonOutSide_2 = null;
    private static Bitmap mIcon_AccLockonOutSide_3 = null;
    private static Bitmap mIcon_AccLockonInSide = null;
    private static Bitmap mIcon_Steering = null;
    private static Bitmap mIcon_Warning = null;


    public static void loadData(Context context) {
        releaseData();

        if (mWatermark == null) {
            mWatermark = BitmapFactory.decodeResource(context.getResources(), R.drawable.via_logo);
        }

        if (mIcon_Warning == null) {
            mIcon_Warning = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_fcw_warning);
        }

        if (mIcon_LockonRegular_OutSide == null) {
            mIcon_LockonRegular_OutSide = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_lockon_regular_outside);
        }

        if (mIcon_LockonRegular_InSide == null) {
            mIcon_LockonRegular_InSide = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_lockon_regular_inside);
        }

        if (mIcon_LockonWarning_OutSide == null) {
            mIcon_LockonWarning_OutSide = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_lockon_warning_outside);
        }

        if (mIcon_LockonWarning_InSide == null) {
            mIcon_LockonWarning_InSide = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_lockon_warning_inside);
        }

        if (mIcon_AccLockonOutSide_1 == null) {
            mIcon_AccLockonOutSide_1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_acc_lockon_outside_1);
        }

        if (mIcon_AccLockonOutSide_2 == null) {
            mIcon_AccLockonOutSide_2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_acc_lockon_outside_2);
        }

        if (mIcon_AccLockonOutSide_3 == null) {
            mIcon_AccLockonOutSide_3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_acc_lockon_outside_3);
        }

        if (mIcon_AccLockonInSide == null) {
            mIcon_AccLockonInSide = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_acc_lockon_inside);
        }

        if (mIcon_Steering == null) {
            mIcon_Steering = BitmapFactory.decodeResource(context.getResources(), R.drawable.steering);
        }

    }

    public static void releaseData() {
        if (mWatermark != null) {
            mWatermark.recycle();
            mWatermark = null;
        }

        if (mIcon_Warning != null) {
            mIcon_Warning.recycle();
            mIcon_Warning = null;
        }

        if (mIcon_LockonWarning_OutSide != null) {
            mIcon_LockonWarning_OutSide.recycle();
            mIcon_LockonWarning_OutSide = null;
        }

        if (mIcon_LockonWarning_InSide != null) {
            mIcon_LockonWarning_InSide.recycle();
            mIcon_LockonWarning_InSide = null;
        }

        if (mIcon_LockonRegular_OutSide != null) {
            mIcon_LockonRegular_OutSide.recycle();
            mIcon_LockonRegular_OutSide = null;
        }

        if (mIcon_LockonRegular_InSide != null) {
            mIcon_LockonRegular_InSide.recycle();
            mIcon_LockonRegular_InSide = null;
        }

        if (mIcon_AccLockonOutSide_1 != null) {
            mIcon_AccLockonOutSide_1.recycle();
            mIcon_AccLockonOutSide_1 = null;
        }

        if (mIcon_AccLockonInSide != null) {
            mIcon_AccLockonInSide.recycle();
            mIcon_AccLockonInSide = null;
        }

        if (mIcon_Steering != null) {
            mIcon_Steering.recycle();
            mIcon_Steering = null;
        }
    }

    public static void drawSensingSample_LDWS(Canvas canvas, SensingSamples.LaneDetectSample sample)
    {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        Paint paint = new Paint();

        if (sample.mLaneStatus != SensingSamples.LaneDetectSample.SampleStatus.Unknown && sample.mLaneStatus !=  SensingSamples.LaneDetectSample.SampleStatus.NoDetected) {
            final int hP = SensingSamples.LaneDetectSample.RESAMPLE_COUNT / 2;
            final int eP = SensingSamples.LaneDetectSample.RESAMPLE_COUNT -1;
            float anchor0x_L = sample.mLaneAnchor_L[0].x * canvasWidth;
            float anchor0y_L = sample.mLaneAnchor_L[0].y * canvasHeight;
            float anchor2x_L = sample.mLaneAnchor_L[eP].x * canvasWidth;
            float anchor2y_L = sample.mLaneAnchor_L[eP].y * canvasHeight;
            float anchor0x_R = sample.mLaneAnchor_L[0].x * canvasWidth;
            float anchor0y_R = sample.mLaneAnchor_L[1].y * canvasHeight;
            float anchor2x_R = sample.mLaneAnchor_L[eP].x * canvasWidth;
            float anchor2y_R = sample.mLaneAnchor_L[eP].y * canvasHeight;

            float x_down = (anchor2x_L + anchor2x_R) / 2;
            float y_down = (anchor2y_L + anchor2y_R) / 2;
            float x_up = (anchor0x_L + anchor0x_R) / 2;
            float y_up = (anchor0y_L + anchor0y_R) / 2;

            int start_color, end_color;
            switch (sample.mLaneStatus) {
                case LeftDeparture: //left departure
                    start_color = Color.argb(70, 255, 255, 0);
                    end_color = Color.argb(0, 255, 255, 0);
                    break;
                case RightDeparture: // right departure
                    start_color = Color.argb(70, 255, 255, 0);
                    end_color = Color.argb(0, 255, 255, 0);
                    break;
                case Calibrating:
                    start_color = Color.argb(70, 255, 0, 0);
                    end_color = Color.argb(0, 255, 0, 0);
                    break;
                case Detected:
                    start_color = Color.argb(70, 0, 0, 255);
                    end_color = Color.argb(0, 0, 0, 255);
                    break;
                default:
                    start_color = Color.argb(70, 160, 160, 160);
                    end_color = Color.argb(0, 160, 160, 160);
                    break;
            }

            LinearGradient linearGradient;
            linearGradient = new LinearGradient(x_down, y_down, x_up, y_up, start_color, end_color, Shader.TileMode.CLAMP);
            //paint.setShader(linearGradient);
            paint.setColor(start_color);


            Path path1 = new Path();
            path1.moveTo(anchor0x_L, anchor0y_L);
            for(int i = 0 ; i < SensingSamples.LaneDetectSample.RESAMPLE_COUNT; i++) {
                float anchorx = sample.mLaneAnchor_L[i].x * canvasWidth;
                float anchory = sample.mLaneAnchor_L[i].y * canvasHeight;
                path1.lineTo(anchorx, anchory);
            }
            for(int i = SensingSamples.LaneDetectSample.RESAMPLE_COUNT  -1 ; i >= 0; i--) {
                float anchorx = sample.mLaneAnchor_R[i].x * canvasWidth;
                float anchory = sample.mLaneAnchor_R[i].y * canvasHeight;
                path1.lineTo(anchorx, anchory);
            }
            path1.lineTo(anchor0x_L, anchor0y_L);
            path1.close();
            canvas.drawPath(path1, paint);
        }

        // Draw two lines, 70m for FCWS, 20 m for LDWS
        boolean isVerificationMode = false;
        if (isVerificationMode) {
            Paint paint_70m = new Paint();
            float anchor0x_L_70m = 0f * canvasWidth;
            float anchor0y_L_70m = 0.288052f * canvasHeight;
            float anchor0x_R_70m = 1f * canvasWidth;
            float anchor0y_R_70m = 0.288052f * canvasHeight;

            paint_70m.setARGB(200, 0, 255, 0);
            paint_70m.setStrokeWidth(10.0f);

            Path path_70m = new Path();
            path_70m.moveTo(anchor0x_L_70m, anchor0y_L_70m);
            path_70m.lineTo(anchor0x_R_70m, anchor0y_R_70m);

            path_70m.close();
            canvas.drawPath(path_70m, paint_70m);
            canvas.drawLine(anchor0x_L_70m, anchor0y_L_70m, anchor0x_R_70m, anchor0y_R_70m, paint_70m);

            Paint paint_20m = new Paint();
            float anchor0x_L_20m = 0f * canvasWidth;
            float anchor0y_L_20m = 0.403941f * canvasHeight;
            float anchor0x_R_20m = 1f * canvasWidth;
            float anchor0y_R_20m = 0.403941f * canvasHeight;

            paint_20m.setARGB(200, 255, 0, 0);
            paint_20m.setStrokeWidth(10.0f);

            Path path1_20m = new Path();
            path1_20m.moveTo(anchor0x_L_20m, anchor0y_L_20m);
            path1_20m.lineTo(anchor0x_R_20m, anchor0y_R_20m);

            path1_20m.close();
            canvas.drawPath(path1_20m, paint_20m);
            canvas.drawLine(anchor0x_L_20m, anchor0y_L_20m, anchor0x_R_20m, anchor0y_R_20m, paint_20m);
        }
    }

    private static float RR = 0;
    public static void drawSensingSample_FCWS(Canvas canvas, SensingSamples.VehicleDetectSample sample, double reactionTime)
    {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        if (sample.mForwardVehicle.mScore > 0) {
            boolean isAccMode = Preferences.getInstance().getAdaptiveCruiseControlData().isEnable();
            //int accDistance = Preferences.getInstance().getAdaptiveCruiseControlData().notifyAll();
            int accDistance = 0;
            float car0_x0 = sample.mForwardVehicle.mMinAnchor[0]  * canvasWidth;
            float car0_y0 = sample.mForwardVehicle.mMinAnchor[1] * canvasHeight;
            float car0_x1 = sample.mForwardVehicle.mMaxAnchor[0]  * canvasWidth;
            float car0_y1 = sample.mForwardVehicle.mMaxAnchor[1] * canvasHeight;
            float distance = sample.mForwardVehicle.mDistance;

            float len = Math.abs(car0_x0 - car0_x1);
            RectF rectCar = new RectF(car0_x0 - len * 0.2f, car0_y0 - len * 1.3f, car0_x0 + len * 1.2f, car0_y0);

            // Draw indicator
            do {
                int start_color;
                int end_color;
                if (reactionTime < 1.0f) {
                    start_color = Color.argb(255, 255, 255, 0);
                    end_color = Color.argb(0, 255, 255, 0);
                } else {
                    start_color = Color.argb(255, 0, 255, 0);
                    end_color = Color.argb(0, 0, 255, 0);
                }

                // Draw circle
                boolean bitmapIsReady = (mIcon_LockonRegular_InSide != null && !mIcon_LockonRegular_InSide.isRecycled()) &&
                        (mIcon_LockonRegular_OutSide != null && !mIcon_LockonRegular_OutSide.isRecycled()) &&
                        (mIcon_AccLockonInSide != null && !mIcon_AccLockonInSide.isRecycled()) &&
                        (mIcon_AccLockonOutSide_1 != null && !mIcon_AccLockonOutSide_1.isRecycled());
                if (bitmapIsReady) {
                    Matrix matrix = new Matrix();
                    Bitmap inside;
                    Bitmap outside;
                    float imgScale;
                    if(isAccMode) {
                        inside = mIcon_AccLockonInSide;
                        outside = mIcon_AccLockonOutSide_1;
                        imgScale = 1.6f;
                    }
                    else {
                        if(reactionTime > Preferences.getInstance().getForwardCollisionWarningData().getAlertReactionTime()) {
                            inside = mIcon_LockonRegular_InSide;
                            outside = mIcon_LockonRegular_OutSide;
                            imgScale = 1.3f;
                        }
                        else {
                            inside = mIcon_LockonWarning_InSide;
                            outside = mIcon_LockonWarning_OutSide;
                            imgScale = 1.3f;
                        }
                    }


                    float scaleFactor = (len/ (float) inside.getWidth()) * imgScale;
                    matrix.setScale(scaleFactor, scaleFactor);
                    matrix.postRotate(RR, inside.getWidth() *0.5f * scaleFactor, inside.getHeight() * 0.5f * scaleFactor);
                    matrix.postTranslate(rectCar.centerX() - inside.getWidth() *0.5f * scaleFactor, rectCar.centerY() - inside.getHeight() * 0.25f * scaleFactor);

                    Paint paint = new Paint();
                    paint.setAlpha(160);
                    canvas.drawBitmap(inside, matrix, paint);

                    scaleFactor = (len / (float) outside.getWidth()) * imgScale;
                    matrix.reset();
                    matrix.setScale(scaleFactor, scaleFactor);
                    matrix.postRotate(-RR * 0.75f, outside.getWidth() *0.5f * scaleFactor, outside.getHeight() * 0.5f * scaleFactor);
                    matrix.postTranslate(rectCar.centerX() - outside.getWidth() *0.5f * scaleFactor, rectCar.centerY() - outside.getHeight() * 0.25f * scaleFactor);

                    paint = new Paint();
                    paint.setAlpha(160);
                    canvas.drawBitmap(outside, matrix, paint);


                    if(accDistance > 1) {
                        scaleFactor = (len / (float) mIcon_AccLockonOutSide_2.getWidth()) * imgScale;
                        matrix.reset();
                        matrix.setScale(scaleFactor, scaleFactor);
                        matrix.postRotate(-RR * 0.5f, mIcon_AccLockonOutSide_2.getWidth() * 0.5f * scaleFactor, mIcon_AccLockonOutSide_2.getHeight() * 0.5f * scaleFactor);
                        matrix.postTranslate(rectCar.centerX() - mIcon_AccLockonOutSide_2.getWidth() * 0.5f * scaleFactor, rectCar.centerY() - mIcon_AccLockonOutSide_2.getHeight() * 0.25f * scaleFactor);

                        paint = new Paint();
                        paint.setAlpha(160);
                        canvas.drawBitmap(mIcon_AccLockonOutSide_2, matrix, paint);
                    }

                    if(accDistance > 2) {
                        scaleFactor = (len / (float) mIcon_AccLockonOutSide_3.getWidth()) * imgScale;
                        matrix.reset();
                        matrix.setScale(scaleFactor, scaleFactor);
                        matrix.postRotate(-RR * 1.5f, mIcon_AccLockonOutSide_3.getWidth() * 0.5f * scaleFactor, mIcon_AccLockonOutSide_3.getHeight() * 0.5f * scaleFactor);
                        matrix.postTranslate(rectCar.centerX() - mIcon_AccLockonOutSide_3.getWidth() * 0.5f * scaleFactor, rectCar.centerY() - mIcon_AccLockonOutSide_3.getHeight() * 0.25f * scaleFactor);
                        canvas.drawBitmap(mIcon_AccLockonOutSide_3, matrix, paint);
                    }
                    RR += 15;

                    if(!isAccMode && reactionTime <= Preferences.getInstance().getForwardCollisionWarningData().getAlertReactionTime()) {
                        scaleFactor = (len/ (float) mIcon_Warning.getWidth()) * 0.5f;
                        matrix.setScale(scaleFactor, scaleFactor);
                        matrix.postTranslate(rectCar.centerX() - mIcon_Warning.getWidth() *0.5f * scaleFactor, rectCar.centerY());
                        canvas.drawBitmap(mIcon_Warning, matrix, paint);
                    }
                }

                // Draw line
                    /*Paint indicatorPaint = new Paint();
                    indicatorPaint.setStrokeWidth(5);
                    indicatorPaint.setColor(start_color);
                    canvas.drawLine(car0_x0, car0_y0, car0_x1, car0_y1, indicatorPaint);*/
            } while (false);


//            do {
//                String text = String.format("%.1f", distance) + " m / " + String.format("%.1f", reactionTime) + " s";
//                float textSize = canvasWidth *0.03f;
//                float textLen = textSize * text.length() * 0.5f;
//                TextPaint  distancePaint = new TextPaint();
//                distancePaint.setTextSize(canvasWidth *0.03f);
//                distancePaint.setStrokeWidth(5);
//                distancePaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
//                if(isAccMode)
//                    distancePaint.setColor(Color.argb(200, 255, 220, 100));
//                else
//                    distancePaint.setColor(Color.argb(200, 100, 255, 100));
//
//                canvas.drawText(text, rectCar.centerX() - textLen * 0.5f, rectCar.bottom + canvasWidth *0.03f* 1.3f, distancePaint);
//            } while (false);

        }
    }

    private static boolean b_showCalibrationHint = false;
    public static void showCalibrationHint(boolean show) {
        b_showCalibrationHint = show;
    }

    public static void drawSymbol(Canvas canvas)
    {
        if(!b_showCalibrationHint) return;
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();


        int accDistance = 0;

        float len = canvasWidth * 0.3f;
        //RectF rectCar = new RectF(canvasWidth * 0.85f, canvasWidth * -0.1f, canvasWidth * 0.15f, canvasWidth * 0.1f);
        PointF rectCar = new PointF(canvasWidth * 0.91f, canvasHeight * 0.01f);

        // Draw indicator
        do {

            // Draw circle
            boolean bitmapIsReady = (mIcon_LockonRegular_InSide != null && !mIcon_LockonRegular_InSide.isRecycled()) &&
                    (mIcon_LockonRegular_OutSide != null && !mIcon_LockonRegular_OutSide.isRecycled()) &&
                    (mIcon_AccLockonInSide != null && !mIcon_AccLockonInSide.isRecycled()) &&
                    (mIcon_AccLockonOutSide_1 != null && !mIcon_AccLockonOutSide_1.isRecycled());
            if (bitmapIsReady) {
                Matrix matrix = new Matrix();
                Bitmap inside= mIcon_LockonWarning_InSide;
                Bitmap outside= mIcon_LockonWarning_OutSide;

                float scaleFactor = (len/ (float) inside.getWidth());
                matrix.setScale(scaleFactor, scaleFactor);
                matrix.postRotate(RR, inside.getWidth() *0.5f * scaleFactor, inside.getHeight() * 0.5f * scaleFactor);
                matrix.postTranslate(rectCar.x - inside.getWidth() *0.5f * scaleFactor, rectCar.y - inside.getHeight() * 0.5f * scaleFactor);

                Paint paint = new Paint();
                paint.setAlpha(160);
                canvas.drawBitmap(inside, matrix, paint);

                scaleFactor = (len / (float) outside.getWidth());
                matrix.reset();
                matrix.setScale(scaleFactor, scaleFactor);
                matrix.postRotate(-RR * 0.75f, outside.getWidth() *0.5f * scaleFactor, outside.getHeight() * 0.5f * scaleFactor);
                matrix.postTranslate(rectCar.x - outside.getWidth() *0.5f * scaleFactor, rectCar.y - outside.getHeight() * 0.5f * scaleFactor);

                paint = new Paint();
                paint.setAlpha(160);
                canvas.drawBitmap(outside, matrix, paint);


                if(accDistance > 1) {
                    scaleFactor = (len / (float) mIcon_AccLockonOutSide_1.getWidth());
                    matrix.reset();
                    matrix.setScale(scaleFactor, scaleFactor);
                    matrix.postRotate(-RR * 0.5f, mIcon_AccLockonOutSide_1.getWidth() * 0.5f * scaleFactor, mIcon_AccLockonOutSide_1.getHeight() * 0.5f * scaleFactor);
                    matrix.postTranslate(rectCar.x - mIcon_AccLockonOutSide_1.getWidth() * 0.5f * scaleFactor, rectCar.y - mIcon_AccLockonOutSide_1.getHeight() * 0.5f * scaleFactor);

                    paint = new Paint();
                    paint.setAlpha(160);
                    canvas.drawBitmap(mIcon_AccLockonOutSide_1, matrix, paint);
                }

                RR += 15;
            }
        } while (false);
    }


    public static void drawSensingSample_ObjectDetect(Canvas canvas, SensingSamples.ObjectDetectSample sample)
    {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        // Render objects
        for (int i = 0 ; i < sample.mSampleCount ; i++) {
            // Focus object is rendered by FCWS
            if(i == sample.mFocusObjectId) continue;

            SensingSamples.ObjectData objCtx = sample.mObjectDataList[i];
            RectF bBox = new RectF(objCtx.mMinAnchor[0], objCtx.mMinAnchor[1], objCtx.mMaxAnchor[0], objCtx.mMaxAnchor[1]);

            // decide information
            String objInfo;
            if(mLabelIndex != null) {
                String labelName = mLabelIndex.get(objCtx.mObjTypeIndex);
                if(labelName != null) {
                    objInfo = "[" + labelName + "] : " + String.format(Locale.US,"%.1f",objCtx.mScore) + " : " + objCtx.mDistance;
                }
                else {
                    objInfo = "[" + objCtx.mObjTypeIndex + "] : " + String.format(Locale.US,"%.1f",objCtx.mScore) + " : " + objCtx.mDistance;
                }
            }
            else {
                objInfo = "[" + objCtx.mObjTypeIndex + "] : " + String.format(Locale.US,"%.1f",objCtx.mScore) + " : " + objCtx.mDistance;
            }

            float x = bBox.left * canvasWidth;
            float y = bBox.top * canvasHeight;

            // render bounding box
            Paint paint = new Paint();
            paint.setARGB(160, 70, 200, 100);
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);

            Path path = new Path();
            path.moveTo(bBox.left * canvasWidth, bBox.top * canvasHeight);
            path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight);
            path.lineTo(bBox.right * canvasWidth, bBox.bottom * canvasHeight);
            path.lineTo(bBox.left * canvasWidth, bBox.bottom * canvasHeight);
            path.close();
            canvas.drawPath(path, paint);

            if(Preferences.getInstance().getDeveloperData().isShowObjectBillboard()) {
                Rect textRect = new Rect();
                // set text information render
                Paint textPaint = new Paint();
                textPaint.setARGB(200, 255, 255, 255);
                textPaint.setTextSize(60);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.getTextBounds(objInfo, 0, objInfo.length(), textRect);

                // set billboard render
                path = new Path();
                paint.setStyle(Paint.Style.FILL);
                path.moveTo(bBox.left * canvasWidth, bBox.top * canvasHeight);
                path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight);
                path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight - textRect.height());
                path.lineTo(bBox.left * canvasWidth, bBox.top * canvasHeight - textRect.height());
                path.close();

                // draw
                canvas.drawPath(path, paint);
                canvas.drawText(objInfo, x, y, textPaint);
            }

        }

    }

    public static void drawSensingSample_TrafficLight(Canvas canvas, SensingSamples.TrafficLightSample sample)
    {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        // Render objects
        for (int i = 0 ; i < sample.mSampleCount ; i++) {
            SensingSamples.ObjectData objCtx = sample.mObjectDataList[i];
            RectF bBox = new RectF(objCtx.mMinAnchor[0], objCtx.mMinAnchor[1], objCtx.mMaxAnchor[0], objCtx.mMaxAnchor[1]);

            // decide information
            String objInfo;
            if(objCtx.mObjTypeIndex == 0) {
                objInfo = "[ Red ]";
            }
            else {
                objInfo = "[ Green ]";
            }


            float x = bBox.left * canvasWidth;
            float y = bBox.top * canvasHeight;

            // render bounding box
            Paint paint = new Paint();
            if(objCtx.mObjTypeIndex == 0) {
                objInfo = "[ Red ]";
                paint.setARGB(160, 250, 100, 100);
            }
            else {
                objInfo = "[ Green ]";
                paint.setARGB(160, 0, 255, 100);
            }

            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);

            Path path = new Path();
            path.moveTo(bBox.left * canvasWidth, bBox.top * canvasHeight);
            path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight);
            path.lineTo(bBox.right * canvasWidth, bBox.bottom * canvasHeight);
            path.lineTo(bBox.left * canvasWidth, bBox.bottom * canvasHeight);
            path.close();
            canvas.drawPath(path, paint);


            canvas.drawLine(0 * canvasWidth,
                    (bBox.top + bBox.bottom) * 0.5f * canvasHeight,
                    bBox.left * canvasWidth,
                    (bBox.top + bBox.bottom) * 0.5f * canvasHeight,
                    paint);


            canvas.drawLine(bBox.right* canvasWidth,
                    (bBox.top + bBox.bottom) *0.5f* canvasHeight,
                    canvasWidth* canvasWidth,
                    (bBox.top + bBox.bottom) *0.5f* canvasHeight,
                    paint);

            if(objCtx.mScore > 0.6f) {

                canvas.drawLine((bBox.left + bBox.right) * 0.5f * canvasWidth,
                        0 * canvasHeight,
                        (bBox.left + bBox.right) * 0.5f * canvasWidth,
                        bBox.top * canvasHeight,
                        paint);
                canvas.drawLine((bBox.left + bBox.right) *0.5f* canvasWidth,
                        bBox.bottom* canvasHeight,
                        (bBox.left + bBox.right) *0.5f* canvasWidth,
                        canvasHeight* canvasHeight,
                        paint);
            }

            if(Preferences.getInstance().getDeveloperData().isShowObjectBillboard()) {
                Rect textRect = new Rect();
                // set text information render
                Paint textPaint = new Paint();
                textPaint.setARGB(200, 255, 255, 255);
                textPaint.setTextSize(60);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.getTextBounds(objInfo, 0, objInfo.length(), textRect);

                // set billboard render
                path = new Path();
                paint.setStyle(Paint.Style.FILL);
                path.moveTo(bBox.left * canvasWidth, bBox.top * canvasHeight);
                path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight);
                path.lineTo(bBox.right * canvasWidth, bBox.top * canvasHeight - textRect.height());
                path.lineTo(bBox.left * canvasWidth, bBox.top * canvasHeight - textRect.height());
                path.close();

                // draw
                canvas.drawPath(path, paint);
                canvas.drawText(objInfo, x, y, textPaint);
            }

        }

    }

    public static void drawLatitudePlan(Canvas canvas, LatitudePlan latiPlane,
                                        SensingSamples.LaneDetectSample laneSample,
                                        CANbusData.CANParams_SteeringSensor steeringSensor,
                                        CANbusData.CANParams_SafetyFeature canSafetyFeature)
    {
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        Paint paint;

        do {
            int lastNodeIndex = (LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT - 1) *2;
            paint = new Paint();
            paint.setColor(Color.argb(180, 255, 125, 0));

            switch (laneSample.mLaneStatus) {
                case LeftDeparture: //left departure
                    paint.setColor(Color.argb(70, 255, 255, 0));
                    break;
                case RightDeparture: // right departure
                    paint.setColor(Color.argb(70, 255, 255, 0));
                    break;
                case Calibrating:
                    paint.setColor(Color.argb(70, 255, 0, 0));
                    break;
                case Detected:
                    paint.setColor(Color.argb(70, 0, 0, 255));
                    break;
                default:
                    paint.setColor(Color.argb(70, 160, 160, 160));
                    break;
            }

            int cId = 0;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
              latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;

            Path rPath = new Path();
            int mm = 0;
            for(int i = cId ; i < LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                int nodeIndex = i * 2;
                if(latiPlane.mLaneAnchors_L[nodeIndex + 1] >= 0) {
                    rPath.moveTo(latiPlane.mLaneAnchors_L[nodeIndex + 0] * canvasWidth, latiPlane.mLaneAnchors_L[nodeIndex + 1] * canvasHeight);
                    mm = i;
                    break;
                }
            }

            for(int i = mm +1 ; i < LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                int nodeIndex = i*2;
                rPath.lineTo(latiPlane.mLaneAnchors_L[nodeIndex] * canvasWidth, latiPlane.mLaneAnchors_L[nodeIndex +1] * canvasHeight);
            }

            for(int i = LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT - 1 ; i >= cId; i--) {
                int nodeIndex = i*2;
                if(latiPlane.mLaneAnchors_R[nodeIndex + 1] >= 0) {
                    rPath.lineTo(latiPlane.mLaneAnchors_R[nodeIndex] * canvasWidth, latiPlane.mLaneAnchors_R[nodeIndex + 1] * canvasHeight);
                }
            }
            rPath.close();

            try {
                canvas.drawPath(rPath, paint);
            }
            catch (Exception e) {
                //e.printStackTrace();
            }


            if(Preferences.getInstance().getDeveloperData().isShowObjectBillboard()) {
                String text = String.format("L : %.3f  <---> R: %.3f", latiPlane.mLaneScore_L , latiPlane.mLaneScore_R);
                TextPaint  laneWidthPaint = new TextPaint();
                laneWidthPaint.setTextSize(canvasHeight *0.1f);
                laneWidthPaint.setStrokeWidth(5);
                laneWidthPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
                laneWidthPaint.setColor(Color.argb(200, 255, 0, 0));

                float tx = 0.25f * canvasWidth;
                float ty = 0.2f * canvasHeight;

                canvas.drawText(text, tx, ty, laneWidthPaint);
            }
        } while(false);

        if (latiPlane.isPlanValid)
        {
            int lastNodeIndex = (LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT - 1) *2;
            float x_down = canvasWidth * ((latiPlane.mTrajectory_L[lastNodeIndex] + latiPlane.mTrajectory_R[lastNodeIndex]) * 0.5f);
            float y_down = canvasHeight * ((latiPlane.mTrajectory_L[lastNodeIndex +1] + latiPlane.mTrajectory_R[lastNodeIndex +1]) * 0.5f);
            float x_up = canvasWidth * ((latiPlane.mTrajectory_L[0] + latiPlane.mTrajectory_R[0]) * 0.5f);
            float y_up = canvasHeight * ((latiPlane.mTrajectory_L[1] + latiPlane.mTrajectory_R[1]) * 0.5f);

            int start_color = Color.argb(160, 0, 255, 0);
            int end_color = Color.argb(240, 0, 255, 0);
//            canSafetyFeature.isEnabled_LKS = true;
            if(!canSafetyFeature.isEnabled_LKS) {
                start_color = Color.argb(160, 200, 200, 200);
                end_color = Color.argb(240, 200, 200, 200);
            }

            int cId = 0;
            if(!(latiPlane.mTrajectory_L[cId *2 + 1] > 0 && latiPlane.mTrajectory_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mTrajectory_R[cId *2 + 1] > 0 && latiPlane.mTrajectory_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mTrajectory_L[cId *2 + 1] > 0 && latiPlane.mTrajectory_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mTrajectory_R[cId *2 + 1] > 0 && latiPlane.mTrajectory_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;
            if(!(latiPlane.mLaneAnchors_L[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_L[cId *2 + 1] < (canvasHeight * 1.1f) &&
                    latiPlane.mLaneAnchors_R[cId *2 + 1] > 0 && latiPlane.mLaneAnchors_R[cId *2 + 1] < (canvasHeight * 1.1f))) cId++;

            LinearGradient linearGradient;
            linearGradient = new LinearGradient(x_down, y_down, x_up, y_up, start_color, end_color, Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);

            Path rPath = new Path();
            int mm = 0;
            for(int i = cId ; i < LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                int nodeIndex = i * 2;
                if(latiPlane.mTrajectory_L[nodeIndex + 1] >= 0)
                {
                    rPath.moveTo(latiPlane.mTrajectory_L[nodeIndex + 0] * canvasWidth, latiPlane.mTrajectory_L[nodeIndex + 1] * canvasHeight);
                    mm = i;
                    break;
                }
            }

            for(int i = mm +1 ; i < LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT ; i++) {
                int nodeIndex = i*2;
                rPath.lineTo(latiPlane.mTrajectory_L[nodeIndex] * canvasWidth, latiPlane.mTrajectory_L[nodeIndex +1] * canvasHeight);
            }

            for(int i = LatitudePlan.LATITUDE_PLAN_TRAJECTORY_SAMPLE_COUNT - 1 ; i >= cId; i--) {
                int nodeIndex = i*2;
//                if(latiPlane.mTrajectory_R[nodeIndex + 0] >= 0 && latiPlane.mTrajectory_R[nodeIndex + 0] <= 1 &&
//                  latiPlane.mTrajectory_R[nodeIndex + 1] >= 0 && latiPlane.mTrajectory_R[nodeIndex + 1] <= 1)
                if(latiPlane.mTrajectory_R[nodeIndex + 1] >= 0)
                {
                    rPath.lineTo(latiPlane.mTrajectory_R[nodeIndex] * canvasWidth, latiPlane.mTrajectory_R[nodeIndex + 1] * canvasHeight);
                }
            }
            rPath.close();

            try {
                canvas.drawPath(rPath, paint);
            }
            catch (Exception e) {
                //e.printStackTrace();
            }

            if(Preferences.getInstance().getDeveloperData().isShowObjectBillboard()) {

                String text = String.format("%.1f ---> %.1f", latiPlane.planeStartSteerAngle , latiPlane.planDesiredSteerAngle);
                TextPaint  laneWidthPaint = new TextPaint();
                laneWidthPaint.setTextSize(canvasWidth *0.1f);
                laneWidthPaint.setStrokeWidth(5);
                laneWidthPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
                laneWidthPaint.setColor(Color.argb(200, 255, 0, 0));

                float tx = 0.25f * canvasWidth;
                float ty = 0.5f * canvasHeight;

                canvas.drawText(text, tx, ty, laneWidthPaint);
            }
        }
    }

    public static boolean loadLabels(String labelsFilePath) {
        boolean ret = false;
        try {
            File fLabel = new File(labelsFilePath);
            final BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(fLabel)));
            String line;

            if(mLabelIndex == null) {
                mLabelIndex = new SparseArray<>();
            }
            else {
                mLabelIndex.clear();
            }
            int lineIndex = 0;
            while ((line = inputStream.readLine()) != null) {
                mLabelIndex.put(lineIndex, line);
                lineIndex++;
            }
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
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
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.viatech.via_ai.R;
import com.victor.loading.rotate.RotateLoading;

public class LoadingWrapper extends ConstraintLayout {
    private RotateLoading mUI_LoadingProgress;
    private AppCompatTextView mUI_LoadingNote;

    public LoadingWrapper(Context context) {
        super(context);
        init(context);
    }

    public LoadingWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_loadingwrapper, this, true);

        mUI_LoadingProgress = findViewById(R.id.layoutActivityAdas_LoadingProgress);
        mUI_LoadingNote  = findViewById(R.id.layoutActivityAdas_LoadingNote);
        this.setClickable(false);
        this.setEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void setLoadNote(String note) {
        mUI_LoadingNote.setText(note);
    }

    /**
        @brief Show the wrapper, this wrapper will block the touch event
      */
    public void show()
    {
        mUI_LoadingProgress.start();
        this.setAnimation(null);
        this.setVisibility(View.VISIBLE);
    }

    /**
     @brief Hide the wrapper.
     */
    public void hide()
    {
        mUI_LoadingProgress.stop();
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(1000);
        anim.setRepeatCount(0);
        this.startAnimation(anim);
        this.setVisibility(View.INVISIBLE);
    }
}

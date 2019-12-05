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

package com.viatech.via_ai.Media;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;

import com.viatech.via_ai.System.SystemEvents;

import java.util.Locale;

public class EventSpeaker implements TextToSpeech.OnInitListener {
    public interface OnEventChangeListener {
        void postStartActivityForResult(Intent intent, int requestCode);
        void postTTSInstallActionRequest(Intent intent);
    }

    public static EventSpeaker getInstance() {
        if(mInstance == null) {
            synchronized (EventSpeaker.class) {
                if(mInstance == null) {
                    mInstance = new EventSpeaker();
                }
            }
        }
        return mInstance;
    }
    private static EventSpeaker mInstance = null;

    private final int TTS_REQUEST_CODE = 74643;

    private TextToSpeech mTTS = null;
    private Context mContext = null;
    private OnEventChangeListener mOnEventChangeListener = null;
    private boolean b_IsInit = false;
    private boolean b_enableTTS = false;
    private boolean b_enableBeep = false;
    private SystemEvents mPrevEvent;
    Ringtone mRingTone;

    public EventSpeaker() {
        mPrevEvent = SystemEvents.NoEvent;
    }

    public void init(@NonNull Context context, @NonNull OnEventChangeListener listener, boolean enableTTS, boolean enableBeep) {
        mContext = context;
        mOnEventChangeListener = listener;
        b_enableTTS = enableTTS;
        b_enableBeep = enableBeep;

        if(b_enableTTS) {
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            mOnEventChangeListener.postStartActivityForResult(checkIntent, TTS_REQUEST_CODE);
        }

        if(b_enableBeep) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mRingTone = RingtoneManager.getRingtone(context, uri);
        }
    }

    public void release() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    public boolean isInit() {
        return b_IsInit;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTTS.setPitch(1.0f);
            mTTS.setSpeechRate(1.0f);
            int result = mTTS.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        }
        else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    public void pushEvent(SystemEvents event) {
        boolean speak = false;

        if(b_enableTTS && mTTS.isSpeaking()) {
            if(mPrevEvent.getLevel().getIndex() >= event.getLevel().getIndex() && mPrevEvent != event) {
                speak = true;
                mTTS.stop();
            }
        }
        else {
            speak = true;
        }

        if(speak) {
            if(b_enableBeep) {
                if (event.getRingCode() == 1) mRingTone.play();
            }

            if(b_enableTTS) {
                String text = event.getText();
                if (text != null) mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
            mPrevEvent = event;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto-generated method stub
        if (requestCode == TTS_REQUEST_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(mContext, this);
                b_IsInit = true;
            }
            else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                mOnEventChangeListener.postTTSInstallActionRequest(intent);
            }
        }
    }
}

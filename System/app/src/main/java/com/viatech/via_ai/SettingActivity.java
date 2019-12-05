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

package com.viatech.via_ai;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.viatech.via_ai.Preferences.Fragments.ADASSettingFragment;
import com.viatech.via_ai.Preferences.Fragments.CameraSettingFragment;
import com.viatech.via_ai.Preferences.Fragments.DeveloperSettingFragment;
import com.viatech.via_ai.Preferences.Fragments.EventChangeListener;
import com.viatech.via_ai.Preferences.Fragments.PreferenceFragmentTypes;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.Preferences.Fragments.RecorderSettingFragment;
import com.viatech.via_ai.Preferences.Fragments.SettingHeaderFragment;
import com.viatech.via_ai.Preferences.Fragments.SystemSettingFragment;
import com.viatech.via_ai.Preferences.Fragments.VehicleBusSettingFragment;
import com.viatech.via_ai.System.Helper;

public class SettingActivity extends Activity implements EventChangeListener {

    private Helper mHelper;
    private PreferenceFragmentTypes mCurSetting;
    private TextView mSimpleActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Preferences.getInstance().load(this);


        setContentView(R.layout.activity_setting);
        mHelper = new Helper();
        mHelper.setupAutoHideSystemUI(this);

        mSimpleActionBar = findViewById(R.id.SimpleActionBar);
        mCurSetting = PreferenceFragmentTypes.SettingHeader;


        mSimpleActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurSetting) {
                    case SettingHeader:
                        finish();
                        break;
                    case CameraSetting:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                    case ADASSetting:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                    case DeveloperSetting:
                        changeFragment(PreferenceFragmentTypes.SystemSetting);
                        break;
                    case RecorderSetting:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                    case SystemSetting:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                    case VehicleBusSetting:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                    case UserInfo:
                        changeFragment(PreferenceFragmentTypes.SettingHeader);
                        break;
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        changeFragment(PreferenceFragmentTypes.SettingHeader);
    }

    @Override
    public void onPause(){
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {

    }

    private void changeSimpleActionBar(final PreferenceFragmentTypes curFrag) {
        final String header = "\u232b    " ;

        if(Looper.myLooper() == Looper.getMainLooper()) {
            switch (curFrag) {
                case SettingHeader:
                    mSimpleActionBar.setText(header +"Settings");
                    break;
                case CameraSetting:
                    mSimpleActionBar.setText(header +"Camera Settings");
                    break;
                case ADASSetting:
                    mSimpleActionBar.setText(header +"ADAS Settings");
                    break;
                case DeveloperSetting:
                    mSimpleActionBar.setText(header +"Developer Settings");
                    break;
                case RecorderSetting:
                    mSimpleActionBar.setText(header +"Recorder Settings");
                    break;
                case SystemSetting:
                    mSimpleActionBar.setText(header +"System Informations");
                    break;
                case VehicleBusSetting:
                    mSimpleActionBar.setText(header +"Vehicle Bus Settings");
                    break;
            }
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeSimpleActionBar(curFrag);
                }
            });
        }
    }

    private void changeFragment(PreferenceFragmentTypes dst) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
        if(currentFragment != null) {
            getFragmentManager().beginTransaction().remove(currentFragment).commit();
        }

        switch (dst) {
            case SettingHeader: {
                SettingHeaderFragment frag = new SettingHeaderFragment();
                frag.setOnEventChangeListener(this);
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case CameraSetting: {
                CameraSettingFragment frag = new CameraSettingFragment();
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case ADASSetting: {
                ADASSettingFragment frag = new ADASSettingFragment();
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case DeveloperSetting: {
                DeveloperSettingFragment frag = new DeveloperSettingFragment();
                frag.setOnEventChangeListener(this);
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case RecorderSetting: {
                RecorderSettingFragment frag = new RecorderSettingFragment();
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case SystemSetting: {
                SystemSettingFragment frag = new SystemSettingFragment();
                frag.setOnFragmentListener(this);
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case VehicleBusSetting: {
                VehicleBusSettingFragment frag = new VehicleBusSettingFragment();
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
            } break;
            case UserInfo: {
                SettingHeaderFragment frag = new SettingHeaderFragment();
                frag.setOnEventChangeListener(this);
                getFragmentManager().beginTransaction().add(R.id.container, frag).commit();

                Intent intent;
                intent = new Intent(this, UserManagerActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } break;
        }

        mCurSetting = dst;
        changeSimpleActionBar(mCurSetting);
    }

    @Override
    public void onFragmentChange(PreferenceFragmentTypes dst) {
        changeFragment(dst);
    }
}

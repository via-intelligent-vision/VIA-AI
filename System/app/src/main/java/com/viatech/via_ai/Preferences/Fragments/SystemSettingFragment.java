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

package com.viatech.via_ai.Preferences.Fragments;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.Preferences.Fragments.EventChangeListener;
import com.viatech.via_ai.Preferences.Fragments.PreferenceFragmentTypes;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.Preferences.TextPreference;
import com.viatech.via_ai.R;

public class SystemSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EventChangeListener mFragmentChangeListener;
    public void setOnFragmentListener(EventChangeListener listener) {
        mFragmentChangeListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = getResources();

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_system, false);

        // config xml
        addPreferencesFromResource(R.xml.pref_system);


        TextPreference dev = (TextPreference)findPreference(resource.getString(R.string.prefKey_System_DeveloperOptions));
        if(dev != null) {
            dev.setBackgroundColor(Color.argb(160, 30, 100, 30));
            dev.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(mFragmentChangeListener != null) {
                        mFragmentChangeListener.onFragmentChange(PreferenceFragmentTypes.DeveloperSetting);
                    }
                    return false;
                }
            });
        }

        TextPreference tPreference = (TextPreference)findPreference(resource.getString(R.string.prefKey_System_BuildVersion));
        tPreference.setText(getVersionName(getActivity()));

        hideRedundantSetting(resource);
    }

    private void hideRedundantSetting(Resources resource) {
        TextPreference p = (TextPreference) findPreference(resource.getString(R.string.prefKey_System_DeveloperOptions));
        if(p != null) getPreferenceScreen().removePreference(p);

    }

    private String getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode="";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            versionCode = Integer.toString(packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private String getVersionName(Context context){
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.prefTitle_System));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sp =  Preferences.getInstance().getLocalPreferences(getActivity());
        //Preferences.getInstance().getDeveloperData().load(getActivity(), sp);
    }
}

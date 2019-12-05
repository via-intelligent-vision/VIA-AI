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
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.Preferences.Fragments.EventChangeListener;
import com.viatech.via_ai.Preferences.Fragments.PreferenceFragmentTypes;
import com.viatech.via_ai.Preferences.TextPreference;
import com.viatech.via_ai.R;

import java.util.HashMap;

public class SettingHeaderFragment extends PreferenceFragment {

    private EventChangeListener mFragmentChangeListener;
    public void setOnEventChangeListener(EventChangeListener listener) {
        mFragmentChangeListener = listener;
    }

    private HashMap<String, PreferenceFragmentTypes> mMap_KeyToType;

    private void createHashMap( Resources resource) {
        mMap_KeyToType = new HashMap<>();
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_CameraSettings), PreferenceFragmentTypes.CameraSetting);
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_RecorderSettings), PreferenceFragmentTypes.RecorderSetting);
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_ADASSettings), PreferenceFragmentTypes.ADASSetting);
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_VehicleBus), PreferenceFragmentTypes.VehicleBusSetting);
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_User), PreferenceFragmentTypes.UserInfo);
        mMap_KeyToType.put(resource.getString(R.string.prefKey_SettingHeader_System), PreferenceFragmentTypes.SystemSetting);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = getResources();

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_setting_header, true);

        // config xml
        addPreferencesFromResource(R.xml.pref_setting_header);

        // create simple hash map
        createHashMap(resource);

        TextPreference pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_CameraSettings));
        pref.setOnPreferenceClickListener(mPrefListener);

        pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_RecorderSettings));
        pref.setOnPreferenceClickListener(mPrefListener);

        pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_ADASSettings));
        pref.setOnPreferenceClickListener(mPrefListener);

        pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_VehicleBus));
        pref.setOnPreferenceClickListener(mPrefListener);

        pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_User));
        pref.setOnPreferenceClickListener(mPrefListener);

        pref = (TextPreference)findPreference(resource.getString(R.string.prefKey_SettingHeader_System));
        pref.setOnPreferenceClickListener(mPrefListener);
    }


    private Preference.OnPreferenceClickListener mPrefListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(mFragmentChangeListener != null) {
                PreferenceFragmentTypes type = mMap_KeyToType.get(preference.getKey());
                mFragmentChangeListener.onFragmentChange(type);
            }
            return false;
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //view.setBackgroundColor(getResources().getColor(android.R.color.your_color));
        //view.setBackgroundColor(getResources().getColor(R.color.prefBkg_System));

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
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

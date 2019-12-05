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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.Preferences.Utils;
import com.viatech.via_ai.R;

import java.util.ArrayList;

public class VehicleBusSettingFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = getResources();

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_vehiclebus_setting, false);

        // config xml
        addPreferencesFromResource(R.xml.pref_vehiclebus_setting);

        // link action (Control internal data)
        ArrayList<Pair<Preference, Integer >> ctls = new ArrayList<>();
        ctls.add(new Pair<>(findPreference(resource.getString(R.string.prefKey_VehicleBus_ProtocolType)),
                Utils.LINK_DIRECT));
        ctls.add(new Pair<>(findPreference(resource.getString(R.string.prefKey_VehicleBus_VehicleType)),
                Utils.LINK_DIRECT));
        Utils.linkSwitch( (SwitchPreference)findPreference(resource.getString(R.string.prefKey_VehicleBus_BusStatus)), ctls);

        // hide preference know
        SwitchPreference pref = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_VehicleBus_BusStatus));
        if(pref != null) getPreferenceScreen().removePreference(pref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.prefTitle_VehicleBus));
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
        if(sharedPreferences == null || key == null) return;

        // Configure slave parameters
        Resources resource = getResources();

        if(key.compareTo(resource.getString(R.string.prefKey_VehicleBus_BusStatus)) == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Boolean b = sharedPreferences.getBoolean(key, false);
            if(editor != null && b != null) {
                // Force Disable
                if(b == Boolean.FALSE) {
                    editor.putBoolean(resource.getString(R.string.prefKey_Recoder_VehicleBusRecord), false);
                    editor.commit();
                }
            }
        }
    }
}

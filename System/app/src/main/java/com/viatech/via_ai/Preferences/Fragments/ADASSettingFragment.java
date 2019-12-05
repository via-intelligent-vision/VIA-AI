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
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.Preferences.FileDialogPreference;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.Preferences.Utils;
import com.viatech.via_ai.R;

public class ADASSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDialogPreference fPreference;
        SwitchPreference sPreference;

        Resources resource = getResources();

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_adas_setting, false);

        // config xml
        addPreferencesFromResource(R.xml.pref_adas_setting);

        // set preference rules (Configuration Path)
        fPreference = (FileDialogPreference)findPreference(resource.getString(R.string.prefKey_ADAS_ConfigurationPath));
        fPreference.setDialog(true, Utils.EXTENSION_XML);

        fPreference = (FileDialogPreference)findPreference(resource.getString(R.string.prefKey_ADAS_AutomotiveConfigurationPath));
        fPreference.setDialog(true, Utils.EXTENSION_XML);

        // set preference rules (LDW Detector)
        Listener_Detector_LDW listener_Detector_LDW = new Listener_Detector_LDW(resource);
        sPreference = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_ADAS_LDW_DetectorStatus));
        sPreference.setOnPreferenceChangeListener(listener_Detector_LDW);
        listener_Detector_LDW.onPreferenceChange(sPreference, sPreference.isChecked());

        // set preference rules (FCW Detector)
        Listener_Detector_FCW listener_Detector_FCW = new Listener_Detector_FCW(resource);
        sPreference = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_ADAS_FCW_DetectorStatus));
        sPreference.setOnPreferenceChangeListener(listener_Detector_FCW);
        listener_Detector_FCW.onPreferenceChange(sPreference, sPreference.isChecked());

        hideRedundantSetting(resource);
    }

    private void hideRedundantSetting(Resources resource) {
        PreferenceCategory category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_ADAS_LDW_DetectorCategory));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_ADAS_FCW_DetectorCategory));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_ADAS_SLD_DetectorCategory));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_ADAS_BSD_DetectorCategory));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_ADAS_TLD_DetectorCategory));
        if(category != null) getPreferenceScreen().removePreference(category);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.prefTitle_ADAS));
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
        Preferences.getInstance().getLaneDepartureWarningData().load(getActivity(), sp);
        Preferences.getInstance().getForwardCollisionWarningData().load(getActivity(), sp);
        Preferences.getInstance().getBlindSpotWarningData().load(getActivity(), sp);
        Preferences.getInstance().getLaneKeerpingAssistData().load(getActivity(), sp);
        Preferences.getInstance().getAdaptiveCruiseControlData().load(getActivity(), sp);
        Preferences.getInstance().getSpeedLimitWarningData().load(getActivity(), sp);
    }

    private class Listener_Detector_LDW implements Preference.OnPreferenceChangeListener {
        private Resources resource;

        public Listener_Detector_LDW(Resources resource) {
            this.resource = resource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            boolean b = (boolean) newValue;
            ((SwitchPreference)preference).setChecked(b);

            // -----------------------------------------------------------------
            // Effected preference in this fragment
            findPreference(resource.getString(R.string.prefKey_ADAS_LDW_DetectorEnableSpeed)).setEnabled(b);

            // -----------------------------------------------------------------
            // Effected preference in other fragment.
            //  NO.
            return false;
        }
    }

    private class Listener_Detector_FCW implements Preference.OnPreferenceChangeListener {
        private Resources resource;

        public Listener_Detector_FCW(Resources resource) {
            this.resource = resource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            boolean b = (boolean) newValue;
            ((SwitchPreference)preference).setChecked(b);

            // -----------------------------------------------------------------
            // Effected preference in this fragment
            findPreference(resource.getString(R.string.prefKey_ADAS_FCW_DetectorMode)).setEnabled(b);

            // -----------------------------------------------------------------
            // Effected preference in other fragment.
            //  NO.
            return false;
        }
    }


}

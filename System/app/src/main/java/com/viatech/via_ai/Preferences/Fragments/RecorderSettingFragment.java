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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.via_ai.Preferences.FileDialogPreference;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.R;

public class RecorderSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int rID [];
        final Resources resource = getResources();

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(resource.getString(R.string.prefTitle_Recoder));
        }

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_recoder_setting, false);

        // config xml
        addPreferencesFromResource(R.xml.pref_recoder_setting);


        FileDialogPreference fPreference;


        // set preference rules (Video Record Path)
        fPreference = (FileDialogPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VideoRecordPath));
        fPreference.setDialog(false, "");

        // set preference rules (VehicleBus Record Path)
        fPreference = (FileDialogPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecordPath));
        fPreference.setDialog(false, "");

        // set preference rules (Video Record)
        Listener_VideoRecord listener_VideoRecord = new Listener_VideoRecord(resource);
        SwitchPreference pvideo = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VideoRecord));
        pvideo.setOnPreferenceChangeListener(listener_VideoRecord);

        // set preference rules (VehicleBus Record)
        Listener_VehicleBusRecord listener_VehicleBusRecord = new Listener_VehicleBusRecord(resource);
        SwitchPreference pvbus = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecord));
        pvbus.setOnPreferenceChangeListener(listener_VehicleBusRecord);


        // notify parts listener
        listener_VideoRecord.onPreferenceChange(pvideo, pvideo.isChecked());
        listener_VehicleBusRecord.onPreferenceChange(pvbus, pvbus.isChecked());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.prefTitle_Recoder));
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
        Preferences.getInstance().getFrameRecordData().load(getActivity(), sp);
        Preferences.getInstance().getVehicleData().load(getActivity(), sp);
    }

    private class Listener_VideoRecord implements Preference.OnPreferenceChangeListener {
        private Resources resource;

        public Listener_VideoRecord(Resources resource) {
            this.resource = resource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            boolean b = (boolean) newValue;
            ((SwitchPreference)preference).setChecked(b);

            // -----------------------------------------------------------------
            // Effected preference in this fragment
            findPreference(resource.getString(R.string.prefKey_Recoder_VideoRecordPath)).setEnabled(b);
            findPreference(resource.getString(R.string.prefKey_Recoder_VideoRecordInterval)).setEnabled(b);
            findPreference(resource.getString(R.string.prefKey_Recoder_VideoRecordFPS)).setEnabled(b);

            boolean b_vehicleBusRecord = b && sp.getBoolean(resource.getString(R.string.prefKey_VehicleBus_BusStatus), false);
            if(b == false) {
                //Log.e("ABC", "Listener_VehicleBusRecord set " + b);
                //((SwitchPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecord))).setChecked(b);
                SwitchPreference swp = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecord));
                //

                swp.getOnPreferenceChangeListener().onPreferenceChange(swp, b);

            }
            findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecord)).setEnabled(b_vehicleBusRecord);

            // -----------------------------------------------------------------
            // Effected preference in other fragment.
            //  NO.
            return false;
        }
    }

    private class Listener_VehicleBusRecord implements Preference.OnPreferenceChangeListener {
        private Resources resource;

        public Listener_VehicleBusRecord(Resources resource) {
            this.resource = resource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            boolean b = (boolean) newValue;
            ((SwitchPreference)preference).setChecked(b);

            // -----------------------------------------------------------------
            // Effected preference in this fragment
            findPreference(resource.getString(R.string.prefKey_Recoder_VehicleBusRecordPath)).setEnabled(b);

            // -----------------------------------------------------------------
            // Effected preference in other fragment.
            //  NO.
            return false;
        }
    }

}

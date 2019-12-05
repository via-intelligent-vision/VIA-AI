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
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viatech.camera.CameraTypes;
import com.viatech.via_ai.Media.CameraPermutation;
import com.viatech.via_ai.Preferences.FileDialogPreference;
import com.viatech.via_ai.Preferences.Preferences;
import com.viatech.via_ai.Preferences.TextPreference;
import com.viatech.via_ai.Preferences.Utils;
import com.viatech.via_ai.Preferences.VideoSelectPreference;
import com.viatech.via_ai.R;

public class CameraSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = getResources();

        // load default value
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_camera_source_setting, false);

        // config xml
        addPreferencesFromResource(R.xml.pref_camera_source_setting);

        // bind default value (to show the summary with value, ps : only ListPreference could use "%s" as summary to show value in xml)
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_0)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_1)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_2)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_3)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_0)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_1)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_2)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_3)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_0)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_1)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_2)));
        Utils.bindPreferenceSummaryToValue(findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_3)));


        SwitchPreference sPreference;

        // set preference rules (ADAS Camera source)
        Listener_ADAS_FrameSource listener_ADAS_FrameSource = new Listener_ADAS_FrameSource(resource);
        sPreference = (SwitchPreference)findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameSource));
        sPreference.setOnPreferenceChangeListener(listener_ADAS_FrameSource);
        listener_ADAS_FrameSource.onPreferenceChange(sPreference, sPreference.isChecked());

        // apply file picker action
        VideoSelectPreference fPref = (VideoSelectPreference)findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath));
        fPref.setDialog(true, Utils.EXTENSION_MP4);

        hideRedundantSetting(resource);
    }

    private void hideRedundantSetting(Resources resource) {
        PreferenceCategory category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_CameraResource_FrameCategory_1));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_CameraResource_FrameCategory_2));
        if(category != null) getPreferenceScreen().removePreference(category);

        category = (PreferenceCategory) findPreference(resource.getString(R.string.prefKey_CameraResource_FrameCategory_3));
        if(category != null) getPreferenceScreen().removePreference(category);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
  //      view.setBackgroundColor(getResources().getColor(R.color.prefBkg_CameraResource));

        // set name of actionbar.
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.prefTitle_CameraSource));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged( Preferences.getInstance().getLocalPreferences(getActivity()), "ALL");
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String changedKey) {
        SharedPreferences sp =  Preferences.getInstance().getLocalPreferences(getActivity());
        Preferences.getInstance().getFrameSourceData().load(getActivity(), sp);

        Resources resource = getActivity().getResources();
        final String camCfgTmp = resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_0);   // _0 ~ _4
        boolean refresh = (changedKey.equals(resource.getString(R.string.prefKey_CameraResource_ADAS_FrameSource))) ||
                          (changedKey.equals(resource.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath))) ||
                          (changedKey.equals(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraPermutation))) ||
                          (changedKey.contains(camCfgTmp.substring(0, camCfgTmp.length() -1))) ||
                          (changedKey.equals("ALL"));
        // -----------------------------------------------------------------
        // refresh permutation
        if(refresh) {
            int camSrcWidth = 1280, camSrcHeight = 720;
            Size frameSize [] = null;

            if (!Preferences.getInstance().getFrameSourceData().isUseCamera()) { // check video info
                ListPreference premutation = (ListPreference) findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraPermutation));
                CameraPermutation permutation = CameraPermutation.getType(Integer.parseInt(premutation.getValue()));
                VideoSelectPreference fPref = (VideoSelectPreference) findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath));
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                try {
                    retriever.setDataSource(fPref.getVideoPathValue());
                    camSrcWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    camSrcHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                }
                catch (Exception e) {
                    camSrcWidth = 1280;
                    camSrcHeight = 720;
                }

                switch (permutation) {
                    case Camera1_In_Frame:
                        frameSize = new Size[1];
                        frameSize[0] = new Size(camSrcWidth, camSrcHeight);  // F
                        break;
                    case Camera2_In_Frame_1x2:
                        frameSize = new Size[2];
                        frameSize[0] = new Size(camSrcWidth / 2, camSrcHeight);  // F
                        frameSize[1] = new Size(camSrcWidth / 2, camSrcHeight);  // B
                        break;
                    case Camera3_In_Frame_1x3:
                        frameSize = new Size[3];
                        frameSize[0] = new Size(camSrcWidth / 3, camSrcHeight);  // F
                        frameSize[1] = new Size(camSrcWidth / 3, camSrcHeight);  // L
                        frameSize[2] = new Size(camSrcWidth / 3, camSrcHeight);  // R
                        break;
                    case Camera4_In_Frame_1x4:
                        frameSize = new Size[4];
                        frameSize[0] = new Size(camSrcWidth / 4, camSrcHeight);  // F
                        frameSize[1] = new Size(camSrcWidth / 4, camSrcHeight);  // L
                        frameSize[2] = new Size(camSrcWidth / 4, camSrcHeight);  // B
                        frameSize[3] = new Size(camSrcWidth / 4, camSrcHeight);  // R
                        break;
                    case Camera4_In_Frame_2x2:
                        frameSize = new Size[4];
                        frameSize[0] = new Size(camSrcWidth / 2, camSrcHeight / 2);  // F
                        frameSize[1] = new Size(camSrcWidth / 2, camSrcHeight / 2);  // L
                        frameSize[2] = new Size(camSrcWidth / 2, camSrcHeight / 2);  // B
                        frameSize[3] = new Size(camSrcWidth / 2, camSrcHeight / 2);  // R
                        break;
                }

            } else {
                Preferences.DeveloperData developerData = Preferences.getInstance().getDeveloperData();
                ListPreference premutation = (ListPreference) findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraPermutation));
                CameraPermutation permutation = CameraPermutation.getType(Integer.parseInt(premutation.getValue()));

                switch (permutation) {
                    case Camera1_In_Frame:
                        camSrcWidth = 1280;
                        camSrcHeight = 720;
                        frameSize = new Size[1];
                        frameSize[0] = new Size(1280, 720);  // F
                        break;
                    case Camera2_In_Frame_1x2:
                        camSrcWidth = 1280 * 2;
                        camSrcHeight = 720;
                        frameSize = new Size[2];
                        frameSize[0] = new Size(1280, 720);  // F
                        frameSize[1] = new Size(1280, 720);  // B
                        break;
                    case Camera3_In_Frame_1x3:
                        camSrcWidth = 1280 * 3;
                        camSrcHeight = 720;
                        frameSize = new Size[3];
                        frameSize[0] = new Size(1280, 720);  // F
                        frameSize[1] = new Size(1280, 720);  // L
                        frameSize[2] = new Size(1280, 720);  // R
                        break;
                    case Camera4_In_Frame_1x4:
                        camSrcWidth = 1280 * 4;
                        camSrcHeight = 720;
                        frameSize = new Size[4];
                        frameSize[0] = new Size(1280, 720);  // F
                        frameSize[1] = new Size(1280, 720);  // L
                        frameSize[2] = new Size(1280, 720);  // B
                        frameSize[3] = new Size(1280, 720);  // R
                        break;
                    case Camera4_In_Frame_2x2:
                        camSrcWidth = 1280 * 2;
                        camSrcHeight = 720 * 2;
                        frameSize = new Size[4];
                        frameSize[0] = new Size(1280, 720);  // F
                        frameSize[1] = new Size(1280, 720);  // L
                        frameSize[2] = new Size(1280, 720);  // B
                        frameSize[3] = new Size(1280, 720);  // R
                        break;
                }

            }

            TextPreference pCameraSourceWidth = (TextPreference) findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceWidth));
            TextPreference pCameraSourceHeight = (TextPreference) findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraSourceHeight));
            pCameraSourceWidth.setText(Integer.toString(camSrcWidth));
            pCameraSourceHeight.setText(Integer.toString(camSrcHeight));

            if (frameSize != null) {
                String key, dStr;
                TextPreference pFrameWidth, pFrameHeight;
                ListPreference pCameraModule;
                FileDialogPreference pCamCfgPath;
                for (int i = 0; i < Preferences.FrameSourceData.CSI_CAMERA_LIMIT ; i++) {
                    key = resource.getString(R.string.prefKey_CameraResource_ADAS_FrameWidth_0);
                    key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                    pFrameWidth = (TextPreference) findPreference(key);

                    key = resource.getString(R.string.prefKey_CameraResource_ADAS_FrameHeight_0);
                    key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                    pFrameHeight = (TextPreference) findPreference(key);

                    key = resource.getString(R.string.prefKey_CameraResource_ADAS_CameraModule_0);
                    key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                    pCameraModule = (ListPreference)findPreference(key);


                    key = resource.getString(R.string.prefKey_CameraResource_CameraConfigurationPath_0);
                    key = key.substring(0, key.length() - 2) + "_" + Integer.toString(i);
                    pCamCfgPath = (FileDialogPreference)findPreference(key);

                    if(i < frameSize.length) {
                        pFrameWidth.setText(Integer.toString(frameSize[i].getWidth()));
                        pFrameHeight.setText(Integer.toString(frameSize[i].getHeight()));
                        pFrameHeight.setEnabled(true);
                        pFrameWidth.setEnabled(true);
                        pCameraModule.setEnabled(true);
                        pCamCfgPath.setEnabled(false);

                        if(Integer.parseInt(pCameraModule.getValue()) == CameraTypes.Custom.getIndex()) {
                            pCamCfgPath.setEnabled(true);
                        }
                        else {
                            pCamCfgPath.setEnabled(false);
                        }
                    }
                    else {
                        pFrameHeight.setEnabled(false);
                        pFrameWidth.setEnabled(false);
                        pCameraModule.setEnabled(false);
                        pCamCfgPath.setEnabled(false);
                    }
                }
            }
        }
    }

    private class Listener_ADAS_FrameSource implements Preference.OnPreferenceChangeListener {
        private Resources resource;

        public Listener_ADAS_FrameSource(Resources resource) {
            this.resource = resource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            ((SwitchPreference)preference).setChecked((boolean) newValue);
            boolean isCamera = !((boolean) newValue);   // false is camera, true is playback

            // -----------------------------------------------------------------
            // Effected preference in this fragment
            findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_PlaybackPath)).setEnabled(!isCamera);
            findPreference(resource.getString(R.string.prefKey_CameraResource_ADAS_CameraDevices)).setEnabled(isCamera);

            // -----------------------------------------------------------------
            // Effected preference in other fragment.
            //  NO.

            return false;
        }
    }


}

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

package com.viatech.via_ai.Preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Pair;
import android.view.WindowManager;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.viatech.via_ai.UI.ExternalStoragePickDialog;

import java.io.File;
import java.util.ArrayList;


public class Utils {
    public static final int LINK_DIRECT = 0;// Map : On to On , Off to Off
    public static final int LINK_INVERSE = 1; // Map : On to Off , Off to On
    public static final int LINK_FORCE_DISABLE = 2; // Map : On to N/A , Off to Off
    public static final String EXTENSION_MP4 = ".mp4";
    public static final String EXTENSION_XML = ".xml";

    public static void linkSwitch(final SwitchPreference switchPreference, final ArrayList<Pair<Preference, Integer > > ctls) {

        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean b = (boolean) o;
                switchPreference.setChecked(b);

                for(int i = 0 ; i < ctls.size() ; i++) {
                    Pair<Preference, Integer> p = ctls.get(i);
                    switch (p.second) {
                        case Utils.LINK_DIRECT:
                            p.first.setEnabled(switchPreference.isChecked());
                            break;
                        case Utils.LINK_INVERSE:
                            p.first.setEnabled(!switchPreference.isChecked());
                            break;
                    }
                }
                return false;
            }
        };

        switchPreference.setOnPreferenceChangeListener(listener);


        // process immediately
        listener.onPreferenceChange(switchPreference, switchPreference.isChecked());
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0? listPreference.getEntries()[index] : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary("    " + stringValue);
            }
            return true;
        }
    };

    public static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private static void showFileDialog(final Context context, final Preference preference, String extension, String rootPath, final boolean isSelectFile)
    {
        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).getDialog().dismiss();
        }

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        if(isSelectFile) {
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = new String[]{extension};
        }
        else {
            properties.selection_type = DialogConfigs.DIR_SELECT;
        }
        properties.root = new File(rootPath);
        properties.error_dir = new File("/storage/emulated/0/");
        properties.offset = new File("/storage");

        FilePickerDialog dialog = new FilePickerDialog(context, properties);
        if(isSelectFile) {
            dialog.setTitle("Select a <" + extension + "> File");
        }
        else {
            dialog.setTitle("Select a Folder");
        }
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference)preference).setText(files[0]); // Saves the text to the SharedPreferences.
                    preference.setSummary("    " + files[0]);
                }
                else if (preference instanceof VideoSelectPreference) {
                    ((VideoSelectPreference)preference).setVideoPath(files[0]); // Saves the text to the SharedPreferences.
                }
                else if (preference instanceof FileDialogPreference) {
                    ((FileDialogPreference)preference).setPath(files[0]); // Saves the text to the SharedPreferences.
                }
            }
        });
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
    }

    public static void postFileDialog(final Context context, final Preference preference, final String extension, final boolean isSelectFile)
    {
        boolean exStorageFound = false;
        do {
            File pStorage = new File("/storage/");
            if (pStorage != null && pStorage.exists() && pStorage.canRead()) {
                File[] list = pStorage.listFiles();
                if(list.length > 0) {
                    for(int i = 0 ; i < list.length; i++) {
                        boolean bName = !list[i].getName().equalsIgnoreCase("emulated") && !list[i].getName().equalsIgnoreCase("self");
                        boolean bR = list[i].canRead();

                        if(bName && bR) {
                            exStorageFound = true;
                            break;
                        }
                    }
                }
            }
        } while(false);

        if(exStorageFound) {
            ExternalStoragePickDialog dialog = new ExternalStoragePickDialog(context);
            dialog.setOnEventChangeListener(new ExternalStoragePickDialog.OnEventChangeListener() {
                @Override
                public void onItemClick(ExternalStoragePickDialog dialog, String storageName, String accesaPath) {
                    String rootPath = accesaPath.substring(0, accesaPath.indexOf("/Android/data"));
                    showFileDialog(context, preference, extension, rootPath, isSelectFile);
                }

                @Override
                public void onDismiss(ExternalStoragePickDialog dialog) {
                }
            });

            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.show();
        }
        else {
            showFileDialog(context, preference, extension, "/storage/emulated/0/", isSelectFile);
        }
    }

    public static void setClickToSelectFile(final Context context, final Preference pref, final String fextension) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                postFileDialog(context, pref, fextension, true);
                return false;
            }
        });
    }

    public static void setClickToSelectDirectory(final Context context, final Preference pref) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                postFileDialog(context, pref, "", false);
                return false;
            }
        });
    }

    public static void setClickDismiss(final Preference pref) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).getDialog().dismiss();
                }
                return false;
            }
        });
    }
}

package com.svb.toiletwall.fragment;

/**
 * Created by mbodis on 9/24/17.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.svb.toiletwall.R;
import com.svb.toiletwall.support.MyShPrefs;

import static android.content.Context.MODE_MULTI_PROCESS;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static String TAG = SettingsFragment.class.getName();

    public static SettingsFragment newInstance(Bundle args) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set preference type
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName(MyShPrefs.TS_PREFS);
        pm.setSharedPreferencesMode(MODE_MULTI_PROCESS);
        SharedPreferences sp = pm.getSharedPreferences();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        sp.registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onDestroy() {
        // unset preference type - file
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName(MyShPrefs.TS_PREFS);
        pm.setSharedPreferencesMode(MODE_MULTI_PROCESS);
        SharedPreferences sp = pm.getSharedPreferences();

        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // do something
    }


}

package com.qualcode.catchafish;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SettingsAboutMe extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_about_me);
            setSummaries();
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
            setSummaries();
        }

        private void setSummaries() {

            final EditTextPreference etUserMsg = ((EditTextPreference)findPreference(getString(R.string.pref_key_user_msg)));
            etUserMsg.setSummary(etUserMsg.getText() == null || etUserMsg.getText().length() == 0 ? getString(R.string.default_display_msg) : etUserMsg.getText());

            final EditTextPreference etUserName = ((EditTextPreference)findPreference(getString(R.string.pref_key_user_name)));
            etUserName.setSummary(etUserName.getText());

            final ListPreference lpUserSex = ((ListPreference)findPreference(getString(R.string.pref_key_user_sex)));
            lpUserSex.setSummary(lpUserSex.getEntry());

            final EditTextPreference etUserAge = ((EditTextPreference)findPreference(getString(R.string.pref_key_user_age)));
            etUserAge.setSummary(etUserAge.getText());

            final ListPreference lpUserRace = ((ListPreference)findPreference(getString(R.string.pref_key_user_race)));
            lpUserRace.setSummary(lpUserRace.getEntry());

            final MultiSelectListPreference mlpUserInterests = ((MultiSelectListPreference)findPreference(getString(R.string.pref_key_user_interests)));
            mlpUserInterests.setSummary(mlpUserInterests.getEntries().toString());
        }
    }

}

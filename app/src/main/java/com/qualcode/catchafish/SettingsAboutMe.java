package com.qualcode.catchafish;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


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

            final EditTextPreference etDisplayMsg = ((EditTextPreference)findPreference(getString(R.string.pref_key_display_msg)));
            etDisplayMsg.setSummary(etDisplayMsg.getText() == null || etDisplayMsg.getText().length() == 0 ? getString(R.string.default_display_msg) : etDisplayMsg.getText());

            final ListPreference lpUserSex = ((ListPreference)findPreference(getString(R.string.pref_key_user_sex)));
            lpUserSex.setSummary(lpUserSex.getEntry());

            final EditTextPreference etUserAge = ((EditTextPreference)findPreference(getString(R.string.pref_key_user_age)));
            etUserAge.setSummary(etUserAge.getText());

            final ListPreference lpUserRace = ((ListPreference)findPreference(getString(R.string.pref_key_user_race)));

            //final MultiSelectListPreference mlpUserInterests = ((MultiSelectListPreference)findPreference(getString(R.string.pref_key_user_interests)));
            //mlpUserInterests.setSummary(mlpUserInterests.getEntries().toString());
        }
    }

}

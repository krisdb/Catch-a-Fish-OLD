package com.qualcode.catchafish;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;


public class SettingsLookingFor extends PreferenceActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }


    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_looking_for);
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

            EditTextPreference etLookingForMinAge = ((EditTextPreference)findPreference(getString(R.string.pref_key_looking_for_min_age)));
            etLookingForMinAge.setSummary(etLookingForMinAge.getText());

            EditTextPreference etLookingForMaxAge = ((EditTextPreference)findPreference(getString(R.string.pref_key_looking_for_max_age)));
            etLookingForMaxAge.setSummary(etLookingForMaxAge.getText());

            MultiSelectListPreference mlpLookingForRace = ((MultiSelectListPreference)findPreference(getString(R.string.pref_key_looking_for_race)));
            mlpLookingForRace.setSummary(mlpLookingForRace.getEntries().toString());
        }
    }
}

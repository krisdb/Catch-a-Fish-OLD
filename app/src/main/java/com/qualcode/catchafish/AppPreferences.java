package com.qualcode.catchafish;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class AppPreferences{

    private static String KEY_PREFS_USER_NAME, KEY_PREFS_USER_MSG, KEY_PREFS_USER_AGE, KEY_PREFS_USER_RACE, KEY_PREFS_USER_INTERESTS;
    private static String KEY_PREFS_USER_SEX, KEY_PREFS_LOOKING_FOR_SEX_MALE, KEY_PREFS_LOOKING_FOR_SEX_FEMALE;
    private static String KEY_PREFS_LOOKING_FOR_MIN_AGE, KEY_PREFS_LOOKING_FOR_MAX_AGE, KEY_PREFS_LOOKING_FOR_RACE;
    private SharedPreferences _sharedPrefs;
    //private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        KEY_PREFS_USER_MSG = context.getString(R.string.pref_key_user_msg);
        KEY_PREFS_USER_NAME = context.getString(R.string.pref_key_user_name);
        KEY_PREFS_USER_SEX = context.getString(R.string.pref_key_user_sex);
        KEY_PREFS_USER_AGE = context.getString(R.string.pref_key_user_age);
        KEY_PREFS_USER_RACE = context.getString(R.string.pref_key_user_race);
        KEY_PREFS_USER_INTERESTS = context.getString(R.string.pref_key_user_interests);

        KEY_PREFS_LOOKING_FOR_SEX_MALE = context.getString(R.string.pref_key_looking_for_male);
        KEY_PREFS_LOOKING_FOR_SEX_FEMALE = context.getString(R.string.pref_key_looking_for_female);
        KEY_PREFS_LOOKING_FOR_MIN_AGE = context.getString(R.string.pref_key_looking_for_min_age);
        KEY_PREFS_LOOKING_FOR_MAX_AGE = context.getString(R.string.pref_key_looking_for_max_age);
        KEY_PREFS_LOOKING_FOR_RACE = context.getString(R.string.pref_key_looking_for_race);

        this._sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //this._prefsEditor = _sharedPrefs.edit();
    }

    public String getUserMsg() { return _sharedPrefs.getString(KEY_PREFS_USER_MSG, ""); }

    public String getUserName() { return _sharedPrefs.getString(KEY_PREFS_USER_NAME, ""); }

    public int getUserSex() { return Integer.valueOf(_sharedPrefs.getString(KEY_PREFS_USER_SEX, "-1")); }

    public int getUserAge() { return Integer.valueOf(_sharedPrefs.getString(KEY_PREFS_USER_AGE, "18")); }

    public int getUserRace() { return Integer.valueOf(_sharedPrefs.getString(KEY_PREFS_USER_RACE, "-1")); }

    public String[] getLookingForInterests() {return _sharedPrefs.getStringSet(KEY_PREFS_USER_INTERESTS, new HashSet<String>()).toArray(new String[]{}); }

    public Boolean getLookingForMale() { return _sharedPrefs.getBoolean(KEY_PREFS_LOOKING_FOR_SEX_MALE, false); }

    public Boolean getLookingForFemale() { return _sharedPrefs.getBoolean(KEY_PREFS_LOOKING_FOR_SEX_FEMALE, false); }

    public int getLookingForMinAge() { return Integer.valueOf(_sharedPrefs.getString(KEY_PREFS_LOOKING_FOR_MIN_AGE, "18")); }


    public int getLookingForMaxAge() { return Integer.valueOf(_sharedPrefs.getString(KEY_PREFS_LOOKING_FOR_MAX_AGE, "99")); }

        public String[] getLookingForRace() { return _sharedPrefs.getStringSet(KEY_PREFS_LOOKING_FOR_RACE, new HashSet<String>()).toArray(new String[]{}); }

    public void saveUserSex(String text) {
        //_prefsEditor.putString(KEY_PREFS_USER_SEX, text);
        //_prefsEditor.commit();
    }

}

package com.qualcode.catchafish;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences{

    public static String KEY_PREFS_USER_NAME, KEY_PREFS_LOOKING_FOR_NAME;
    public static String KEY_PREFS_USER_SEX, KEY_PREFS_LOOKING_FOR_SEX_MALE, KEY_PREFS_LOOKING_FOR_SEX_FEMALE;
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName();
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        KEY_PREFS_USER_SEX = context.getString(R.string.pref_key_user_sex);
        KEY_PREFS_USER_NAME = context.getString(R.string.pref_key_user_name);
        KEY_PREFS_LOOKING_FOR_SEX_MALE = context.getString(R.string.pref_key_looking_for_male);
        KEY_PREFS_LOOKING_FOR_SEX_FEMALE = context.getString(R.string.pref_key_looking_for_female);

        this._sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public Boolean getLookingForMale() {
        return _sharedPrefs.getBoolean(KEY_PREFS_LOOKING_FOR_SEX_MALE, false);
    }

    public Boolean getLookingForFemale() {
        return _sharedPrefs.getBoolean(KEY_PREFS_LOOKING_FOR_SEX_FEMALE, false);
    }

    public String getUserName() {
        return _sharedPrefs.getString(KEY_PREFS_USER_NAME, "");
    }

    public String getUserSex() {
        return _sharedPrefs.getString(KEY_PREFS_USER_SEX, "");
    }

    public void saveUserSex(String text) {
        _prefsEditor.putString(KEY_PREFS_USER_SEX, text);
        _prefsEditor.commit();
    }

}

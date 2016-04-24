package com.qualcode.catchafish;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences{

    public static String KEY_PREFS_USER_SEX;
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName();
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        KEY_PREFS_USER_SEX = context.getString(R.string.pref_key_user_sex);
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public String getUserSex() {
        return _sharedPrefs.getString(KEY_PREFS_USER_SEX, "");
    }

    public void saveUserSex(String text) {
        _prefsEditor.putString(KEY_PREFS_USER_SEX, text);
        _prefsEditor.commit();
    }

}

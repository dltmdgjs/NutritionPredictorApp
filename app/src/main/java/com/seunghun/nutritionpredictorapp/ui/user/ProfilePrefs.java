package com.seunghun.nutritionpredictorapp.ui.user;

import android.content.Context;
import android.content.SharedPreferences;

public class ProfilePrefs {
    private static final String PREF = "profile_prefs";
    private static final String KEY_PROFILE_PATH = "profile_image_path";

    public static void saveProfilePath(Context context, String path) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PROFILE_PATH, path).apply();
    }

    public static String getProfilePath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_PROFILE_PATH, null);
    }
}

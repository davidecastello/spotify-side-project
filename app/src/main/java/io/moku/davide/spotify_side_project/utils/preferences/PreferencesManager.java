package io.moku.davide.spotify_side_project.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Davide Castello on 16/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */

public class PreferencesManager {

    private static final String SHARED_PREFERENCES_NAME = "shared_preferences";
    private static final String ACCESS_TOKEN = "access_token";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String getAccessToken(Context context) {
        return getPrefs(context).getString(ACCESS_TOKEN, null);
    }

    public static void storeAccessToken(Context context, String token) {
        getPrefs(context).edit()
                .putString(ACCESS_TOKEN, token)
                .apply();
    }

    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}
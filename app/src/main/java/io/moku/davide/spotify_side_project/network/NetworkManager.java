package io.moku.davide.spotify_side_project.network;

import android.content.Context;

import io.moku.davide.spotify_side_project.utils.preferences.PreferencesManager;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by Davide Castello on 23/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */

public class NetworkManager {

    public static SpotifyService getService(Context context) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(PreferencesManager.getAccessToken(context));
        return api.getService();
    }
}

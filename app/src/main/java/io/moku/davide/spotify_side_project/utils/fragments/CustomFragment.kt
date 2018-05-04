package io.moku.davide.spotify_side_project.utils.fragments

import android.support.v4.app.Fragment
import kaaes.spotify.webapi.android.models.TrackSimple

/**
 * Created by Davide Castello on 09/03/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
abstract class CustomFragment : Fragment() {
    abstract fun updateView()
    abstract fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?)
    abstract fun updatePlayerInfo()
    abstract fun updatePlayButton()
}
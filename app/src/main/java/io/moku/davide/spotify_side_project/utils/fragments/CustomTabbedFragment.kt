package io.moku.davide.spotify_side_project.utils.fragments

import android.support.v4.app.Fragment
import kaaes.spotify.webapi.android.models.Track

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
abstract class CustomTabbedFragment : Fragment() {
    abstract fun updateView()
    abstract fun notifySongs(oldSong: Track?, currentSong: Track?)
    abstract fun back()
    abstract fun canHandleBack() : Boolean
}
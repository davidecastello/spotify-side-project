package io.moku.davide.spotify_side_project.utils

import android.support.v4.app.Fragment

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
abstract class CustomFragment : Fragment() {
    abstract fun updateView()
}
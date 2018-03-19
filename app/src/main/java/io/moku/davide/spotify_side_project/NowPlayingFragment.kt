package io.moku.davide.spotify_side_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
import kaaes.spotify.webapi.android.models.TrackSimple

/**
 * Created by Davide Castello on 06/03/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class NowPlayingFragment : CustomFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_now_playing, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // here you can use the UI fields
    }

    override fun updateView() {
        if (isAdded) {
            // TODO update the UI
        }
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        // no need for now
    }

    companion object {

        val TAG = NowPlayingFragment::class.simpleName

        fun newInstance(): NowPlayingFragment {

            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)

            val fragment = NowPlayingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun getMainActivity() : MainActivity = activity as MainActivity

    override fun updatePlayButton() {
        // no need for now
    }

    override fun updatePlayerInfo() {
        // no need for now
    }

    override fun enablePlayer(enable: Boolean) {
        // no need for now
    }
}
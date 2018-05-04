package io.moku.davide.spotify_side_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_now_playing.*

/**
 * Created by Davide Castello on 06/03/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class NowPlayingFragment : CustomFragment() {

    companion object {
        /* Constants */
        val TAG = NowPlayingFragment::class.simpleName

        fun newInstance(): NowPlayingFragment {
            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)
            val fragment = NowPlayingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_now_playing, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
        _updateView()
    }

    override fun updateView() {
        if (isAdded) {
            _updateView()
        }
    }

    fun setup() {
        setListeners()
    }

    fun setListeners() {
        nowPlayingTopLayout.setOnClickListener({ getMainActivity().toggleNowPlaying() })
        nowPlayingPlayButton.setOnClickListener({ playButtonPressed() })
        nowPlayingPrevButton.setOnClickListener({ getMainActivity().prev() })
        nowPlayingNextButton.setOnClickListener({ getMainActivity().next() })
    }

    fun playButtonPressed() {
        if (getMainActivity().isPlaying) {
            nowPlayingPlayButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_play_circle))
        } else {
            nowPlayingPlayButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_pause_circle))
        }
        getMainActivity()._playButtonPressed()
    }

    fun _updateView() {
        updatePlayButton()
        updatePlayerInfo()
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        // no need for now
    }

    fun getMainActivity() : MainActivity = activity as MainActivity

    override fun updatePlayButton() {
        if (getMainActivity().isPlaying) {
            nowPlayingPlayButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_pause_circle))
        } else {
            nowPlayingPlayButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_play_circle))
        }
    }

    override fun updatePlayerInfo() {
        // updating its info
        nowPlayingCurrentTrackTV.text = getMainActivity().currentTrack?.name
        nowPlayingCurrentTrackArtistTV.text = getMainActivity().currentTrack?.artist()
    }

    fun TrackSimple.artist() : String = artists.map { it -> it.name }.joinToString(separator = ", ")

}
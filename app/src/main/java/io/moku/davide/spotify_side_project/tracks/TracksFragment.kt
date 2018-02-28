package io.moku.davide.spotify_side_project.tracks

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.SavedTracksAdapter
import io.moku.davide.spotify_side_project.network.NetworkManager
import io.moku.davide.spotify_side_project.utils.CustomFragment
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.SavedTrack
import kotlinx.android.synthetic.main.fragment_tracks.*
import retrofit.client.Response

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class TracksFragment : CustomFragment() {

    private var savedTracksAdapter: SavedTracksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tracks, container, false)
        updateView()
        return view
    }

    override fun updateView() {
        if (isAdded) {
            // retrieve saved tracks
            tryToRetrieveSavedTracks()
        }
    }

    companion object {

        val TAG = TracksFragment::class.simpleName

        fun newInstance(): TracksFragment {

            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)

            val fragment = TracksFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun getMainActivity() : MainActivity = activity as MainActivity

    /**
     *
     * NETWORK
     *
     */

    fun tryToRetrieveSavedTracks() {
        NetworkManager.getService(context).getMySavedTracks(mapOf(Pair("limit", 20)), object : SpotifyCallback<Pager<SavedTrack>>() {
            override fun success(savedTrackPager: Pager<SavedTrack>, response: Response) {
                savedTracksDownloaded(savedTrackPager.items)
            }

            override fun failure(error: SpotifyError) {
                handleNetworkError(error)
            }
        })
    }

    fun savedTracksDownloaded(savedTracks: List<SavedTrack>) {
        savedTracksAdapter = SavedTracksAdapter(activity, savedTracks)
        savedTracksRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedTracksRV.adapter = savedTracksAdapter
        getMainActivity().enablePlayer(true)
    }

    fun handleNetworkError(error: SpotifyError) {
        Log.e(TAG, error.message)
        if (error.hasErrorDetails() && error.errorDetails.status == 401) {
            getMainActivity().openLogin()
        }
    }

}
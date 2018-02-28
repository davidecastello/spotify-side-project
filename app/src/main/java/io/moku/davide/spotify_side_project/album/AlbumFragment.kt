package io.moku.davide.spotify_side_project.album

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.CustomFragment
import kotlinx.android.synthetic.main.fragment_album.view.*

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumFragment : CustomFragment() {

    // TODO ALBUM RV private var savedTracksAdapter: SavedTracksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        updateView()
        return view
    }

    override fun updateView() {
        // retrieve albums
        //tryToRetrieveSavedTracks()
    }

    companion object {

        val TAG = AlbumFragment::class.simpleName

        fun newInstance(): AlbumFragment {

            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)

            val fragment = AlbumFragment()
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

    /*fun tryToRetrieveSavedTracks() {
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
    }*/

}
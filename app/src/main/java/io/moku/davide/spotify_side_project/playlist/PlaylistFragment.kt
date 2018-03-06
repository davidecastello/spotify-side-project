package io.moku.davide.spotify_side_project.playlist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.network.NetworkManager
import io.moku.davide.spotify_side_project.utils.CustomFragment
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_playlist.*
import retrofit.client.Response

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class PlaylistFragment : CustomFragment() {

    private var savedPlaylistsAdapter: SavedPlaylistsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        updateView()
        return view
    }

    override fun updateView() {
        if (isAdded) {
            // retrieve playlists
            tryToRetrievePlaylists()
        }
    }

    override fun onResume() {
        super.onResume()
        savedPlaylistsAdapter?.notifyDataSetChanged()
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        val TAG = PlaylistFragment::class.simpleName

        fun newInstance(): PlaylistFragment {

            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)

            val fragment = PlaylistFragment()
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

    fun tryToRetrievePlaylists() {
        NetworkManager.getService(context).getMyPlaylists(mapOf(Pair("limit", 20)), object : SpotifyCallback<Pager<PlaylistSimple>>() {
            override fun success(savedPlaylistPager: Pager<PlaylistSimple>, response: Response) {
                savedPlaylistsDownloaded(savedPlaylistPager.items)
            }

            override fun failure(error: SpotifyError) {
                handleNetworkError(error)
            }
        })
    }

    fun savedPlaylistsDownloaded(savedPlaylists: List<PlaylistSimple>) {
        savedPlaylistsAdapter = SavedPlaylistsAdapter(activity, savedPlaylists)
        savedPlaylistRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedPlaylistRV.adapter = savedPlaylistsAdapter
        getMainActivity().enablePlayer(true)
    }

    fun handleNetworkError(error: SpotifyError) {
        Log.e(TAG, error.message)
        if (error.hasErrorDetails() && error.errorDetails.status == 401) {
            getMainActivity().openLogin()
        }
    }

}
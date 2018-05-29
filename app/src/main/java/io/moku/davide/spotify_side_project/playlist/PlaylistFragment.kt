package io.moku.davide.spotify_side_project.playlist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.moku.davide.spotify_side_project.Constants
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.network.NetworkManager
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
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
class PlaylistFragment : CustomTabbedFragment() {

    private var savedPlaylistsAdapter: SavedPlaylistsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateView()
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
        // TODO PlaylistFragment.notifySongs()
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
        if (savedPlaylistsAdapter != null) {
            if (savedPlaylistRV.adapter != null) {
                savedPlaylistsAdapter?.notifyDataSetChanged()
            } else {
                initRecyclerView()
            }
        } else {
            retrievePlaylistsFromNetwork(0)
        }
    }

    fun retrievePlaylistsFromNetwork(currentOffsetMultiplier: Int) {
        val limit = Constants.PLAYLIST_SINGLE_CALL_LIMIT
        val currentOffset = currentOffsetMultiplier * limit
        NetworkManager.getService(context).getMyPlaylists(
                mapOf(
                        Pair(Constants.QUERY_PARAMETER_LIMIT, limit),
                        Pair(Constants.QUERY_PARAMETER_OFFSET, currentOffset)),
                object : SpotifyCallback<Pager<PlaylistSimple>>() {
                    override fun success(savedPlaylistPager: Pager<PlaylistSimple>, response: Response) {
                        savedPlaylistsDownloaded(savedPlaylistPager, currentOffsetMultiplier)
                    }

                    override fun failure(error: SpotifyError) {
                        handleNetworkError(error)
                    }
        })
    }

    fun savedPlaylistsDownloaded(savedPlaylistPager: Pager<PlaylistSimple>, currentOffsetMultiplier: Int) {
        if (isAdded) {
            val limit = Constants.PLAYLIST_SINGLE_CALL_LIMIT
            if (savedPlaylistsAdapter == null) {
                savedPlaylistsAdapter = SavedPlaylistsAdapter(activity, ArrayList())
            }
            savedPlaylistsAdapter?.savedPlaylists?.addAll(savedPlaylistPager.items)
            // notify the adapter that we added some albums to the list
            savedPlaylistsAdapter?.notifyItemRangeInserted(currentOffsetMultiplier * limit,
                    savedPlaylistPager.items.size)

            if (savedPlaylistRV.adapter == null) {
                initRecyclerView()
            }
            // retrieve more albums from the network
            if (savedPlaylistPager.next != null) {
                retrievePlaylistsFromNetwork(currentOffsetMultiplier + 1)
            }
        }
    }

    fun initRecyclerView() {
        savedPlaylistRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedPlaylistRV.adapter = savedPlaylistsAdapter
    }

    fun handleNetworkError(error: SpotifyError) {
        Log.e(TAG, error.message)
        if (error.hasErrorDetails() && error.errorDetails.status == 401) {
            getMainActivity().openLogin()
        }
    }

}
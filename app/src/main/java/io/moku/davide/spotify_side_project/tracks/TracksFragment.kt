package io.moku.davide.spotify_side_project.tracks

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
import kaaes.spotify.webapi.android.models.SavedTrack
import kaaes.spotify.webapi.android.models.Track
import kotlinx.android.synthetic.main.fragment_tracks.*
import retrofit.client.Response

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class TracksFragment : CustomTabbedFragment() {

    private var savedTracksAdapter: SavedTracksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateView()
    }

    override fun updateView() {
        if (isAdded) {
            // retrieve saved tracks
            tryToRetrieveSavedTracks()
        }
    }

    override fun onResume() {
        super.onResume()
        savedTracksAdapter?.notifyDataSetChanged()
    }

    override fun notifySongs(oldSong: Track?, currentSong: Track?) {
        savedTracksAdapter?.notifyItemChangedIfPresent(oldSong?.uri)
        savedTracksAdapter?.notifyItemChangedIfPresent(currentSong?.uri)
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
        if (savedTracksAdapter != null) {
            if (savedTracksRV.adapter != null) {
                savedTracksAdapter?.notifyDataSetChanged()
            } else {
                initRecyclerView()
            }
        } else {
            retrieveSavedTracksFromNetwork(0)
        }
    }

    fun retrieveSavedTracksFromNetwork(currentOffsetMultiplier: Int) {
        val limit = Constants.SAVED_TRACKS_SINGLE_CALL_LIMIT
        val currentOffset = currentOffsetMultiplier * limit
        NetworkManager.getService(context).getMySavedTracks(
                mapOf(
                        Pair(Constants.QUERY_PARAMETER_LIMIT, limit),
                        Pair(Constants.QUERY_PARAMETER_OFFSET, currentOffset)),
                object : SpotifyCallback<Pager<SavedTrack>>() {
            override fun success(savedTrackPager: Pager<SavedTrack>, response: Response) {
                savedTracksDownloaded(savedTrackPager, currentOffsetMultiplier)
            }

            override fun failure(error: SpotifyError) {
                handleNetworkError(error)
            }
        })
    }

    fun savedTracksDownloaded(savedTrackPager: Pager<SavedTrack>, currentOffsetMultiplier: Int) {
        if (isAdded) {
            val limit = Constants.SAVED_TRACKS_SINGLE_CALL_LIMIT
            if (savedTracksAdapter == null) {
                savedTracksAdapter = SavedTracksAdapter(activity, ArrayList())
            }
            savedTracksAdapter?.savedTracks?.addAll(savedTrackPager.items)
            // notify the adapter that we added some tracks to the list
            savedTracksAdapter?.notifyItemRangeInserted(currentOffsetMultiplier * limit,
                    savedTrackPager.items.size)

            if (savedTracksRV.adapter == null) {
                initRecyclerView()
            }
            // retrieve more tracks from the network
            if (savedTrackPager.next != null) {
                retrieveSavedTracksFromNetwork(currentOffsetMultiplier + 1)
            }
        }
    }

    fun initRecyclerView() {
        savedTracksRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedTracksRV.adapter = savedTracksAdapter
    }

    fun handleNetworkError(error: SpotifyError) {
        Log.e(TAG, error.message)
        if (error.hasErrorDetails() && error.errorDetails.status == 401) {
            getMainActivity().openLogin()
        }
    }

    override fun back() {
        // do nothing for now
    }

    override fun canHandleBack() : Boolean = false

}
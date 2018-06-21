package io.moku.davide.spotify_side_project.album

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
import kaaes.spotify.webapi.android.models.SavedAlbum
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_album_list.*
import retrofit.client.Response
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 *
 */
class AlbumListFragment : CustomTabbedFragment() {

    private var savedAlbumsAdapter: SavedAlbumsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateView()
    }

    override fun updateView() {
        if (isAdded) {
            // retrieve albums
            tryToRetrieveAlbums()
        }
    }

    override fun onResume() {
        super.onResume()
        savedAlbumsAdapter?.notifyDataSetChanged()
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        // do nothing
    }

    companion object {

        val TAG = AlbumListFragment::class.simpleName

        fun newInstance(): AlbumListFragment {

            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)

            val fragment = AlbumListFragment()
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

    fun tryToRetrieveAlbums() {
        if (savedAlbumsAdapter != null) {
            if (savedAlbumsRV.adapter != null) {
                savedAlbumsAdapter?.notifyDataSetChanged()
            } else {
                initRecyclerView()
            }
        } else {
            retrieveAlbumsFromNetwork(0)
        }
    }

    fun retrieveAlbumsFromNetwork(currentOffsetMultiplier: Int) {
        val limit = Constants.ALBUM_SINGLE_CALL_LIMIT
        val currentOffset = currentOffsetMultiplier * limit
        NetworkManager.getService(context).getMySavedAlbums(
                mapOf(
                        Pair(Constants.QUERY_PARAMETER_LIMIT, limit),
                        Pair(Constants.QUERY_PARAMETER_OFFSET, currentOffset)),
                object : SpotifyCallback<Pager<SavedAlbum>>() {
                    override fun success(savedAlbumPager: Pager<SavedAlbum>, response: Response) {
                        savedAlbumsDownloaded(savedAlbumPager, currentOffsetMultiplier)
                    }

                    override fun failure(error: SpotifyError) {
                        handleNetworkError(error)
                    }
                })
    }

    fun savedAlbumsDownloaded(savedAlbumPager: Pager<SavedAlbum>, currentOffsetMultiplier: Int) {
        if (isAdded) {
            val limit = Constants.ALBUM_SINGLE_CALL_LIMIT
            if (savedAlbumsAdapter == null) {
                savedAlbumsAdapter = SavedAlbumsAdapter(activity, parentFragment as AlbumFragment, ArrayList())
            }
            savedAlbumsAdapter?.savedAlbums?.addAll(savedAlbumPager.items)
            // notify the adapter that we added some albums to the list
            savedAlbumsAdapter?.notifyItemRangeInserted(currentOffsetMultiplier * limit,
                    savedAlbumPager.items.size)

            if (savedAlbumsRV.adapter == null) {
                initRecyclerView()
            }
            // retrieve more albums from the network
            if (savedAlbumPager.next != null) {
                retrieveAlbumsFromNetwork(currentOffsetMultiplier + 1)
            }
        }
    }

    fun initRecyclerView() {
        savedAlbumsRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedAlbumsRV.adapter = savedAlbumsAdapter
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
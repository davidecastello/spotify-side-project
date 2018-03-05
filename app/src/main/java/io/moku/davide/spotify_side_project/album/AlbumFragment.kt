package io.moku.davide.spotify_side_project.album

import android.os.Bundle
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
import kaaes.spotify.webapi.android.models.SavedAlbum
import kotlinx.android.synthetic.main.fragment_album.*
import retrofit.client.Response

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumFragment : CustomFragment() {

    private var savedAlbumsAdapter: SavedAlbumsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        updateView()
        return view
    }

    override fun updateView() {
        if (isAdded) {
            // retrieve albums
            tryToRetrieveAlbums()
        }
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

    fun tryToRetrieveAlbums() {
        NetworkManager.getService(context).getMySavedAlbums(mapOf(Pair("limit", 20)), object : SpotifyCallback<Pager<SavedAlbum>>() {
            override fun success(savedAlbumPager: Pager<SavedAlbum>, response: Response) {
                savedAlbumsDownloaded(savedAlbumPager.items)
            }

            override fun failure(error: SpotifyError) {
                handleNetworkError(error)
            }
        })
    }

    fun savedAlbumsDownloaded(savedAlbums: List<SavedAlbum>) {
        savedAlbumsAdapter = SavedAlbumsAdapter(activity, savedAlbums)
        savedAlbumsRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        savedAlbumsRV.adapter = savedAlbumsAdapter
        getMainActivity().enablePlayer(true)
    }

    fun handleNetworkError(error: SpotifyError) {
        Log.e(TAG, error.message)
        if (error.hasErrorDetails() && error.errorDetails.status == 401) {
            getMainActivity().openLogin()
        }
    }

}
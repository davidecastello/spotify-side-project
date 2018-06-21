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
import kaaes.spotify.webapi.android.models.*
import retrofit.client.Response
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumFragment : CustomTabbedFragment() {

    /* Fields */
    private var isAlbumPageFragmentVisible = false

    /**
     *
     * FRAGMENTS
     *
     */

    fun currentFragment() : CustomTabbedFragment = childFragmentManager.findFragmentByTag(currentTag()) as CustomTabbedFragment

    fun currentTag() = if (isAlbumPageFragmentVisible) AlbumPageFragment.TAG else AlbumListFragment.TAG

    fun loadAlbumListFragment() {
        childFragmentManager.beginTransaction()
                .add(R.id.albumContainer, AlbumListFragment.newInstance(), AlbumListFragment.TAG)
                .commit()
    }

    fun showAlbumPageFragment(album: Album) {
        if (!isAlbumPageFragmentVisible) {
            isAlbumPageFragmentVisible = true
            // Hide AlbumListFragment
            val albumListFragment = childFragmentManager.findFragmentByTag(AlbumListFragment.TAG)
            if (albumListFragment.isAdded && !albumListFragment.isHidden) {
                childFragmentManager.beginTransaction().hide(albumListFragment).commit()
            }
            // Show AlbumPageFragment
            childFragmentManager.beginTransaction()
                    .add(R.id.albumContainer, AlbumPageFragment.newInstance(album), AlbumPageFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }

    fun hideAlbumPageFragment() {
        if (isAlbumPageFragmentVisible) {
            isAlbumPageFragmentVisible = false
            // Hide AlbumPageFragment
            childFragmentManager.beginTransaction()
                    .remove(childFragmentManager.findFragmentByTag(AlbumPageFragment.TAG))
                    .commit()
            // Show AlbumListFragment
            val albumListFragment = childFragmentManager.findFragmentByTag(AlbumListFragment.TAG)
            if (albumListFragment.isAdded && albumListFragment.isHidden) {
                childFragmentManager.beginTransaction().show(albumListFragment).commit()
                (albumListFragment as CustomTabbedFragment).updateView()
            }
        }
    }

    override fun back() {
        hideAlbumPageFragment()
    }

    override fun canHandleBack() : Boolean = isAlbumPageFragmentVisible

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAlbumListFragment()
    }

    override fun updateView() {
        if (isAdded) {
            val currentFragment = currentFragment()
            if (currentFragment.isAdded && !currentFragment.isHidden) {
                currentFragment.updateView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun notifySongs(oldSong: Track?, currentSong: Track?) {
        if (isAdded) {
            val currentFragment = currentFragment()
            if (currentFragment.isAdded && !currentFragment.isHidden) {
                currentFragment.notifySongs(oldSong, currentSong)
            }
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

}
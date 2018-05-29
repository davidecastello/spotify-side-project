package io.moku.davide.spotify_side_project

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import io.moku.davide.spotify_side_project.album.AlbumFragment
import io.moku.davide.spotify_side_project.playlist.PlaylistFragment
import io.moku.davide.spotify_side_project.tracks.TracksFragment
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

/**
 * Created by Davide Castello on 06/03/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class MainFragment : CustomFragment() {

    companion object {
        /* Constants */
        val TAG = MainFragment::class.simpleName
        val SECONDS = 1000
        // Tabs
        val TAB_TRACKS = 0
        val TAB_ALBUMS = 1
        val TAB_PLAYLISTS = 2

        fun newInstance(): MainFragment {
            val args = Bundle()
            //args.putString(MovieHelper.KEY_TITLE, movie.title)
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }

    /* Fields */
    private var prevMenuItem : MenuItem? = null
    private var isPlayerVisible = false
    var whosPlaying : WhosPlaying? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup view pager
        setupViewPager()
        // listeners
        setListeners()
    }

    override fun updateView() {
        if (isAdded) {
            (viewpager.adapter as MainFragmentPagerAdapter).updateFragments()

            updatePlayerInfo()
            updatePlayButton()
        }
    }


    fun getMainActivity() : MainActivity = activity as MainActivity

    fun setListeners() {
        playButton.setOnClickListener({ playButtonPressed() })
        expandButton.setOnClickListener({ getMainActivity().toggleNowPlaying() })
        currentTrackInfoLayout.setOnClickListener({ getMainActivity().toggleNowPlaying() })
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemTracks -> {
                    viewpager.setCurrentItem(TAB_TRACKS)
                }
                R.id.itemAlbum -> {
                    viewpager.setCurrentItem(TAB_ALBUMS)
                }
                R.id.itemPlaylist -> {
                    viewpager.setCurrentItem(TAB_PLAYLISTS)
                }
            }
            false
        }
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //
            }
            override fun onPageScrollStateChanged(state: Int) {
                //
            }
            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    (prevMenuItem as MenuItem).setChecked(true)
                } else {
                    bottom_navigation.menu.getItem(0).setChecked(false)
                }
                Log.d(TAG, "onPageSelected: $position")
                bottom_navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = bottom_navigation.menu.getItem(position)
            }
        })
    }

    fun setupViewPager() {
        val adapter = MainFragmentPagerAdapter(getMainActivity().supportFragmentManager)
        adapter.addFragments(listOf(
                TracksFragment.newInstance(),
                AlbumFragment.newInstance(),
                PlaylistFragment.newInstance()))
        viewpager.adapter = adapter
        viewpager.setCurrentItem(TAB_TRACKS)
    }

    /**
     *
     * UI
     *
     */

    private fun enableButton(button: ImageButton?, enable: Boolean) {
        button?.isEnabled = enable
        button?.alpha = if (enable) 1.0f else 0.3f
    }

    fun enableTextView(textView: TextView?, enable: Boolean) {
        textView?.alpha = if (enable) 1.0f else 0.3f
    }

    fun playButtonPressed() {
        if (getMainActivity().isPlaying) {
            playButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_play_circle))
        } else {
            playButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_pause_circle))
        }
        getMainActivity()._playButtonPressed()
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        if (needsUpdate()) {
            (viewpager.adapter as MainFragmentPagerAdapter).getFragment(viewpager.currentItem).notifySongs(oldSong, currentSong)
        }
    }

    fun needsUpdate() : Boolean {
        when (viewpager.currentItem) {
            TAB_TRACKS -> return whosPlaying == WhosPlaying.MY_TRACKS
            TAB_ALBUMS -> return whosPlaying == WhosPlaying.MY_ALBUMS
            TAB_PLAYLISTS -> return whosPlaying == WhosPlaying.MY_PLAYLISTS
        }
        return false
    }

    override fun updatePlayerInfo() {
        // showing the player only if not already visible
        showPlayer()
        // updating its info
        currentTrackTV.text = getMainActivity().currentTrack?.name
        currentTrackArtistTV.text = getMainActivity().currentTrack?.artist()
    }

    fun TrackSimple.artist() : String = artists.map { it -> it.name }.joinToString(separator = ", ")

    fun showPlayer() {
        if (!isPlayerVisible) {
            playerLayout.visibility = View.VISIBLE
            line2.visibility = View.VISIBLE
            isPlayerVisible = true
        }
    }

    override fun updatePlayButton() {
        if (getMainActivity().isPlaying) {
            playButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_pause_circle))
        } else {
            playButton.setImageDrawable(getMainActivity().getDrawable(R.drawable.ic_play_circle))
        }
    }









}
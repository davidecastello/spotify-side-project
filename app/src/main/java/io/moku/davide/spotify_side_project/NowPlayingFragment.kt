package io.moku.davide.spotify_side_project

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import butterknife.OnPageChange
import com.gigamole.infinitecycleviewpager.OnInfiniteCyclePageTransformListener
import io.moku.davide.spotify_side_project.nowPlaying.ArtworksPagerAdapter
import io.moku.davide.spotify_side_project.utils.fragments.CustomFragment
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
        setupArtworksViewPager()
        setArtworksPageListener()
    }

    fun setListeners() {
        nowPlayingTopLayout.setOnClickListener({ getMainActivity().toggleNowPlaying() })
        nowPlayingPlayButton.setOnClickListener({ playButtonPressed() })
        nowPlayingPrevButton.setOnClickListener({ getMainActivity().prev(true) })
        nowPlayingNextButton.setOnClickListener({ getMainActivity().next() })
    }

    fun setupArtworksViewPager() {
        artworksViewPager.adapter = ArtworksPagerAdapter(getMainActivity())
        artworksViewPager.interpolator = object : Interpolator {
            private val FACTOR = 1.0f
            override fun getInterpolation(input: Float): Float {
                return (Math.pow(2.0, (-10.0f * input).toDouble()) * Math.sin((input - FACTOR / 4.0f) * (2.0f * Math.PI) / FACTOR) + 1.0f).toFloat()
            }
        }
    }

    fun setArtworksPageListener() {
        artworksViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            /**
             * This method will be invoked when a new page becomes selected.
             * Animation is not necessarily complete.
             * @param position Position index of the new selected page.
             */
            override fun onPageSelected(position: Int) {
                // Listener to handle the swipe
                val currentPosition = artworksViewPager.realItem
                val currentTrackPosition = getMainActivity().getCurrentTrackPositionInQueue()
                val last = getMainActivity().queue.size - 1
                // Really important check to avoid loops: onPageSelected is called also when you
                // update the artwork after the getMainActivity().next() or prev() method call
                if (currentTrackPosition != currentPosition) {
                    if (currentTrackPosition == last && currentPosition == 0) {
                        getMainActivity().next()
                    } else if (currentTrackPosition == 0 && currentPosition == last) {
                        getMainActivity().prev(false)
                    } else if (currentPosition > currentTrackPosition) {
                        getMainActivity().next()
                    } else {
                        getMainActivity().prev(false)
                    }
                }
            }
        })
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

    fun updateArtworksViewPager() {
        artworksViewPager.setCurrentItem(artworksViewPager.adapter.getItemPosition(getMainActivity().currentTrack))
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
        updateArtworksViewPager()
    }

    fun TrackSimple.artist() : String = artists.map { it -> it.name }.joinToString(separator = ", ")

}
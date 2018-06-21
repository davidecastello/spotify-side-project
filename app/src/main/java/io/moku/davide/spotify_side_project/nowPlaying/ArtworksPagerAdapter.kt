package io.moku.davide.spotify_side_project.nowPlaying

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.assets.ImagesUtils
import kaaes.spotify.webapi.android.models.Track
import kotlinx.android.synthetic.main.artwork_cell.view.*

/**
 * Created by Davide Castello on 24/05/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class ArtworksPagerAdapter(var mainActivity: MainActivity) : PagerAdapter() {

    override fun getCount(): Int = mainActivity.queue.size
    override fun getItemPosition(`object`: Any?): Int = mainActivity.getTrackPositionInQueue((`object` as Track).uri)
    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val view = mainActivity.layoutInflater.inflate(R.layout.artwork_cell, container, false)
        setupItem(view, mainActivity.queue.get(position))
        container?.addView(view)
        return view
    }
    fun setupItem(view: View?, track: Track) {
        if (track.album != null) {
            val images = track.album.images
            val coverUrl = images.first().url
            ImagesUtils.loadUrlIntoImageView(coverUrl, view?.context, view?.albumCover, R.drawable.ic_album_white_24dp, false)
        } else {
            view?.albumCover?.setImageResource(R.drawable.ic_album_black_24dp)
        }
    }
    override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view!!.equals(`object`)
    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }
}
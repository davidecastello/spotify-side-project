package io.moku.davide.spotify_side_project.album

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import io.moku.davide.spotify_side_project.Constants
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.assets.ImagesUtils
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.SavedAlbum
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_album_page.*

/**
 * Created by Davide Castello on 30/05/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumPageFragment : CustomTabbedFragment() {

    private var album: Album? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_page, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        album = arguments.get(ALBUM) as Album
        setListeners()
        updateView()
    }

    override fun onDestroyView() {
        (activity as AppCompatActivity).setSupportActionBar(null)
        super.onDestroyView()
    }

    fun setListeners() {
        //albumPageBackButton.setOnClickListener({ (parentFragment as AlbumFragment).hideAlbumPageFragment() })
    }

    override fun updateView() {
        if (isAdded) {
            // TODO create the UI and update it with:
            album?.name
            album?.artist()
            album?.coverUrl()
            album?.durationInMins()
            album?.year()
            album?.tracks?.items

            collapsing_toolbar.title = album?.name
            ImagesUtils.loadUrlIntoImageView(album?.coverUrl(), context, backdrop, R.drawable.ic_album_white_24dp, false)
        }
    }

    override fun onResume() {
        super.onResume()
        //albumSongsAdapter?.notifyDataSetChanged()
    }

    override fun notifySongs(oldSong: TrackSimple?, currentSong: TrackSimple?) {
        // TODO AlbumPageFragment.notifySongs()
    }

    override fun back() {
        // do nothing for now
    }

    override fun canHandleBack() : Boolean = false

    companion object {

        val TAG = AlbumPageFragment::class.simpleName
        val ALBUM = "album"

        fun newInstance(album: Album): AlbumPageFragment {

            val args = Bundle()
            args.putParcelable(ALBUM, album)

            val fragment = AlbumPageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun getMainActivity() : MainActivity = activity as MainActivity

    /* Useful extensions */
    fun Album.artist() : String = artists.map { it -> it.name }.joinToString(separator = ", ")
    fun Album.smallCoverUrl() : String = images.last().url
    fun Album.coverUrl() : String = images.first().url
    fun Album.durationInSecs() : Int = (tracks.items.map { it -> it.duration_ms }.sum() / Constants.SECONDS).toInt()
    fun Album.durationInMins() : Int = durationInSecs() / Constants.SECONDS
    fun Album.year() : String = release_date.substringBefore("-")
}
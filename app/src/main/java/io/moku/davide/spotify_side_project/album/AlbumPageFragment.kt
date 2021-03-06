package io.moku.davide.spotify_side_project.album

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toolbar
import io.moku.davide.spotify_side_project.Constants
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.assets.ImagesUtils
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment
import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.SavedAlbum
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.fragment_album_page.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Davide Castello on 30/05/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumPageFragment : CustomTabbedFragment() {

    private var album: Album? = null
    private var albumTracksAdapter: AlbumTracksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_page, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        album = arguments.get(ALBUM) as Album
        setListeners()
        updateView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun setListeners() {
        albumPageBackButton.setOnClickListener({ (parentFragment as AlbumFragment).hideAlbumPageFragment() })
        albumPlayButton.setOnClickListener({
            albumTracksAdapter?.playAlbum(getMainActivity())
        })
    }

    override fun updateView() {
        if (isAdded) {
            // Album info
            albumTitle.text = album?.name
            albumArtist.text = album?.artist()
            albumYear.text = album?.year()
            albumDuration.text = String.format(Locale.getDefault(), getString(R.string.album_duration), album?.durationInMins())
            // Album images
            ImagesUtils.loadUrlIntoImageView(album?.coverUrl(), context, albumCover, R.drawable.ic_album_white_24dp, false)
            ImagesUtils.loadUrlIntoImageView(album?.coverUrl(), context, albumCoverBackground, R.drawable.ic_album_white_24dp, false)
            // Album tracks
            updateAlbumTracks()
        }
    }

    fun updateAlbumTracks() {
        if (albumTracksAdapter != null) {
            if (albumTracksRV.adapter != null) {
                albumTracksAdapter?.notifyDataSetChanged()
            } else {
                initRecyclerView()
            }
        } else {
            setupAdapter()
        }
    }

    fun TrackSimple.toTrack() : Track {
        val track = Track()
        track.artists = this.artists
        track.available_markets = this.available_markets
        track.is_playable = this.is_playable
        track.linked_from = this.linked_from
        track.disc_number = this.disc_number
        track.duration_ms = this.duration_ms
        track.explicit = this.explicit
        track.external_urls = this.external_urls
        track.href = this.href
        track.id = this.id
        track.name = this.name
        track.preview_url = this.preview_url
        track.track_number = this.track_number
        track.type = this.type
        track.uri = this.uri
        return track
    }

    fun Track.setAlbum(album: Album?) : Track {
        this.album = album
        return this
    }

    fun setupAdapter() {
        val albumTracks = album?.tracks?.items?.map { it -> it.toTrack().setAlbum(album) }
        albumTracksAdapter = AlbumTracksAdapter(activity, albumTracks as ArrayList<Track>)
        if (albumTracksRV.adapter == null) {
            initRecyclerView()
        }
    }

    fun initRecyclerView() {
        albumTracksRV.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        albumTracksRV.adapter = albumTracksAdapter
    }

    override fun onResume() {
        super.onResume()
        albumTracksAdapter?.notifyDataSetChanged()
    }

    override fun notifySongs(oldSong: Track?, currentSong: Track?) {
        albumTracksAdapter?.notifyItemChangedIfPresent(oldSong?.uri)
        albumTracksAdapter?.notifyItemChangedIfPresent(currentSong?.uri)
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
    fun Album.durationInMins() : Int = durationInSecs() / Constants.MINUTES
    fun Album.year() : String = release_date.substringBefore("-")
}
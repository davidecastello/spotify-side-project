package io.moku.davide.spotify_side_project.album

import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.WhosPlaying
import kaaes.spotify.webapi.android.models.TrackSimple
import kotlinx.android.synthetic.main.album_track_cell_layout.view.*

/**
 * Created by Davide Castello on 20/06/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class AlbumTracksAdapter(val context: Context, var albumTracks: ArrayList<TrackSimple>) : RecyclerView.Adapter<AlbumTracksAdapter.AlbumTrackViewHolder>() {

    override fun getItemCount(): Int = albumTracks.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AlbumTrackViewHolder
            = AlbumTrackViewHolder(LayoutInflater.from(context).inflate(R.layout.album_track_cell_layout, parent, false))

    override fun onBindViewHolder(holder: AlbumTrackViewHolder?, position: Int) {
        val track = albumTracks.get(position)
        val view = holder?.itemView
        // update UI
        view?.trackTitle?.text = track.name
        view?.trackArtist?.text = track.artist()
        val isCurrentTrack = track.id == mainActivity(context).currentTrack?.id
        val colorId : Int = if (isCurrentTrack) R.color.grey else R.color.white
        val color : Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getColor(colorId) else context.resources.getColor(colorId)
        view?.setBackgroundColor(color)
        // listeners
        view?.setOnClickListener { v -> run {
            playTrack(track, mainActivity(v.context))
        }}
    }

    fun playAlbum(activity: MainActivity) {
        if (!albumTracks.isEmpty()) {
            playTrack(albumTracks.first(), activity)
        }
    }

    fun playTrack(track: TrackSimple, a: MainActivity) {
        val oldWhosPlaying = a.getWhosPlaying()
        val isAlreadyPlayingThisTrack : Boolean = a.currentTrack?.id == track.id

        // set who's playing: we are playing music from "My albums"
        a.setWhosPlaying(WhosPlaying.MY_ALBUMS)

        if (oldWhosPlaying == null) {
            proceed(track, a, true)
        } else {
            if (oldWhosPlaying != WhosPlaying.MY_ALBUMS) {
                proceed(track, a, true)
                a.updateView()
            } else {
                if (!isAlreadyPlayingThisTrack) {
                    // update UI
                    notifyItemChangedIfPresent(a.currentTrack?.uri)
                    // proceed
                    proceed(track, a, shouldResetQueue = !a.isTrackCurrentlyInQueue(track.uri))
                } else {
                    // we are already in "My albums", the user is tapping on the song
                    // that is already playing
                }
            }
        }
    }

    fun proceed(track: TrackSimple, a: MainActivity, shouldResetQueue: Boolean) {
        // clear queue and add all saved tracks if necessary
        if (shouldResetQueue) {
            a.clearAndAddToQueue(albumTracks)
        }
        // play the chosen song
        a.playTrack(a.getTrackInQueue(track.uri))
        notifyItemChanged(albumTracks.indexOf(track))
    }

    fun isSongInAlbumTracks(uri: String?) = albumTracks.count { it.uri == uri } > 0
    fun getSongFromAlbumTracks(uri : String?) = albumTracks.filter { it.uri == uri }.first()

    fun notifyItemChangedIfPresent(uri: String?) {
        if (isSongInAlbumTracks(uri)) {
            notifyItemChanged(albumTracks.indexOf(getSongFromAlbumTracks(uri)))
        }
    }

    fun mainActivity(context: Context) : MainActivity = context as MainActivity

    class AlbumTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /* Useful extensions */
    fun TrackSimple.artist() : String = artists.map { it -> it.name }.joinToString(separator = ", ")
}
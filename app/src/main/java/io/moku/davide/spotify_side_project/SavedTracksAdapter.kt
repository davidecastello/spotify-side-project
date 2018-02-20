package io.moku.davide.spotify_side_project

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kaaes.spotify.webapi.android.models.SavedTrack
import kotlinx.android.synthetic.main.saved_track_cell_layout.view.*
import android.os.Build



/**
 * Created by Davide Castello on 19/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class SavedTracksAdapter(val context: Context, var savedTracks: List<SavedTrack>) : RecyclerView.Adapter<SavedTracksAdapter.SavedTrackViewHolder>() {

    var currentTrack : SavedTrack? = null

    override fun getItemCount(): Int = savedTracks.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SavedTrackViewHolder
            = SavedTrackViewHolder(LayoutInflater.from(context).inflate(R.layout.saved_track_cell_layout, parent, false))

    override fun onBindViewHolder(holder: SavedTrackViewHolder?, position: Int) {
        val track = savedTracks.get(position)
        val view = holder?.itemView
        // update UI
        view?.trackTitle?.text = track.name()
        view?.trackArtistAndAlbum?.text = track.artistsAndAlbum()
        val isCurrentTrack = track.id() == currentTrack?.id()
        val colorId : Int = if (isCurrentTrack) R.color.grey else R.color.white
        val color : Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getColor(colorId) else context.resources.getColor(colorId)
        view?.setBackgroundColor(color)
        // listeners
        view?.setOnClickListener { v -> run {
            // replace current track
            val oldCurrent = currentTrack
            currentTrack = track
            // update UI
            notifyItemsChanged(pos1 = savedTracks.indexOf(oldCurrent), pos2 = savedTracks.indexOf(currentTrack as SavedTrack))
            // play song
            (v.context as MainActivity).playSong(currentTrack?.uri())
        }}
    }

    fun notifyItemsChanged(pos1 : Int, pos2 : Int) {
        notifyItemChanged(pos1)
        notifyItemChanged(pos2)
    }

    fun prevSong() : SavedTrack {

        val oldCurrentPosition = findSong(true, savedTracks.first())
        val currentPosition : Int = savedTracks.indexOf(currentTrack as SavedTrack)
        notifyItemsChanged(oldCurrentPosition, currentPosition)

        return currentTrack as SavedTrack
    }

    fun nextSong() : SavedTrack {
        
        val oldCurrentPosition = findSong(false, savedTracks.last())
        val currentPosition : Int = savedTracks.indexOf(currentTrack as SavedTrack)
        notifyItemsChanged(oldCurrentPosition, currentPosition)

        return currentTrack as SavedTrack
    }

    // updated the currentTrack property and returns the old current position
    fun findSong(prev : Boolean, comparisonTrack : SavedTrack) : Int {
        var oldCurrentPosition : Int = -1
        if (currentTrack == null)
            currentTrack = if (prev) savedTracks.last() else savedTracks.first()
        else if (currentTrack == comparisonTrack) {
            oldCurrentPosition = savedTracks.indexOf(comparisonTrack)
            currentTrack = if (prev) savedTracks.last() else savedTracks.first()
        } else {
            oldCurrentPosition = savedTracks.indexOf(currentTrack as SavedTrack)
            var p = savedTracks.indexOf(currentTrack as SavedTrack)
            p = if (prev) p-1 else p+1
            currentTrack = savedTracks.get(p)
        }
        return oldCurrentPosition
    }

    fun SavedTrack.id() : String = track.id
    fun SavedTrack.name() : String = track.name
    fun SavedTrack.uri() : String = track.uri
    fun SavedTrack.album() : String = track.album.name
    fun SavedTrack.artist() : String = track.artists.map { it -> it.name }.joinToString(separator = ", ")
    fun SavedTrack.artistsAndAlbum() : String = "${artist()} • ${album()}"

    class SavedTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
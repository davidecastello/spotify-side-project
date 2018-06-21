package io.moku.davide.spotify_side_project.tracks

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kaaes.spotify.webapi.android.models.SavedTrack
import kotlinx.android.synthetic.main.saved_track_cell_layout.view.*
import android.os.Build
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.WhosPlaying
import kaaes.spotify.webapi.android.models.TrackSimple


/**
 * Created by Davide Castello on 19/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class SavedTracksAdapter(val context: Context, var savedTracks: ArrayList<SavedTrack>) : RecyclerView.Adapter<SavedTracksAdapter.SavedTrackViewHolder>() {

    override fun getItemCount(): Int = savedTracks.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SavedTrackViewHolder
            = SavedTrackViewHolder(LayoutInflater.from(context).inflate(R.layout.saved_track_cell_layout, parent, false))

    override fun onBindViewHolder(holder: SavedTrackViewHolder?, position: Int) {
        val track = savedTracks.get(position)
        val view = holder?.itemView
        // update UI
        view?.trackTitle?.text = track.name()
        view?.trackArtistAndAlbum?.text = track.artistsAndAlbum()
        val isCurrentTrack = track.id() == mainActivity(context).currentTrack?.id
        val colorId : Int = if (isCurrentTrack) R.color.grey else R.color.white
        val color : Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getColor(colorId) else context.resources.getColor(colorId)
        view?.setBackgroundColor(color)
        // listeners
        view?.setOnClickListener { v -> run {
            val a = mainActivity(v.context)
            val oldWhosPlaying = a.getWhosPlaying()
            val isAlreadyPlayingThisTrack : Boolean = a.currentTrack?.id == track.id()

            // set who's playing: we are playing music from "My tracks"
            a.setWhosPlaying(WhosPlaying.MY_TRACKS)

            if (oldWhosPlaying == null) {
                proceed(track, a, true)
            } else {
                if (oldWhosPlaying != WhosPlaying.MY_TRACKS) {
                    // TODO: 2. a.tryToUpdateUIinOldWhosPlaying
                    proceed(track, a, true)
                } else {
                    if (!isAlreadyPlayingThisTrack) {
                        // update UI
                        notifyItemChangedIfPresent(a.currentTrack?.uri)
                        // proceed
                        proceed(track, a, shouldResetQueue = !a.isTrackCurrentlyInQueue(track.uri()))
                    } else {
                        // we are already in "My tracks", the user is tapping on the song
                        // that is already playing
                    }
                }
            }
        }}
    }

    fun proceed(track: SavedTrack, a: MainActivity, shouldResetQueue: Boolean) {
        // clear queue and add all saved tracks if necessary
        if (shouldResetQueue) {
            a.clearAndAddToQueue(savedTracks.trackSimples())
        }
        // play the chosen song
        a.playTrack(a.getTrackInQueue(track.uri()))
        notifyItemChanged(savedTracks.indexOf(track))
    }

    fun isSongInSavedTracks(uri: String?) = savedTracks.count { it.uri() == uri } > 0
    fun getSongFromSavedTracks(uri : String?) = savedTracks.filter { it.uri() == uri }.first()

    fun notifyItemChangedIfPresent(uri: String?) {
        if (isSongInSavedTracks(uri)) {
            notifyItemChanged(savedTracks.indexOf(getSongFromSavedTracks(uri)))
        }
    }

    fun mainActivity(context: Context) : MainActivity = context as MainActivity

    class SavedTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /* Useful extensions */
    fun SavedTrack.id() : String = track.id
    fun SavedTrack.name() : String = track.name
    fun SavedTrack.uri() : String = track.uri
    fun SavedTrack.album() : String = track.album.name
    fun SavedTrack.artist() : String = track.artists.map { it -> it.name }.joinToString(separator = ", ")
    fun SavedTrack.artistsAndAlbum() : String = "${artist()} • ${album()}"
    fun ArrayList<SavedTrack>.trackSimples() : List<TrackSimple> = map { it -> it.track as TrackSimple }
}
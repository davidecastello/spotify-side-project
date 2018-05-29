package io.moku.davide.spotify_side_project.playlist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.assets.ImagesUtils
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kotlinx.android.synthetic.main.playlist_cell_layout.view.*

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class SavedPlaylistsAdapter(val context: Context, var savedPlaylists: ArrayList<PlaylistSimple>) : RecyclerView.Adapter<SavedPlaylistsAdapter.SavedPlaylistViewHolder>() {

    override fun getItemCount(): Int = savedPlaylists.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SavedPlaylistViewHolder
            = SavedPlaylistViewHolder(LayoutInflater.from(context).inflate(R.layout.playlist_cell_layout, parent, false))

    override fun onBindViewHolder(holder: SavedPlaylistViewHolder?, position: Int) {
        val playlist = savedPlaylists.get(position)
        val view = holder?.itemView
        // update UI
        view?.playlistTitle?.text = playlist.name
        view?.playlistOwner?.text = playlist.owner()
        ImagesUtils.loadUrlIntoImageView(playlist.coverUrl(), view?.context, view?.playlistCover, R.drawable.ic_playlist_black_24dp, false)
        // listener
        view?.setOnClickListener { v -> run {
            //Toast.makeText(context, playlist.name, Toast.LENGTH_SHORT).show()
        }}
    }

    class SavedPlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /* Useful extensions */
    fun PlaylistSimple.owner() : String = String.format(context.getString(R.string.playlist_owner_format), owner.display_name)
    fun PlaylistSimple.coverUrl() : String = images.last().url
}
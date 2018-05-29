package io.moku.davide.spotify_side_project.album

import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.moku.davide.spotify_side_project.MainActivity
import io.moku.davide.spotify_side_project.R
import io.moku.davide.spotify_side_project.utils.assets.ImagesUtils
import kaaes.spotify.webapi.android.models.SavedAlbum
import kotlinx.android.synthetic.main.album_cell_layout.view.*

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class SavedAlbumsAdapter(val context: Context, var savedAlbums: ArrayList<SavedAlbum>) : RecyclerView.Adapter<SavedAlbumsAdapter.SavedAlbumViewHolder>() {

    override fun getItemCount(): Int = savedAlbums.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SavedAlbumViewHolder
            = SavedAlbumViewHolder(LayoutInflater.from(context).inflate(R.layout.album_cell_layout, parent, false))

    override fun onBindViewHolder(holder: SavedAlbumViewHolder?, position: Int) {
        val album = savedAlbums.get(position)
        val view = holder?.itemView
        // update UI
        view?.albumTitle?.text = album.name()
        view?.albumArtist?.text = album.artist()
        ImagesUtils.loadUrlIntoImageView(album.coverUrl(), view?.context, view?.albumCover, R.drawable.ic_album_black_24dp, false)
        // listener
        view?.setOnClickListener { v -> run {
            //Toast.makeText(context, album.name(), Toast.LENGTH_SHORT).show()
        }}
    }

    class SavedAlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /* Useful extensions */
    fun SavedAlbum.name() : String = album.name
    fun SavedAlbum.artist() : String = album.artists.map { it -> it.name }.joinToString(separator = ", ")
    fun SavedAlbum.coverUrl() : String = album.images.last().url
}
package io.moku.davide.spotify_side_project

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.moku.davide.spotify_side_project.utils.CustomFragment

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class MainFragmentPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val mFragmentList = ArrayList<CustomFragment>()

    override fun getItem(position: Int): CustomFragment = mFragmentList.get(position)
    override fun getCount(): Int = mFragmentList.size
    fun addFragment(fragment: CustomFragment) = mFragmentList.add(fragment)
    fun addFragments(fragments: Collection<CustomFragment>) = mFragmentList.addAll(fragments)
    fun updateFragments() {
        mFragmentList.forEach({ f -> f.updateView()})
    }
}
package io.moku.davide.spotify_side_project

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.moku.davide.spotify_side_project.utils.fragments.CustomTabbedFragment

/**
 * Created by Davide Castello on 28/02/18.
 * Project: spotify-side-project
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 */
class MainFragmentPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val mFragmentList = ArrayList<CustomTabbedFragment>()

    override fun getItem(position: Int): CustomTabbedFragment = mFragmentList.get(position)
    override fun getCount(): Int = mFragmentList.size
    fun addFragment(fragment: CustomTabbedFragment) = mFragmentList.add(fragment)
    fun addFragments(fragments: Collection<CustomTabbedFragment>) = mFragmentList.addAll(fragments)
    fun updateFragments() {
        mFragmentList.forEach({ f -> f.updateView() })
    }
    fun getFragment(position: Int) : CustomTabbedFragment = if (position < mFragmentList.size) mFragmentList.get(position) else mFragmentList.last()
}
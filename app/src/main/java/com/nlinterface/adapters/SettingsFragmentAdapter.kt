package com.nlinterface.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlinterface.fragments.SettingsScreen1
import com.nlinterface.fragments.SettingsScreen2
import com.nlinterface.fragments.SettingsScreen3

class SettingsFragmentAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        // Return the count of static fragments plus the dynamic ones
        return fragmentList.size
    }

    init {
        // Add static fragments
        fragmentList.add(SettingsScreen1())
        fragmentList.add(SettingsScreen2())
        fragmentList.add(SettingsScreen3())
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }
}
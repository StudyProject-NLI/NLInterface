package com.nlinterface.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlinterface.fragments.PlaceDetailsListOverview
import com.nlinterface.fragments.PlaceDetailsScreen1
import com.nlinterface.fragments.PlaceDetailsScreenBase

/**
 * Fragment Adapter for the Place Details Activity. On initialization only the fixed Fragments are
 * added to fragmentList.
 */
class PlaceDetailsFragmentAdapter (
    private val fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {

    val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    init {
        fragmentList.add(PlaceDetailsListOverview())
        fragmentList.add(PlaceDetailsScreen1())
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment(fragment: PlaceDetailsScreenBase){
        fragmentList.add(fragment)
    }

    /**
     * Removes a fragment and handles proper notification to all isntances.
     */

    fun removeFragment(fragment: PlaceDetailsScreenBase){
        val position = fragmentList.indexOf(fragment)
        fragmentList.removeAt(position)
        fragmentActivity.supportFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        // Return a unique ID for each fragment
        return fragmentList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        // Ensure that the item is still in the fragment list
        return fragmentList.any { it.hashCode().toLong() == itemId }
    }
}
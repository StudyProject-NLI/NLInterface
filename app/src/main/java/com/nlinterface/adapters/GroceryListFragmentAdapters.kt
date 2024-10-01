package com.nlinterface.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlinterface.fragments.GroceryListScreen1
import com.nlinterface.fragments.GroceryListScreen2
import com.nlinterface.fragments.GroceryListScreenBase
import com.nlinterface.fragments.GroceryListScreenListView

/**
 * Fragment Adapter for the Grocery List Activity. On initialization only the fixed Fragments are
 * added to fragmentList.
 */
class GroceryListFragmentAdapter(
    private val fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {

    val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    init {
        fragmentList.add(GroceryListScreenListView())
        fragmentList.add(GroceryListScreen1())
        fragmentList.add(GroceryListScreen2())
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment(fragment: GroceryListScreenBase){
        fragmentList.add(fragment)
    }

    /**
     * Removes a fragment and handles proper notification to all isntances.
     */

    fun removeFragment(fragment: GroceryListScreenBase){
        val position = fragmentList.indexOf(fragment)
        fragmentList.removeAt(position)
        fragmentActivity.supportFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        // Return a unique ID for each fragment
        return fragmentList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        // Ensure that the item is still in the fragment list
        return fragmentList.any { it.hashCode().toLong() == itemId }
    }

    /**
     * Clears all dynamically created fragments.
     */
    fun clearFragments(){
        if (fragmentList.size > 3) {
            val fragmentsToRemove = fragmentList.size - 3
            // Remove all fragments starting from the third one
            fragmentList.subList(3, fragmentList.size).clear()
            notifyItemRangeRemoved(3, fragmentsToRemove)
        }
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }
}
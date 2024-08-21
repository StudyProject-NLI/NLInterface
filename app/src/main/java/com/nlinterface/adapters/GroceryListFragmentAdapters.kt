package com.nlinterface.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlinterface.fragments.GroceryListScreen1
import com.nlinterface.fragments.GroceryListScreen2
import com.nlinterface.fragments.GroceryListScreenBase
import com.nlinterface.fragments.GroceryListScreenListView

class GroceryListFragmentAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {

    val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        // Return the count of static fragments plus the dynamic ones
        return fragmentList.size
    }

    init {
        // Add static fragments
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

    fun removeFragment(fragment: GroceryListScreenBase){
        val position = fragmentList.indexOf(fragment)
        fragmentList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearFragments(){
        if (fragmentList.size > 2) {
            val fragmentsToRemove = fragmentList.size - 2
            // Remove all fragments starting from the third one
            fragmentList.subList(2, fragmentList.size).clear()
            notifyItemRangeRemoved(2, fragmentsToRemove)
        }
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }
}
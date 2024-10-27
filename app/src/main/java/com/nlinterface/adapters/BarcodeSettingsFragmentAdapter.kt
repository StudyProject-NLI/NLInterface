package com.nlinterface.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlinterface.fragments.BarcodeScannerScreen
import com.nlinterface.fragments.BarcodeSettingsScreen1
import com.nlinterface.fragments.BarcodeSettingsScreen2
import com.nlinterface.fragments.BarcodeSettingsScreen3

/**
 * Fragment Adapter for the Barcode Settings Activity.
 */
class BarcodeSettingsFragmentAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    init {
        fragmentList.add(BarcodeScannerScreen())
        fragmentList.add(BarcodeSettingsScreen1())
        fragmentList.add(BarcodeSettingsScreen2())
        fragmentList.add(BarcodeSettingsScreen3())
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getCurrentFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }
}
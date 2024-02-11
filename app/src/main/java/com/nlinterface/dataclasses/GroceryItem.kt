package com.nlinterface.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data Class modelling a GroceryItem.
 *
 * @param itemName: String, the name of the GroceryItem
 * @param id: Int, the unique (to the list it belongs to) ID of the GroceryItem
 * @param inCart: Boolean, whether the GroceryItem is currently in the 'shopping cart'
 */
@Parcelize
data class GroceryItem(
    val itemName: String,
    val id: Int,
    var inCart: Boolean
) : Parcelable
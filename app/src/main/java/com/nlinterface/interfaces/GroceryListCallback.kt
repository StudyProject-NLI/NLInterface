package com.nlinterface.interfaces

import com.nlinterface.dataclasses.GroceryItem

/**
 * Interface for a class requiring a LongClick and Click callback on a GroceryItem in a RecyclerView
 */
interface GroceryListCallback {

    /**
     * Handles a long click on a GroceryItem
     */
    fun onLongClick(item: GroceryItem)

    /**
     * Handles a click on a GroceryItem
     */
    fun onClick(item: GroceryItem)

}
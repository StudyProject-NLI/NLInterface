package com.nlinterface.interfaces

import com.nlinterface.dataclasses.GroceryItem

/**
 * Interface for a class requiring a LongClick and Click callback on a GroceryItem in a RecyclerView
 */
interface GroceryListCallback {

    /**
     * Handles a long click on a GroceryItem
     *
     * @param item: GroceryItem that was long-clicked
     */
    fun onLongClick(item: GroceryItem)

    /**
     * Handles a click on a GroceryItem
     *
     * @param item: GroceryItem that was clicked
     */
    fun onClick(item: GroceryItem)
}
package com.nlinterface.interfaces
import com.nlinterface.dataclasses.GroceryItem

interface GroceryListCallback {
    fun onLongClick(item: GroceryItem)
}
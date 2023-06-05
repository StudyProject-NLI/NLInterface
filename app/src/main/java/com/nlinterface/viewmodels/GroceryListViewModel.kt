package com.nlinterface.viewmodels

import androidx.lifecycle.ViewModel
import com.nlinterface.dataclasses.GroceryItem

class GroceryListViewModel : ViewModel() {

    var groceryList: ArrayList<GroceryItem> = ArrayList<GroceryItem>()

    fun fetchGroceryList() {
        groceryList = arrayListOf(GroceryItem("Apples"),
            GroceryItem("Oranges"), GroceryItem("Bananas"))
    }

    fun addGroceryItem(itemName: String): ArrayList<GroceryItem> {
        groceryList.add(GroceryItem(itemName))
        return groceryList
    }



}
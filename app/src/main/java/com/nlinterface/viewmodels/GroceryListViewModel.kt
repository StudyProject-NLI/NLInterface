package com.nlinterface.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.dataclasses.GroceryItem
import java.io.BufferedReader
import java.io.File


class GroceryListViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val context = application
    private val groceryListFileName = "GroceryList.json"
    private val groceryListFile: File = File(context.filesDir, groceryListFileName)
    var groceryList: ArrayList<GroceryItem> = ArrayList<GroceryItem>()
    private var gson = Gson()

    val ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun fetchGroceryList() {

        if (!groceryListFile.exists()) {
            groceryListFile.createNewFile()
        }

        if (groceryListFile.length() > 0) {
            val bufferedReader: BufferedReader = groceryListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            groceryList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<GroceryItem?>?>() {}.type
            ) as ArrayList<GroceryItem>
        }

        Log.println(Log.DEBUG, "1", groceryList.toString())
    }

    fun addGroceryItem(itemName: String): ArrayList<GroceryItem> {
        groceryList.add(GroceryItem(itemName, groceryList.size, false))
        storeGroceryList()
        return groceryList
    }

    fun deleteGroceryItem(groceryItem: GroceryItem) {
        storeGroceryList()
        groceryList.remove(groceryItem)
    }

    fun placeGroceryItemInCart(groceryItem: GroceryItem): Boolean {
        storeGroceryList()
        groceryItem.inCart = !groceryItem.inCart

        return groceryItem.inCart
    }

    fun storeGroceryList() {
        val jsonString : String = gson.toJson(groceryList)
        groceryListFile.writeText(jsonString)
    }
}
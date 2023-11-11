package com.nlinterface.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.BuildConfig
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.dataclasses.PlaceDetailsItem
import kotlinx.coroutines.CompletionHandler
import java.io.BufferedReader
import java.io.File

class PlaceDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private lateinit var placesClient: PlacesClient

    private val placeDetailsItemListFileName = "PlaceDetailsItemList.json"
    private val placeDetailsItemListFile: File = File(context.filesDir, placeDetailsItemListFileName)
    var placeDetailsItemList: ArrayList<PlaceDetailsItem> = ArrayList<PlaceDetailsItem> ()
    private var gson = Gson()

    val ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun fetchPlaceDetailsItemList() {

        if (!placeDetailsItemListFile.exists()) {
            placeDetailsItemListFile.createNewFile()
        }

        if (placeDetailsItemListFile.length() > 0) {
            val bufferedReader: BufferedReader = placeDetailsItemListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            placeDetailsItemList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<PlaceDetailsItem?>?>() {}.type
            ) as ArrayList<PlaceDetailsItem>
        }

        Log.println(Log.DEBUG, "1", placeDetailsItemList.toString())
    }

    private fun addPlaceDetailsItem(placeID: String, storeName: String, openingHours: List<String>): ArrayList<PlaceDetailsItem> {
        placeDetailsItemList.add(PlaceDetailsItem(placeID, storeName, openingHours, false))
        return placeDetailsItemList
    }

    fun initPlaceClient(context: Context) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(context)
    }

    fun onPlaceSelected(place: Place, completion: (success: Boolean) -> Unit) {
        val placeID = place.id
        fetchPlaceDetails(placeID) {
            completion(it)
        }
    }

    private fun fetchPlaceDetails(placeID: String?, completion: (success: Boolean) -> Unit) {

        // Specify the fields to return.
        val placeFields = listOf(
            Place.Field.NAME, Place.Field.OPENING_HOURS, Place.Field.ADDRESS
        )

        // Construct a request object, passing the place ID and fields array.
        val request = placeID?.let { FetchPlaceRequest.newInstance(it, placeFields) }

        if (request != null) {
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    addPlaceDetailsItem(placeID, response.place.name, response.place.openingHours.weekdayText)
                    completion(true)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Log.println(Log.DEBUG, "MAPSAPI", "failed")
                    }
                    completion(false)
                }
        }
    }

    fun onError(status: Status) {
        // TODO
    }

    fun storePlaceDetailsItemList() {
        val jsonString : String = gson.toJson(placeDetailsItemList)
        placeDetailsItemListFile.writeText(jsonString)
    }

    fun changeFavorite(item: PlaceDetailsItem): Boolean {
        item.favorite = !item.favorite

        return item.favorite
    }

    fun deletePlaceDetailsItem(placeDetailsItem: PlaceDetailsItem): ArrayList<PlaceDetailsItem> {
        placeDetailsItemList.remove(placeDetailsItem)
        return placeDetailsItemList
    }

}
package com.nlinterface.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.nlinterface.BuildConfig
import kotlinx.coroutines.CompletionHandler

class PlaceDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private lateinit var placesClient: PlacesClient
    private lateinit var place: Place

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
                    place = response.place
                    completion(true)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Log.println(Log.DEBUG, "MAPSAPI", "failed")
                    }
                    completion(false)
                }
        }
    }

    fun getPlaceName(): String {
        return place.name.toString()
    }

    fun getPlaceOpeningHours(): MutableList<String> {
        return place.openingHours.weekdayText
    }

    fun onError(status: Status) {
        // TODO
    }

}
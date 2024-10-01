package com.nlinterface.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data Class modelling a PlaceDetailsItem aka a store.
 *
 * @param placeID: String, unique ID of the place
 * @param storeName: String, the name of the place
 * @param openingHours: List<String>, List of the last known opening hours of the place
 * @param address: String, the address of the place
 * @param favorite: Boolean, whether the user has market this place as a favorite
 */
@Parcelize
data class PlaceDetailsItem(

    val placeID: String,
    val storeName: String,
    val openingHours: List<String>,
    val address: String,
    var favorite: Boolean

) : Parcelable
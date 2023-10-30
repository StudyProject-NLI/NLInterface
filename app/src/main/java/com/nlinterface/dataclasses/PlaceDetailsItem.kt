package com.nlinterface.dataclasses

import android.os.Parcelable
import com.google.android.libraries.places.api.model.OpeningHours
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceDetailsItem(

    val placeID: String,
    val storeName: String,
    val openingHours: List<String>,
    var favorite: Boolean

): Parcelable {

}
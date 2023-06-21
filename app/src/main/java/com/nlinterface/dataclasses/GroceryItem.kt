package com.nlinterface.dataclasses
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroceryItem(
    val itemName: String,
    val id: Int,
    var inCart: Boolean
    ): Parcelable {

}
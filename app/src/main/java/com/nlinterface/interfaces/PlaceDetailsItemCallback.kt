package com.nlinterface.interfaces
import com.nlinterface.dataclasses.PlaceDetailsItem

interface PlaceDetailsItemCallback {
    fun onFavoriteClick(item: PlaceDetailsItem)
    fun onCardClick(item: PlaceDetailsItem)
}
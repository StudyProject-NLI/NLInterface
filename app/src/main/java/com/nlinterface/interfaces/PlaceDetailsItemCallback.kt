package com.nlinterface.interfaces
import com.nlinterface.dataclasses.PlaceDetailsItem

interface PlaceDetailsItemCallback {
    fun onClick(item: PlaceDetailsItem)
}
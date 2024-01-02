package com.nlinterface.interfaces

import com.nlinterface.dataclasses.PlaceDetailsItem

/**
 * Interface for a class requiring a callback for a click on a CardView of a PlaceDetailsItem and
 * for a click on the favorite icon of a PlaceDetailsItem in a RecyclerView
 */
interface PlaceDetailsItemCallback {

    /**
     * Handles a click on the favorite icon of a PlaceDetailsItem
     */
    fun onFavoriteClick(item: PlaceDetailsItem)

    /**
     * Handles a click on the CardView of a PlaceDetailsItem
     */
    fun onCardClick(item: PlaceDetailsItem)

}
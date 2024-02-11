package com.nlinterface.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.interfaces.PlaceDetailsItemCallback
import java.util.Calendar

/**
 * Implements the interface between the PlaceDetailsActivity and the PlaceDetails RecyclerView.
 * Contains the ViewHolder for the PlaceDetailsItems in the RecyclerView.
 */
class PlaceDetailsAdapter(
    private val data: ArrayList<PlaceDetailsItem>,
    private val placeDetailsItemCallback: PlaceDetailsItemCallback
) : RecyclerView.Adapter<PlaceDetailsAdapter.ViewHolder>() {

    /**
     * The ViewHolder for the PlaceDetailsItems shown in the RecyclerView. Stores the storeName
     * TextView, openingHours TextView, favorite ImageView and the CardView.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val placeDetailsItemStoreNameTextView: TextView =
            itemView.findViewById(R.id.place_details_store_name_tv)
        val placeDetailsItemOpeningHoursTextView: TextView =
            itemView.findViewById(R.id.place_details_opening_hours_tv)
        val placeDetailsItemFavoriteImageView: ImageView =
            itemView.findViewById(R.id.place_details_favorite_iv)
        val placeDetailsItemCardView: CardView = itemView.findViewById(R.id.place_details_cv)
    }

    /**
     * Sets up the Layout for the ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val placeDetailsView = inflater.inflate(R.layout.place_detail_item, parent, false)

        return ViewHolder(placeDetailsView)
    }

    /**
     * Configures the View in the ViewHolder, is called separately for each position in the data:
     *
     * 1- sets the storeName and displays the favorite status (filled in star if favorite, else
     * outline
     * 2- calculates the day of the week, retrieves the current opening hours, formats and displays
     * them
     * 3- configures the onClick listeners for the favoriteImageView and the cardView for the delete
     * functionality
     *
     * @param holder: ViewHolder
     * @param position: Int
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val placeDetailsItem: PlaceDetailsItem = data[position]

        // Set item views based on views and data model
        val storeNameTextView = holder.placeDetailsItemStoreNameTextView
        val openingHoursTextView = holder.placeDetailsItemOpeningHoursTextView
        val favoriteImageView = holder.placeDetailsItemFavoriteImageView
        val cardView = holder.placeDetailsItemCardView

        storeNameTextView.text = placeDetailsItem.storeName

        // format opening hours neatly and according to the current day
        val dayOfWeek = Math.floorMod(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2, 7)
        val regex = "(?<=: \\[?)(\\w:?–? ?ö?)+( ?–?\\d?:?)+".toRegex()
        val openingHoursText = regex.find(placeDetailsItem.openingHours[dayOfWeek])?.value

        openingHoursTextView.text = openingHoursText

        // display favorite icon filled in, if favorite, outlined else
        if (placeDetailsItem.favorite) {
            holder.placeDetailsItemFavoriteImageView.setImageResource(
                R.drawable.ic_baseline_star_24
            )
        } else {
            holder.placeDetailsItemFavoriteImageView.setImageResource(
                R.drawable.ic_baseline_star_border_24
            )
        }

        // Notifies the PlaceDetailsItemCallback if favorite icon clicked
        favoriteImageView.setOnClickListener {
            placeDetailsItemCallback.onFavoriteClick(data[holder.adapterPosition])
        }

        // Notifies the PlaceDetailsItemCallback if card clicked
        cardView.setOnClickListener {
            placeDetailsItemCallback.onCardClick(data[holder.adapterPosition])
        }

    }

    /**
     * Returns the size of the data stored in the adapter.
     *
     * @return length of GroceryList
     */
    override fun getItemCount(): Int {
        return data.size
    }

}
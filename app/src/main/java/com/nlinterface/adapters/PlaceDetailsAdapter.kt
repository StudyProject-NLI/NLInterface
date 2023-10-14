package com.nlinterface.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.interfaces.PlaceDetailsItemCallback
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.utility.setViewRelativeWidth
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar

class PlaceDetailsAdapter (
    private val data: ArrayList<PlaceDetailsItem>,
    private val placeDetailsItemCallback: PlaceDetailsItemCallback
) : RecyclerView.Adapter<PlaceDetailsAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val placeDetailsItemStoreNameTextView: TextView = itemView.findViewById(R.id.place_details_store_name_tv)
        val placeDetailsItemOpeningHoursTextView: TextView = itemView.findViewById(R.id.place_details_opening_hours_tv)
        val placeDetailsItemFavoriteImageView: ImageView = itemView.findViewById(R.id.place_details_favorite_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val placeDetailsView = inflater.inflate(R.layout.place_detail_item, parent, false)

        return ViewHolder(placeDetailsView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val placeDetailsItem: PlaceDetailsItem = data[position]

        val storeNameTextView = holder.placeDetailsItemStoreNameTextView
        val openingHoursTextView = holder.placeDetailsItemOpeningHoursTextView
        val favoriteImageView = holder.placeDetailsItemFavoriteImageView

        val res = holder.itemView.resources

        storeNameTextView.text = placeDetailsItem.storeName

        val dayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2) % 7
        val regex = "\\ .+\\ ".toRegex()
        val openingHoursText = regex.find(placeDetailsItem.openingHours[dayOfWeek])?.value
        openingHoursTextView.text = openingHoursText

        if (placeDetailsItem.favorite) {
            favoriteImageView.setImageResource(R.drawable.ic_baseline_star_24)
        }

        favoriteImageView.setOnClickListener {
            placeDetailsItemCallback.onClick(data[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}
package com.nlinterface.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.interfaces.GroceryListCallback

class GroceryListAdapter(
    private val data: ArrayList<GroceryItem>,
    private val groceryListCallback: GroceryListCallback
) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val groceryItemView: TextView = itemView.findViewById(R.id.grocery_item_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val groceryItemView = inflater.inflate(R.layout.grocery_item, parent, false)

        return ViewHolder(groceryItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groceryItem: GroceryItem = data[position]
        // Set item views based on your views and data model
        val textView = holder.groceryItemView
        textView.text = groceryItem.itemName

        textView.setOnLongClickListener {
            groceryListCallback.onLongClick(data[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
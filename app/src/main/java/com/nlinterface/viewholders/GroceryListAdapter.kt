package com.nlinterface.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R

class GroceryListAdapter(private val grocerylist: List<String>) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val groceryItemView = itemView.findViewById<TextView>(com.nlinterface.R.id.grocery_item_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.grocery_item, parent, false)

        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groceryitem: String = grocerylist[position]
        // Set item views based on your views and data model
        val textView = holder.groceryItemView
        textView.text = groceryitem
    }

    override fun getItemCount(): Int {
        return grocerylist.size
    }
}
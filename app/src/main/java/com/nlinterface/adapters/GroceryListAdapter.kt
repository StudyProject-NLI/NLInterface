package com.nlinterface.adapters

import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.interfaces.GroceryListCallback


class GroceryListAdapter(
    private val data: ArrayList<GroceryItem>,
    private val groceryListCallback: GroceryListCallback,
) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {

    private lateinit var parent: ViewGroup

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val groceryItemTextView: TextView = itemView.findViewById(R.id.grocery_item_tv)
        val groceryItemCardView: CardView = itemView.findViewById(R.id.grocery_item_cv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        this.parent = parent
        val context = this.parent.context
        val inflater = LayoutInflater.from(context)
        val groceryItemView = inflater.inflate(R.layout.grocery_item, parent, false)

        return ViewHolder(groceryItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groceryItem: GroceryItem = data[position]
        // Set item views based on your views and data model
        val textView = holder.groceryItemTextView
        val cardView = holder.groceryItemCardView
        val res = holder.itemView.resources
        textView.text = groceryItem.itemName

        if (groceryItem.inCart) {
            val attrs = intArrayOf(androidx.appcompat.R.attr.colorAccent)
            val ta: TypedArray = parent.context.obtainStyledAttributes(attrs)
            val color = ta.getResourceId(0, android.R.color.white)
            ta.recycle()
            cardView.background.setTint(ContextCompat.getColor(holder.itemView.context, color))
        } else {
            val attrs = intArrayOf(androidx.appcompat.R.attr.colorPrimary)
            val ta: TypedArray = parent.context.obtainStyledAttributes(attrs)
            val color = ta.getResourceId(0, android.R.color.white)
            ta.recycle()
            cardView.background.setTint(ContextCompat.getColor(holder.itemView.context, color))
        }

        textView.layoutParams.width = LayoutParams.WRAP_CONTENT
        cardView.layoutParams.width = LayoutParams.WRAP_CONTENT

        cardView.setOnLongClickListener {
            groceryListCallback.onLongClick(data[holder.adapterPosition])
            true
        }

        cardView.setOnClickListener {
            groceryListCallback.onClick(data[holder.adapterPosition])
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
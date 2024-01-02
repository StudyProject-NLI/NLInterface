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


/**
 * Implements the interface between the GroceryListActivity and the Grocery List RecyclerView.
 * Contains the ViewHolder for the GroceryItems in the RecyclerView.
 */
class GroceryListAdapter(
    private val data: ArrayList<GroceryItem>,
    private val groceryListCallback: GroceryListCallback,
) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {

    private lateinit var parent: ViewGroup

    /**
     * The ViewHolder for the GroceryItems shown in the RecyclerView. Stores the TextView and the
     * CardView.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val groceryItemTextView: TextView = itemView.findViewById(R.id.grocery_item_tv)
        val groceryItemCardView: CardView = itemView.findViewById(R.id.grocery_item_cv)
    }

    /**
     * Sets up the Layout for the ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        this.parent = parent
        val context = this.parent.context
        val inflater = LayoutInflater.from(context)
        val groceryItemView = inflater.inflate(R.layout.grocery_item, parent, false)

        return ViewHolder(groceryItemView)
    }

    /**
     * Configures the View in the ViewHolder, is called separately for each position in the data:
     *
     * 1- formats the background color of the CardViews depending on the inCart state of the
     * GroceryItem,
     * 2- sets the text to be shown,
     * 3- resizes the Card and TextViews based on the content and
     * 4- configures the onLongClick and onClick listeners for the CardViews, which is required for
     * deleting and placing into/taking out of cart functionalities.
     *
     * @param holder: ViewHolder
     * @param position: Int
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val groceryItem: GroceryItem = data[position]

        // Set item views based on your views and data model
        val textView = holder.groceryItemTextView
        val cardView = holder.groceryItemCardView

        textView.text = groceryItem.itemName

        // set backgroundTint of CardView to accent color if item is in cart, else to primary color
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

        // resize widths based on length of content
        textView.layoutParams.width = LayoutParams.WRAP_CONTENT
        cardView.layoutParams.width = LayoutParams.WRAP_CONTENT

        // Notifies GroceryListCallback onLongClick
        cardView.setOnLongClickListener {
            groceryListCallback.onLongClick(data[holder.adapterPosition])
            true
        }

        // Notifies GroceryListCallback onClick
        cardView.setOnClickListener {
            groceryListCallback.onClick(data[holder.adapterPosition])
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
package com.nlinterface.activities

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.adapters.GroceryListAdapter
import com.nlinterface.databinding.ActivityGroceryListBinding
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.interfaces.GroceryListCallback
import com.nlinterface.viewmodels.GroceryListViewModel


class GroceryListActivity : AppCompatActivity(), GroceryListCallback {

    private lateinit var binding: ActivityGroceryListBinding
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var layout: ConstraintLayout
    private lateinit var adapter: GroceryListAdapter
    private lateinit var viewModel: GroceryListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[GroceryListViewModel::class.java]
        viewModel.fetchGroceryList()

        binding = ActivityGroceryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.layout = findViewById(R.id.activity_grocery_list_cl)

        // RecyclerView
        val rvGroceryList = findViewById<View>(R.id.grocery_list_rv) as RecyclerView
        groceryItemList = viewModel.groceryList

        adapter = GroceryListAdapter(groceryItemList, this)
        rvGroceryList.adapter = adapter
        rvGroceryList.layoutManager = LinearLayoutManager(this)

        // Add Item Button Listener
        val addItemButton: Button = findViewById<View>(R.id.add_item_bt) as Button
        addItemButton.setOnClickListener {
            onAddItemButtonClick()
        }

        val voiceActivationButton = findViewById<View>(R.id.grocery_list_bt) as ImageButton
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels / 3
        voiceActivationButton.layoutParams = ConstraintLayout.LayoutParams(width, height)

        voiceActivationButton.setOnClickListener {
            onAddVoiceActivationButtonClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.storeGroceryList()
    }

    private fun onAddVoiceActivationButtonClick() {
        Log.println(Log.ASSERT, "GroceryListActivity: onAddVoiceActivationButtonClick", "Button CLicked")
    }

    private fun onAddItemButtonClick() {

        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            val view = layoutInflater.inflate(R.layout.add_item_dialog, null)
            builder.setView(view)
            builder.apply {
                setPositiveButton(R.string.add) { _, _ ->

                    val addItemEt = view.findViewById<EditText>(R.id.add_item_et)
                    val newItemName = addItemEt.text.toString()

                    viewModel.addGroceryItem(newItemName)
                    adapter.notifyItemInserted(groceryItemList.size - 1)
                }

                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Set other dialog properties
            builder.setMessage(R.string.add_new_grocery_item)
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    override fun onLongClick(item: GroceryItem) {
        val index = groceryItemList.indexOf(item)
        viewModel.deleteGroceryItem(item)
        adapter?.notifyItemRemoved(index)
    }

}
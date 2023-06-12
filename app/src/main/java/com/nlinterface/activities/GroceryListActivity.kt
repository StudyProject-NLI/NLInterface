package com.nlinterface.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.databinding.ActivityGroceryListBinding
import com.nlinterface.adapters.GroceryListAdapter
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.viewmodels.GroceryListViewModel

class GroceryListActivity : AppCompatActivity() {

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

        adapter = GroceryListAdapter(groceryItemList)
        rvGroceryList.adapter = adapter
        rvGroceryList.layoutManager = LinearLayoutManager(this)

        // Add Item Button Listener
        val addItemButton: Button = findViewById<View>(R.id.add_item_bt) as Button
        addItemButton.setOnClickListener {
            onAddItemButtonClick()
        }

        // RecyclerView GroceryItem Listener
    }

    private fun onAddItemButtonClick() {

        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            val view = layoutInflater.inflate(R.layout.add_item_dialog, null)
            builder.setView(view)
            builder.apply {
                setPositiveButton(R.string.add) { _, _ ->

                    var addItemEt = view.findViewById<EditText>(R.id.add_item_et)
                    var newItemName = addItemEt.text.toString()

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


}
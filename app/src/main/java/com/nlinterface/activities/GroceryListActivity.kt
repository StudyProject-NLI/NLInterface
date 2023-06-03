package com.nlinterface.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.databinding.ActivityGrocerylistBinding
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.viewholders.GroceryListAdapter

class GroceryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrocerylistBinding
    private lateinit var grocerylist: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGrocerylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rvGroceryList = findViewById<View>(R.id.grocery_list_rv) as RecyclerView

        grocerylist = arrayListOf("Apples", "Oranges", "Bananas")

        val adapter = GroceryListAdapter(grocerylist)

        rvGroceryList.adapter = adapter

        rvGroceryList.layoutManager = LinearLayoutManager(this)
    }

}
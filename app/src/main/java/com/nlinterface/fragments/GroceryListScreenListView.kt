package com.nlinterface.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.adapters.GroceryListAdapter
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.GroceryListViewModel

class GroceryListScreenListView : Fragment(), SwipeAction {

    lateinit var viewModel: GroceryListViewModel
    private lateinit var adapter: GroceryListAdapter
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var rvGroceryList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.grocerylist_listview, container, false)
        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        rvGroceryList = view.findViewById<View>(R.id.grocery_list_rv) as RecyclerView
        viewModel = ViewModelProvider(this)[GroceryListViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set up add item button listener
        viewModel.fetchGroceryList()
        groceryItemList = viewModel.groceryList
        configureRecyclerView()
    }

    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}
    override fun onSwipeUp() {}
    override fun onSwipeDown(){}
    override fun onDoubleTap() {}
    override fun onLongPress() {}

    private fun configureRecyclerView() {
        adapter = GroceryListAdapter(groceryItemList, activity as GroceryListActivity)

        rvGroceryList.adapter = adapter
        rvGroceryList.layoutManager = LinearLayoutManager(activity as GroceryListActivity)
    }
}
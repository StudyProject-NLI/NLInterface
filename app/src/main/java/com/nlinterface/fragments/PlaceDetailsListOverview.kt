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
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.adapters.PlaceDetailsAdapter
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.PlaceDetailsViewModel

class PlaceDetailsListOverview : Fragment(), SwipeAction {

    lateinit var viewModel: PlaceDetailsViewModel
    private lateinit var adapter: PlaceDetailsAdapter
    private lateinit var placesList: ArrayList<PlaceDetailsItem>
    private lateinit var rvPlacesList: RecyclerView

    /**
     * On Create View creates the layout and sets up the swipe Navigation. It also accesses
     * the grocery list
     * On ViewCreated accesses the shared viewmodel and displays the grocery list.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.places_listview, container, false)
        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        rvPlacesList = view.findViewById<View>(R.id.places_list_rv) as RecyclerView
        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchPlaceDetailsItemList()
        placesList = viewModel.placeDetailsItemList
        configureRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPlaceDetailsItemList()
        placesList = viewModel.placeDetailsItemList
        configureRecyclerView()
    }

    /**
     * Does not implement any swipe functionalities. This screen is a mere overview.
     */

    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}
    override fun onSwipeUp() {}
    override fun onSwipeDown(){}
    override fun onDoubleTap() {}
    override fun onLongPress() {}

    /**
     * Handles the RecyclerView and sets up necessary parameters for displaying the grocery list.
     */
    private fun configureRecyclerView() {
        adapter = PlaceDetailsAdapter(placesList, activity as PlaceDetailsActivity)
        rvPlacesList.adapter = adapter
        rvPlacesList.layoutManager = LinearLayoutManager(activity as PlaceDetailsActivity)
    }
}
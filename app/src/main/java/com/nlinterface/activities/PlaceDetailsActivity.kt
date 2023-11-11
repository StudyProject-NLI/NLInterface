package com.nlinterface.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.nlinterface.R
import com.nlinterface.adapters.PlaceDetailsAdapter
import com.nlinterface.databinding.ActivityPlaceDetailsBinding
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.interfaces.PlaceDetailsItemCallback
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.PlaceDetailsViewModel


class PlaceDetailsActivity: AppCompatActivity(), PlaceDetailsItemCallback {

    private lateinit var binding: ActivityPlaceDetailsBinding
    private lateinit var viewModel: PlaceDetailsViewModel
    private lateinit var placeDetailsItemList: ArrayList<PlaceDetailsItem>
    private lateinit var adapter: PlaceDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]
        viewModel.initPlaceClient(this)

        viewModel.fetchPlaceDetailsItemList()

        configureUI()
        configureAutocompleteFragment()

    }

    private fun configureUI() {

        // set up voice activation button listener
        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }

        // resize Voice Activation Button to 1/3 of display size
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        configureRecyclerView()
    }

    private fun configureRecyclerView() {

        val rvPlaceDetails = findViewById<View>(R.id.place_details_rv) as RecyclerView
        placeDetailsItemList = viewModel.placeDetailsItemList

        adapter = PlaceDetailsAdapter(placeDetailsItemList, this)
        rvPlaceDetails.adapter = adapter
        rvPlaceDetails.layoutManager = LinearLayoutManager(this)

        rvPlaceDetails.itemAnimator?.changeDuration = 0

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {return false}

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val placeDetailsItem: PlaceDetailsItem =
                    placeDetailsItemList[viewHolder.adapterPosition]

                val index = viewHolder.adapterPosition

                viewModel.deletePlaceDetailsItem(placeDetailsItem)
                adapter.notifyItemRemoved(index)
            }
        }).attachToRecyclerView(rvPlaceDetails)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {return false}

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val placeDetailsItem: PlaceDetailsItem =
                    placeDetailsItemList[viewHolder.adapterPosition]

                val index = viewHolder.adapterPosition

                viewModel.deletePlaceDetailsItem(placeDetailsItem)
                adapter.notifyItemRemoved(index)
            }
        }).attachToRecyclerView(rvPlaceDetails)

    }

    private fun configureAutocompleteFragment() {

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID))
        autocompleteFragment.setTypesFilter(listOf("supermarket"))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                viewModel.onError(status)
            }
            override fun onPlaceSelected(place: Place) {
                viewModel.onPlaceSelected(place) { if (it) adapter.notifyItemInserted(placeDetailsItemList.size - 1) }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.storePlaceDetailsItemList()
    }

    override fun onClick(item: PlaceDetailsItem) {
        val index = placeDetailsItemList.indexOf(item)
        viewModel.changeFavorite(item)
        adapter.notifyItemChanged(index)
    }

    private fun onVoiceActivationButtonClick() {
        // TODO
    }
}
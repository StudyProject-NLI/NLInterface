package com.nlinterface.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.button.MaterialButton
import com.nlinterface.R
import com.nlinterface.databinding.ActivityPlaceDetailsBinding
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.GroceryListViewModel
import com.nlinterface.viewmodels.PlaceDetailsViewModel

class PlaceDetailsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailsBinding
    private lateinit var viewModel: PlaceDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]
        viewModel.initPlaceClient(this)

        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        voiceActivationButton.setOnClickListener {} // TODO

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                viewModel.onError(status)
            }
            override fun onPlaceSelected(place: Place) {
                viewModel.onPlaceSelected(place) { if (it) loadInformation() }
            }
        })
    }

    private fun loadInformation() {
        val nameField = findViewById<View>(R.id.place_details_name_tv) as TextView
        val openingHoursField = findViewById<View>(R.id.place_details_opening_hours_tv) as TextView

        nameField.text = viewModel.getPlaceName()
        openingHoursField.text = viewModel.getPlaceOpeningHours().toString()
    }
}
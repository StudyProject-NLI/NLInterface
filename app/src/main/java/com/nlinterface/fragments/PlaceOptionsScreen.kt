package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nlinterface.R
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.PlaceDetailsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaceOptionsScreen(
    private var correspondingPlace: String
) : Fragment(), SwipeAction {

    private lateinit var viewModel: PlaceDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.place_options_screen, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[PlaceDetailsViewModel::class.java]

    }

    /**
     * Implements functionalities on swipe inputs.
     * Swiping directions matched to usual swiping navigation in other apps.
     */

    /**
     * A swipe to the left states the opening hours.
     */

    override fun onSwipeLeft() {
        (activity as PlaceDetailsActivity).stateOpeningHours(correspondingPlace)
    }

    override fun onSwipeRight() {

    }

    /**
     * Deletes the Place from the List and navigates back to places screen.
     */

    override fun onSwipeUp() {
        (activity as PlaceDetailsActivity).deletePlaceDetailsItem(correspondingPlace)
        lifecycleScope.launch {
            delay(1000L)
            navigateBack()
        }
    }

    /**
     * Navigates back to the corresponding places fragment.
     */

    override fun onSwipeDown() {
        navigateBack()
    }

    /**
     * Navigates to the Voice Only Activity.
     */
    override fun onDoubleTap() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    /**
     * Activates the LLM to start listening.
     */
    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    /**
     * Function that handles the navigation to the places fragment.
     */
    private fun navigateBack() {
        parentFragmentManager.popBackStack()
    }
}
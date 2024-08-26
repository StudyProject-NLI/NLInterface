package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nlinterface.R
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.MainViewModel

/**
 * Fragment for the main activity.
 */
class MainScreen2 : Fragment(), SwipeAction {

    private lateinit var viewModel: MainViewModel

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * On ViewCreated accesses the shared viewmodel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_screen2, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    /**
     * Implements functionalities on swipe inputs.
     * Swiping directions matched to usual swiping navigation in other apps.
     */

    /**
     * Navigates to the Place Details Activity.
     */
    override fun onSwipeLeft() {
        val intent = Intent(activity, PlaceDetailsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigates to the first screen of the main activity.
     */
    override fun onSwipeRight() {
        findNavController().navigate(R.id.Main2_to_Main1)
    }

    /**
     * Navigates to the barcode scanner.
     * (It is included in the navigational framework for simplification purposes.)
     */
    override fun onSwipeUp() {
        findNavController().navigate(R.id.Main2_to_BarcodeScanner)
    }
    /**
     *
     */
    override fun onSwipeDown() {

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
}
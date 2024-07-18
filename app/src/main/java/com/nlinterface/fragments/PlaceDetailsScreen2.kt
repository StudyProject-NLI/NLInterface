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
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.PlaceDetailsViewModel

class PlaceDetailsScreen2 : Fragment(), SwipeAction {

    private lateinit var viewModel: PlaceDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.place_details_screen2, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[PlaceDetailsViewModel::class.java]

    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.PlaceDetails2_to_PlaceDetails3)
    }

    override fun onSwipeRight() {
        findNavController().navigate(R.id.BarcodeScannerSettings1_to_BarcodeScannerSettings2)
    }

    override fun onSwipeUp() {
        findNavController().navigate(R.id.PlaceDetails2_to_PlaceOptions)
    }

    override fun onSwipeDown() {
        findNavController().navigate(R.id.PlaceDetails2_to_PlaceOptions)
    }

    override fun onDoubleTap() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }
}
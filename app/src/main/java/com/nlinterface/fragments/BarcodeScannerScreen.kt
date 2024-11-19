package com.nlinterface.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.ConstantScanning
import com.nlinterface.viewmodels.MainViewModel

/**
 * Fragment for the barcode scanner.
 */
class BarcodeScannerScreen : Fragment(), SwipeAction {


    private lateinit var barcodeService: Intent

    private lateinit var viewModel: MainViewModel

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * Creates the barcode service to allow this fragment to start it.
     * On View Created accesses the viewmodel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.barcode_scanner, container, false)
        barcodeService = Intent(activity, ConstantScanning()::class.java)
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
     * Starts the barcode scanner.
     */
    override fun onSwipeLeft() {

    }

    /**
     * Stops the barcode scanner.
     */
    override fun onSwipeRight() {

    }

    /**
     * Navigates to the barcode scanner settings.
     */
    override fun onSwipeUp() {
        if (activity?.checkCallingOrSelfPermission( Manifest.permission.CAMERA ) ==
            PackageManager.PERMISSION_GRANTED) {
            activity?.startService(barcodeService)
        }
    }

    /**
     * Navigates to the second screen of the main activity.
     */
    override fun onSwipeDown() {
        activity?.stopService(barcodeService)
        Log.i("Scanner", "Stopping the Barcode Scanning Service")
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
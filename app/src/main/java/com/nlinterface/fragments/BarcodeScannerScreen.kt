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
import androidx.navigation.fragment.findNavController
import com.nlinterface.R
import com.nlinterface.activities.BarcodeSettingsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.ConstantScanning
import com.nlinterface.viewmodels.MainViewModel

class BarcodeScannerScreen : Fragment(), SwipeAction {

    private val globalParameters = GlobalParameters.instance!!
    private lateinit var barcodeService: Intent

    private lateinit var viewModel: MainViewModel

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

    override fun onSwipeLeft() {
        val intent = Intent(activity, BarcodeSettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onSwipeRight() {
        findNavController().navigate(R.id.BarcodeScanner_to_Main2)
    }

    override fun onSwipeUp() {
        activity?.stopService(barcodeService)
        Log.i("Scanner", "Stopping the Barcode Scanning Service")
    }

    override fun onSwipeDown() {
        if (activity?.checkCallingOrSelfPermission( Manifest.permission.CAMERA ) ==
            PackageManager.PERMISSION_GRANTED) {
            activity?.startService(barcodeService)
        }
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
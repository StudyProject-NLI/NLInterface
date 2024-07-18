package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nlinterface.R
import com.nlinterface.activities.SettingsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.BarcodeSettingsViewModel

class BarcodeSettingsScreen1 : Fragment(), SwipeAction {

    private val globalParameters = GlobalParameters.instance!!

    private lateinit var viewModel: BarcodeSettingsViewModel

    private lateinit var nameAndVolumeOptions: MutableList<String>
    private lateinit var nameAndVolumeButton: Button

    private lateinit var ingredientsAndAllergiesOptions: MutableList<String>
    private lateinit var ingredientsAndAllergiesButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.barcode_scanner_settings_screen1, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[BarcodeSettingsViewModel::class.java]

        // Find the button
        nameAndVolumeButton = view.findViewById(R.id.settings_name_and_volume)
        nameAndVolumeOptions = mutableListOf()
        resources.getStringArray(R.array.settings_name_and_volume).forEach { option ->
            nameAndVolumeOptions.add(option)
        }

        ingredientsAndAllergiesButton = view.findViewById(R.id.settings_ingredients_and_allergies)
        ingredientsAndAllergiesOptions = mutableListOf()
        resources.getStringArray(R.array.settings_ingredients_and_allergies).forEach { option ->
            ingredientsAndAllergiesOptions.add(option)
        }
    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.BarcodeScannerSettings1_to_BarcodeScannerSettings2)
    }

    override fun onSwipeRight() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onSwipeUp() {
        if (globalParameters.navState.ordinal == GlobalParameters.NavState.values().size - 1) {
            globalParameters.navState = GlobalParameters.NavState.values()[0]
        } else {
            globalParameters.navState =
                GlobalParameters.NavState.values()[globalParameters.navState.ordinal + 1]
        }

        nameAndVolumeButton.text =
            nameAndVolumeOptions[globalParameters.navState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, nameAndVolumeButton.text))
    }

    override fun onSwipeDown() {
        if (globalParameters.iaaState.ordinal == GlobalParameters.IaaState.values().size - 1) {
            globalParameters.iaaState = GlobalParameters.IaaState.values()[0]
        } else {
            globalParameters.iaaState =
                GlobalParameters.IaaState.values()[globalParameters.iaaState.ordinal + 1]
        }

        ingredientsAndAllergiesButton.text = ingredientsAndAllergiesOptions[globalParameters.iaaState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, ingredientsAndAllergiesButton.text))
    }

    override fun onDoubleTap() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.setSTTSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSTTSpeechBegin()
        } else {
            viewModel.cancelSTTListening()
        }
    }
}
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
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.BarcodeSettingsViewModel

class BarcodeSettingsScreen3 : Fragment(), SwipeAction {

    private val globalParameters = GlobalParameters.instance!!

    private lateinit var viewModel:BarcodeSettingsViewModel

    private lateinit var shortNutritionalValuesOptions: MutableList<String>
    private lateinit var shortNutritionalValuesButton: Button

    private lateinit var brandsOptions: MutableList<String>
    private lateinit var brandsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.barcode_scanner_settings_screen3, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[BarcodeSettingsViewModel::class.java]

        // Find the button
        shortNutritionalValuesButton = view.findViewById(R.id.settings_short_nutritional_values)
        shortNutritionalValuesOptions = mutableListOf()
        resources.getStringArray(R.array.settings_short_nutritional_values).forEach { option ->
            shortNutritionalValuesOptions.add(option)
        }

        brandsButton = view.findViewById(R.id.settings_brands)
        brandsOptions = mutableListOf()
        resources.getStringArray(R.array.settings_brand).forEach { option ->
            brandsOptions.add(option)
        }
    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.BarcodeScannerSettings3_to_BarcodeScannerSettings1)
    }

    override fun onSwipeRight() {
        findNavController().navigate(R.id.BarcodeScannerSettings3_to_BarcodeScannerSettings2)
    }

    override fun onSwipeUp() {
        if (globalParameters.snvState.ordinal == GlobalParameters.SnvState.values().size - 1) {
            globalParameters.snvState = GlobalParameters.SnvState.values()[0]
        } else {
            globalParameters.snvState =
                GlobalParameters.SnvState.values()[globalParameters.snvState.ordinal + 1]
        }
        shortNutritionalValuesButton.text = shortNutritionalValuesOptions[globalParameters.snvState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, shortNutritionalValuesButton.text))
    }

    override fun onSwipeDown() {
        if (globalParameters.brandsState.ordinal == GlobalParameters.SnvState.values().size - 1) {
            globalParameters.brandsState = GlobalParameters.BrandsState.values()[0]
        } else {
            globalParameters.brandsState =
                GlobalParameters.BrandsState.values()[globalParameters.brandsState.ordinal + 1]
        }
        brandsButton.text = brandsOptions[globalParameters.brandsState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, brandsButton.text))
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
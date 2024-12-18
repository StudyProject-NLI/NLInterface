package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.BarcodeSettingsViewModel

/**
 * Fragment for the barcode settings activity.
 */
class BarcodeSettingsScreen2 : Fragment(), SwipeAction {

    private val globalParameters = GlobalParameters.instance!!

    private lateinit var viewModel: BarcodeSettingsViewModel

    private lateinit var labelsOptions: MutableList<String>
    private lateinit var labelsButton: Button

    private lateinit var countryOfOriginOptions: MutableList<String>
    private lateinit var countryOfOriginButton: Button

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * On ViewCreated accesses the shared viewmodel, creates the visual buttons and gets the
     * options for the buttons values.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.barcode_scanner_settings_screen2, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[BarcodeSettingsViewModel::class.java]

        // Find the button
        labelsButton = view.findViewById(R.id.settings_labels)
        labelsOptions = mutableListOf()
        resources.getStringArray(R.array.settings_labels).forEach { option ->
            labelsOptions.add(option)
        }
        countryOfOriginButton = view.findViewById(R.id.settings_country_of_origin)
        countryOfOriginOptions = mutableListOf()
        resources.getStringArray(R.array.settings_country_of_origin).forEach { option ->
            countryOfOriginOptions.add(option)
        }
    }

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}

    override fun onSwipeRight() {}

    /**
     * Activates/Deactivates specified information retrieval.
     */
    override fun onSwipeUp() {
        if (globalParameters.labelsState.ordinal == GlobalParameters.LabelsState.entries.size - 1) {
            globalParameters.labelsState = GlobalParameters.LabelsState.entries.toTypedArray()[0]
        } else {
            globalParameters.labelsState =
                GlobalParameters.LabelsState.entries.toTypedArray()[globalParameters.labelsState.ordinal + 1]
        }
        labelsButton.text = labelsOptions[globalParameters.labelsState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, labelsButton.text))
    }

    /**
     * Activates/Deactivates specified information retrieval.
     */
    override fun onSwipeDown() {
        if (globalParameters.cooState.ordinal == GlobalParameters.CooState.entries.size - 1) {
            globalParameters.cooState = GlobalParameters.CooState.entries.toTypedArray()[0]
        } else {
            globalParameters.cooState =
                GlobalParameters.CooState.entries.toTypedArray()[globalParameters.cooState.ordinal + 1]
        }
        countryOfOriginButton.text = countryOfOriginOptions[globalParameters.cooState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, countryOfOriginButton.text))
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
            viewModel.setSTTSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSTTSpeechBegin()
        } else {
            viewModel.cancelSTTListening()
        }
    }
}
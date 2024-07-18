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
import com.nlinterface.viewmodels.SettingsViewModel

class SettingsScreen3 : Fragment(), SwipeAction {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var narrationAmountOptions: MutableList<String>
    private lateinit var narrationAmountButton: Button

    private lateinit var narrationSpeedOptions: MutableList<String>
    private lateinit var narrationSpeedButton: Button

    private val globalParameters = GlobalParameters.instance!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.settings_screen3, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

        // Find the button
        narrationAmountButton = view.findViewById(R.id.settings_narration_amount)
        narrationAmountOptions = mutableListOf()
        resources.getStringArray(R.array.narration_amount_options).forEach { option ->
            narrationAmountOptions.add(option)
        }

        narrationSpeedButton = view.findViewById(R.id.settings_narration_speed)
        narrationSpeedOptions = mutableListOf()
        resources.getStringArray(R.array.narration_speed_options).forEach { option ->
            narrationSpeedOptions.add(option)
        }
    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.Settings3_to_Settings1)
    }

    override fun onSwipeRight() {
        findNavController().navigate(R.id.Settings3_to_Settings2)
    }

    override fun onSwipeUp() {
        if (globalParameters.narrationAmountChoice.ordinal ==
            GlobalParameters.NarrationAmountChoice.values().size - 1) {
            globalParameters.narrationAmountChoice = GlobalParameters.NarrationAmountChoice.values()[0]
        } else {
            globalParameters.narrationAmountChoice =
                GlobalParameters.NarrationAmountChoice.values()[globalParameters.narrationAmountChoice.ordinal + 1]
        }

       narrationAmountButton.text = narrationAmountOptions[globalParameters.narrationAmountChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, narrationAmountButton.text))
    }

    override fun onSwipeDown() {
        if (globalParameters.narrationSpeedChoice.ordinal ==
            GlobalParameters.NarrationSpeedChoice.values().size - 1) {
            globalParameters.narrationSpeedChoice = GlobalParameters.NarrationSpeedChoice.values()[0]
        } else {
            globalParameters.narrationSpeedChoice =
                GlobalParameters.NarrationSpeedChoice.values()[globalParameters.narrationSpeedChoice.ordinal + 1]
        }

        narrationSpeedButton.text = narrationSpeedOptions[globalParameters.narrationSpeedChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, narrationSpeedButton.text))
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
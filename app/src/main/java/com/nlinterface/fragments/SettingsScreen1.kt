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
import com.nlinterface.activities.BarcodeSettingsActivity
import com.nlinterface.activities.MainActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.SettingsViewModel

class SettingsScreen1 : Fragment(), SwipeAction {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var feedbackOptions: MutableList<String>
    private lateinit var feedbackButton: Button

    private val globalParameters = GlobalParameters.instance!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.settings_screen1, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

        // Find the button
        feedbackButton = view.findViewById(R.id.settings_feedback)
        feedbackOptions = mutableListOf()
        resources.getStringArray(R.array.feedback_options).forEach { option ->
            feedbackOptions.add(option)
        }
    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.Settings1_to_Settings2)
    }

    override fun onSwipeRight() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onSwipeUp() {
        if (globalParameters.feedbackChoice.ordinal ==
            GlobalParameters.FeedbackChoice.values().size - 1) {
            globalParameters.feedbackChoice = GlobalParameters.FeedbackChoice.values()[0]
        } else {
            globalParameters.feedbackChoice =
                GlobalParameters.FeedbackChoice.values()[globalParameters.feedbackChoice.ordinal + 1]
        }

        feedbackButton.text = feedbackOptions[globalParameters.feedbackChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, feedbackButton.text))
    }

    override fun onSwipeDown() {
        val intent = Intent(activity, BarcodeSettingsActivity::class.java)
        startActivity(intent)
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
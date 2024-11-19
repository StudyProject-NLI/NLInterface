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
import com.nlinterface.viewmodels.SettingsViewModel

/**
 * Fragment for the settings activity.
 */
class SettingsScreen1 : Fragment(), SwipeAction {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var feedbackOptions: MutableList<String>
    private lateinit var feedbackButton: Button

    private val globalParameters = GlobalParameters.instance!!

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

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}

    /**
     * Changes the specified setting.
     */
    override fun onSwipeUp() {
        if (globalParameters.feedbackChoice.ordinal ==
            GlobalParameters.FeedbackChoice.entries.size - 1) {
            globalParameters.feedbackChoice = GlobalParameters.FeedbackChoice.entries.toTypedArray()[0]
        } else {
            globalParameters.feedbackChoice =
                GlobalParameters.FeedbackChoice.entries.toTypedArray()[globalParameters.feedbackChoice.ordinal + 1]
        }

        feedbackButton.text = feedbackOptions[globalParameters.feedbackChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, feedbackButton.text))
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
            viewModel.setSTTSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSTTSpeechBegin()
        } else {
            viewModel.cancelSTTListening()
        }
    }
}
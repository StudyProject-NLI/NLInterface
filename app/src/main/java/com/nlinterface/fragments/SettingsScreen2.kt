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
import com.nlinterface.R.layout
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.SettingsViewModel

/**
 * Fragment for the settings activity.
 */
class SettingsScreen2 : Fragment(), SwipeAction {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var keepScreenOnOptions: MutableList<String>
    private lateinit var keepScreenOnButton: Button

    private lateinit var themeOptions: MutableList<String>
    private lateinit var themeButton: Button

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
        val view = inflater.inflate(layout.settings_screen2, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

        // Find the button
        themeButton = view.findViewById(R.id.settings_theme)
        themeOptions = mutableListOf()
        resources.getStringArray(R.array.theme_options).forEach { option ->
            themeOptions.add(option)
        }

        keepScreenOnButton = view.findViewById(R.id.settings_keep_screen_on)
        keepScreenOnOptions = mutableListOf()
        resources.getStringArray(R.array.keep_screen_on_options).forEach { option ->
            keepScreenOnOptions.add(option)
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
        if (globalParameters.themeChoice.ordinal == GlobalParameters.ThemeChoice.entries.size - 1) {
            globalParameters.themeChoice = GlobalParameters.ThemeChoice.entries.toTypedArray()[0]
        } else {
            globalParameters.themeChoice =
                GlobalParameters.ThemeChoice.entries.toTypedArray()[globalParameters.themeChoice.ordinal + 1]
        }

        themeButton.text = themeOptions[globalParameters.themeChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, themeButton.text))
    }

    /**
     * Changes the specified setting.
     */
    override fun onSwipeDown() {
        if (globalParameters.keepScreenOn.ordinal == GlobalParameters.KeepScreenOn.entries.size - 1) {
            globalParameters.keepScreenOn = GlobalParameters.KeepScreenOn.entries.toTypedArray()[0]
        } else {
            globalParameters.keepScreenOn =
                GlobalParameters.KeepScreenOn.entries.toTypedArray()[globalParameters.keepScreenOn.ordinal + 1]
        }

        keepScreenOnButton.text = keepScreenOnOptions[globalParameters.keepScreenOn.ordinal]

        viewModel.say(resources.getString(R.string.new_screen_setting, keepScreenOnButton.text))
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
package com.nlinterface.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.SettingsViewModel

/**
 * The SettingsActivity handles user interaction in Settings Menu.
 *
 * The Settings Menu comprises the Voice Activation Buttons and a button for each settings
 * functionality. Each click on a settings button will cycle through the available settings,
 * narrating each action. The settings are applied once the MainActivity is selected. Current
 * setting options are:
 *
 * 1- Screen Always On/Dim Screen after some time
 * 2- Device Theme/Dark Theme/Light Theme
 *
 * Possible Voice Commands:
 * - 'Read Screen Settings'
 * - 'Read Theme Settings'
 * - 'List Current Settings'
 * - 'Set Screen Settings' --> Always On or Dim? --> X
 * - 'Set Theme Settings' --> Default, Light or Dark? --> X
 *
 * TODO: Add TTS Speed Settings
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private lateinit var keepScreenOnOptions: MutableList<String>
    private lateinit var keepScreenOnButton: Button

    private lateinit var themeOptions: MutableList<String>
    private lateinit var themeButton: Button

    private lateinit var voiceActivationButton: ImageButton

    /**
     * The onCreate Function initializes the view by binding the Activity and the Layout,
     * retrieving the ViewModel, loading the options for each preference type, configuring the UI
     * and configuring the TTS/STT systems.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        keepScreenOnOptions = mutableListOf()
        resources.getStringArray(R.array.keep_screen_on_options).forEach { option ->
            keepScreenOnOptions.add(option)
        }

        themeOptions = mutableListOf()
        resources.getStringArray(R.array.theme_options).forEach { option ->
            themeOptions.add(option)
        }

        configureUI()
        configureTTS()
        configureSTT()
    }

    /**
     * Sets up all UI elements, i.e. the voiceActivation/theme/keepScreenOn buttons and their
     * respective onClickListeners
     */
    private fun configureUI() {

        voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }

        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        themeButton = findViewById(R.id.settings_theme)
        themeButton.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]

        themeButton.setOnClickListener {
            onThemeButtonClick()
        }

        keepScreenOnButton = findViewById(R.id.settings_keep_screen_on)
        keepScreenOnButton.text =
            keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        keepScreenOnButton.setOnClickListener {
            onKeepScreenOnButtonClick()
        }

    }

    /**
     * Cycle through the options for the Theme settings, when the button is clicked. Narrate the
     * action.
     */
    private fun onThemeButtonClick() {

        if (
            GlobalParameters.instance!!.themeChoice.ordinal ==
            GlobalParameters.ThemeChoice.values().size - 1
        ) {
            GlobalParameters.instance!!.themeChoice = GlobalParameters.ThemeChoice.values()[0]
        } else {
            GlobalParameters.instance!!.themeChoice =
                GlobalParameters.ThemeChoice.values()[
                    GlobalParameters.instance!!.themeChoice.ordinal + 1
                ]
        }
        themeButton.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, themeButton.text))
    }

    /**
     * Cycle through the options for the Screen On settings, when the button is clicked. Narrate the
     * action.
     */
    private fun onKeepScreenOnButtonClick() {

        if (
            GlobalParameters.instance!!.keepScreenOnSwitch.ordinal ==
            GlobalParameters.KeepScreenOn.values().size - 1
        ) {
            GlobalParameters.instance!!.keepScreenOnSwitch =
                GlobalParameters.KeepScreenOn.values()[0]
        } else {
            GlobalParameters.instance!!.keepScreenOnSwitch =
                GlobalParameters.KeepScreenOn.values()[
                    GlobalParameters.instance!!.keepScreenOnSwitch.ordinal + 1
                ]
        }
        keepScreenOnButton.text =
            keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        viewModel.say(resources.getString(R.string.new_screen_setting, keepScreenOnButton.text))
    }

    /**
     * If the activity is paused, save the current preferences to SharedPreferences.
     */
    override fun onPause() {
        super.onPause()

        val sharedPref = this.getSharedPreferences(
            getString(R.string.settings_preferences_key),
            Context.MODE_PRIVATE
        ) ?: return
        with(sharedPref.edit()) {
            putString(
                getString(R.string.settings_keep_screen_on_key),
                GlobalParameters.instance!!.keepScreenOnSwitch.toString()
            )
            putString(
                getString(R.string.settings_theme_key),
                GlobalParameters.instance!!.themeChoice.toString()
            )
            apply()
        }
    }

    /**
     * Called when voiceActivationButton is clicked and handles the result. If clicked while the
     * STT system is listening, call to viewModel to cancel listening. Else, call viewModel to begin
     * listening.
     */
    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    /**
     * Called by the onCreate Function and calls upon the ViewModel to initialize the TTS system. On
     * successful initialization, the Activity name is read aloud.
     */
    private fun configureTTS() {

        viewModel.initTTS()

        // once the TTS is successfully initialized, read out the activity name
        // required, since TTS initialization is asynchronous
        val ttsInitializedObserver = Observer<Boolean> { _ ->
            viewModel.say(resources.getString(R.string.settings))
        }

        // observe LiveDate change, to be notified if TTS initialization is completed
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    /**
     * Called by the onCreate function and calls upon the ViewModel to initialize the STT system.
     * The voiceActivationButton is configured to change it microphone color to green, if the STT
     * system is active and to change back to white, if it is not. Also retrieves the text output
     * of the voice input to the STT system, aka the 'command'
     */
    private fun configureSTT() {

        viewModel.initSTT()

        // if listening: microphone color green, else microphone color white
        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_green)
            } else {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_white)
            }
        }

        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)

        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<ArrayList<String>> { command ->
            executeCommand(command)
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)

    }

    /**
     * Called once the STT system returns a command. It is then processed and, if valid,
     * finally executed by navigating to the next activity
     *
     * @param command: ArrayList<String>? containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun executeCommand(command: ArrayList<String>?) {

        /*
        if ((command != null) && (command.size == 3)) {
            if (command[0] == "GOTO") {
                navToActivity(command[1])
            } else {
                viewModel.say(resources.getString(R.string.choose_activity_to_navigate_to))
            }
        }
         */

    }

    /**
     * Handles navigation to next activity. Called either by button click or by execution of the
     * voice command. If the called for activity is the current one, read out the activity name.
     *
     * @param activity: ActivityType, Enum specifying the activity
     */
    private fun navToActivity(activity: String) {

        Log.println(Log.DEBUG, "navToActivity", activity)

        when (activity) {

            ActivityType.SETTINGS.toString() -> {
                viewModel.say(resources.getString(R.string.settings))
            }

            ActivityType.MAIN.toString() -> {
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.GROCERYLIST.toString() -> {
                val intent = Intent(this, GroceryListActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.PLACEDETAILS.toString() -> {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                this.startActivity(intent)
            }

        }
    }
}
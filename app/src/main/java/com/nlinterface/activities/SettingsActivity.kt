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
import com.nlinterface.utility.GlobalParameters.ThemeChoice
import com.nlinterface.utility.GlobalParameters.KeepScreenOn
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    
    private lateinit var lastCommand: String
    private lateinit var lastResponse: String
    
    private val globalParameters = GlobalParameters.instance!!

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
        voiceActivationButton.setOnClickListener { onVoiceActivationButtonClick() }
        
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        themeButton = findViewById(R.id.settings_theme)
        themeButton.setOnClickListener { onThemeButtonClick() }
        themeButton.text = themeOptions[globalParameters.themeChoice.ordinal]
        
        keepScreenOnButton = findViewById(R.id.settings_keep_screen_on)
        keepScreenOnButton.setOnClickListener { onKeepScreenOnButtonClick() }
        keepScreenOnButton.text = keepScreenOnOptions[globalParameters.keepScreenOn.ordinal]

    }

    /**
     * Cycle through the options for the Theme settings, when the button is clicked. Narrate the
     * action.
     */
    private fun onThemeButtonClick() {

        if (globalParameters.themeChoice.ordinal == ThemeChoice.values().size - 1) {
            globalParameters.themeChoice = ThemeChoice.values()[0]
        } else {
            globalParameters.themeChoice =
                ThemeChoice.values()[globalParameters.themeChoice.ordinal + 1]
        }
        
        themeButton.text = themeOptions[globalParameters.themeChoice.ordinal]

        viewModel.say(resources.getString(R.string.new_theme_setting, themeButton.text))
    }

    /**
     * Cycle through the options for the Screen On settings, when the button is clicked. Narrate the
     * action.
     */
    private fun onKeepScreenOnButtonClick() {

        if (globalParameters.keepScreenOn.ordinal == KeepScreenOn.values().size - 1) {
            globalParameters.keepScreenOn = KeepScreenOn.values()[0]
        } else {
            globalParameters.keepScreenOn =
                KeepScreenOn.values()[globalParameters.keepScreenOn.ordinal + 1]
        }
        
        keepScreenOnButton.text = keepScreenOnOptions[globalParameters.keepScreenOn.ordinal]

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
                globalParameters.keepScreenOn.toString()
            )
            putString(
                getString(R.string.settings_theme_key), globalParameters.themeChoice.toString()
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
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
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
        val commandObserver = Observer<String> { command ->
            lastCommand = command
            executeCommand(command)
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)
    
        // if a response is successfully generated, process and execute it
        val responseObserver = Observer<String> { response ->
            lastResponse = response
            executeSettingsCommand(lastCommand, lastResponse)
        }
    
        // observe LiveData change to be notified when the STT returns a response
        viewModel.response.observe(this, responseObserver)

    }

    /**
     * Called once the STT system returns a command. It is then processed and, if valid,
     * finally executed by navigating to the next activity
     *
     * @param command: ArrayList<String>? containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun executeCommand(command: String) {
    
        if (command.contains("go to")) {
            executeNavigationCommand(command)
        
        } else if (command == resources.getString(R.string.change_theme)) {
    
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                requestResponse(
                    resources.getString(R.string.light_theme) + " " +
                        resources.getString(R.string.dark_theme) +
                        " or " + resources.getString(R.string.default_theme))
            }
        
        } else if (command == resources.getString(R.string.change_screen_settings)) {
    
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                requestResponse(
                    resources.getString(R.string.keep_screen_always_on) +
                            " or " + resources.getString(R.string.dim_screen_after_a_while))
            }
            
        } else if (command == resources.getString(R.string.tell_me_my_options)) {
    
            viewModel.say(
                "${resources.getString(R.string.your_options_are)} " +
                        "${resources.getString(R.string.change_theme)}," +
                        "${resources.getString(R.string.change_screen_settings)}," +
                        "${resources.getString(R.string.navigate_to_grocery_list)}," +
                        "${resources.getString(R.string.navigate_to_place_details)} and" +
                        "${resources.getString(R.string.navigate_to_settings)}."
            )
    
        } else {
            viewModel.say(resources.getString(R.string.invalid_command))
        }
        
    }
    
    private fun executeSettingsCommand(command: String, response: String) {
        
        if (response != resources.getString(R.string.cancel)) {
            
            when (command) {
    
                resources.getString(R.string.change_theme) -> {
                    changeTheme(response)
                }
    
                resources.getString(R.string.change_screen_settings) -> {
                    changeScreenSettings(response)
                }
                
            }
            
        }
    
    }
    
    private fun changeTheme(response: String) {
        
        when (response) {
            
            resources.getString(R.string.default_theme) -> {
                viewModel.setTheme(ThemeChoice.SYSTEM_DEFAULT)
                viewModel.say(
                    resources.getString(R.string.new_theme_setting, " default")
                )
            }
    
            resources.getString(R.string.light_theme) -> {
                viewModel.setTheme(ThemeChoice.LIGHT)
                viewModel.say(
                    resources.getString(R.string.new_theme_setting, " light theme")
                )
            }
    
            resources.getString(R.string.dark_theme) -> {
                viewModel.setTheme(ThemeChoice.DARK)
                viewModel.say(
                    resources.getString(R.string.new_theme_setting, " dark theme")
                )
            }
            
        }
        
    }
    
    private fun changeScreenSettings(response: String) {
        
        when (response) {
            
            resources.getString(R.string.keep_screen_always_on) -> {
                viewModel.setScreenSettings(KeepScreenOn.YES)
                viewModel.say(
                    resources.getString(R.string.new_screen_setting,
                        " keep screen always on")
                )
            }
    
            resources.getString(R.string.dim_screen_after_a_while) -> {
                viewModel.setScreenSettings(KeepScreenOn.NO)
                viewModel.say(
                    resources.getString(R.string.new_screen_setting,
                        " dim screen after a while")
                )
            }
            
        }
        
    }
    
    /**
     * Handles Navigation commands of the format "go to X". If the command is valid, navigate to
     * the desired activity.
     *
     * @param command: String, the command to be executed
     */
    private fun executeNavigationCommand(command: String) {
        
        if ((command == resources.getString(R.string.navigate_to_grocery_list))) {
            navToActivity(ActivityType.GROCERYLIST)
        } else if ((command == resources.getString(R.string.navigate_to_place_details))) {
            navToActivity(ActivityType.PLACEDETAILS)
        } else if ((command == resources.getString(R.string.navigate_to_settings))) {
            navToActivity(ActivityType.SETTINGS)
        } else if ((command == resources.getString(R.string.navigate_to_main_menu))) {
            navToActivity(ActivityType.MAIN)
        } else {
            viewModel.say(resources.getString(R.string.invalid_command))
        }
        
    }
    
    private suspend fun requestResponse(question: String) {
        viewModel.sayAndAwait(question)
        viewModel.setSpeechRecognitionListener(STTInputType.ANSWER)
        viewModel.handleSpeechBegin()
    }

    /**
     * Handles navigation to next activity. Called either by button click or by execution of the
     * voice command. If the called for activity is the current one, read out the activity name.
     *
     * @param activity: ActivityType, Enum specifying the activity
     */
    private fun navToActivity(activity: ActivityType) {

        Log.println(Log.DEBUG, "navToActivity", activity.toString())

        when (activity) {

            ActivityType.SETTINGS -> {
                viewModel.say(resources.getString(R.string.settings))
            }

            ActivityType.MAIN -> {
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.GROCERYLIST -> {
                val intent = Intent(this, GroceryListActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.PLACEDETAILS -> {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                this.startActivity(intent)
            }

        }
    }
}
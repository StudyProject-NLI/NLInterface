package com.nlinterface.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.TextToSpeechUtility
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.MainViewModel
import com.nlinterface.viewmodels.SettingsViewModel
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private lateinit var keepScreenOnOptions: MutableList<String>
    private var keepScreenOnButton: Button? = null

    private lateinit var themeOptions: MutableList<String>
    private var themeButton: Button? = null

    private lateinit var voiceActivationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        //process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

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

    private fun configureUI() {

        voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }

        themeButton = findViewById(R.id.settings_theme)
        themeButton!!.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]

        keepScreenOnButton = findViewById(R.id.settings_keep_screen_on)
        keepScreenOnButton!!.text = keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

    }

    override fun onStart() {
        super.onStart()

        keepScreenOnButton!!.setOnClickListener {
            onKeepScreenOnButtonClick()
        }

        themeButton!!.setOnClickListener {
            onThemeButtonClick()
        }
    }

    private fun onThemeButtonClick() {

        if (GlobalParameters.instance!!.themeChoice.ordinal == GlobalParameters.ThemeChoice.values().size - 1) {
            GlobalParameters.instance!!.themeChoice = GlobalParameters.ThemeChoice.values()[0]
        } else {
            GlobalParameters.instance!!.themeChoice = GlobalParameters.ThemeChoice.values()[GlobalParameters.instance!!.themeChoice.ordinal + 1]
        }
        themeButton!!.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]
        //GlobalParameters.instance!!.updateTheme()

        viewModel.say(resources.getString(R.string.new_theme_setting, themeButton!!.text))
    }

    private fun onKeepScreenOnButtonClick() {

        if (GlobalParameters.instance!!.keepScreenOnSwitch.ordinal == GlobalParameters.KeepScreenOn.values().size - 1) {
            GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[0]
        } else {
            GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal + 1]
        }
        keepScreenOnButton!!.text = keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        viewModel.say(resources.getString(R.string.new_screen_setting, keepScreenOnButton!!.text))
    }

    // save data to SharedPreferences
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

    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    private fun configureTTS() {

        viewModel.initTTS()

        val ttsInitializedObserver = Observer<Boolean> { _ ->
            viewModel.say(resources.getString(R.string.settings))
        }

        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    private fun configureSTT() {

        viewModel.initSTT()

        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_green)
            } else {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_white)
            }
        }

        viewModel.isListening.observe(this, sttIsListeningObserver)

        val commandObserver = Observer<ArrayList<String>> {command ->
            executeCommand(command)
        }

        viewModel.command.observe(this, commandObserver)

    }

    private fun executeCommand(command: ArrayList<String>?) {

        if ((command != null) && (command.size == 3)) {
            if (command[0] == "GOTO") {
                navToActivity(command[1])
            } else {
                viewModel.say(resources.getString(R.string.choose_activity_to_navigate_to))
            }
        }

    }

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
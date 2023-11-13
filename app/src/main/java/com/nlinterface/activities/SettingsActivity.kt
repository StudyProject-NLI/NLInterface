package com.nlinterface.activities

import android.content.Context
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
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.TextToSpeechUtility
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.MainViewModel
import com.nlinterface.viewmodels.SettingsViewModel
import java.util.Locale

class SettingsActivity : AppCompatActivity(), OnInitListener {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private lateinit var keepScreenOnOptions: MutableList<String>
    private var keepScreenOnButton: Button? = null

    private lateinit var themeOptions: MutableList<String>
    private var themeButton: Button? = null

    private lateinit var tts: TextToSpeechUtility

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

        initTTS()

        configureUI()
    }

    private fun configureUI() {

        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
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

        say(resources.getString(R.string.new_theme_setting, themeButton!!.text))
    }

    private fun onKeepScreenOnButtonClick() {

        if (GlobalParameters.instance!!.keepScreenOnSwitch.ordinal == GlobalParameters.KeepScreenOn.values().size - 1) {
            GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[0]
        } else {
            GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal + 1]
        }
        keepScreenOnButton!!.text = keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        say(resources.getString(R.string.new_screen_setting, keepScreenOnButton!!.text))
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

    private fun initTTS() {

        val ttsInitializedObserver = Observer<Boolean> { _ ->
            say(resources.getString(R.string.settings))
        }
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

        tts = TextToSpeechUtility(this, this)

    }

    private fun say(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (viewModel.ttsInitialized.value == true) {
            tts.say(text, queueMode)
        }
    }

    private fun onVoiceActivationButtonClick() {

        say(resources.getString(R.string.list_all_settings) +
                resources.getString(R.string.read_screen_setting) +
                resources.getString(R.string.read_theme_setting),
                TextToSpeech.QUEUE_ADD)

    }

    private fun readSettings(all: Boolean = false, screen: Boolean = false, theme: Boolean = false) {

    var text = ""

        if (all || screen) {
            text = text.plus("${keepScreenOnButton?.text}")
        }
        if (all || theme) {
            text = text.plus("${themeButton?.text}")
        }
    say(text, TextToSpeech.QUEUE_ADD)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.setLocale(Locale.getDefault())
            viewModel.ttsInitialized.value = true
        } else {
            Log.println(Log.ERROR, "tts onInit", "Couldn't initialize TTS Engine")
        }

    }

}
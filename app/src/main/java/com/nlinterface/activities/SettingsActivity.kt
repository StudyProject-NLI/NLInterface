package com.nlinterface.activities

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.setViewRelativeSize

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private lateinit var keepScreenOnOptions: MutableList<String>
    private var keepScreenOnButton: Button? = null

    private lateinit var themeOptions: MutableList<String>
    private var themeButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            if (GlobalParameters.instance!!.keepScreenOnSwitch.ordinal == GlobalParameters.KeepScreenOn.values().size - 1) {
                GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[0]
            } else {
                GlobalParameters.instance!!.keepScreenOnSwitch = GlobalParameters.KeepScreenOn.values()[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal + 1]
            }
            keepScreenOnButton!!.text = keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]
        }

        themeButton!!.setOnClickListener {
            if (GlobalParameters.instance!!.themeChoice.ordinal == GlobalParameters.ThemeChoice.values().size - 1) {
                GlobalParameters.instance!!.themeChoice = GlobalParameters.ThemeChoice.values()[0]
            } else {
                GlobalParameters.instance!!.themeChoice = GlobalParameters.ThemeChoice.values()[GlobalParameters.instance!!.themeChoice.ordinal + 1]
            }
            themeButton!!.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]
            //GlobalParameters.instance!!.updateTheme()
        }
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
        // TODO
    }

}
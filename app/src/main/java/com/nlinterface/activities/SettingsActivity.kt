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

    private var header: TextView? = null

    //private lateinit var impairmentOptions: MutableList<String>
    //private var impairmentButton: Button? = null

    //private lateinit var colorOptions: MutableList<String>
    //private var colorButton: Button? = null

    //private lateinit var layoutSwitchOptions: MutableList<String>
    //private var layoutSwitchButton: Button? = null

    //private lateinit var voiceCommandOptions: MutableList<String>
    //private var voiceCommandSwitchButton: Button? = null

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

        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        /*impairmentOptions = mutableListOf()
        resources.getStringArray(R.array.impairment_options).forEach { option ->
            impairmentOptions.add(option)
        }
        impairmentButton = findViewById(R.id.settings_impairment)
        impairmentButton!!.text = impairmentOptions[GlobalParameters.instance!!.visualImpairment.ordinal]

        colorOptions = mutableListOf()
        resources.getStringArray(R.array.color_options).forEach { option ->
            colorOptions.add(option)
        }
        colorButton = findViewById(R.id.settings_colors)
        colorButton!!.text = colorOptions[GlobalParameters.instance!!.colorChoice.ordinal]*/

        /*layoutSwitchOptions = mutableListOf()
        resources.getStringArray(R.array.layout_switch_options).forEach { option ->
            layoutSwitchOptions.add(option)
        }
        layoutSwitchButton = findViewById(R.id.settings_layout)
        layoutSwitchButton!!.text = layoutSwitchOptions[GlobalParameters.instance!!.layoutSwitch.ordinal]*/

        /*voiceCommandOptions = mutableListOf()
        resources.getStringArray(R.array.voice_command_options).forEach { option ->
            voiceCommandOptions.add(option)
        }
        voiceCommandSwitchButton = findViewById(R.id.settings_voice_command)
        voiceCommandSwitchButton!!.text = voiceCommandOptions[GlobalParameters.instance!!.voiceCommandTrigger.ordinal]*/

        keepScreenOnOptions = mutableListOf()
        resources.getStringArray(R.array.keep_screen_on_options).forEach { option ->
            keepScreenOnOptions.add(option)
        }
        keepScreenOnButton = findViewById(R.id.settings_keep_screen_on)
        keepScreenOnButton!!.text = keepScreenOnOptions[GlobalParameters.instance!!.keepScreenOnSwitch.ordinal]

        themeOptions = mutableListOf()
        resources.getStringArray(R.array.theme_options).forEach { option ->
            themeOptions.add(option)
        }
        themeButton = findViewById(R.id.settings_theme)
        themeButton!!.text = themeOptions[GlobalParameters.instance!!.themeChoice.ordinal]
    }

    override fun onStart() {
        super.onStart()

        // on button click:
        // check if last option was reached before -> YES: set to first option, NO: set to next option
        // update the button text to the new selection
        // TODO: implement actual change
        /*impairmentButton!!.setOnClickListener {
            if (GlobalParameters.instance!!.visualImpairment.ordinal == GlobalParameters.VisualImpairment.values().size - 1) {
                GlobalParameters.instance!!.visualImpairment = GlobalParameters.VisualImpairment.values()[0]
            } else {
                GlobalParameters.instance!!.visualImpairment = GlobalParameters.VisualImpairment.values()[GlobalParameters.instance!!.visualImpairment.ordinal + 1]
            }
            impairmentButton!!.text = impairmentOptions[GlobalParameters.instance!!.visualImpairment.ordinal]
        }*/


        // TODO: implement actual change
        /*colorButton!!.setOnClickListener {
            if (GlobalParameters.instance!!.colorChoice.ordinal == GlobalParameters.ColorChoice.values().size - 1) {
                GlobalParameters.instance!!.colorChoice = GlobalParameters.ColorChoice.values()[0]
            } else {
                GlobalParameters.instance!!.colorChoice = GlobalParameters.ColorChoice.values()[GlobalParameters.instance!!.colorChoice.ordinal + 1]
            }
            colorButton!!.text = colorOptions[GlobalParameters.instance!!.colorChoice.ordinal]
        }*/

        // TODO: implement actual change
        /*layoutSwitchButton!!.setOnClickListener {
            if (GlobalParameters.instance!!.layoutSwitch.ordinal == GlobalParameters.LayoutSwitch.values().size - 1) {
                GlobalParameters.instance!!.layoutSwitch = GlobalParameters.LayoutSwitch.values()[0]
            } else {
                GlobalParameters.instance!!.layoutSwitch = GlobalParameters.LayoutSwitch.values()[GlobalParameters.instance!!.layoutSwitch.ordinal + 1]
            }
            layoutSwitchButton!!.text = layoutSwitchOptions[GlobalParameters.instance!!.layoutSwitch.ordinal]
        }*/

        // TODO: implement actual change
        /*voiceCommandSwitchButton!!.setOnClickListener {
            if (GlobalParameters.instance!!.voiceCommandTrigger.ordinal == GlobalParameters.VoiceCommandTrigger.values().size - 1) {
                GlobalParameters.instance!!.voiceCommandTrigger = GlobalParameters.VoiceCommandTrigger.values()[0]
            } else {
                GlobalParameters.instance!!.voiceCommandTrigger = GlobalParameters.VoiceCommandTrigger.values()[GlobalParameters.instance!!.voiceCommandTrigger.ordinal + 1]
            }
            voiceCommandSwitchButton!!.text = voiceCommandOptions[GlobalParameters.instance!!.voiceCommandTrigger.ordinal]
        }*/

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
            /*putString(
                getString(R.string.settings_impairment_key),
                GlobalParameters.instance!!.visualImpairment.toString()
            )*/
            /*putString(
                getString(R.string.settings_color_key),
                GlobalParameters.instance!!.colorChoice.toString()
            )*/
            /*putString(
                getString(R.string.settings_layout_key),
                GlobalParameters.instance!!.layoutSwitch.toString()
            )*/
            /*putString(
                getString(R.string.settings_voice_command_key),
                GlobalParameters.instance!!.voiceCommandTrigger.toString()
            )*/
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

}
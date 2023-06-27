package com.nlinterface.activities

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings.Global
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.component1
import androidx.core.view.WindowCompat
import androidx.core.view.get
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.GlobalParameters

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var header: TextView? = null

    private var languageSettingText: TextView? = null
    private var languageDropDown: Spinner? = null
    private var languageDropDownAdapter: ArrayAdapter<CharSequence>? = null

    private var impairmentSettingText: TextView? = null
    private var impairmentDropDown: Spinner? = null
    private var impairmentDropDownAdapter: ArrayAdapter<CharSequence>? = null

    private var colorSettingText: TextView? = null
    private var colorsDropDown: Spinner? = null
    private var colorsDropDownAdapter: ArrayAdapter<CharSequence>? = null

    private var layoutSettingText: TextView? = null
    private var layoutSwitch: SwitchCompat? = null

    private var voiceCommandSettingText: TextView? = null
    private var voiceCommandDropDown: Spinner? = null
    private var voiceCommandDropDownAdapter: ArrayAdapter<CharSequence>? = null

    private var keepScreenOnSettingText: TextView? = null
    private var keepScreenOnSwitch: SwitchCompat? = null

    private var themeSettingText: TextView? = null
    private var themeDropDown: Spinner? = null
    private var themeDropDownAdapter: ArrayAdapter<CharSequence>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        header = findViewById(R.id.header)
        languageSettingText = findViewById(R.id.settings_language)
        impairmentSettingText = findViewById(R.id.settings_impairment)
        colorSettingText = findViewById(R.id.settings_colors)
        layoutSettingText = findViewById(R.id.settings_layout)
        voiceCommandSettingText = findViewById(R.id.settings_voice_command)
        keepScreenOnSettingText = findViewById(R.id.settings_keep_screen_on)
        themeSettingText = findViewById(R.id.settings_theme)
    }

    override fun onStart() {
        super.onStart()

        // language drop down menu
        languageDropDown = findViewById(R.id.language_dropdown)
        // fill drop down with options (set in strings resource)
        languageDropDownAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )
        languageDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageDropDown!!.adapter = languageDropDownAdapter
        // set currently selected item to settings saved in GlobalParameters
        // setSelection parameter is the index of the dropdown item to be selected, language.ordinal gets the index of the saved language in the language enumerator
        languageDropDown!!.setSelection(GlobalParameters.instance!!.language.ordinal, false)
        var previousLanguageSelection = languageDropDown!!.selectedItemPosition

        // visual impairment drop down menu
        impairmentDropDown = findViewById(R.id.impairment_dropdown)
        //impairmentDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
        //impairmentOptions!![GlobalParameters.instance!!.visualImpairment.ordinal],
        //android.R.layout.simple_spinner_item)
        impairmentDropDownAdapter = ArrayAdapter.createFromResource(
            this@SettingsActivity,
            R.array.impairment_options,
            android.R.layout.simple_spinner_item
        )
        impairmentDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        impairmentDropDown!!.adapter = impairmentDropDownAdapter
        impairmentDropDown!!.setSelection(GlobalParameters.instance!!.visualImpairment.ordinal, false)
        var previousImpairmentSelection = impairmentDropDown!!.selectedItemPosition

        // color drop down menu
        colorsDropDown = findViewById(R.id.colors_dropdown)
        colorsDropDownAdapter = ArrayAdapter.createFromResource(
            this@SettingsActivity,
            R.array.color_options,
            android.R.layout.simple_spinner_item
        )
        colorsDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorsDropDown!!.adapter = colorsDropDownAdapter
        colorsDropDown!!.setSelection(GlobalParameters.instance!!.colorChoice.ordinal, false)
        var previousColorSelection = colorsDropDown!!.selectedItemPosition

        // layout change switch
        layoutSwitch = findViewById(R.id.layout_switch)
        layoutSwitch!!.isChecked = GlobalParameters.instance!!.layoutSwitch == true
        var previousLayoutSwitchState = layoutSwitch!!.isChecked

        // voice command drop down menu
        voiceCommandDropDown = findViewById(R.id.voice_command_dropdown)
        voiceCommandDropDownAdapter = ArrayAdapter.createFromResource(
            this@SettingsActivity,
            R.array.voice_command_options,
            android.R.layout.simple_spinner_item
        )
        voiceCommandDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voiceCommandDropDown!!.adapter = voiceCommandDropDownAdapter
        voiceCommandDropDown!!.setSelection(GlobalParameters.instance!!.voiceCommandTrigger.ordinal, false)
        var previousVoiceCommandSelection = voiceCommandDropDown!!.selectedItemPosition

        // keep screen on switch
        keepScreenOnSwitch = findViewById(R.id.keep_screen_on_switch)
        keepScreenOnSwitch!!.isChecked = GlobalParameters.instance!!.keepScreenOnSwitch == true
        var previousKeepScreenOnSwitchState = keepScreenOnSwitch!!.isChecked

        // theme drop down menu
        themeDropDown = findViewById(R.id.theme_dropdown)
        themeDropDownAdapter = ArrayAdapter.createFromResource(
            this@SettingsActivity,
            R.array.theme_options,
            android.R.layout.simple_spinner_item
        )
        themeDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeDropDown!!.adapter = themeDropDownAdapter
        themeDropDown!!.setSelection(GlobalParameters.instance!!.themeChoice.ordinal, false)
        var previousThemeSelection = themeDropDown!!.selectedItemPosition


        // on language changed
        languageDropDown!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // save selected language globally
                GlobalParameters.instance!!.language = GlobalParameters.Language.values()[position]

                impairmentDropDown!!.setSelection(previousImpairmentSelection, false)

                colorsDropDown!!.setSelection(previousColorSelection, false)

                voiceCommandDropDown!!.setSelection(previousVoiceCommandSelection, false)

                layoutSwitch!!.isChecked = previousLayoutSwitchState

                keepScreenOnSwitch!!.isChecked = previousKeepScreenOnSwitchState

                themeDropDown!!.setSelection(previousThemeSelection, false)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // TODO: on type of impairment changed
        impairmentDropDown!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                previousImpairmentSelection = impairmentDropDown!!.selectedItemPosition
                // update globally saved parameters
                GlobalParameters.instance!!.visualImpairment =
                    GlobalParameters.VisualImpairment.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // TODO: on color changed
        colorsDropDown!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                previousColorSelection = colorsDropDown!!.selectedItemPosition
                GlobalParameters.instance!!.colorChoice =
                    GlobalParameters.ColorChoice.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // TODO: on layout change switch state changed
        layoutSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                previousLayoutSwitchState = true
                GlobalParameters.instance!!.layoutSwitch = true
                // change layout to 2 versions
            } else {
                previousLayoutSwitchState = false
                GlobalParameters.instance!!.layoutSwitch = false
                // change layout to normal version
            }
        }

        // TODO: on voice command mode changed
        voiceCommandDropDown!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    previousVoiceCommandSelection = voiceCommandDropDown!!.selectedItemPosition
                    GlobalParameters.instance!!.voiceCommandTrigger =
                        GlobalParameters.VoiceCommandTrigger.values()[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // on keep screen on switch state changed
        keepScreenOnSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                previousKeepScreenOnSwitchState = true
                GlobalParameters.instance!!.keepScreenOnSwitch = true
            } else {
                previousKeepScreenOnSwitchState = false
                GlobalParameters.instance!!.keepScreenOnSwitch = false
            }
        }

        // on theme changed
        themeDropDown!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    previousThemeSelection = themeDropDown!!.selectedItemPosition
                    GlobalParameters.instance!!.themeChoice =
                            GlobalParameters.ThemeChoice.values()[position]
                    GlobalParameters.instance!!.updateTheme()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                getString(R.string.settings_language_key),
                GlobalParameters.instance!!.language.toString()
            )
            putString(
                getString(R.string.settings_impairment_key),
                GlobalParameters.instance!!.visualImpairment.toString()
            )
            putString(
                getString(R.string.settings_color_key),
                GlobalParameters.instance!!.colorChoice.toString()
            )
            putBoolean(
                getString(R.string.settings_layout_key),
                GlobalParameters.instance!!.layoutSwitch
            )
            putString(
                getString(R.string.settings_voice_command_key),
                GlobalParameters.instance!!.voiceCommandTrigger.toString()
            )
            putBoolean(
                getString(R.string.settings_keep_screen_on_key),
                GlobalParameters.instance!!.keepScreenOnSwitch
            )
            putString(
                getString(R.string.settings_theme_key),
                GlobalParameters.instance!!.themeChoice.toString()
            )
            apply()
        }
    }

}
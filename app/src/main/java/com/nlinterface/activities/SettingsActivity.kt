package com.nlinterface.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.WindowCompat
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.GlobalParameters

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var header : TextView? = null

    private var languageSettingText : TextView? = null
    private var languageDropDown : Spinner? = null
    private var languageDropDownAdapter : ArrayAdapter<CharSequence>? = null

    private var impairmentSettingText : TextView? = null
    private var impairmentDropDown : Spinner? = null
    private var impairmentDropDownAdapter : ArrayAdapter<CharSequence>? = null

    private var colorSettingText : TextView? = null
    private var colorsDropDown : Spinner? = null
    private var colorsDropDownAdapter : ArrayAdapter<CharSequence>? = null

    private var layoutSettingText : TextView? = null
    private var layoutSwitch : SwitchCompat? = null

    private var voiceCommandSettingText : TextView? = null
    private var voiceCommandDropDown : Spinner? = null
    private var voiceCommandDropDownAdapter : ArrayAdapter<CharSequence>? = null

    // used to change the language of all texts at once
    private var textArray : Array<TextView?>? = null
    private var enStringsArray : Array<String?>? = null
    private var deStringsArray : Array<String?>? = null
    private var newStringsArray : Array<Array<String?>>? = null // combination of EN and DE strings
    private var impairmentOptions : Array<Int>? = null
    private var colorOptions : Array<Int>? = null
    private var voiceCommandOptions : Array<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get saved data from SharedPreferences or create SharedPreferences
        // and set the respective GlobalParameters to the saved data
        val sharedPref : SharedPreferences = this.getSharedPreferences(getString(R.string.settings_preferences_key), Context.MODE_PRIVATE) ?: return
        val prefLanguage = sharedPref.getString(getString(R.string.settings_language_key), GlobalParameters.Language.EN.toString())
        GlobalParameters.instance!!.language = GlobalParameters.Language.valueOf(prefLanguage!!)
        val prefImpairment = sharedPref.getString(getString(R.string.settings_impairment_key), GlobalParameters.VisualImpairment.BLIND.toString())
        GlobalParameters.instance!!.visualImpairment = GlobalParameters.VisualImpairment.valueOf(prefImpairment!!)
        val prefColor = sharedPref.getString(getString(R.string.settings_color_key), GlobalParameters.ColorChoice.DEFAULT.toString())
        GlobalParameters.instance!!.colorChoice = GlobalParameters.ColorChoice.valueOf(prefColor!!)
        val prefLayout = sharedPref.getBoolean(getString(R.string.settings_layout_key), false)
        GlobalParameters.instance!!.layoutSwitch = prefLayout
        val prefVoiceTrigger = sharedPref.getString(getString(R.string.settings_voice_command_key), GlobalParameters.VoiceCommandTrigger.BUTTON.toString())
        GlobalParameters.instance!!.voiceCommandTrigger = GlobalParameters.VoiceCommandTrigger.valueOf(prefVoiceTrigger!!)

        header = findViewById(R.id.header)
        languageSettingText = findViewById(R.id.settings_language)
        impairmentSettingText = findViewById(R.id.settings_impairment)
        colorSettingText = findViewById(R.id.settings_colors)
        layoutSettingText = findViewById(R.id.settings_layout)
        voiceCommandSettingText = findViewById(R.id.settings_voice_command)

        textArray = arrayOf(header,
            languageSettingText,
            impairmentSettingText,
            colorSettingText,
            layoutSettingText,
            voiceCommandSettingText)

        enStringsArray = arrayOf(getString(R.string.settings),
            getString(R.string.settings_language),
            getString(R.string.settings_impairment),
            getString(R.string.settings_colors),
            getString(R.string.settings_layout),
            getString(R.string.settings_voice_command))
        deStringsArray  = arrayOf(getString(R.string.settings_DE),
            getString(R.string.settings_language_DE),
            getString(R.string.settings_impairment_DE),
            getString(R.string.settings_colors_DE),
            getString(R.string.settings_layout_DE),
            getString(R.string.settings_voice_command_DE))
        newStringsArray = arrayOf(enStringsArray!!, deStringsArray!!)

        impairmentOptions = arrayOf(R.array.impairment_options, R.array.impairment_options_DE)
        colorOptions = arrayOf(R.array.color_options, R.array.color_options_DE)
        voiceCommandOptions = arrayOf(R.array.voice_command_options, R.array.voice_command_options_DE)
    }

    override fun onStart() {
        super.onStart()

        // language drop down menu
        languageDropDown = findViewById(R.id.language_dropdown)
        // fill drop down with options (set in strings resource)
        languageDropDownAdapter = ArrayAdapter.createFromResource(this,
            R.array.language_options,
            android.R.layout.simple_spinner_item)
        languageDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageDropDown!!.adapter = languageDropDownAdapter
        // set currently selected item to settings saved in GlobalParameters
        // setSelection parameter is the index of the dropdown item to be selected, language.ordinal gets the index of the saved language in the language enumerator
        languageDropDown!!.setSelection(GlobalParameters.instance!!.language.ordinal)

        // visual impairment drop down menu
        impairmentDropDown = findViewById(R.id.impairment_dropdown)
        impairmentDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            impairmentOptions!![GlobalParameters.instance!!.visualImpairment.ordinal],
            android.R.layout.simple_spinner_item)
        impairmentDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        impairmentDropDown!!.adapter = impairmentDropDownAdapter
        impairmentDropDown!!.setSelection(GlobalParameters.instance!!.visualImpairment.ordinal)
        var previousImpairmentSelection = impairmentDropDown!!.selectedItemPosition

        // color drop down menu
        colorsDropDown = findViewById(R.id.colors_dropdown)
        colorsDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            colorOptions!![GlobalParameters.instance!!.colorChoice.ordinal],
            android.R.layout.simple_spinner_item)
        colorsDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorsDropDown!!.adapter = colorsDropDownAdapter
        colorsDropDown!!.setSelection(GlobalParameters.instance!!.colorChoice.ordinal)
        var previousColorSelection = colorsDropDown!!.selectedItemPosition

        // layout change switch
        layoutSwitch = findViewById(R.id.layout_switch)
        layoutSwitch!!.isChecked = GlobalParameters.instance!!.layoutSwitch == true
        var previousLayoutSwitchState = layoutSwitch!!.isChecked

        // voice command drop down menu
        voiceCommandDropDown = findViewById(R.id.voice_command_dropdown)
        voiceCommandDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            voiceCommandOptions!![GlobalParameters.instance!!.voiceCommandTrigger.ordinal],
            android.R.layout.simple_spinner_item)
        voiceCommandDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voiceCommandDropDown!!.adapter = voiceCommandDropDownAdapter
        voiceCommandDropDown!!.setSelection(GlobalParameters.instance!!.voiceCommandTrigger.ordinal)
        var previousVoiceCommandSelection = voiceCommandDropDown!!.selectedItemPosition

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

                // change all texts to the strings of the selected language (position == 0: EN, position == 1: DE)
                GlobalParameters.instance!!.changeTextLanguage(textArray!!, newStringsArray!![position])

                // update dropdown menus
                impairmentDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    impairmentOptions!![position],
                    android.R.layout.simple_spinner_item)
                impairmentDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                impairmentDropDown!!.adapter = impairmentDropDownAdapter
                // make sure that the same item as before the language change is selected
                impairmentDropDown!!.setSelection(previousImpairmentSelection)

                colorsDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    colorOptions!![position],
                    android.R.layout.simple_spinner_item)
                colorsDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                colorsDropDown!!.adapter = colorsDropDownAdapter
                colorsDropDown!!.setSelection(previousColorSelection)

                voiceCommandDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    voiceCommandOptions!![position],
                    android.R.layout.simple_spinner_item)
                voiceCommandDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                voiceCommandDropDown!!.adapter = voiceCommandDropDownAdapter
                voiceCommandDropDown!!.setSelection(previousVoiceCommandSelection)

                layoutSwitch!!.isChecked = previousLayoutSwitchState
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
                GlobalParameters.instance!!.visualImpairment = GlobalParameters.VisualImpairment.values()[position]
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
                GlobalParameters.instance!!.colorChoice = GlobalParameters.ColorChoice.values()[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // TODO: on switch state changed
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
        voiceCommandDropDown!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                previousVoiceCommandSelection = voiceCommandDropDown!!.selectedItemPosition
                GlobalParameters.instance!!.voiceCommandTrigger = GlobalParameters.VoiceCommandTrigger.values()[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    // save data to SharedPreferences
    override fun onPause() {
        super.onPause()
        val sharedPref = this.getSharedPreferences(getString(R.string.settings_preferences_key), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.settings_language_key),
                GlobalParameters.instance!!.language.toString())
            putString(getString(R.string.settings_impairment_key),
                GlobalParameters.instance!!.visualImpairment.toString())
            putString(getString(R.string.settings_color_key),
                GlobalParameters.instance!!.colorChoice.toString())
            putBoolean(getString(R.string.settings_layout_key),
                GlobalParameters.instance!!.layoutSwitch)
            putString(getString(R.string.settings_voice_command_key),
                GlobalParameters.instance!!.voiceCommandTrigger.toString())
            apply()
        }
    }


    }
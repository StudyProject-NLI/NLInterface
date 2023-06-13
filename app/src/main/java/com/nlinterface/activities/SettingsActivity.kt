package com.nlinterface.activities

import android.os.Bundle
import android.provider.Settings.Global
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.nlinterface.R
import com.nlinterface.databinding.ActivitySettingsBinding
import com.nlinterface.utility.GlobalParameters
import org.w3c.dom.Text

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
    private var layoutToggle : ToggleButton? = null

    private var voiceCommandSettingText : TextView? = null
    private var voiceCommandDropDown : Spinner? = null
    private var voiceCommandDropDownAdapter : ArrayAdapter<CharSequence>? = null

    private var textArray : Array<TextView?>? = null
    private var enStringsArray : Array<String?>? = null
    private var deStringsArray : Array<String?>? = null
    private var newStringsArray : Array<Array<String?>>? = null
    private var impairmentOptions : Array<Int>? = null
    private var colorOptions : Array<Int>? = null
    private  var voiceCommandOptions : Array<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        header = findViewById(R.id.header)
        languageSettingText = findViewById(R.id.settings_language)
        impairmentSettingText = findViewById(R.id.settings_impairment)
        colorSettingText = findViewById(R.id.settings_colors)
        layoutSettingText = findViewById(R.id.settings_layout)
        voiceCommandSettingText = findViewById(R.id.settings_voice_command)

        textArray  = arrayOf(header,
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
        languageDropDownAdapter = ArrayAdapter.createFromResource(this,
            R.array.language_options,
            android.R.layout.simple_spinner_item)
        languageDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageDropDown!!.adapter = languageDropDownAdapter
        languageDropDown!!.setSelection(GlobalParameters.instance!!.language.ordinal)
        //languageDropDown!!.setSelection(0, false)

        // visual impairment drop down menu
        impairmentDropDown = findViewById(R.id.impairment_dropdown)
        impairmentDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            impairmentOptions!![GlobalParameters.instance!!.visualImpairment.ordinal],
            android.R.layout.simple_spinner_item)
        impairmentDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        impairmentDropDown!!.adapter = impairmentDropDownAdapter
        impairmentDropDown!!.setSelection(GlobalParameters.instance!!.visualImpairment.ordinal)
        //impairmentDropDown!!.setSelection(0, false)
        var previousImpairmentSelection = impairmentDropDown!!.selectedItemPosition

        // color drop down menu
        colorsDropDown = findViewById(R.id.colors_dropdown)
        colorsDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            colorOptions!![GlobalParameters.instance!!.colorChoice.ordinal],
            android.R.layout.simple_spinner_item)
        colorsDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorsDropDown!!.adapter = colorsDropDownAdapter
        colorsDropDown!!.setSelection(GlobalParameters.instance!!.colorChoice.ordinal)
        //colorsDropDown!!.setSelection(0, false)
        var previousColorSelection = colorsDropDown!!.selectedItemPosition

        // layout change toggle
        layoutToggle = findViewById(R.id.layout_toggle)
        layoutToggle!!.isChecked = GlobalParameters.instance!!.layoutSwitch == true
        var previousLayoutToggleState = layoutToggle!!.isChecked

        // voice command drop down menu
        voiceCommandDropDown = findViewById(R.id.voice_command_dropdown)
        voiceCommandDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
            voiceCommandOptions!![GlobalParameters.instance!!.voiceCommandTrigger.ordinal],
            android.R.layout.simple_spinner_item)
        voiceCommandDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voiceCommandDropDown!!.adapter = voiceCommandDropDownAdapter
        voiceCommandDropDown!!.setSelection(GlobalParameters.instance!!.voiceCommandTrigger.ordinal)
        var previousVoiceCommandSelection = voiceCommandDropDown!!.selectedItemPosition

        //val languages = resources.getStringArray(R.array.language_options)

        // on language changed
        languageDropDown!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                GlobalParameters.instance!!.language = GlobalParameters.Language.values()[position]
                GlobalParameters.instance!!.changeTextLanguage(textArray!!, newStringsArray!![position])
                impairmentDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    impairmentOptions!![position],
                    android.R.layout.simple_spinner_item)

                colorsDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    colorOptions!![position],
                    android.R.layout.simple_spinner_item)

                voiceCommandDropDownAdapter = ArrayAdapter.createFromResource(this@SettingsActivity,
                    voiceCommandOptions!![position],
                    android.R.layout.simple_spinner_item)

                impairmentDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                impairmentDropDown!!.adapter = impairmentDropDownAdapter
                impairmentDropDown!!.setSelection(previousImpairmentSelection)

                colorsDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                colorsDropDown!!.adapter = colorsDropDownAdapter
                colorsDropDown!!.setSelection(previousColorSelection)

                layoutToggle!!.isChecked = previousLayoutToggleState

                voiceCommandDropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                voiceCommandDropDown!!.adapter = voiceCommandDropDownAdapter
                voiceCommandDropDown!!.setSelection(previousVoiceCommandSelection)
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

        // TODO: on toggle state changed
        layoutToggle!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                previousLayoutToggleState = true
                GlobalParameters.instance!!.layoutSwitch = true
                // change layout to 2 versions
            } else {
                previousLayoutToggleState = false
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




    }
package com.nlinterface.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nlinterface.R
import com.nlinterface.databinding.ActivityBarcodeSettingsBinding
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.navToActivity
import com.nlinterface.viewmodels.BarcodeSettingsViewModel


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
 * - 'Set Screen Settings' --> Always On or Dim? --> X
 * - 'Set Theme Settings' --> Default, Light or Dark? --> X
 *
 * TODO: Add TTS Speed Settings
 */

class BarcodeSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeSettingsBinding
    private lateinit var viewModel: BarcodeSettingsViewModel

    private lateinit var nameAndVolumeOptions: MutableList<String>
    private lateinit var nameAndVolumeButton: Button

    private lateinit var labelsOptions: MutableList<String>
    private lateinit var labelsButton: Button

    private lateinit var countryOfOriginOptions: MutableList<String>
    private lateinit var countryOfOriginButton: Button

    private lateinit var ingredientsAndAllergiesOptions: MutableList<String>
    private lateinit var ingredientsAndAllergiesButton: Button

    private lateinit var shortNutritionalValuesOptions: MutableList<String>
    private lateinit var shortNutritionalValuesButton: Button

    private lateinit var lastCommand: String

    private val globalParameters = GlobalParameters.instance!!
    private lateinit var navController: NavController

    /**
     * The onCreate Function initializes the view by binding the Activity and the Layout,
     * retrieving the ViewModel, loading the options for each preference type, configuring the UI
     * and configuring the TTS/STT systems.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarcodeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BarcodeSettingsViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.barcode_settings_nav_host_fragment) as NavHostFragment
        if (navHostFragment == null) {
            Log.i("SettingsActivity", "NavHostFragment is null")
        } else {
            navController = navHostFragment.navController
        }

        nameAndVolumeOptions = mutableListOf()
        resources.getStringArray(R.array.settings_name_and_volume).forEach { option ->
            nameAndVolumeOptions.add(option)
        }

        labelsOptions = mutableListOf()
        resources.getStringArray(R.array.settings_labels).forEach { option ->
            labelsOptions.add(option)
        }

        countryOfOriginOptions = mutableListOf()
        resources.getStringArray(R.array.settings_country_of_origin).forEach { option ->
            countryOfOriginOptions.add(option)
        }

        ingredientsAndAllergiesOptions = mutableListOf()
        resources.getStringArray(R.array.settings_ingredients_and_allergies).forEach { option ->
            ingredientsAndAllergiesOptions.add(option)
        }

        shortNutritionalValuesOptions = mutableListOf()
        resources.getStringArray(R.array.settings_short_nutritional_values).forEach { option ->
            shortNutritionalValuesOptions.add(option)
        }

        configureTTS()
        configureSTT()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()

        val sharedBarcodePref = this.getSharedPreferences(
            getString(R.string.barcode_settings_preferences_key),
            Context.MODE_PRIVATE
        ) ?: return

        with(sharedBarcodePref.edit()) {

            putString(
                getString(R.string.settings_name_and_volume_key),
                globalParameters.navState.toString()
            )
            putString(
                getString(R.string.settings_labels_key),
                globalParameters.labelsState.toString()
            )
            putString(
                getString(R.string.settings_country_of_origin_key),
                globalParameters.cooState.toString()
            )
            putString(
                getString(R.string.settings_ingredients_and_allergies_key),
                globalParameters.iaaState.toString()
            )
            putString(
                getString(R.string.settings_short_nutritional_values_key),
                globalParameters.snvState.toString()
            )

            apply()
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
            viewModel.say(resources.getString(R.string.barcode_scanner_settings))
        }

        // observe LiveDate change, to be notified if TTS initialization is completed
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    /**
     * Called by the onCreate function and calls upon the ViewModel to initialize the STT system.
     * The voiceActivationButton is configured to change it microphone color to green, if the STT
     * system is active and to change back to white, if it is not. Also retrieves the text output
     * of the voice input to the STT system, aka the 'command', as well as a 'response', if a
     * question was asked by the system.
     */
    private fun configureSTT() {

        viewModel.initSTT()

        // if listening: microphone color green, else microphone color white
        val sttIsListeningObserver = Observer<Boolean> {

        }

        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)

        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<String> { command ->
            lastCommand = command
            handleSTTCommand(command)
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)

    }

    /**
     * Called once the STT system returns a command. It is then processed and, if valid,
     * executed by further methods.
     *
     * @param command: String containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun handleSTTCommand(command: String) {

        // any attempted navigation commands are handled are passed on
        if (command.contains(resources.getString(R.string.go_to))) {
            executeNavigationCommand(command)
        }

        else if (command == resources.getString(R.string.product_name_on)){
            globalParameters.navState = GlobalParameters.NavState.values()[0]
            changeAndReadButtonName()
        } else if (command == resources.getString(R.string.product_name_off)){
            globalParameters.navState = GlobalParameters.NavState.values()[1]
            changeAndReadButtonName()
        }
        else if (command == resources.getString(R.string.labels_on)){
            globalParameters.labelsState = GlobalParameters.LabelsState.values()[0]
            changeAndReadButtonLabels()
        } else if (command == resources.getString(R.string.labels_off)){
            globalParameters.labelsState = GlobalParameters.LabelsState.values()[1]
            changeAndReadButtonLabels()
        }
        else if (command == resources.getString(R.string.country_of_origin_on)){
            globalParameters.cooState = GlobalParameters.CooState.values()[0]
            changeAndReadButtonCountry()
        } else if (command == resources.getString(R.string.country_of_origin_off)){
            globalParameters.cooState = GlobalParameters.CooState.values()[1]
            changeAndReadButtonCountry()
        }
        else if (command == resources.getString(R.string.ingredients_and_allergies_on)){
            globalParameters.iaaState = GlobalParameters.IaaState.values()[0]
            changeAndReadButtonIngredients()
        } else if (command == resources.getString(R.string.ingredients_and_allergies_off)){
            globalParameters.iaaState = GlobalParameters.IaaState.values()[1]
            changeAndReadButtonIngredients()
        }
        else if (command == resources.getString(R.string.nutritional_values_on)){
            globalParameters.snvState = GlobalParameters.SnvState.values()[0]
            changeAndReadButtonNutritionalValues()
        } else if (command == resources.getString(R.string.nutritional_values_off)){
            globalParameters.snvState = GlobalParameters.SnvState.values()[1]
            changeAndReadButtonNutritionalValues()
        }

        else if(command == resources.getString(R.string.stop_speech)) {

            val intent = Intent("BarcodeInfo_Stop").apply {
                putExtra("stop_speech", true)
            }
            sendBroadcast(intent)
        }

        else if (command == resources.getString(R.string.tell_me_my_options)) {

            viewModel.say(
                "${resources.getString(R.string.your_options_are)} " +
                        "${resources.getString(R.string.change_theme)}, " +
                        "${resources.getString(R.string.change_screen_settings)}, " +
                        "${resources.getString(R.string.navigate_to_grocery_list)}, " +
                        "${resources.getString(R.string.navigate_to_place_details)} ${
                            resources.getString(
                                R.string.and
                            )
                        } " +
                        "${resources.getString(R.string.navigate_to_settings)}." +
                        "${resources.getString(R.string.navigate_to_barcode_scanner_settings)}."+
                        "${resources.getString(R.string.stop_speech)}."
            )

        } else {
            viewModel.say(resources.getString(R.string.invalid_command))
        }

    }

    /**
     * Handles Navigation commands of the format "go to X". If the command is valid, navigate to
     * the desired activity.
     *
     * @param command: String, the command to be executed
     */
    private fun executeNavigationCommand(command: String) {

        when (command) {
            resources.getString(R.string.navigate_to_grocery_list) ->
                navToActivity(this, ActivityType.GROCERYLIST)

            resources.getString(R.string.navigate_to_place_details) ->
                navToActivity(this, ActivityType.PLACEDETAILS)

            resources.getString(R.string.navigate_to_settings) ->
                navToActivity(this, ActivityType.SETTINGS)

            resources.getString(R.string.navigate_to_main_menu) ->
                navToActivity(this, ActivityType.MAIN)

            resources.getString(R.string.navigate_to_barcode_scanner_settings) ->
                navToActivity(this, ActivityType.BARCODESETTINGS)


            else -> viewModel.say(resources.getString(R.string.invalid_command))
        }
    }

    private fun changeAndReadButtonName(){
        nameAndVolumeButton.text =
            nameAndVolumeOptions[globalParameters.navState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, nameAndVolumeButton.text))
    }

    private fun changeAndReadButtonLabels(){
        labelsButton.text = labelsOptions[globalParameters.labelsState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, labelsButton.text))
    }

    private fun changeAndReadButtonCountry(){
        countryOfOriginButton.text = countryOfOriginOptions[globalParameters.cooState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, countryOfOriginButton.text))
    }

    private fun changeAndReadButtonIngredients(){
        ingredientsAndAllergiesButton.text = ingredientsAndAllergiesOptions[globalParameters.iaaState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, ingredientsAndAllergiesButton.text))
    }

    private fun changeAndReadButtonNutritionalValues(){
        shortNutritionalValuesButton.text = shortNutritionalValuesOptions[globalParameters.snvState.ordinal]

        viewModel.say(resources.getString(R.string.new_barcode_setting, shortNutritionalValuesButton.text))
    }
}
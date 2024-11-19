package com.nlinterface.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nlinterface.R
import com.nlinterface.adapters.BarcodeSettingsFragmentAdapter
import com.nlinterface.databinding.ActivityBarcodeSettingsBinding
import com.nlinterface.fragments.BarcodeScannerScreen
import com.nlinterface.fragments.BarcodeSettingsScreen1
import com.nlinterface.fragments.BarcodeSettingsScreen2
import com.nlinterface.fragments.BarcodeSettingsScreen3
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.OnSwipeTouchInterceptor
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.navToActivity
import com.nlinterface.viewmodels.BarcodeSettingsViewModel


/**
 * The BarcodeSettingsActivity handles user interaction in Barcode Settings Menu.
 *
 * The Barcode Settings Menu comprises of several screens of which each displays two
 * possible settings. Those settings set what information about the scanned product are retrieved
 * and given to the user.
 * Scrolling left an right navigates trough the different screens. A swipe up and down
 * activates/deactivates the specific information retrieval.
 *
 * 1- Name and Volume on/off
 * 2- Labels on/off
 * 3- Country of Origin on/off
 * 4- Ingredients and Allergies on/off
 * 5- Compromised nutritional values on/off
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

    private lateinit var viewPager: ViewPager2
    lateinit var fragmentAdapter: BarcodeSettingsFragmentAdapter

    /**
     * The onCreate Function initializes the view by binding the Activity and the Layout,
     * retrieving the ViewModel, loading the options for each preference type, configuring the
     * viewpager and configuring the TTS/STT systems.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarcodeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BarcodeSettingsViewModel::class.java]

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

        viewPagerSetUp()
        configureTTS()
        configureSTT()
    }

    /**
     * On pause the current state of the settings is saved, so it can be retrieved when the
     * barcode settings activity is started again.
     *
     */
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
            globalParameters.navState = GlobalParameters.NavState.entries.toTypedArray()[0]
            changeAndReadButtonName()
        } else if (command == resources.getString(R.string.product_name_off)){
            globalParameters.navState = GlobalParameters.NavState.entries.toTypedArray()[1]
            changeAndReadButtonName()
        }
        else if (command == resources.getString(R.string.labels_on)){
            globalParameters.labelsState = GlobalParameters.LabelsState.entries.toTypedArray()[0]
            changeAndReadButtonLabels()
        } else if (command == resources.getString(R.string.labels_off)){
            globalParameters.labelsState = GlobalParameters.LabelsState.entries.toTypedArray()[1]
            changeAndReadButtonLabels()
        }
        else if (command == resources.getString(R.string.country_of_origin_on)){
            globalParameters.cooState = GlobalParameters.CooState.entries.toTypedArray()[0]
            changeAndReadButtonCountry()
        } else if (command == resources.getString(R.string.country_of_origin_off)){
            globalParameters.cooState = GlobalParameters.CooState.entries.toTypedArray()[1]
            changeAndReadButtonCountry()
        }
        else if (command == resources.getString(R.string.ingredients_and_allergies_on)){
            globalParameters.iaaState = GlobalParameters.IaaState.entries.toTypedArray()[0]
            changeAndReadButtonIngredients()
        } else if (command == resources.getString(R.string.ingredients_and_allergies_off)){
            globalParameters.iaaState = GlobalParameters.IaaState.entries.toTypedArray()[1]
            changeAndReadButtonIngredients()
        }
        else if (command == resources.getString(R.string.nutritional_values_on)){
            globalParameters.snvState = GlobalParameters.SnvState.entries.toTypedArray()[0]
            changeAndReadButtonNutritionalValues()
        } else if (command == resources.getString(R.string.nutritional_values_off)){
            globalParameters.snvState = GlobalParameters.SnvState.entries.toTypedArray()[1]
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

    /**
     * The following five functions encode the visual button change on setting change.
     */

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

    /**
     * Function that sets and configures the viewPager2. ViewPager2 is a tool that can take multiple
     * fragments in a list and allows navigation between those fragments by swiping left and right.
     * To achieve this the groceryListFragmentAdapter is set as the viewpagers adapter.
     */

    private fun viewPagerSetUp(){
        viewPager = findViewById(R.id.view_pager)
        fragmentAdapter = BarcodeSettingsFragmentAdapter(this)
        viewPager.adapter = fragmentAdapter

        /**
         * The swipe interceptor makes sure that vertical swipes are recognized more reliably.
         * In a default state a swipe to the top or bottom with the slightest movement left
         * or right will be recognized as a horizontal swipe. With the interceptor the app checks
         * first for a vertical swipe, which makes it more reliable and better to navigate.
         *
         * Overriding the onSwipeUp and onSwipeDown functions in the way it is done, is necessary
         * to assure the correct fragments onSwipe functions are referenced, since the activity is
         * shared among all the activities Fragment.
         *
         */

        val swipeInterceptor = OnSwipeTouchInterceptor(object : SwipeAction {
            override fun onSwipeLeft(){}
            override fun onSwipeRight(){}
            override fun onSwipeUp(){
                val currentPosition = viewPager.currentItem
                val currentFragment = fragmentAdapter.getCurrentFragment(currentPosition)
                when (currentPosition) {
                    0 -> {
                        (currentFragment as BarcodeScannerScreen).onSwipeUp()
                    }
                    1 -> {
                        (currentFragment as BarcodeSettingsScreen1).onSwipeUp()
                    }
                    2 -> {
                        (currentFragment as BarcodeSettingsScreen2).onSwipeUp()
                    }
                    3 -> {
                        (currentFragment as BarcodeSettingsScreen3).onSwipeUp()
                    }
                }
            }
            override fun onSwipeDown(){
                val currentPosition = viewPager.currentItem
                val currentFragment = fragmentAdapter.getCurrentFragment(currentPosition)
                when (currentPosition) {
                    0 -> {
                        (currentFragment as BarcodeScannerScreen).onSwipeUp()
                    }
                    1 -> {
                        (currentFragment as BarcodeSettingsScreen1).onSwipeUp()
                    }
                    2 -> {
                        (currentFragment as BarcodeSettingsScreen2).onSwipeUp()
                    }
                    3 -> {
                        (currentFragment as BarcodeSettingsScreen3).onSwipeUp()
                    }
                }
            }
            override fun onLongPress(){}
            override fun onDoubleTap() {}
        })

        viewPager.getChildAt(0).let { recyclerView ->
            if (recyclerView is RecyclerView) {
                recyclerView.addOnItemTouchListener(swipeInterceptor)
            }
        }
    }
}
package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.*
import com.nlinterface.viewmodels.ConstantScanning
import com.nlinterface.viewmodels.MainViewModel

/**
 * The MainActivity handles user interaction in the Main Screen / Main Menu.
 *
 * The Main Menu comprises of the Voice Activation Button and a button for each Menu Item (one for
 * each Activity / Feature. The focal task for the Main Menu is to handle navigation to the other
 * features, either through touch interaction or voice commands.
 *
 * Possible Voice Commands:
 * - 'Navigate to Grocery List'
 * - 'Navigate to Place Details'
 * - 'Navigate to Settings'
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var voiceActivationButton: ImageButton
    
    /**
     * Companion Object / Singleton implementation required to handle audio permissions
     */
    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }
    
    /**
     * The onCreate Function initializes the view by binding the Activity and the Layout,
     * retrieving the ViewModel, loading the preferences, verifying the audio permissions and
     * initializing UI elements, the text to speech system and the speech to text system.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        GlobalParameters.instance!!.loadPreferences(this)
        
        verifyAudioPermissions()
        configureUI()
        configureTTS()
        configureSTT()

        verifyCameraPermissions()
        if (checkCallingOrSelfPermission(
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
                val serviceIntent = Intent(this, ConstantScanning()::class.java)
                startService(serviceIntent)
            }
    }
    
    /**
     * Called when the activity is started. It reads out the name of the activity and processes
     * the theme and keep screen on settings.
     */
    override fun onStart() {
        super.onStart()
        
        // reads activity name out loud, so that the user is aware which screen they are on
        viewModel.say(resources.getString(R.string.main_menu))
        
        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOn == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        // process theme settings
        GlobalParameters.instance!!.updateTheme()
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
            viewModel.say(resources.getString(R.string.main_menu))
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
            executeCommand(command)
        }
        
        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)
        
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
    
        Log.println(Log.DEBUG, "STT Results", command)
        
        if (command.contains(resources.getString(R.string.go_to))) {
            executeNavigationCommand(command)
        } else if ((command == resources.getString(R.string.tell_me_my_options))) {
            
            viewModel.say(
                "${resources.getString(R.string.your_options_are)} " +
                        "${resources.getString(R.string.navigate_to_grocery_list)}," +
                        "${resources.getString(R.string.navigate_to_place_details)} and" +
                        "${resources.getString(R.string.navigate_to_settings)}."
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
        
            else -> viewModel.say(resources.getString(R.string.invalid_command))
        }
        
    }
    
    /**
     * Sets up all UI elements, i.e. the groceryList/placeDetails/settingsActivity/voiceActivation
     * buttons and their respective onClickListeners
     */
    private fun configureUI() {
        
        // set up button to navigate to GroceryListActivity
        val groceryListButton: Button = findViewById<View>(R.id.grocery_list_bt) as Button
        groceryListButton.setOnClickListener { _ ->
            navToActivity(this, ActivityType.GROCERYLIST)
        }
        
        // set up button to navigate to PlaceDetailsActivity
        val placeDetailsButton: Button = findViewById<View>(R.id.place_details_bt) as Button
        placeDetailsButton.setOnClickListener { _ ->
            navToActivity(this, ActivityType.PLACEDETAILS)
        }
        
        // set up button to navigate to ClassificationActivity
        val classificationButton: Button = findViewById<View>(R.id.classification_bt) as Button
        classificationButton.setOnClickListener { _ -> 
            navToActivity(this, ActivityType.CLASSIFICATION)
        }
        
        // set up button to navigate to SettingsActivity
        val settingsActivityButton: Button = findViewById<View>(R.id.settings_bt) as Button
        settingsActivityButton.setOnClickListener { _ ->
            navToActivity(this, ActivityType.SETTINGS)
        }
        
        // set up voice Activation Button listener
        voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }
        
        // resize Voice Activation Button to 1/3 of display size
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)
        
    }
    
    /**
     * Request the user to grant record audio permissions, if not already granted.
     */
    private fun verifyAudioPermissions() {
        if (checkCallingOrSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                STT_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Called when the permissions request is answered by the user and processes the result. If
     * record audio permissions are granted, confirm that it was granted to the user. If not
     * granted, request that it be granted for the full functionality to work.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     *
     * TODO: read permissions confirmation, re-request out loud
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this, R.string.audio_permission_granted, Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this, R.string.audio_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Called when voiceActivationButton is clicked and handles the result. If clicked while the
     * STT system is listening, call to viewModel to cancel listening. Else, call viewModel to begin
     * listening.
     */
    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    /**
     * Request the user to grant camera permissions, if not already granted.
     * Will probably not be used further once other camera is included
     */
    private fun verifyCameraPermissions() {
        if (checkCallingOrSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                STT_PERMISSION_REQUEST_CODE
            )
        }
    }
}
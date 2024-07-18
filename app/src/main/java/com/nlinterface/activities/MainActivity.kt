package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.LocationGetter
import com.nlinterface.utility.navToActivity
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
    private val globalParameters = GlobalParameters.instance!!
    private lateinit var navController: NavController



    /**
     * Companion Object / Singleton implementation required to handle permissions
     */
    companion object {
        // needed to verify the audio, camera and location permission result
        const val STT_PERMISSION_REQUEST_CODE = 0
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
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

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        globalParameters.loadSettingsPreferences(this)
        globalParameters.loadBarcodePreferences(this)

        verifyAudioPermissions()
        configureTTS()
        configureSTT()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Called when the activity is started. It reads out the name of the activity and processes
     * the theme and keep screen on settings. It also starts the camera and location tracking if
     * the permissions are granted.
     */
    override fun onStart() {
        super.onStart()
        
        // reads activity name out loud, so that the user is aware which screen they are on
        viewModel.say(resources.getString(R.string.main_menu))
        
        // process keep screen on settings
        if (globalParameters.keepScreenOn == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        // process theme settings
        globalParameters.updateTheme()

        if (checkCallingOrSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION ) ==
            PackageManager.PERMISSION_GRANTED) {
            val locationService = Intent(this, LocationGetter()::class.java)
            startService(locationService)
            Log.i("Location", "Service for location tracking started.")
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
                        "${resources.getString(R.string.navigate_to_settings)}." +
                        "${resources.getString(R.string.navigate_to_barcode_scanner_settings)}."+
                        "${resources.getString(R.string.stop_speech)}."
            )
            
        } else if(command == resources.getString(R.string.stop_speech)) {

            val intent = Intent("BarcodeInfo_Stop").apply {
                putExtra("stop_speech", true)
            }
            sendBroadcast(intent)

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
        } else{
            verifyForegroundLocationPermissions()
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
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Request the user to grant access to current location permissions, if not already granted.
     *
     */
    private fun verifyForegroundLocationPermissions(){
        if (checkCallingOrSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        else { verifyCameraPermissions()}

    }

    /**
     * Called when the permissions request is answered by the user and processes the result. If
     * record audio permissions are granted, confirm that it was granted to the user. If not
     * granted, request that it be granted for the full functionality to work. If audio permissions
     * are granted further ask for location permissions.
     *
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
        if (requestCode == STT_PERMISSION_REQUEST_CODE){
            if (requestCode == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this, R.string.audio_permission_granted, Toast.LENGTH_LONG
                ).show()
                verifyForegroundLocationPermissions()
            }   else {
                Toast.makeText(
                    this, R.string.audio_permission_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        else if(requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (requestCode == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this, R.string.location_permission_granted, Toast.LENGTH_LONG
                ).show()
                verifyCameraPermissions()
            } else {
                Toast.makeText(
                    this, R.string.location_permission_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        else if(requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (requestCode == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this, R.string.camera_permission_granted, Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this, R.string.camera_permission_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
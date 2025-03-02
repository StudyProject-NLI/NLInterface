package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.LLMAppConnector
import com.nlinterface.utility.OnSwipeTouchListener
import com.nlinterface.viewmodels.VoiceOnlyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The Voice Only activity allows users to use the app solely by voice commands with the help of a
 * large language Model. The activity consist of three stages representing the three stages:
 * Listening, Processing and Speaking.
 *
 * TODO: Improve and Test
 */
class VoiceOnlyActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VoiceOnlyViewModel
    private lateinit var viewFlipper: ViewFlipper

    /**
     * The onCreate function sets up the viewFlipper that changes the screens layout based on the
     * stage the speech system is in. It also sets up the OnTouchListener and starts in listening
     * mode.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_voice_only)

        viewModel = ViewModelProvider(this)[VoiceOnlyViewModel::class.java]

        viewFlipper = findViewById(R.id.viewSwitcher)

        val rootView: View = findViewById(android.R.id.content)

        /**
         * The OnSwipeTouchListener is simplified to two possible inputs.
         * DoubleTap navigates back to the Main Menu and exists the Voice Only Mode.
         * Pressing and holding starts listening. (Only necessary when an error occurs. Per default
         * the system should be listening without any additional input.)
         */

        rootView.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onDoubleTap() {
                val intent = Intent(this@VoiceOnlyActivity, MainActivity::class.java)
                startActivity(intent)
            }
            override fun onLongPress() {
                startListening()
            }
        })

        startAsynchronously()
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
            viewModel.say(resources.getString(R.string.voice_only_mode))
        }

        // observe LiveDate change, to be notified if TTS initialization is completed
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    /**
     * Called by the onCreate function and calls upon the ViewModel to initialize the STT system.
     * It processed and observes the state of the activity to accurately represent it with the
     * corresponding screen and make sure the next processing state is started only when the prior
     * is finished.
     */
    private fun configureSTT() {

        viewModel.initSTT()

        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                showListeningStage()
            }
            else {
                viewModel.handleSTTSpeechBegin()
            }
        }

        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)

        val processObserver = Observer<Boolean> {isProcessing ->
            if(isProcessing){
                showProcessingStage()
            }
        }

        viewModel.isProcessing.observe(this, processObserver)

        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<String> {command ->
            if (command.isNotEmpty()) {
                processVoiceInput(command)
            }
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)

    }

    /**
     * The following three functions handle the UI.
     */
    private fun showListeningStage() {
        if (viewFlipper.currentView.id != R.layout.mode_listening) {
            viewFlipper.displayedChild = 0
        }
    }

    private fun showProcessingStage() {
        if (viewFlipper.currentView.id != R.layout.mode_processing) {
            viewFlipper.displayedChild = 1
        }

    }

    private fun showSpeakingStage() {
        if (viewFlipper.currentView.id != R.layout.mode_speaking) {
            viewFlipper.displayedChild = 2
        }
    }

    private fun startAsynchronously() {
        configureTTS()
        firstListeningProcess()
    }

    /**
     * Checks for permission to use audio. If granted start listening. If not ask for permission
     * again and call itself again.
     */
    private fun firstListeningProcess(){
        lifecycleScope.launch{
            delay(2500)
            configureSTT()
            if (checkCallingOrSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            }
            else {
                ActivityCompat.requestPermissions(
                    this@VoiceOnlyActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MainActivity.STT_PERMISSION_REQUEST_CODE
                )
                firstListeningProcess()
            }
        }
    }

    private fun processVoiceInput(command: String) {
        lifecycleScope.launch {
            showSpeakingStage()

            try {
                val llmConnector = LLMAppConnector.getInstance

                // Authenticate with the API
                val token = llmConnector.authenticate()

                // Send command to the LLM API
                val apiResponse = llmConnector.sendCommandToLLM(command, token)

                // Parse the response and get label and additional data requirement
                val (label, needsAdditionalData) = llmConnector.parseResponse(apiResponse)

                if (label != null) {
                    // If the label is found in the supported commands, execute it
                    if (isValidCommand(label)) {
                        executeCommand(label, needsAdditionalData)
                    } else {
                        // No valid command found, just say the response
                        viewModel.sayAndAwait(label)
                    }
                } else {
                    viewModel.sayAndAwait("I heard: $command, but couldn't process it.")
                }
            } catch (e: Exception) {
                viewModel.sayAndAwait("Sorry, I encountered an error: ${e.message}")
                e.printStackTrace()
            }

            // Automatically start listening again after processing
            //showListeningStage()
            //startListening()
        }
    }

    /**
     * Checks if the given label is a valid command that the app can handle
     */
    private fun isValidCommand(label: String): Boolean {
        val supportedCommands = listOf(
            "grocery-list",
        )
        return supportedCommands.contains(label)
    }

    /**
     * Executes the appropriate action based on the label from the LLM API.
     */
    private suspend fun executeCommand(label: String, needsAdditionalData: Boolean) {
        when (label) {
            "grocery-list" -> {
                if (needsAdditionalData) {
                    viewModel.sayAndAwait(getString(R.string.which_item))
                    // Next voice input will be processed in context of grocery list
                } else {
                    viewModel.sayAndAwait(getString(R.string.navigate_to_grocery_list))
                    val intent = Intent(this@VoiceOnlyActivity, GroceryListActivity::class.java)
                    startActivity(intent)
                }
            }
            // Add more commands as needed based on strings.xml and support of the LLM
            else -> {
                viewModel.sayAndAwait("I don't know how to handle: $label")
            }
        }
    }

    private fun startListening() {
        viewModel.handleSTTSpeechBegin()
    }
}
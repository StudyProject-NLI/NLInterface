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
import com.nlinterface.utility.OnSwipeTouchListener
import com.nlinterface.viewmodels.VoiceOnlyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceOnlyActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VoiceOnlyViewModel
    private lateinit var viewFlipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_voice_only)

        viewModel = ViewModelProvider(this)[VoiceOnlyViewModel::class.java]

        viewFlipper = findViewById(R.id.viewSwitcher)

        val rootView: View = findViewById(android.R.id.content)
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
     * The voiceActivationButton is configured to change it microphone color to green, if the STT
     * system is active and to change back to white, if it is not. Also retrieves the text output
     * of the voice input to the STT system, aka the 'command'
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
            viewModel.sayAndAwait(command)
            showListeningStage()
            startListening()
        }
    }

    private fun startListening() {
        viewModel.handleSTTSpeechBegin()
    }


}

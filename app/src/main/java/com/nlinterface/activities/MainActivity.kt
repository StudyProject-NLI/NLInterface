package com.nlinterface.activities

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
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
import com.nlinterface.viewmodels.MainViewModel
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var voiceActivationButton: ImageButton

    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        GlobalParameters.instance!!.loadPreferences(this)

        viewModel.initTTS()

        verifyAudioPermissions()

        configureUI()

        configureVoiceControl()

        viewModel.initSTT()
    }

    override fun onStart() {
        super.onStart()

        viewModel.say(resources.getString(R.string.main_activity))

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // process theme settings
        GlobalParameters.instance!!.updateTheme()
    }

    private fun configureVoiceControl() {

        val ttsInitializedObserver = Observer<Boolean> { _ ->
            viewModel.say(resources.getString(R.string.main_menu))
        }

        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_green)
            } else {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_white)
            }
        }

        viewModel.isListening.observe(this, sttIsListeningObserver)

        val commandObserver = Observer<ArrayList<String>> {command ->
            executeCommand(command)
        }

        viewModel.command.observe(this, commandObserver)
    }

    private fun executeCommand(command: ArrayList<String>?) {

        if ((command != null) && (command.size == 3)) {
            if (command[0] == "GOTO") {
                navToActivity(command[1])
            } else {
                viewModel.say(resources.getString(R.string.choose_activity_to_navigate_to))
            }
        }
    }

    private fun navToActivity(activity: String) {

        Log.println(Log.DEBUG, "navToActivity", activity)

        when (activity) {

            ActivityType.MAIN.toString() -> {
                viewModel.say(resources.getString(R.string.main_menu))
            }

            ActivityType.GROCERYLIST.toString() -> {
                val intent = Intent(this, GroceryListActivity::class.java)
                this.startActivity(intent)
            }
            ActivityType.PLACEDETAILS.toString() -> {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                this.startActivity(intent)
            }
            ActivityType.SETTINGS.toString() -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }

        }

    }

    private fun configureUI() {

        // set up button to navigate to GroceryListActivity
        val groceryListButton: Button = findViewById<View>(R.id.grocery_list_bt) as Button
        groceryListButton.setOnClickListener { view ->
            val intent = Intent(view.context, GroceryListActivity::class.java)
            view.context.startActivity(intent)
        }

        // set up button to navigate to PlaceDetailsActivity
        val placeDetailsButton: Button = findViewById<View>(R.id.place_details_bt) as Button
        placeDetailsButton.setOnClickListener { view ->
            val intent = Intent(view.context, PlaceDetailsActivity::class.java)
            view.context.startActivity(intent)
        }

        // set up button to navigate to SettingsActivity
        val settingsActivityButton: Button = findViewById<View>(R.id.settings_bt) as Button
        settingsActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, SettingsActivity::class.java)
            view.context.startActivity(intent)
        }

        // set up voice Activation Button listener
        voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }

        // resize Voice Activation Button to 1/3 of display size
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.audio_permission_granted, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.audio_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyAudioPermissions() {
        if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                STT_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun readMenuOptions() {
        viewModel.say(
            resources.getString(R.string.grocery_list) +
            resources.getString(R.string.place_details) +
            resources.getString(R.string.settings)
        )
    }

    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

}
package com.nlinterface.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.Global
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.core.view.WindowCompat
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.SpeechToTextButton
import com.nlinterface.utility.SpeechToTextUtility


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groceryListButton: Button = findViewById<View>(com.nlinterface.R.id.grocery_list) as Button
        groceryListButton.setOnClickListener { view ->
            val intent = Intent(view.context, GroceryListActivity::class.java)
            view.context.startActivity(intent)
        }

        val navigationActivityButton: Button = findViewById<View>(R.id.navigation) as Button
        navigationActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, NavigationActivity::class.java)
            view.context.startActivity(intent)
        }

        val settingsActivity: Button = findViewById<View>(R.id.settings) as Button
        settingsActivity.setOnClickListener { view ->
            val intent = Intent(view.context, SettingsActivity::class.java)
            view.context.startActivity(intent)
        }



        val locationActivityButton: Button = findViewById<View>(R.id.location) as Button
        locationActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, LocationActivity::class.java)
            view.context.startActivity(intent)
        }

        verifyAudioPermissions()
        initCommands()

        outputText = findViewById(R.id.outputTV)

        sttTrigger = findViewById(R.id.stt_btn)
        sttTrigger!!.setOnClickListener {
            if (isListening) {
                speechToTextUtility.handleSpeechEnd(outputText!!, sttTrigger!!)
                isListening = false
            } else {
                speechToTextUtility.handleSpeechBegin(outputText!!, sttTrigger!!)
                isListening = true
            }
        }

        speechToTextUtility.createSpeechRecognizer(this, outputText!!)
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

    private fun initCommands() {
        speechToTextUtility.commandsList = ArrayList()
        speechToTextUtility.commandsList!!.add(getString(R.string.goto_grocerylist_command))
    }

}
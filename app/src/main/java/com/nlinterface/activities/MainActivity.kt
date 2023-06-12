package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.SpeechToTextButton
import com.nlinterface.utility.SpeechToTextUtility


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // part of Speech-to-Text functionality
    private var isListening = false
    private var outputText: TextView? = null
    private var sttTrigger: SpeechToTextButton? = null
    private val speechToTextUtility = SpeechToTextUtility()
    //

    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verifyAudioPermissions()

        val exampleNextActivity: Button = findViewById<View>(R.id.next_activity) as Button
        exampleNextActivity.setOnClickListener { view ->
            val intent = Intent(view.context, NextActivityExample::class.java)
            view.context.startActivity(intent)
        }

        val settingsActivity: Button = findViewById<View>(R.id.settings) as Button
        settingsActivity.setOnClickListener { view ->
            val intent = Intent(view.context, SettingsActivity::class.java)
            view.context.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted for using voice commands!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please provide microphone permission to use voice commands.", Toast.LENGTH_LONG).show()
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

}
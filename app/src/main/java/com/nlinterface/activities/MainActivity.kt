package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalParameters.instance!!.loadPreferences(this)

        verifyAudioPermissions()

        configureUI()
    }

    override fun onStart() {
        super.onStart()

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // process theme settings
        GlobalParameters.instance!!.updateTheme()
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
        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
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

    private fun onVoiceActivationButtonClick() {
        // TODO
    }
}
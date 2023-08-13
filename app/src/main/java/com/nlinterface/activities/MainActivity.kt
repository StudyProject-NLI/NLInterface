package com.nlinterface.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
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
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.utility.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var voiceActivationButton: ImageButton

    companion object {
        // needed to verify the audio permission result
        private const val STT_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        GlobalParameters.instance!!.loadPreferences(this)

        val groceryListButton: Button = findViewById<View>(R.id.grocery_list_bt) as Button
        groceryListButton.setOnClickListener { view ->
            val intent = Intent(view.context, GroceryListActivity::class.java)
            view.context.startActivity(intent)
        }

        val navigationActivityButton: Button = findViewById<View>(R.id.navigation_bt) as Button
        navigationActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, NavigationActivity::class.java)
            view.context.startActivity(intent)
        }

        /*val MotorModuleButton: Button = findViewById<View>(R.id.motor_module_bt) as Button
        navigationActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, NavigationActivity::class.java)
            view.context.startActivity(intent)
        }*/

        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        voiceActivationButton.setOnClickListener {
            onAddVoiceActivationButtonClick()
        }

        //TODO: main activity layout needs to be adjusted to show more buttons
        /*val settingsActivityButton: Button = findViewById<View>(R.id.settings_bt) as Button
        settingsActivityButton.setOnClickListener { view ->
            val intent = Intent(view.context, SettingsActivity::class.java)
            view.context.startActivity(intent)
        }*/

        verifyAudioPermissions()
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

    private fun onAddVoiceActivationButtonClick() {
        Log.println(Log.ASSERT, "MainActivity: onAddVoiceActivationButtonClick", "Button CLicked")
    }

}
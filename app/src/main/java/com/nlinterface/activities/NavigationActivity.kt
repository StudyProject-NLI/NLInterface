package com.nlinterface.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nlinterface.R.*
import com.nlinterface.utility.GlobalParameters


class NavigationActivity : AppCompatActivity() {

    private lateinit var addressEditText: EditText
    private lateinit var openMapsButton: Button
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.navigation_activity)

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        addressEditText = findViewById(id.addressEditText)
        openMapsButton = findViewById(id.openMapsButton)
        openMapsButton.setOnClickListener {
            val address = addressEditText.text.toString()

            // Create a Uri with the address as the query parameter
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")

            // Create an Intent with the ACTION_VIEW action and the Uri
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            // Set the package name explicitly to open Google Maps
            mapIntent.setPackage("com.google.android.apps.maps")

            // Start the activity with the created Intent
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(
                    this,
                    "Google Maps is not installed on your device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}




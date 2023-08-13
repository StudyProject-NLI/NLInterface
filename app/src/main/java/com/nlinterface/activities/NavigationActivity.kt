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
import com.nlinterface.R
import com.nlinterface.R.*
import com.nlinterface.utility.GlobalParameters


class NavigationActivity : AppCompatActivity() {

    private lateinit var addressEditText: EditText
    private lateinit var openMapsButton: Button
    private lateinit var saveLocationButton: Button
    private lateinit var openSavedLocationsButton: Button

    object SavedLocationsDataSource {
        val savedLocations: MutableList<String> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        addressEditText = findViewById(R.id.addressEditText)
        openMapsButton = findViewById(R.id.openMapsButton)
        saveLocationButton = findViewById(R.id.saveLocationButton)
        openSavedLocationsButton = findViewById(R.id.openSavedLocationsButton)

        openMapsButton.setOnClickListener {
            val address = addressEditText.text.toString()
            openMaps(address)
        }

        saveLocationButton.setOnClickListener {
            val address = addressEditText.text.toString()
            saveLocation(address)
        }

        openSavedLocationsButton.setOnClickListener {
            openSavedLocations()
        }

        // Retrieve the saved locations from SharedPreferences on app launch
        SavedLocationsDataSource.savedLocations.clear()
        SavedLocationsDataSource.savedLocations.addAll(SharedPreferencesHelper.getSavedLocations(this))
    }

    private fun openMaps(address: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

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
    private fun saveLocation(address: String) {
        SavedLocationsDataSource.savedLocations.add(address)
        Toast.makeText(
            this,
            "Location saved: $address",
            Toast.LENGTH_SHORT
        ).show()

        // Save the updated list of locations to SharedPreferences
        SharedPreferencesHelper.saveLocations(this, SavedLocationsDataSource.savedLocations)

        val intent = Intent(this, SavedLocationsActivity::class.java)
        intent.putStringArrayListExtra("savedLocations", ArrayList(SavedLocationsDataSource.savedLocations))
        startActivity(intent)
    }

    private fun openSavedLocations() {
        val intent = Intent(this, SavedLocationsActivity::class.java)
        intent.putStringArrayListExtra("savedLocations", ArrayList(SavedLocationsDataSource.savedLocations))
        startActivity(intent)
    }
}

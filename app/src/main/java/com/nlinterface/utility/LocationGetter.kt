package com.nlinterface.utility

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


/**
 * Location Getter is a service that allows to access the users location to use it for further
 * context data for the AI. It helps the AI with choosing the appropriate functions with those
 * additional context information.
 */
class LocationGetter: Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var globalParameters = GlobalParameters.instance!!

    /**
     * Function that manages the Service, once initialized.
     * It maps a value to location Client and
     * starts the setupLocationCallback and startLocationUpdates
     * Returns Start-Sticky to ensure it will try to start again,
     * if it is destroyed for whatever reason.
     *
     * @param intent
     * @param flags
     * @param startId
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        startLocationUpdates()

        return START_STICKY
    }

    /**
     * Function that gets the location and saves it in a global variable to be accessed from
     * everywhere.
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    globalParameters.location = locationResult.lastLocation!!
                    Log.i("CurrentLocation", "The current location is:" +
                            "${globalParameters.location.latitude}, " +
                            "${globalParameters.location.longitude}")
                }
            }
        }
    }

    /**
     * Function that updates the location prioritizing low power drainage.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_LOW_POWER, 10000
            ).apply {
                setMinUpdateIntervalMillis(5000)
            }.build()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    /**
     * This method is required by the Service architecture,
     * but not needed because the scanning should be done constantly.
     * Therefore it just return null
     */

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * When service is terminated stop updating the location.
     */
    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.i("Location", "Accessing the location was stopped")
        super.onDestroy()
    }
}
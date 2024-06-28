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

class LocationGetter: Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val globalParameters = GlobalParameters.instance!!

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        startLocationUpdates()

        return START_STICKY
    }

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.i("Location", "Accessing the location was stopped")
        super.onDestroy()
    }
}
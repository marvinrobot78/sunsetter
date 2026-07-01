package com.atkinson.sunsetter

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchAndStoreLocation() else showStatus("Location permission denied.\nComplications will show default times.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchAndStoreLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun fetchAndStoreLocation() {
        showStatus("Getting location…")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationObtained(location.latitude, location.longitude)
                } else {
                    // No cached location; request a fresh fix
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { fresh ->
                            if (fresh != null) onLocationObtained(fresh.latitude, fresh.longitude)
                            else showStatus("Could not get location.\nPlease ensure location is enabled and try again.")
                        }
                        .addOnFailureListener { showStatus("Location error: ${it.message}") }
                }
            }
        } catch (e: SecurityException) {
            showStatus("Location permission error.")
        }
    }

    private fun onLocationObtained(lat: Double, lng: Double) {
        val repo = SunTimeRepository(this)
        repo.storeLocation(lat, lng)
        val sunTimes = repo.getSunTimesForToday()
        val sunsetValue = sunTimes.sunsetMinute - HALF_DAY.toInt()
        showStatus(
            "Sunrise: ${minuteOfDayToTimeString(sunTimes.sunriseMinute)}\n" +
            "Value: ${HALF_DAY.toInt() - sunTimes.sunriseMinute} / ${HALF_DAY.toInt()}\n\n" +
            "Sunset: ${minuteOfDayToTimeString(sunTimes.sunsetMinute)}\n" +
            "Value: $sunsetValue / ${HALF_DAY.toInt()}"
        )
        requestComplicationUpdates()
    }

    private fun requestComplicationUpdates() {
        listOf(
            SunriseComplicationDataSourceService::class.java,
            SunsetComplicationDataSourceService::class.java
        ).forEach { cls ->
            ComplicationDataSourceUpdateRequester
                .create(this, ComponentName(this, cls))
                .requestUpdateAll()
        }
    }

    private fun showStatus(message: String) {
        runOnUiThread { findViewById<TextView>(R.id.status_text)?.text = message }
    }
}

package br.dev.emiliano.mobile.trackerlocalization

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class GpsActivity : AppCompatActivity() {

    private lateinit var addressText: TextView
    private lateinit var button: Button
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gps)

        addressText = findViewById<TextView>(R.id.addressText)
        button = findViewById<Button>(R.id.button)

        locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000


        button.setOnClickListener(View.OnClickListener { getCurrentLocation() })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGpsEnabled()) {
                    getCurrentLocation()
                } else {
                    turnOnGPS()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isGpsEnabled()) {
                LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, object : LocationCallback() {
                        @SuppressLint("SetTextI18n")
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            LocationServices.getFusedLocationProviderClient(this@GpsActivity)
                                .removeLocationUpdates(this)
                            if (locationResult.locations.size > 0) {
                                val index = locationResult.locations.size - 1
                                val latitude =
                                    locationResult.locations[index].latitude
                                val longitude =
                                    locationResult.locations[index].longitude
                                addressText.text = "Latitude: $latitude\nLongitude: $longitude"
                            }
                        }
                    }, Looper.getMainLooper())
            } else {
                turnOnGPS()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(applicationContext).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                Toast.makeText(this@GpsActivity, "GPS já está ligado", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@GpsActivity, 2)
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        var locationManager: LocationManager? = null

        if (locationManager == null) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
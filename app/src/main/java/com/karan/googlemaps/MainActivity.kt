package com.karan.googlemaps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.BuildConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karan.googlemaps.databinding.ActivityMainBinding
import com.squareup.okhttp.internal.framed.Settings
import java.security.Permission

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    var Gmaps: GoogleMap? = null
    var userLocation = LatLng(0.0, 0.0)
    var markerOptions = MarkerOptions() //location marke
    var centerMarker: Marker? = null

    private val locationPermission = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            getLastLocation()
            Toast.makeText(this, "All permisiion granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!hasPermissions()) {
            requestPermissionsWithRationale()
        } else {
            getLastLocation()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mp) as SupportMapFragment?
        mapFragment?.getMapAsync(this) // async is used to get inbuild function

    }

    private fun hasPermissions(): Boolean {
        return locationPermission.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissionsWithRationale() {
        val shouldShowRational = locationPermission.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
        if (shouldShowRational) {
            Toast.makeText(
                this,
                "Permission are required for the app to function properly",
                Toast.LENGTH_SHORT
            ).show()
            openAppSettings()
        } else {
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            locationPermission
        )
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = android.net.Uri.fromParts(
            "Package",
            com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID,
            null
        )
        intent.data = uri
        startActivity(intent)
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: android.location.Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    userLocation = LatLng(location.latitude, location.longitude)
                    updateMarker()
                }
            }
        } else {
            Toast.makeText(this, "Turn on your location", Toast.LENGTH_SHORT).show()
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun updateMarker() {
        centerMarker?.position = userLocation
        Gmaps?.animateCamera(CameraUpdateFactory.newLatLng(userLocation))
        binding.locationMap.text = Maps.getLocationName(this@MainActivity, userLocation)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.Builder(100000).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.myLooper()

        )
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(loationResult: LocationResult) {
            super.onLocationResult(loationResult)
            userLocation = LatLng(
                loationResult.lastLocation?.latitude ?: 0.0,
                loationResult.lastLocation?.longitude ?: 0.0
            )
            updateMarker()
        }
    }

    override fun onMapReady(map: GoogleMap) {//ook
        this.Gmaps = map
        centerMarker = map.addMarker(
            MarkerOptions().position(userLocation)
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        map.setOnCameraIdleListener {
            userLocation = map.cameraPosition.target
            updateMarker()
        }
    }
}
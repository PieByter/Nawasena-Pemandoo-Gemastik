package com.nawasena.pemandoo.database

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.api.ApiConfig
import com.nawasena.pemandoo.api.LandmarkResponseItem
import com.nawasena.pemandoo.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: MapsViewModel

    // Set to track active geofence IDs
    private val activeGeofenceIds = mutableSetOf<String>()

    private val geofencePendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupMap()
                getAllLandmarksAndDisplay()
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = MapsRepository(ApiConfig.apiService)
        viewModel = ViewModelProvider(this, MapsViewModelFactory(repository))[MapsViewModel::class.java]

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            setupMap()
            getAllLandmarksAndDisplay()
        }

        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getAllLandmarksAndDisplay()
        } else {
            requestLocationPermission()
        }
    }

    private fun addGeofence(latLng: LatLng, radius: Float, geofenceId: String) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

            // Check if geofence with same ID is already active, remove it if true
            if (activeGeofenceIds.contains(geofenceId)) {
                removeGeofence(geofenceId)
            }

            val geofence = Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            try {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener {
                        Log.d("MapsActivity", "Geofence added successfully: $geofenceId")
                        activeGeofenceIds.add(geofenceId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MapsActivity", "Failed to add geofence: $geofenceId", e)
                    }
            } catch (securityException: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${securityException.message}")
                // Handle security exception (e.g., show error message to user)
                Toast.makeText(this, "Failed to add geofence: SecurityException", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle case where permission is not granted
            Toast.makeText(this, "Location permission is required to add geofence", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId))
            .addOnSuccessListener {
                Log.d("MapsActivity", "Geofence removed successfully: $geofenceId")
                activeGeofenceIds.remove(geofenceId)
            }
            .addOnFailureListener { e ->
                Log.e("MapsActivity", "Failed to remove geofence: $geofenceId", e)
            }
    }

    private fun getAllLandmarksAndDisplay() {
        viewModel.fetchAllLandmarks().observe(this) { landmarks ->
            if (landmarks != null) {
                Log.d("MapsActivity", "Successfully fetched ${landmarks.size} landmarks")
                displayLandmarks(landmarks)
            } else {
                Log.e("MapsActivity", "Failed to fetch landmarks")
            }
        }
    }

    private fun displayLandmarks(landmarks: List<LandmarkResponseItem>) {
        mMap.clear()
        // Clear activeGeofenceIds to start fresh
        activeGeofenceIds.clear()

        for (landmark in landmarks) {
            val latLng = LatLng(
                landmark.coordinates?.latitude as Double,
                landmark.coordinates.longitude as Double
            )
            val radius = 100f

            val markerOptions = MarkerOptions().position(latLng).title(landmark.name).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mMap.addMarker(markerOptions)

            val circleOptions = CircleOptions()
                .center(latLng)
                .radius(radius.toDouble())
                .strokeColor(Color.GREEN)
                .fillColor(0x220A6847)
                .strokeWidth(3f)
            mMap.addCircle(circleOptions)
            addGeofence(latLng, radius, landmark.id.toString())
        }

        if (landmarks.isNotEmpty()) {
            val firstLandmark = landmarks[4]
            val firstLatLng = LatLng(
                firstLandmark.coordinates!!.latitude as Double,
                firstLandmark.coordinates.longitude as Double
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12f))
        }
    }
}

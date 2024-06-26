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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.databinding.ActivityMapsBinding
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: MapsViewModel
    private lateinit var repository: LandmarkRepository

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

        val database = AppDatabase.getDatabase(applicationContext)
        repository = LandmarkRepository(database.landmarkDao())

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            setupMap()
            getAllLandmarksAndDisplay()
        }

        val factory = MapsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]

        testDatabaseOperations()
    }

    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)
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
        } else {
            requestLocationPermission()
        }
    }

    private fun addGeofence(latLng: LatLng, radius: Float, geofenceId: String) {
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

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d("MapsActivity", "Geofence added successfully: $geofenceId")
                }
                .addOnFailureListener { e ->
                    Log.e("MapsActivity", "Failed to add geofence: $geofenceId", e)
                }
        }
    }

    private fun getAllLandmarksAndDisplay() {
        lifecycleScope.launch {
            try {
                val landmarks = repository.getAllLandmarksWithDetails()
                mMap.clear()
                for (landmark in landmarks) {
                    val latLng = LatLng(landmark.latitude, landmark.longitude)
                    val radius = landmark.geofenceRadius

                    val markerOptions = MarkerOptions().position(latLng).title(landmark.name)
                    mMap.addMarker(markerOptions)

                    val circleOptions = CircleOptions()
                        .center(latLng)
                        .radius(radius.toDouble())
                        .strokeColor(Color.RED)
                        .fillColor(0x220000FF)
                        .strokeWidth(3f)
                    mMap.addCircle(circleOptions)
                    addGeofence(latLng, radius, landmark.id.toString())
                }

                if (landmarks.isNotEmpty()) {
                    val firstLandmark = landmarks[0]
                    val firstLatLng = LatLng(firstLandmark.latitude, firstLandmark.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12f))
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error fetching landmarks", e)
                Toast.makeText(this@MapsActivity, "Error fetching landmarks", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun testDatabaseOperations() {
        lifecycleScope.launch {
            try {
                val description = getString(R.string.test_description)
                val landmark = Landmark(
                    id = 0,
                    name = "Tjong A Fie Museum",
                    latitude = 3.585609,
                    longitude = 98.680198,
                    image = "sample_image_uri",
                    description = description,
                    geofenceRadius = 100f
                )
                viewModel.insert(landmark)
                Log.d("MapsActivity", "Test landmark inserted")

                val allLandmarks = repository.getAllLandmarksWithDetails()
                Log.d("MapsActivity", "All landmarks: $allLandmarks")

                if (allLandmarks.isNotEmpty()) {
                    val firstLandmark = allLandmarks[0]
                    firstLandmark.name = "Tjong A Fie"
                    viewModel.update(firstLandmark)
                    Log.d("MapsActivity", "Test landmark updated")
                }

                if (allLandmarks.isNotEmpty()) {
                    val firstLandmark = allLandmarks[0]
                    viewModel.delete(firstLandmark.id)
                    Log.d("MapsActivity", "Test landmark deleted")
                }

                getAllLandmarksAndDisplay()
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error during test database operations", e)
            }
        }
    }
}
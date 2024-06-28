package com.nawasena.pemandoo.fragment.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.nawasena.pemandoo.GuideActivity
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.api.ApiConfig
import com.nawasena.pemandoo.api.LandmarkResponseItem
import com.nawasena.pemandoo.authentication.SettingActivity
import com.nawasena.pemandoo.database.AddLandmarkActivity
import com.nawasena.pemandoo.database.MapsRepository
import com.nawasena.pemandoo.database.MapsViewModel
import com.nawasena.pemandoo.database.MapsViewModelFactory
import com.nawasena.pemandoo.databinding.FragmentNavigationBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NavigationFragment : Fragment() {
    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    private lateinit var viewModel: MapsViewModel

    private val geofenceRadius = 100f
    private var geofenceLocations: List<LatLng> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { newLocation ->
                    userLocation = newLocation
                    checkGeofenceStatus()
                }
            }
        }

        val repository = MapsRepository(ApiConfig.apiService)
        viewModel =
            ViewModelProvider(this, MapsViewModelFactory(repository))[MapsViewModel::class.java]

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            requestLocation()
        }

        lifecycleScope.launch {
            val sharedPreferences =
                requireContext().getSharedPreferences("LandmarkPrefs", Context.MODE_PRIVATE)
            val landmarkId = sharedPreferences.getString("landmarkId", null)

            if (landmarkId != null) {
                fetchLandmarkById(landmarkId)
            }
        }

        binding.btnPanduan.setOnClickListener {
            startActivity(Intent(requireContext(), GuideActivity::class.java))
        }

        binding.btnTambah.setOnClickListener {
            startActivity(Intent(requireContext(), AddLandmarkActivity::class.java))
        }

        binding.btnPengaturan.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                locationCallback,
                null
            )
        }
    }

  /*  private fun checkGeofenceStatus() {
        val sharedPreferences =
            requireContext().getSharedPreferences("LandmarkPrefs", Context.MODE_PRIVATE)
        val landmarkId = sharedPreferences.getString("landmarkId", null)

        // Periksa apakah userLocation dan geofenceLocations tidak null
        val withinGeofence = userLocation != null && geofenceLocations.any { geofence ->
            val distance = FloatArray(1)
            Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                geofence.latitude, geofence.longitude,
                distance
            )
            distance[0] <= geofenceRadius
        }

        binding.btnPlay.isEnabled = withinGeofence
        binding.btnPlay.visibility = if (withinGeofence) View.VISIBLE else View.GONE

        if (withinGeofence) {
            binding.btnPlay.setOnClickListener {
                landmarkId?.let { id ->
                    navigateToResultFragment(id)
                }
            }
        } else {
            binding.btnPlay.setOnClickListener(null)
        }
    }*/

    private fun checkGeofenceStatus() {
        val sharedPreferences =
            requireContext().getSharedPreferences("LandmarkPrefs", Context.MODE_PRIVATE)
        val landmarkId = sharedPreferences.getString("landmarkId", null)

        // Tetapkan OnClickListener tanpa pengecekan tambahan
        binding.btnPlay.setOnClickListener {
            landmarkId?.let { id ->
                navigateToResultFragment(id)
            }
        }
    }



    private fun fetchLandmarkById(landmarkId: String) {
        val apiService = ApiConfig.apiService
        val call = apiService.getLandmarkById(landmarkId)

        call.enqueue(object : Callback<LandmarkResponseItem> {
            override fun onResponse(
                call: Call<LandmarkResponseItem>,
                response: Response<LandmarkResponseItem>,
            ) {
                if (response.isSuccessful) {
                    val landmark = response.body()
                    if (landmark != null) {
                        updateUI(landmark)
                    }
                }
            }

            override fun onFailure(call: Call<LandmarkResponseItem>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun updateUI(landmark: LandmarkResponseItem) {
        val binding = _binding

        binding?.let {
            Glide.with(requireContext())
                .load(landmark.photo)
                .into(it.ivLandmarkImage)
            it.tvLandmarkName.text = landmark.name
            it.tvLandmarkDescription.text = landmark.description?.overview ?: ""
        }
    }


    private fun navigateToResultFragment(landmarkId: String) {
        val actionId = R.id.action_navigationFragment_to_resultFragment
        findNavController().navigate(actionId, Bundle().apply {
            putString("landmarkId", landmarkId)
        })
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}

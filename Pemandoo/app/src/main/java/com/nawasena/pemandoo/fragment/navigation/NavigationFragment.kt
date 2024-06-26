package com.nawasena.pemandoo.fragment.navigation

import android.Manifest
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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.nawasena.pemandoo.GuideActivity
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.SettingActivity
import com.nawasena.pemandoo.database.AddLandmarkActivity
import com.nawasena.pemandoo.databinding.FragmentNavigationBinding
import com.nawasena.pemandoo.database.*

class NavigationFragment : Fragment() {
    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    private lateinit var viewModel: MapsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
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
                }
            }
        }

        val application = requireActivity().application
        val dataSource = AppDatabase.getDatabase(application).landmarkDao()
        val repository = LandmarkRepository(dataSource)
        viewModel = ViewModelProvider(requireActivity(), MapsViewModelFactory(repository))[MapsViewModel::class.java]

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

        binding.btnPlay.setOnClickListener {
            val landmarkId = 0
            val actionId = R.id.action_navigationFragment_to_resultFragment
            findNavController().navigate(actionId, Bundle().apply {
                putInt("landmarkId", landmarkId)
            })
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
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
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

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}

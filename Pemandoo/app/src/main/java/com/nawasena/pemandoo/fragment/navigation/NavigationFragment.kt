package com.nawasena.pemandoo.fragment.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nawasena.pemandoo.AddLandmarkActivity
import com.nawasena.pemandoo.GuideActivity
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.SettingActivity
import com.nawasena.pemandoo.databinding.FragmentNavigationBinding

class NavigationFragment : Fragment() {
    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlay.setOnClickListener {
            findNavController().navigate(R.id.action_navigationFragment_to_resultFragment)
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
    }
}

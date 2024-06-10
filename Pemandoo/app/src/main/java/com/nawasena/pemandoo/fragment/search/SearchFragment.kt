package com.nawasena.pemandoo.fragment.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startLoading()

        binding.btnSearchStart.setOnClickListener {
            onStartTour()
        }
    }

    private fun startLoading() {
        binding.loadingBar.visibility = View.VISIBLE
        binding.loadingBar.show()

        Handler(Looper.getMainLooper()).postDelayed({
            stopLoading()
        }, 10000)
    }

    private fun stopLoading() {
        binding.loadingBar.hide()
/*        binding.loadingBar.visibility = View.GONE*/
    }

    private fun onStartTour() {
        findNavController().navigate(R.id.action_searchFragment_to_navigationFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

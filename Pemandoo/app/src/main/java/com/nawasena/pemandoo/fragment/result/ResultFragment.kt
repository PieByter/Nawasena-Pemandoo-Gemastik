package com.nawasena.pemandoo.fragment.result

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.api.LandmarkResponseItem
import com.nawasena.pemandoo.database.MapsViewModel
import com.nawasena.pemandoo.databinding.FragmentResultBinding
import java.util.Locale

class ResultFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapsViewModel
    private lateinit var tts: TextToSpeech
    private var isPlaying: Boolean = false
    private var pendingDescription: String? = null
    private lateinit var soundWaveAnimation: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val landmarkId = arguments?.getString("landmarkId", "") ?: ""

        viewModel = ViewModelProvider(requireActivity())[MapsViewModel::class.java]

        tts = TextToSpeech(requireContext(), this)

        soundWaveAnimation = binding.ivSoundWave

        viewModel.getLandmarkById(landmarkId).observe(viewLifecycleOwner) { landmark ->
            landmark?.let {
                updateUI(landmark)
            }
        }

        setupButtons()
    }

    private fun updateUI(landmark: LandmarkResponseItem) {
        binding.tvLandmarkName.text = landmark.name
        binding.tvLandmarkDescription.text = landmark.description?.overview ?: ""
        Glide.with(this)
            .load(landmark.photo)
            .into(binding.ivLandmarkImage)

        binding.cvPlayDescription.setOnClickListener {
            navigateToDetailFragment(landmark)
        }

        pendingDescription = landmark.description?.overview

        // Automatically start TTS and update UI on first entry or from NavigationFragment
        if (!isPlaying) {
            isPlaying = true
            speakDescription(pendingDescription ?: "")
            soundWaveAnimation.playAnimation()
            updatePauseButton()
        }
    }

    private fun navigateToDetailFragment(landmark: LandmarkResponseItem) {
        val bundle = Bundle().apply {
            putString("name", landmark.name)
            putString("radius", landmark.coordinates?.latitude.toString())
            putString("image", landmark.photo)
            putString("description", landmark.description?.overview)
        }

        findNavController().navigate(
            R.id.action_resultFragment_to_detailLandmarkFragment,
            bundle
        )
    }

    private fun setupButtons() {
        binding.btnPause.setOnClickListener {
            isPlaying = if (isPlaying) {
                tts.stop()
                soundWaveAnimation.pauseAnimation()
                false
            } else {
                pendingDescription?.let {
                    speakDescription(it)
                    soundWaveAnimation.playAnimation()
                }
                true
            }
            updatePauseButton()
        }

        binding.btnBackResult.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_navigationFragment)
        }
    }

    private fun speakDescription(description: String) {
        if (::tts.isInitialized) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show()
                Log.e("TTS", "The language is not supported")
            } else {
                if (description.isNotEmpty()) {
                    val speakResult = tts.speak(description, TextToSpeech.QUEUE_FLUSH, null, null)
                    if (speakResult == TextToSpeech.ERROR) {
                        Log.e("TTS", "Error in speaking the text")
                    }
                } else {
                    Log.e("TTS", "Description is empty or null")
                }
            }
        } else {
            pendingDescription = description
        }
    }

    private fun updatePauseButton() {
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val icon = ContextCompat.getDrawable(requireContext(), iconRes)
        binding.btnPause.setImageDrawable(icon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show()
                Log.e("TTS", "The language is not supported")
            } else {
                pendingDescription?.let {
                    speakDescription(it)
                    pendingDescription = null
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to initialize Text-to-Speech", Toast.LENGTH_SHORT).show()
            Log.e("TTS", "Initialization failed with status: $status")
        }
    }
}

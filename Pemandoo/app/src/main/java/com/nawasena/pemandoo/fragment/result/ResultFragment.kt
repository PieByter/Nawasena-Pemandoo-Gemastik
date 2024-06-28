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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.database.AppDatabase
import com.nawasena.pemandoo.database.LandmarkRepository
import com.nawasena.pemandoo.database.MapsViewModel
import com.nawasena.pemandoo.database.MapsViewModelFactory
import com.nawasena.pemandoo.databinding.FragmentResultBinding
import java.util.*

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

        val landmarkId = arguments?.getInt("landmarkId", 0) ?: 0

        val application = requireActivity().application
        val dataSource = AppDatabase.getDatabase(application).landmarkDao()
        val repository = LandmarkRepository(dataSource)
        viewModel = ViewModelProvider(this, MapsViewModelFactory(repository))[MapsViewModel::class.java]

        tts = TextToSpeech(requireContext(), this)

        viewModel.getLandmarkById(landmarkId).observe(viewLifecycleOwner) { landmark ->
            landmark?.let {
                binding.tvLandmarkDescription.text = it.description
                binding.tvLandmarkName.text = it.name
                binding.tvLandmarkRange.text = it.geofenceRadius.toString()
                Glide.with(this)
                    .load(it.image)
                    .into(binding.ivLandmarkImage)

                viewModel.updateCurrentLandmark(it)

                pendingDescription = it.description

                if (isPlaying) {
                    speakDescription(it.description)
                }

                binding.cvPlayDescription.setOnClickListener { _ ->
                    val bundle = Bundle().apply {
                        putString("name", it.name)
                        putString("radius", it.geofenceRadius.toString())
                        putString("image", it.image)
                        putString("description", it.description)
                    }

                    val extras = FragmentNavigatorExtras(
                        binding.ivLandmarkImage to "image_transition",
                        binding.tvLandmarkName to "name_transition",
                        binding.tvLandmarkRange to "radius_transition",
                        binding.tvLandmarkDescription to "description_transition"
                    )

                    findNavController().navigate(
                        R.id.action_resultFragment_to_detailLandmarkFragment,
                        bundle,
                        null,
                        extras
                    )
                }

            }
        }
        soundWaveAnimation = binding.ivSoundWave

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnPause.setOnClickListener {
            isPlaying = if (isPlaying) {
                tts.stop()
                soundWaveAnimation.pauseAnimation()
                false
            } else {
                viewModel.getCurrentLandmark()?.let { landmark ->
                    speakDescription(landmark.description)
                }
                soundWaveAnimation.playAnimation()
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
            val result = tts.speak(description, TextToSpeech.QUEUE_FLUSH, null, null)
            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Error in speaking the text")
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

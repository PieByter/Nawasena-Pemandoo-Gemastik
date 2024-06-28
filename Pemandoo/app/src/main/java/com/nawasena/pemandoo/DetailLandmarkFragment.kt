package com.nawasena.pemandoo

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.nawasena.pemandoo.databinding.FragmentDetailLandmarkBinding
import java.util.*

class DetailLandmarkFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentDetailLandmarkBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeech
    private var isPlaying: Boolean = false
    private var pendingDescription: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailLandmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val name = bundle.getString("name")
            val radius = bundle.getString("radius")
            val image = bundle.getString("image")
            val description = bundle.getString("description")

            binding.tvDetailName.text = name
            binding.tvDetailRadius.text = radius
            binding.tvDetailDescription.text = description

            Glide.with(requireContext())
                .load(image)
                .into(binding.ivDetailPhoto)

            // Initialize TextToSpeech
            tts = TextToSpeech(requireContext(), this)
        }

        // Setup play/pause button
        binding.btnPlaySound.setOnClickListener {
            isPlaying = if (isPlaying) {
                stopSpeaking()
                false
            } else {
                startSpeaking(binding.tvDetailDescription.text.toString())
                true
            }
            updatePlayPauseButton()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Shutdown TextToSpeech
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                pendingDescription?.let {
                    startSpeaking(it)
                    pendingDescription = null
                }
            }
        } else {
            // Handle TTS initialization failure
        }
    }

    private fun startSpeaking(text: String) {
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            // Handle TTS speak error
        }
    }

    private fun stopSpeaking() {
        tts.stop()
    }

    private fun updatePlayPauseButton() {
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        binding.btnPlaySound.setImageResource(iconRes)
    }
}

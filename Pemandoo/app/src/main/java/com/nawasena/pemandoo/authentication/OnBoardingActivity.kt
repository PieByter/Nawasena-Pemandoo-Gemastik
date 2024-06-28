package com.nawasena.pemandoo.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nawasena.pemandoo.database.MapsActivity
import com.nawasena.pemandoo.databinding.ActivityOnboardingBinding

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoginOnboarding.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegisterOnboarding.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvContinueAsGuest.setOnClickListener {
            onContinueAsGuest()
        }
    }

    fun onContinueAsGuest() {
        startActivity(Intent(this, MapsActivity::class.java))
    }
}

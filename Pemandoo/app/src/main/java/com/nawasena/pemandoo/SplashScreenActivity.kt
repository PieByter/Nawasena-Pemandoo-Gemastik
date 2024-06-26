package com.nawasena.pemandoo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logoImageView: ImageView = findViewById(R.id.iv_logo_splash_screen)

        logoImageView.alpha = 0f
        logoImageView.scaleX = 0f
        logoImageView.scaleY = 0f

        logoImageView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(1500)
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, OnBoardingActivity::class.java))
                    finish()
                }, 1000)
            }
            .start()
    }
}

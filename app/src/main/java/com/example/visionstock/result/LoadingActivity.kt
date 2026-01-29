package com.example.visionstock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.login.LoginActivity
import com.example.visionstock.result.ResultActivity

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        supportActionBar?.hide()

        // Check if we received an image URI from the Scanner
        val scannedUri = intent.getStringExtra("scanned_image_uri")

        Handler(Looper.getMainLooper()).postDelayed({

            if (scannedUri != null) {
                // SCENARIO 1: We have an image -> Go to RESULTS
                val resultIntent = Intent(this, ResultActivity::class.java)
                resultIntent.putExtra("scanned_image_uri", scannedUri) // Pass the image along
                startActivity(resultIntent)
            } else {
                // SCENARIO 2: No image -> Go to LOGIN (App Startup)
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
            }

            finish()
        }, 3000) // 3 Seconds Delay
    }
}
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

        // --- 1. CAPTURE ALL DATA FROM SEARCH/SCANNER ---
        val scannedUri = intent.getStringExtra("scanned_image_uri")
        val itemId = intent.getStringExtra("item_id")
        val itemName = intent.getStringExtra("item_name")
        val itemCategory = intent.getStringExtra("item_category")
        val itemLocation = intent.getStringExtra("item_location")
        val itemQuantity = intent.getIntExtra("item_quantity", 0)

        Handler(Looper.getMainLooper()).postDelayed({

            if (scannedUri != null) {
                // SCENARIO 1: We have data -> Go to RESULTS
                val resultIntent = Intent(this, ResultActivity::class.java)

                // --- 2. FORWARD ALL DATA TO RESULT ACTIVITY ---
                // Without these lines, ResultActivity gets nothing but the image
                resultIntent.putExtra("scanned_image_uri", scannedUri)
                resultIntent.putExtra("item_id", itemId)
                resultIntent.putExtra("item_name", itemName)
                resultIntent.putExtra("item_category", itemCategory)
                resultIntent.putExtra("item_location", itemLocation)
                resultIntent.putExtra("item_quantity", itemQuantity)

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
package com.example.visionstock.result

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.R

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 1. Setup Back Button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // Closes result and goes back to Scanner
        }


        val imageUriString = intent.getStringExtra("scanned_image_uri")
        val productImage = findViewById<ImageView>(R.id.productImage)

        if (imageUriString != null) {
            // If we found an image, show it!
            val imageUri = Uri.parse(imageUriString)
            productImage.setImageURI(imageUri)
        } else {
            // Fallback if something went wrong (optional)
            productImage.setImageResource(R.drawable.ic_launcher_background)
        }
    }
}
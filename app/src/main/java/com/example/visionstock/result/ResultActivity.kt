package com.example.visionstock.result

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.R

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // --- 1. BIND VIEWS ---
        // Ensure these IDs match your activity_result.xml perfectly
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val productImage = findViewById<ImageView>(R.id.productImage)
        val tvItemId = findViewById<TextView>(R.id.tvResultItemId)
        val tvItemName = findViewById<TextView>(R.id.tvResultItemName)
        val tvCategory = findViewById<TextView>(R.id.tvResultCategory)
        val tvLocation = findViewById<TextView>(R.id.tvResultLocation)
        val tvQuantity = findViewById<TextView>(R.id.tvResultQuantity)

        // --- 2. BACK BUTTON ---
        btnBack.setOnClickListener {
            finish()
        }

        // --- 3. RETRIEVE DATA ---
        // These keys must match the ones sent by LoadingActivity/ImageSearchFragment
        val imageBase64String = intent.getStringExtra("scanned_image_uri")
        val itemId = intent.getStringExtra("item_id") ?: "N/A"
        val itemName = intent.getStringExtra("item_name") ?: "Unknown Item"
        val itemCategory = intent.getStringExtra("item_category") ?: "Others"
        val itemLoc = intent.getStringExtra("item_location") ?: "Not Specified"

        // Use getIntExtra for the quantity
        val itemQty = intent.getIntExtra("item_quantity", 0)

        // --- 4. REFLECT DATA IN UI ---
        // Setting the text to the values retrieved from the database
        tvItemId.text = itemId
        tvItemName.text = itemName
        tvCategory.text = itemCategory
        tvLocation.text = itemLoc
        tvQuantity.text = itemQty.toString()

        // --- 5. DECODE AND DISPLAY IMAGE ---
        if (!imageBase64String.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(imageBase64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                productImage.setImageBitmap(bitmap)
                productImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                productImage.setImageResource(R.drawable.logo)
            }
        } else {
            productImage.setImageResource(R.drawable.logo)
        }
    }
}
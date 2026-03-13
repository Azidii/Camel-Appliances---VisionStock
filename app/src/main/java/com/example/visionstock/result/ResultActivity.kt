package com.example.visionstock.result

import android.graphics.BitmapFactory
import android.net.Uri
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
        val imageString = intent.getStringExtra("scanned_image_uri")
        val itemId = intent.getStringExtra("item_id") ?: "N/A"
        val itemName = intent.getStringExtra("item_name") ?: "Unknown Item"
        val itemCategory = intent.getStringExtra("item_category") ?: "Others"
        val itemLoc = intent.getStringExtra("item_location") ?: "Not Specified"
        val itemQty = intent.getIntExtra("item_quantity", 0)

        // --- 4. REFLECT DATA IN UI ---
        tvItemId.text = itemId
        tvItemName.text = itemName
        tvCategory.text = itemCategory
        tvLocation.text = itemLoc
        tvQuantity.text = itemQty.toString()

        // --- 5. DISPLAY IMAGE ---
        // Supports both file/content URIs (from scanner) and Base64 strings (from Firestore)
        if (!imageString.isNullOrEmpty()) {
            try {
                if (imageString.startsWith("file://") || imageString.startsWith("content://")) {
                    // Load from URI (scanner captured or gallery-picked image)
                    val uri = Uri.parse(imageString)
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        productImage.setImageBitmap(bitmap)
                        productImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        productImage.setImageResource(R.drawable.logo)
                    }
                } else if (imageString.startsWith("http")) {
                    // It's a web URL. Since we don't have Glide/Picasso, fallback to logo
                    productImage.setImageResource(R.drawable.logo)
                } else {
                    // Decode Base64 string (from Firestore database)
                    var cleanBase64 = imageString
                    val commaIndex = cleanBase64.indexOf(",")
                    if (commaIndex != -1) {
                        cleanBase64 = cleanBase64.substring(commaIndex + 1)
                    }
                    val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        productImage.setImageBitmap(bitmap)
                        productImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        productImage.setImageResource(R.drawable.logo)
                    }
                }
            } catch (e: Exception) {
                productImage.setImageResource(R.drawable.logo)
            }
        } else {
            productImage.setImageResource(R.drawable.logo)
        }
    }
}
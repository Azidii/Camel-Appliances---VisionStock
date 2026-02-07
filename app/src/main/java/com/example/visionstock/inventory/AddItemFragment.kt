package com.example.visionstock.inventory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AddItemFragment : Fragment(R.layout.fragment_add_item) {

    private lateinit var imgPreview: ImageView
    private lateinit var overlay: LinearLayout
    private var selectedImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    // REMOVED: private val storage = FirebaseStorage.getInstance() (Not needed for free method)

    // 1. Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data

            if (selectedImageUri != null) {
                overlay.visibility = View.GONE
                imgPreview.setImageURI(selectedImageUri)
                imgPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BIND VIEWS ---
        imgPreview = view.findViewById(R.id.imgProductPreview)
        overlay = view.findViewById(R.id.layoutAddPhotoOverlay)

        val etId = view.findViewById<TextInputEditText>(R.id.etItemId)
        val etName = view.findViewById<TextInputEditText>(R.id.etItemName)

        // --- DROPDOWN SETUP ---
        val etCategory = view.findViewById<MaterialAutoCompleteTextView>(R.id.etItemCategory)
        try {
            val categories = arrayOf(
                "Raw Materials",
                "Electronic & Electrical Components",
                "Mechanical Components",
                "Fasteners & Hardware",
                "Production Consumables",
                "Others"
            )
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
            etCategory.setAdapter(adapter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val etQty = view.findViewById<TextInputEditText>(R.id.etItemQuantity)
        val etLoc = view.findViewById<TextInputEditText>(R.id.etItemLocation)

        val btnSave = view.findViewById<Button>(R.id.btnSaveItem)
        val cardImagePicker = view.findViewById<CardView>(R.id.cardImagePicker)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        // --- LISTENERS ---
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        cardImagePicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // Close keyboard on touch
        view.setOnTouchListener { v, _ ->
            v.performClick()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            true
        }

        // SAVE BUTTON
        btnSave.setOnClickListener {
            val id = etId.text.toString().trim()
            val name = etName.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val qtyStr = etQty.text.toString().trim()
            val loc = etLoc.text.toString().trim()

            if (id.isEmpty() || name.isEmpty() || category.isEmpty() || qtyStr.isEmpty() || loc.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImageAndSaveItem(id, name, category, qtyStr.toInt(), loc)
        }
    }

    // --- NEW FREE METHOD: Convert Image to Text (Base64) ---
    private fun uploadImageAndSaveItem(id: String, name: String, category: String, qty: Int, loc: String) {
        DialogHelper.showLoading(requireContext(), "Saving Item...")

        if (selectedImageUri != null) {
            try {
                // 1. Convert URI to Bitmap
                val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // 2. Resize Bitmap (CRITICAL: Firestore has a 1MB limit per document)
                // We resize to 500px width to keep it small and fast
                val resizedBitmap = getResizedBitmap(bitmap, 500)

                // 3. Convert Bitmap to Base64 String
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) // Quality 60%
                val byteArray = outputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                // 4. Save directly to Firestore (No Storage Bucket needed!)
                saveToFirestore(id, name, category, qty, loc, base64Image)

            } catch (e: Exception) {
                DialogHelper.hideLoading()
                Toast.makeText(requireContext(), "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Save without image
            saveToFirestore(id, name, category, qty, loc, "")
        }
    }

    // Helper function to resize the image
    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun saveToFirestore(id: String, name: String, category: String, qty: Int, loc: String, imageUrl: String) {
        val newItem = hashMapOf(
            "itemID" to id,
            "name" to name,
            "itemCategory" to category,
            "quantity" to qty,
            "itemLocation" to loc,
            "itemPicture" to imageUrl
        )

        db.collection("inventory")
            .add(newItem)
            .addOnSuccessListener {
                DialogHelper.hideLoading()
                DialogHelper.showSuccess(requireContext(), "Success", "Item added successfully!") {
                    parentFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { e ->
                DialogHelper.hideLoading()
                DialogHelper.showError(requireContext(), "Error", "Failed to save: ${e.message}")
            }
    }
}
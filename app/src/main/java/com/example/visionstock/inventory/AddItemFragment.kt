package com.example.visionstock.inventory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.google.android.material.textfield.TextInputEditText

class AddItemFragment : Fragment(R.layout.fragment_add_item) {

    private lateinit var imgPreview: ImageView
    private lateinit var overlay: View // Reference to the red icon/text
    private var selectedImageUri: Uri? = null

    // 1. Setup Gallery Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data

            // UPDATE UI: Hide overlay and make image fill the box
            overlay.visibility = View.GONE
            imgPreview.apply {
                setPadding(0, 0, 0, 0)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(selectedImageUri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        imgPreview = view.findViewById(R.id.imgProductPreview)
        overlay = view.findViewById(R.id.layoutAddPhotoOverlay)
        val etId = view.findViewById<TextInputEditText>(R.id.etItemId)
        val etName = view.findViewById<TextInputEditText>(R.id.etItemName)
        val etDesc = view.findViewById<TextInputEditText>(R.id.etItemDesc)
        val etLoc = view.findViewById<TextInputEditText>(R.id.etItemLocation)

        view.findViewById<View>(R.id.rootLayout).setOnTouchListener { v, _ ->
            v.performClick() // Best practice for touch listeners
            v.requestFocus() // Steal focus from the text box

            // Close the keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            false
        }

        // 2. BACK BUTTON
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 3. OPEN GALLERY ON CLICK
        view.findViewById<CardView>(R.id.cardImagePicker).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // 4. SAVE BUTTON
        view.findViewById<Button>(R.id.btnSaveItem).setOnClickListener {
            val id = etId.text.toString().trim()
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val loc = etLoc.text.toString().trim()

            if (id.isEmpty() || name.isEmpty() || desc.isEmpty() || loc.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please add an image", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Item Added: $name", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }
}
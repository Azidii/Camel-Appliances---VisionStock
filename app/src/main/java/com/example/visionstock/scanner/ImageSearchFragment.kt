package com.example.visionstock.inventory

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.google.android.material.textfield.TextInputEditText

// Note: Ensure this matches your XML filename (fragment_search_inventory)
class ImageSearchFragment : Fragment(R.layout.fragment_image_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BIND VIEWS ---
        val etId = view.findViewById<TextInputEditText>(R.id.etSearchId)
        val etName = view.findViewById<TextInputEditText>(R.id.etSearchName)
        val etLoc = view.findViewById<TextInputEditText>(R.id.etSearchLocation)
        val btnSearch = view.findViewById<Button>(R.id.btnPerformSearch)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        // 1. BACK BUTTON
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. HIDE KEYBOARD ON BACKGROUND TAP
        // We attach the listener to the root view itself
        view.setOnClickListener {
            hideKeyboard(it)
        }

        // 3. SEARCH BUTTON
        btnSearch.setOnClickListener {
            val id = etId.text.toString().trim()
            val name = etName.text.toString().trim()
            val loc = etLoc.text.toString().trim()

            // Check if at least one field has text
            if (id.isEmpty() && name.isEmpty() && loc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter at least one search term", Toast.LENGTH_SHORT).show()
            } else {
                // Logic to filter the list will go here later
                Toast.makeText(requireContext(), "Searching...", Toast.LENGTH_SHORT).show()

                // Optional: Hide keyboard after search is clicked
                hideKeyboard(view)
            }
        }
    }

    // Helper function to hide keyboard
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}
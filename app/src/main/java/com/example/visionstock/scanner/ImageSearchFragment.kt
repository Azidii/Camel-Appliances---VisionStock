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

class ImageSearchFragment : Fragment(R.layout.fragment_image_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        val etId = view.findViewById<TextInputEditText>(R.id.etSearchId)
        val etName = view.findViewById<TextInputEditText>(R.id.etSearchName)
        val etDesc = view.findViewById<TextInputEditText>(R.id.etSearchDesc)
        val etLoc = view.findViewById<TextInputEditText>(R.id.etSearchLocation)

        // 1. BACK BUTTON
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. HIDE KEYBOARD ON BACKGROUND TAP
        view.findViewById<View>(R.id.rootLayout).setOnTouchListener { v, _ ->
            v.performClick()
            v.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            false
        }

        // 3. SEARCH BUTTON
        view.findViewById<Button>(R.id.btnDoSearch).setOnClickListener {
            val id = etId.text.toString().trim()
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val loc = etLoc.text.toString().trim()

            if (id.isEmpty() && name.isEmpty() && desc.isEmpty() && loc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter at least one search term", Toast.LENGTH_SHORT).show()
            } else {
                // Logic to filter the list will go here later
                Toast.makeText(requireContext(), "Searching...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
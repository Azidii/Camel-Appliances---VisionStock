package com.example.visionstock.inventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.item.InventoryItem
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.example.visionstock.result.ResultActivity
import com.example.visionstock.LoadingActivity

class ImageSearchFragment : Fragment(R.layout.fragment_image_search) {

    private val db = FirebaseFirestore.getInstance()

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

        // 2. HIDE KEYBOARD ON TAP
        view.setOnClickListener { hideKeyboard(it) }

        // 3. SEARCH BUTTON
        btnSearch.setOnClickListener {
            val idQuery = etId.text.toString().trim()
            val nameQuery = etName.text.toString().trim()
            val locQuery = etLoc.text.toString().trim()

            if (idQuery.isEmpty() && nameQuery.isEmpty() && locQuery.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter at least one search term", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(view)
                performSearch(idQuery, nameQuery, locQuery)
            }
        }
    }

    private fun performSearch(idQuery: String, nameQuery: String, locQuery: String) {
        // Show a brief dialog while fetching data from Firestore
        DialogHelper.showLoading(requireContext(), "Searching Inventory...")

        db.collection("inventory").get()
            .addOnSuccessListener { result ->
                var foundItem: InventoryItem? = null

                for (document in result) {
                    val itemID = document.getString("itemID") ?: ""
                    val itemName = document.getString("name") ?: ""
                    val itemLoc = document.getString("itemLocation") ?: ""

                    // Matching logic
                    val matchesId = idQuery.isEmpty() || itemID.equals(idQuery, ignoreCase = true)
                    val matchesName = nameQuery.isEmpty() || itemName.contains(nameQuery, ignoreCase = true)
                    val matchesLoc = locQuery.isEmpty() || itemLoc.contains(locQuery, ignoreCase = true)

                    if (matchesId && matchesName && matchesLoc) {
                        foundItem = InventoryItem(
                            documentId = document.id,
                            itemID = itemID,
                            name = itemName,
                            category = document.getString("itemCategory") ?: "Others",
                            quantity = document.getLong("quantity")?.toInt() ?: 0,
                            location = itemLoc,
                            imageUrl = document.getString("itemPicture") ?: ""
                        )
                        break
                    }
                }

                DialogHelper.hideLoading()

                if (foundItem != null) {
                    // --- MATCH SCANNER FLOW ---
                    // Navigate to LoadingActivity first to show Camel Appliances animation
                    val intent = Intent(requireContext(), LoadingActivity::class.java)

                    // Pass all database fields to be reflected in the result
                    intent.putExtra("scanned_image_uri", foundItem.imageUrl)
                    intent.putExtra("item_id", foundItem.itemID)
                    intent.putExtra("item_name", foundItem.name)
                    intent.putExtra("item_category", foundItem.category)
                    intent.putExtra("item_location", foundItem.location)
                    intent.putExtra("item_quantity", foundItem.quantity)

                    startActivity(intent)
                } else {
                    DialogHelper.showError(requireContext(), "Not Found", "No item matches your search criteria.")
                }
            }
            .addOnFailureListener { e ->
                DialogHelper.hideLoading()
                DialogHelper.showError(requireContext(), "Error", "Search failed: ${e.message}")
            }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}
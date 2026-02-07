package com.example.visionstock.mainpage

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.example.visionstock.history.HistoryFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. SET DYNAMIC DATE
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        // --- 2. DYNAMIC INVENTORY COUNT ---
        // Find the TextView for total items (Make sure ID matches your XML)
        val tvTotalItems = view.findViewById<TextView>(R.id.tvTotalStock)
        fetchInventoryCount(tvTotalItems)

        // --- ADMIN USER COUNT LOGIC ---
        val cardUserCount = view.findViewById<View>(R.id.cardUserCount)
        val tvTotalUsers = view.findViewById<TextView>(R.id.tvTotalUsers)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user")

        if (role == "admin") {
            cardUserCount.visibility = View.VISIBLE
            fetchActiveUserCount(tvTotalUsers)
        } else {
            cardUserCount.visibility = View.GONE
        }

        // 3. VIEW ALL CLICK -> OPEN HISTORY
        view.findViewById<View>(R.id.tvViewAll).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.content_frame, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // --- NEW: Fetch Total Inventory Amount ---
    private fun fetchInventoryCount(textView: TextView) {
        db.collection("inventory").get()
            .addOnSuccessListener { result ->
                // Reflects the amount of items inside the inventory database
                textView.text = result.size().toString()
            }
            .addOnFailureListener {
                textView.text = "0"
            }
    }

    // --- Count Only Active Users (Not Banned) ---
    private fun fetchActiveUserCount(textView: TextView) {
        db.collection("users")
            .whereNotEqualTo("status", "banned")
            .get()
            .addOnSuccessListener { result ->
                textView.text = result.size().toString()
            }
            .addOnFailureListener {
                textView.text = "!"
            }
    }
}
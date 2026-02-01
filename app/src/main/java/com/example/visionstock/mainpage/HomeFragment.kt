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

        // --- ADMIN USER COUNT LOGIC ---
        val cardUserCount = view.findViewById<View>(R.id.cardUserCount)
        val tvTotalUsers = view.findViewById<TextView>(R.id.tvTotalUsers)

        // Retrieve role
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user")

        if (role == "admin") {
            cardUserCount.visibility = View.VISIBLE
            fetchActiveUserCount(tvTotalUsers) // Call the updated function
        } else {
            cardUserCount.visibility = View.GONE
        }

        // 2. VIEW ALL CLICK -> OPEN HISTORY
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

    // --- UPDATED: Count Only Active Users (Not Banned) ---
    private fun fetchActiveUserCount(textView: TextView) {
        db.collection("users")
            .whereNotEqualTo("status", "banned") // <--- THIS FILTERS OUT BANNED USERS
            .get()
            .addOnSuccessListener { result ->
                textView.text = result.size().toString()
            }
            .addOnFailureListener {
                textView.text = "!"
            }
    }
}
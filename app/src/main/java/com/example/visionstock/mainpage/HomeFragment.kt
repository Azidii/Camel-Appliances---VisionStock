package com.example.visionstock.mainpage

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.adapter.RecentScanAdapter
import com.example.visionstock.adapter.ScanHistoryItem
import com.example.visionstock.history.HistoryFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recentScanAdapter: RecentScanAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. SET DYNAMIC DATE
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        // --- 2. DYNAMIC INVENTORY COUNT ---
        val tvTotalItems = view.findViewById<TextView>(R.id.tvTotalStock)
        fetchInventoryCount(tvTotalItems)

        // --- 3. DYNAMIC SCAN COUNT ---
        val tvTotalScans = view.findViewById<TextView>(R.id.tvTotalScans)
        fetchScanCount(tvTotalScans)

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

        // --- 4. RECENT SCANS RecyclerView ---
        val rvRecentScans = view.findViewById<RecyclerView>(R.id.rvRecentScans)
        val cardEmptyScans = view.findViewById<View>(R.id.cardEmptyScans)

        recentScanAdapter = RecentScanAdapter(emptyList())
        rvRecentScans.layoutManager = LinearLayoutManager(requireContext())
        rvRecentScans.adapter = recentScanAdapter

        // Fetch recent scans from Firestore
        fetchRecentScans(rvRecentScans, cardEmptyScans)

        // 5. VIEW ALL CLICK -> OPEN HISTORY
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

    // --- Fetch Total Inventory Amount ---
    private fun fetchInventoryCount(textView: TextView) {
        db.collection("inventory").get()
            .addOnSuccessListener { result ->
                textView.text = result.size().toString()
            }
            .addOnFailureListener {
                textView.text = "0"
            }
    }

    // --- Fetch Total Scan Count ---
    private fun fetchScanCount(textView: TextView) {
        db.collection("camel_scan_history").get()
            .addOnSuccessListener { result ->
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

    // --- Fetch Recent Scans (last 5, newest first) ---
    private fun fetchRecentScans(recyclerView: RecyclerView, emptyState: View) {
        db.collection("camel_scan_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshot ->
                val scanList = mutableListOf<ScanHistoryItem>()
                for (doc in snapshot) {
                    val item = ScanHistoryItem(
                        name = doc.getString("name") ?: "Unknown",
                        category = doc.getString("category") ?: "Others",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        itemId = doc.getString("itemId") ?: "N/A",
                        location = doc.getString("location") ?: "",
                        quantity = doc.getLong("quantity")?.toInt() ?: 0,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                    scanList.add(item)
                }

                if (scanList.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    recentScanAdapter.updateList(scanList)
                } else {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                recyclerView.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
    }
}
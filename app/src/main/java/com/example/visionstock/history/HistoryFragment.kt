package com.example.visionstock.history

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.adapter.RecentScanAdapter
import com.example.visionstock.adapter.ScanHistoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: RecentScanAdapter
    private var historyList: MutableList<ScanHistoryItem> = mutableListOf()
    private var allDocs: MutableList<String> = mutableListOf() // Firestore doc IDs for deletion

    private var isSelectionMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BIND VIEWS ---
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnCancelDelete = view.findViewById<ImageView>(R.id.btnCancelDelete)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnClearAll = view.findViewById<TextView>(R.id.btnClearAll)
        val normalActions = view.findViewById<LinearLayout>(R.id.normalActions)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)
        val fabDeleteAll = view.findViewById<FloatingActionButton>(R.id.fabDeleteAll)
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)

        // Initialize Adapter with empty list
        adapter = RecentScanAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // Fetch scan history from Firestore
        fetchScanHistory()

        // --- LISTENERS ---
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. FAB: CLEAR ALL CONFIRMATION
        fabDeleteAll.setOnClickListener {
            if (historyList.isEmpty()) {
                Toast.makeText(requireContext(), "History is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Clear All History")
                .setMessage("This will permanently delete ALL scan history. Are you sure?")
                .setPositiveButton("Delete All") { _, _ ->
                    deleteAllScanHistory()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnCancelDelete.setOnClickListener { exitSelectionMode() }

        // --- SEARCH LOGIC ---
        btnSearch.setOnClickListener {
            tvTitle.visibility = View.GONE
            normalActions.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE
            etSearchBar.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        btnCloseSearch.setOnClickListener {
            etSearchBar.text.clear()
            adapter.updateList(historyList)
            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            normalActions.visibility = View.VISIBLE
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = historyList.filter {
                    it.name.lowercase().contains(query) || it.category.lowercase().contains(query)
                }
                adapter.updateList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchScanHistory() {
        db.collection("camel_scan_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                historyList.clear()
                allDocs.clear()

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
                    historyList.add(item)
                    allDocs.add(doc.id)
                }

                adapter.updateList(historyList)
                toggleEmptyState(historyList.size)
            }
            .addOnFailureListener {
                toggleEmptyState(0)
            }
    }

    private fun deleteAllScanHistory() {
        val batch = db.batch()
        for (docId in allDocs) {
            batch.delete(db.collection("camel_scan_history").document(docId))
        }
        batch.commit()
            .addOnSuccessListener {
                historyList.clear()
                allDocs.clear()
                adapter.updateList(historyList)
                toggleEmptyState(0)
                Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to clear history", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        view?.findViewById<ImageView>(R.id.btnBack)?.visibility = View.VISIBLE
        view?.findViewById<ImageView>(R.id.btnCancelDelete)?.visibility = View.GONE
        view?.findViewById<LinearLayout>(R.id.normalActions)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.btnClearAll)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.tvTitle)?.text = "Recent Scans"
    }

    private fun toggleEmptyState(itemCount: Int) {
        val rvHistory = view?.findViewById<RecyclerView>(R.id.rvHistory)
        val emptyState = view?.findViewById<LinearLayout>(R.id.emptyState)
        val fabDeleteAll = view?.findViewById<FloatingActionButton>(R.id.fabDeleteAll)

        if (itemCount == 0) {
            rvHistory?.visibility = View.GONE
            emptyState?.visibility = View.VISIBLE
            fabDeleteAll?.visibility = View.GONE
        } else {
            rvHistory?.visibility = View.VISIBLE
            emptyState?.visibility = View.GONE
            fabDeleteAll?.visibility = View.VISIBLE
        }
    }
}
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
import com.example.visionstock.adapter.InventoryAdapter
import com.example.visionstock.item.InventoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var adapter: InventoryAdapter
    private var historyList: MutableList<InventoryItem> = mutableListOf()

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

        // --- DUMMY DATA (Categorized) ---
        if (historyList.isEmpty()) {
            historyList = mutableListOf(
                InventoryItem(itemID = "SCAN-001", name = "Scanned Item 1", category = "Raw Materials", quantity = 10),
                InventoryItem(itemID = "SCAN-002", name = "Scanned Item 2", category = "Electronic & Electrical Components", quantity = 5),
                InventoryItem(itemID = "SCAN-003", name = "Scanned Item 3", category = "Fasteners & Hardware", quantity = 20)
            )
        }

        // Initialize Adapter with the categorized logic implemented previously
        adapter = InventoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // Initial empty state check
        toggleEmptyState(historyList.size)

        // --- LISTENERS ---

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. FAB CLICK LOGIC (Dual Function)
        fabDeleteAll.setOnClickListener {
            if (adapter.isEmpty()) {
                Toast.makeText(requireContext(), "History is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isSelectionMode) {
                isSelectionMode = true

                // Update UI: Show Header Options
                btnBack.visibility = View.GONE
                btnCancelDelete.visibility = View.VISIBLE
                normalActions.visibility = View.GONE
                btnClearAll.visibility = View.VISIBLE
                tvTitle.text = "Select Items"

                adapter.toggleSelectionMode(true)
            } else {
                // STATE B: DELETE SELECTED ITEMS
                val selectedItems = adapter.getSelectedItems()

                if (selectedItems.isNotEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Selected")
                        .setMessage("Are you sure you want to delete ${selectedItems.size} items?")
                        .setPositiveButton("Delete") { _, _ ->
                            // This now references the new method in InventoryAdapter
                            adapter.deleteSelectedItems()

                            exitSelectionMode()
                            // Re-check empty state after deletion
                            toggleEmptyState(if(adapter.isEmpty()) 0 else 1)
                            Toast.makeText(requireContext(), "Selected items deleted", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "No items selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. CLEAR ALL CLICK (Full wipe)
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All History")
                .setMessage("This will permanently delete ALL items from history. Are you sure?")
                .setPositiveButton("Delete All") { _, _ ->
                    // This now references the new method in InventoryAdapter
                    adapter.deleteAllItems()
                    exitSelectionMode()
                    toggleEmptyState(0) // Explicitly show empty state
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
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
            adapter.filterList("")
            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            normalActions.visibility = View.VISIBLE
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        view?.findViewById<ImageView>(R.id.btnBack)?.visibility = View.VISIBLE
        view?.findViewById<ImageView>(R.id.btnCancelDelete)?.visibility = View.GONE
        view?.findViewById<LinearLayout>(R.id.normalActions)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.btnClearAll)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.tvTitle)?.text = "History"
        adapter.toggleSelectionMode(false)
    }

    private fun toggleEmptyState(itemCount: Int) {
        val rvHistory = view?.findViewById<RecyclerView>(R.id.rvHistory)
        val emptyState = view?.findViewById<LinearLayout>(R.id.emptyState)
        val fabDeleteAll = view?.findViewById<FloatingActionButton>(R.id.fabDeleteAll)

        if (itemCount == 0) {
            rvHistory?.visibility = View.GONE
            emptyState?.visibility = View.VISIBLE // Fixed unused reference
            fabDeleteAll?.visibility = View.GONE
        } else {
            rvHistory?.visibility = View.VISIBLE
            emptyState?.visibility = View.GONE
            fabDeleteAll?.visibility = View.VISIBLE
        }
    }
}
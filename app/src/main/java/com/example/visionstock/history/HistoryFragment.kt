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

    // Track selection mode state
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

        // The Red Trash Button (FAB)
        val fabDeleteAll = view.findViewById<FloatingActionButton>(R.id.fabDeleteAll)

        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)

        // --- DUMMY DATA ---
        if (historyList.isEmpty()) {
            historyList = mutableListOf(
                InventoryItem("Scanned Item 1", "SKU-001", 10),
                InventoryItem("Scanned Item 2", "SKU-002", 5),
                InventoryItem("Scanned Item 3", "SKU-003", 20)
            )
        }

        adapter = InventoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

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
                // STATE A: ENTER SELECTION MODE
                isSelectionMode = true

                // Update UI: Show Header Options
                btnBack.visibility = View.GONE
                btnCancelDelete.visibility = View.VISIBLE
                normalActions.visibility = View.GONE
                btnClearAll.visibility = View.VISIBLE
                tvTitle.text = "Select Items"

                // CRITICAL: Keep FAB Visible
                fabDeleteAll.visibility = View.VISIBLE

                // Show Checkboxes
                adapter.toggleSelectionMode(true)

            } else {
                // STATE B: DELETE SELECTED ITEMS (User clicked FAB again)
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Selected")
                    .setMessage("Are you sure you want to delete the selected items?")
                    .setPositiveButton("Delete") { _, _ ->
                        adapter.deleteSelectedItems()

                        // Exit selection mode after deleting
                        btnCancelDelete.performClick()

                        toggleEmptyState(if(adapter.isEmpty()) 0 else 1)
                        Toast.makeText(requireContext(), "Selected items deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        // 2. CLEAR ALL CLICK (Wipes Everything)
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All History")
                .setMessage("This will permanently delete ALL items from history. Are you sure?")
                .setPositiveButton("Delete All") { _, _ ->

                    // CALL DELETE ALL (Wipe everything)
                    adapter.deleteAllItems()

                    // Exit selection mode
                    btnCancelDelete.performClick()

                    toggleEmptyState(0)
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // 3. CANCEL / EXIT MODE (Click X)
        btnCancelDelete.setOnClickListener {
            isSelectionMode = false

            // Revert UI
            btnBack.visibility = View.VISIBLE
            btnCancelDelete.visibility = View.GONE
            normalActions.visibility = View.VISIBLE
            btnClearAll.visibility = View.GONE

            tvTitle.text = "History"

            // Hide Checkboxes
            adapter.toggleSelectionMode(false)
        }

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
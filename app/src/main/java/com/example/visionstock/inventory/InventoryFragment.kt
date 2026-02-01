package com.example.visionstock.inventory

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.adapter.InventoryAdapter
import com.example.visionstock.item.InventoryItem
import java.util.Locale

class InventoryFragment : Fragment(R.layout.activity_inventory) {

    private lateinit var adapter: InventoryAdapter
    // Regular users just view the list
    private var inventoryList: MutableList<InventoryItem> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. BIND VIEWS ---
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)

        // --- SEARCH BAR VIEWS ---
        val normalActions = view.findViewById<LinearLayout>(R.id.normalActions)
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvInventory)

        // --- 2. SETUP DATA & ADAPTER ---
        if (inventoryList.isEmpty()) {
            inventoryList = mutableListOf(
                InventoryItem("Samsung TV", "SAM-TV-55", 12),
                InventoryItem("Wireless Mouse", "LOGI-M-220", 45),
                InventoryItem("HDMI Cable", "CAB-HDMI-2M", 100),
                InventoryItem("Mechanical Keyboard", "KEY-MECH-RGB", 8)
            )
        }

        // Setup Adapter
        adapter = InventoryAdapter(inventoryList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Check Empty State
        toggleEmptyState(inventoryList.size)

        // --- 3. EVENT LISTENERS ---

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // --- SEARCH LOGIC ---

        // Open Search
        btnSearch.setOnClickListener {
            tvTitle.visibility = View.GONE
            normalActions?.visibility = View.GONE // Safe call ? just in case
            searchContainer.visibility = View.VISIBLE
            etSearchBar.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        // Close Search
        btnCloseSearch.setOnClickListener {
            etSearchBar.text.clear()
            adapter.filterList("") // Reset filter

            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            normalActions?.visibility = View.VISIBLE

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Type to Filter
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // --- HELPER FUNCTIONS ---
    private fun toggleEmptyState(itemCount: Int) {
        val rvInventory = view?.findViewById<RecyclerView>(R.id.rvInventory)
        val emptyState = view?.findViewById<LinearLayout>(R.id.emptyState)

        if (itemCount == 0) {
            rvInventory?.visibility = View.GONE
            emptyState?.visibility = View.VISIBLE
        } else {
            rvInventory?.visibility = View.VISIBLE
            emptyState?.visibility = View.GONE
        }
    }
}
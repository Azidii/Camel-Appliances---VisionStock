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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class AdminInventoryFragment : Fragment(R.layout.fragment_admin_inventory) {

    private lateinit var adapter: InventoryAdapter
    private lateinit var dummyList: List<InventoryItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        // Search Views
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)

        // List & Add Button
        val rvInventory = view.findViewById<RecyclerView>(R.id.rvInventory)
        val btnAddItem = view.findViewById<FloatingActionButton>(R.id.btnAddItem)

        // 1. BACK BUTTON
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 2: ADD ITEM BUTTON (Opens AddItemFragment)
        btnAddItem.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_up, R.anim.fade_out,
                    R.anim.fade_in, R.anim.slide_out_down
                )
                .replace(R.id.content_frame, AddItemFragment())
                .addToBackStack("AddItem")
                .commit()
        }

        // 3. SETUP RECYCLERVIEW
        dummyList = listOf(
            InventoryItem("Organic Quinoa", "SKU-91823", 45),
            InventoryItem("Apple iPhone 15", "SKU-11002", 12),
            InventoryItem("Logitech Mouse", "SKU-55421", 89),
            InventoryItem("Gaming Monitor", "SKU-77221", 5),
            InventoryItem("Office Chair", "SKU-33211", 20)
        )

        adapter = InventoryAdapter(dummyList)
        rvInventory.layoutManager = LinearLayoutManager(requireContext())
        rvInventory.adapter = adapter

        // 4. SHOW SEARCH BAR
        btnSearch.setOnClickListener {
            tvTitle.visibility = View.GONE
            btnSearch.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE

            etSearchBar.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        // 5. HIDE SEARCH BAR
        btnCloseSearch.setOnClickListener {
            etSearchBar.text.clear()
            adapter.filterList("") // Reset list

            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // 6. FILTER LOGIC
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
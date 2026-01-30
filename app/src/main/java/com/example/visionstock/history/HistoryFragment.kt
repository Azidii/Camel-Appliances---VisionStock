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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.inventory.InventoryAdapter // Reusing Adapter
import com.example.visionstock.inventory.InventoryItem
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var adapter: InventoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Setup Dummy History Data
        val historyList = listOf(
            InventoryItem("Scanned: Apple iPhone", "Today, 10:00 AM", 1),
            InventoryItem("Scanned: Keyboard", "Yesterday", 1)
        )

        adapter = InventoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // SHOW SEARCH
        btnSearch.setOnClickListener {
            tvTitle.visibility = View.GONE
            btnSearch.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE

            etSearchBar.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        // HIDE SEARCH
        view.findViewById<ImageView>(R.id.btnCloseSearch).setOnClickListener {
            etSearchBar.text.clear()
            adapter.filterList("")

            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // FILTER
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
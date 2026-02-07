package com.example.visionstock.mainpage

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.adapter.InventoryAdapter
import com.example.visionstock.item.InventoryItem
import com.google.firebase.firestore.FirebaseFirestore

class InventoryFragment : Fragment(R.layout.activity_inventory) {

    private lateinit var adapter: InventoryAdapter
    // Starts completely empty (No Dummy Data)
    private var inventoryList: MutableList<InventoryItem> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var normalActions: LinearLayout
    private lateinit var searchContainer: LinearLayout
    private lateinit var etSearchBar: EditText
    private lateinit var btnCloseSearch: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. INITIALIZATION (Order fixed to prevent crash) ---
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyState = view.findViewById(R.id.emptyState)
        tvTitle = view.findViewById(R.id.tvTitle)
        btnBack = view.findViewById(R.id.btnBack)
        btnSearch = view.findViewById(R.id.btnSearch)
        normalActions = view.findViewById(R.id.normalActions)
        searchContainer = view.findViewById(R.id.searchContainer)
        etSearchBar = view.findViewById(R.id.etSearchBar)
        btnCloseSearch = view.findViewById(R.id.btnCloseSearch)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvInventory)

        // --- 2. SETUP ADAPTER ---
        adapter = InventoryAdapter(inventoryList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // --- 3. FETCH DATA (Firestore only) ---
        fetchInventory()

        // --- 4. NAVIGATION & LISTENERS ---
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchContainer.visibility == View.VISIBLE) closeSearch()
                else { isEnabled = false; requireActivity().onBackPressed() }
            }
        })

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        swipeRefresh.setOnRefreshListener { fetchInventory() }
        btnSearch.setOnClickListener { openSearch() }
        btnCloseSearch.setOnClickListener { closeSearch() }

        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchInventory() {
        if (!swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = true

        db.collection("inventory").get()
            .addOnSuccessListener { result ->
                val newList = mutableListOf<InventoryItem>()
                for (document in result) {
                    newList.add(InventoryItem(
                        documentId = document.id,
                        itemID = document.getString("itemID") ?: "N/A",
                        name = document.getString("name") ?: "Unknown",
                        category = document.getString("itemCategory") ?: "Others",
                        quantity = document.getLong("quantity")?.toInt() ?: 0,
                        location = document.getString("itemLocation") ?: "",
                        imageUrl = document.getString("itemPicture") ?: ""
                    ))
                }
                adapter.updateList(newList)
                toggleEmptyState(newList.size)
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { e ->
                swipeRefresh.isRefreshing = false
                DialogHelper.showError(requireContext(), "Error", e.message ?: "Unknown error")
            }
    }

    private fun toggleEmptyState(itemCount: Int) {
        val rvInventory = view?.findViewById<RecyclerView>(R.id.rvInventory)
        if (itemCount == 0) {
            rvInventory?.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvInventory?.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun openSearch() {
        tvTitle.visibility = View.GONE; normalActions.visibility = View.GONE; searchContainer.visibility = View.VISIBLE
        etSearchBar.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeSearch() {
        etSearchBar.text.clear(); adapter.filterList("")
        searchContainer.visibility = View.GONE; tvTitle.visibility = View.VISIBLE; normalActions.visibility = View.VISIBLE
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
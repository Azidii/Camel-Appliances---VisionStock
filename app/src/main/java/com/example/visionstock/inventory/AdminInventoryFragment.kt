package com.example.visionstock.inventory

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.adapter.InventoryAdapter
import com.example.visionstock.item.InventoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminInventoryFragment : Fragment(R.layout.fragment_admin_inventory) {

    private lateinit var adapter: InventoryAdapter
    private var inventoryList: MutableList<InventoryItem> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private var isFabOpen = false
    private var isSelectionMode = false

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View

    // Search Views
    private lateinit var normalActions: LinearLayout
    private lateinit var searchContainer: LinearLayout
    private lateinit var etSearchBar: EditText
    private lateinit var btnCloseSearch: ImageView

    // FABs
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabDelete: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. INITIALIZE ALL VIEWS FIRST (Critical Crash Fix) ---
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyState = view.findViewById(R.id.emptyState)
        tvTitle = view.findViewById(R.id.tvTitle)
        btnBack = view.findViewById(R.id.btnBack)
        btnCancel = view.findViewById(R.id.btnCancelDelete)
        btnSearch = view.findViewById(R.id.btnSearch)

        normalActions = view.findViewById(R.id.normalActions)
        searchContainer = view.findViewById(R.id.searchContainer)
        etSearchBar = view.findViewById(R.id.etSearchBar)
        btnCloseSearch = view.findViewById(R.id.btnCloseSearch)

        fabMain = view.findViewById(R.id.fabSettings)
        fabAdd = view.findViewById(R.id.fabAdd)
        fabDelete = view.findViewById(R.id.fabDelete)

        val rvInventory = view.findViewById<RecyclerView>(R.id.rvInventory)

        // --- 2. SETUP ADAPTER ---
        adapter = InventoryAdapter(inventoryList)
        rvInventory.layoutManager = LinearLayoutManager(requireContext())
        rvInventory.adapter = adapter

        // --- 3. FETCH DATA (Safe now because views are initialized) ---
        fetchInventory()

        // --- 4. BACK BUTTON LOGIC ---
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else if (isFabOpen) {
                    closeFabMenu()
                } else if (searchContainer.visibility == View.VISIBLE) {
                    closeSearch()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        // --- 5. LISTENERS ---
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        swipeRefresh.setOnRefreshListener { fetchInventory() }
        btnSearch.setOnClickListener { openSearch() }
        btnCloseSearch.setOnClickListener { closeSearch() }
        btnCancel.setOnClickListener { exitSelectionMode() }

        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { adapter.filterList(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        // --- FAB LOGIC ---
        fabMain.setOnClickListener {
            if (isSelectionMode) {
                val selected = adapter.getSelectedItems()
                if (selected.isNotEmpty()) confirmDelete(selected)
                else DialogHelper.showError(requireContext(), "Selection Required", "Please select items.")
            } else {
                toggleFabMenu()
            }
        }

        fabAdd.setOnClickListener {
            closeFabMenu()
            // Get parent container ID dynamically
            val containerId = (requireView().parent as View).id

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(containerId, AddItemFragment())
                .addToBackStack(null)
                .commit()
        }

        fabDelete.setOnClickListener {
            closeFabMenu()
            if (!adapter.isEmpty()) enterSelectionMode()
            else Toast.makeText(requireContext(), "Inventory is empty.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FIRESTORE FUNCTIONS ---
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

                // --- THE CATEGORIZATION LOGIC ---
                // Sorting by category ensures the adapter can detect "Groups"
                val sortedList = newList.sortedBy { it.category }

                adapter.updateList(sortedList)
                toggleEmptyState(sortedList.size)
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { e ->
                swipeRefresh.isRefreshing = false
                DialogHelper.showError(requireContext(), "Error", e.message ?: "Unknown error")
            }
    }

    private fun confirmDelete(items: List<InventoryItem>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Items")
            .setMessage("Delete ${items.size} items?")
            .setPositiveButton("Delete") { _, _ ->
                DialogHelper.showLoading(requireContext(), "Deleting...")
                val batch = db.batch()
                for (item in items) {
                    val ref = db.collection("inventory").document(item.documentId)
                    batch.delete(ref)
                }
                batch.commit().addOnSuccessListener {
                    DialogHelper.hideLoading()
                    DialogHelper.showSuccess(requireContext(), "Success", "Items deleted.") {
                        exitSelectionMode()
                        fetchInventory()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- UI HELPERS ---
    private fun toggleEmptyState(count: Int) {
        val rv = view?.findViewById<RecyclerView>(R.id.rvInventory)
        if (count == 0) {
            rv?.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rv?.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun toggleFabMenu() { if (isFabOpen) closeFabMenu() else openFabMenu() }

    private fun openFabMenu() {
        isFabOpen = true
        ObjectAnimator.ofFloat(fabMain, "rotation", 0f, 45f).start()
        fabAdd.visibility = View.VISIBLE; fabAdd.alpha = 0f; fabAdd.translationY = 50f
        fabAdd.animate().translationY(0f).alpha(1f).setDuration(300).setInterpolator(OvershootInterpolator()).start()
        fabDelete.visibility = View.VISIBLE; fabDelete.alpha = 0f; fabDelete.translationY = 50f
        fabDelete.animate().translationY(0f).alpha(1f).setStartDelay(50).setDuration(300).setInterpolator(OvershootInterpolator()).start()
    }

    private fun closeFabMenu() {
        isFabOpen = false
        ObjectAnimator.ofFloat(fabMain, "rotation", 45f, 0f).start()
        fabAdd.animate().translationY(50f).alpha(0f).withEndAction { fabAdd.visibility = View.GONE }.start()
        fabDelete.animate().translationY(50f).alpha(0f).withEndAction { fabDelete.visibility = View.GONE }.start()
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        tvTitle.text = "Delete Items"
        btnBack.visibility = View.GONE; btnSearch.visibility = View.GONE; btnCancel.visibility = View.VISIBLE
        closeFabMenu()
        fabMain.setImageResource(R.drawable.ic_delete)
        fabMain.backgroundTintList = ColorStateList.valueOf(Color.RED)
        adapter.toggleSelectionMode(true)
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        tvTitle.text = "Admin Inventory"
        btnBack.visibility = View.VISIBLE; btnSearch.visibility = View.VISIBLE; btnCancel.visibility = View.GONE
        fabMain.setImageResource(R.drawable.ic_settings)
        fabMain.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#212121"))
        adapter.toggleSelectionMode(false)
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
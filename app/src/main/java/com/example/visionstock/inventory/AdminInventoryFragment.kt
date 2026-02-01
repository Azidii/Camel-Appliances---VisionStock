package com.example.visionstock.inventory

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.OvershootInterpolator
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

class AdminInventoryFragment : Fragment(R.layout.fragment_admin_inventory) {

    private var isFabOpen = false
    private var isSelectionMode = false

    private lateinit var adapter: InventoryAdapter
    private var inventoryList: MutableList<InventoryItem> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BIND VIEWS ---
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnCancelDelete = view.findViewById<ImageView>(R.id.btnCancelDelete)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnClearAll = view.findViewById<TextView>(R.id.btnClearAll)

        val normalActions = view.findViewById<LinearLayout>(R.id.normalActions)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearch = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)

        // FABs
        val fabSettings = view.findViewById<FloatingActionButton>(R.id.fabSettings)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        val fabDelete = view.findViewById<FloatingActionButton>(R.id.fabDelete)

        val rvInventory = view.findViewById<RecyclerView>(R.id.rvInventory)

        // Dummy Data
        if (inventoryList.isEmpty()) {
            inventoryList = mutableListOf(
                InventoryItem("Admin Item 1", "ADM-001", 100),
                InventoryItem("Admin Item 2", "ADM-002", 50)
            )
        }

        adapter = InventoryAdapter(inventoryList)
        rvInventory.layoutManager = LinearLayoutManager(requireContext())
        rvInventory.adapter = adapter

        toggleEmptyState(inventoryList.size)

        // --- LISTENERS ---

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // CANCEL DELETE MODE (X)
        btnCancelDelete.setOnClickListener {
            exitSelectionMode(fabSettings, fabAdd, fabDelete, view)
        }

        // DELETE ALL (Header Text)
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete All Items")
                .setMessage("This will permanently remove ALL items. Are you sure?")
                .setPositiveButton("Delete All") { _, _ ->
                    adapter.deleteAllItems()
                    exitSelectionMode(fabSettings, fabAdd, fabDelete, view)
                    toggleEmptyState(0)
                    Toast.makeText(requireContext(), "All items removed", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // --- FAB LOGIC ---
        fabSettings.setOnClickListener {
            if (isSelectionMode) {
                // DELETE SELECTED MODE
                if (adapter.isEmpty()) return@setOnClickListener

                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Selected")
                    .setMessage("Delete the selected items?")
                    .setPositiveButton("Delete") { _, _ ->
                        adapter.deleteSelectedItems()
                        exitSelectionMode(fabSettings, fabAdd, fabDelete, view)
                        toggleEmptyState(if(adapter.isEmpty()) 0 else 1)
                        Toast.makeText(requireContext(), "Selected items deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                // NORMAL MENU MODE
                if (isFabOpen) {
                    closeFabMenu(fabSettings, fabAdd, fabDelete)
                } else {
                    openFabMenu(fabSettings, fabAdd, fabDelete)
                }
            }
        }

        fabAdd.setOnClickListener {
            closeFabMenu(fabSettings, fabAdd, fabDelete)
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.content_frame, AddItemFragment())
                .addToBackStack(null)
                .commit()
        }

        fabDelete.setOnClickListener {
            closeFabMenu(fabSettings, fabAdd, fabDelete)

            // Double check (redundant but safe)
            if (adapter.isEmpty()) return@setOnClickListener

            isSelectionMode = true

            // Show Header Options
            btnBack.visibility = View.GONE
            btnCancelDelete.visibility = View.VISIBLE
            normalActions.visibility = View.GONE
            btnClearAll.visibility = View.VISIBLE
            tvTitle.text = "Select Items"

            // Switch Main FAB to Trash Icon
            fabSettings.setImageResource(R.drawable.ic_delete)

            adapter.toggleSelectionMode(true)
        }

        // --- SEARCH ---
        btnSearch.setOnClickListener {
            tvTitle.visibility = View.GONE
            btnSearch.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE
            etSearch.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)
        }

        btnCloseSearch.setOnClickListener {
            searchContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE
            etSearch.text.clear()
            adapter.filterList("")
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun exitSelectionMode(mainFab: FloatingActionButton, fabAdd: FloatingActionButton, fabDelete: FloatingActionButton, view: View) {
        isSelectionMode = false

        view.findViewById<ImageView>(R.id.btnBack).visibility = View.VISIBLE
        view.findViewById<ImageView>(R.id.btnCancelDelete).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.normalActions).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.btnClearAll).visibility = View.GONE
        view.findViewById<TextView>(R.id.tvTitle).text = "Admin Inventory"

        adapter.toggleSelectionMode(false)
        mainFab.setImageResource(R.drawable.ic_settings)

        isFabOpen = false
        mainFab.rotation = 0f
        fabAdd.visibility = View.INVISIBLE
        fabDelete.visibility = View.INVISIBLE
    }

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

    // --- UPDATED ANIMATIONS ---
    private fun openFabMenu(main: FloatingActionButton, add: FloatingActionButton, delete: FloatingActionButton) {
        isFabOpen = true
        ObjectAnimator.ofFloat(main, "rotation", 0f, 45f).start()

        // 1. Always Show ADD Button
        add.visibility = View.VISIBLE
        add.alpha = 0f
        add.translationY = 50f
        add.animate().translationY(0f).alpha(1f).setDuration(300).setInterpolator(OvershootInterpolator()).start()

        // 2. Only Show DELETE Button if List is NOT Empty
        if (!adapter.isEmpty()) {
            delete.visibility = View.VISIBLE
            delete.alpha = 0f
            delete.translationY = 50f
            delete.animate().translationY(0f).alpha(1f).setStartDelay(50).setDuration(300).setInterpolator(OvershootInterpolator()).start()
        }
    }

    private fun closeFabMenu(main: FloatingActionButton, add: FloatingActionButton, delete: FloatingActionButton) {
        isFabOpen = false
        ObjectAnimator.ofFloat(main, "rotation", 45f, 0f).start()

        // Hide Add
        add.animate().translationY(50f).alpha(0f).setDuration(300).withEndAction { add.visibility = View.INVISIBLE }.start()

        // Hide Delete (Always try to hide it, just in case)
        delete.animate().translationY(50f).alpha(0f).setDuration(300).withEndAction { delete.visibility = View.INVISIBLE }.start()
    }
}
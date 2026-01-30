package com.example.visionstock.inventory

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import java.util.Locale

class InventoryFragment : Fragment(R.layout.activity_inventory) {

    private lateinit var adapter: InventoryAdapter
    private lateinit var dummyList: List<InventoryItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- BIND VIEWS ---
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnSearch = view.findViewById<ImageView>(R.id.btnSearch)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        // Search Bar Views
        val searchContainer = view.findViewById<LinearLayout>(R.id.searchContainer)
        val etSearchBar = view.findViewById<EditText>(R.id.etSearchBar)
        val btnCloseSearch = view.findViewById<ImageView>(R.id.btnCloseSearch)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvInventory)

        // 1. SETUP BACK BUTTON
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. SETUP DATA & ADAPTER
        dummyList = listOf(
            InventoryItem("Organic Quinoa", "SKU-91823", 45),
            InventoryItem("Apple iPhone 15", "SKU-11002", 12),
            InventoryItem("Logitech Mouse", "SKU-55421", 89),
            InventoryItem("Gaming Monitor", "SKU-77221", 5),
            InventoryItem("Office Chair", "SKU-33211", 20),
            InventoryItem("USB-C Cable", "SKU-99882", 150),
            InventoryItem("Mechanical Keyboard", "SKU-12345", 8)
        )

        adapter = InventoryAdapter(dummyList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 3. SEARCH ICON CLICK -> SHOW BAR
        btnSearch.setOnClickListener {
            // Hide Title & Search Icon
            tvTitle.visibility = View.GONE
            btnSearch.visibility = View.GONE

            // Show Search Bar
            searchContainer.visibility = View.VISIBLE

            // Auto-focus and Show Keyboard
            etSearchBar.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT)
        }

        // 4. CLOSE ICON CLICK -> HIDE BAR
        btnCloseSearch.setOnClickListener {
            // Clear text and Restore List
            etSearchBar.text.clear()
            adapter.filterList("")

            // Hide Search Bar
            searchContainer.visibility = View.GONE

            // Show Title & Search Icon
            tvTitle.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE

            // Hide Keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // 5. TYPE TO SEARCH (Filtering Logic)
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

// --- DATA MODEL ---
data class InventoryItem(
    val name: String,
    val sku: String,
    val quantity: Int
)

// --- UPDATED ADAPTER WITH FILTERING ---
class InventoryAdapter(private var items: List<InventoryItem>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    // Keep a copy of the original full list
    private var fullList: List<InventoryItem> = ArrayList(items)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvSku: TextView = view.findViewById(R.id.tvItemSku)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvSku.text = item.sku
        // FIX: Removed " pcs" string to prevent duplication
        holder.tvQty.text = item.quantity.toString()
    }

    override fun getItemCount() = items.size

    // FILTER FUNCTION
    fun filterList(query: String) {
        val searchText = query.lowercase(Locale.getDefault())

        items = if (searchText.isEmpty()) {
            fullList // If empty, restore original list
        } else {
            fullList.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                        it.sku.lowercase(Locale.getDefault()).contains(searchText)
            }
        }
        notifyDataSetChanged() // Update UI
    }
}
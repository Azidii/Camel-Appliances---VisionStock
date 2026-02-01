package com.example.visionstock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.item.InventoryItem
import java.util.Locale

class InventoryAdapter(private var items: MutableList<InventoryItem>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    // Keep a copy of the full list for filtering searches
    private var fullList: MutableList<InventoryItem> = ArrayList(items)

    // SELECTION MODE VARIABLES
    private var isSelectionMode = false
    private val selectedItems = HashSet<Int>() // Stores positions of checked items

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvSku: TextView = view.findViewById(R.id.tvItemSku)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect) // The checkbox for delete mode
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
        holder.tvQty.text = item.quantity.toString()

        // --- HANDLE SELECTION MODE ---
        if (isSelectionMode) {
            holder.cbSelect.visibility = View.VISIBLE // Show checkbox
            holder.cbSelect.isChecked = selectedItems.contains(position)

            // Handle Checkbox Click
            holder.cbSelect.setOnClickListener {
                toggleSelection(position)
            }
            // Handle Row Click (toggles checkbox too)
            holder.itemView.setOnClickListener {
                holder.cbSelect.isChecked = !holder.cbSelect.isChecked
                toggleSelection(position)
            }
        } else {
            holder.cbSelect.visibility = View.GONE // Hide checkbox
            holder.itemView.setOnClickListener(null) // Disable click in normal mode
        }
    }

    override fun getItemCount() = items.size

    // --- NEW FUNCTIONS FOR DELETE MODE ---

    fun toggleSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        selectedItems.clear() // Reset selection
        notifyDataSetChanged() // Refresh UI to show/hide checkboxes
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
    }

    fun deleteSelectedItems() {
        // Remove items from the list (sorting descending to avoid index shifting)
        val sortedIndices = selectedItems.sortedDescending()
        for (index in sortedIndices) {
            if (index < items.size) {
                items.removeAt(index)
            }
        }
        // Sync fullList so search still works correctly
        fullList = ArrayList(items)

        // Exit selection mode
        toggleSelectionMode(false)
    }

    // Check if list is empty (Used to show "No History" text)
    fun isEmpty(): Boolean = items.isEmpty()

    // --- SEARCH FILTERING ---
    fun filterList(query: String) {
        val searchText = query.lowercase(Locale.getDefault())
        items = if (searchText.isEmpty()) {
            ArrayList(fullList)
        } else {
            fullList.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                        it.sku.lowercase(Locale.getDefault()).contains(searchText)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
    fun deleteAllItems() {
        items.clear()          // Clear the visible list
        fullList.clear()       // Clear the backup list (used for search)
        selectedItems.clear()  // Clear any selections
        notifyDataSetChanged() // Refresh UI
    }
}
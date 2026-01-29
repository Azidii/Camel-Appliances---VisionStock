package com.example.visionstock.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R

class InventoryFragment : Fragment(R.layout.activity_inventory) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. SETUP BACK BUTTON
        // Since we are in a fragment, we use popBackStack() to go back
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. SETUP RECYCLER VIEW
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 3. DUMMY DATA
        val dummyList = listOf(
            InventoryItem("Organic Quinoa", "SKU-91823", 45),
            InventoryItem("Apple iPhone 15", "SKU-11002", 12),
            InventoryItem("Logitech Mouse", "SKU-55421", 89),
            InventoryItem("Gaming Monitor", "SKU-77221", 5),
            InventoryItem("Office Chair", "SKU-33211", 20),
            InventoryItem("USB-C Cable", "SKU-99882", 150),
            InventoryItem("Mechanical Keyboard", "SKU-12345", 8)
        )

        // 4. ATTACH ADAPTER
        recyclerView.adapter = InventoryAdapter(dummyList)
    }
}

// --- DATA MODEL ---
data class InventoryItem(
    val name: String,
    val sku: String,
    val quantity: Int
)

// --- ADAPTER ---
class InventoryAdapter(private val items: List<InventoryItem>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

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
        holder.tvQty.text = item.quantity.toString()
    }

    override fun getItemCount() = items.size
}
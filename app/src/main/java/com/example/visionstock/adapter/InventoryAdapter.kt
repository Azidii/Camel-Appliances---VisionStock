package com.example.visionstock.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.item.InventoryItem
import java.util.Locale

// 1. Define View Types for Headers and Items
private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

class InventoryAdapter(
    private var allItems: MutableList<InventoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var fullList: MutableList<InventoryItem> = ArrayList(allItems)
    private var displayList: MutableList<Any> = mutableListOf()
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<InventoryItem>()

    init {
        organizeData(allItems)
    }

    private fun organizeData(items: List<InventoryItem>) {
        displayList.clear()

        val rawMaterials = mutableListOf<InventoryItem>()
        val electrical = mutableListOf<InventoryItem>()
        val mechanical = mutableListOf<InventoryItem>()
        val fasteners = mutableListOf<InventoryItem>()
        val consumables = mutableListOf<InventoryItem>()
        val others = mutableListOf<InventoryItem>()

        for (item in items) {
            when (item.category.trim()) { // Added trim to handle spacing issues
                "Raw Materials" -> rawMaterials.add(item)
                "Electronic & Electrical Components" -> electrical.add(item)
                "Mechanical Components" -> mechanical.add(item)
                "Fasteners & Hardware" -> fasteners.add(item)
                "Production Consumables" -> consumables.add(item)
                else -> others.add(item)
            }
        }

        addSection("Raw Materials", rawMaterials)
        addSection("Electronic & Electrical Components", electrical)
        addSection("Mechanical Components", mechanical)
        addSection("Fasteners & Hardware", fasteners)
        addSection("Production Consumables", consumables)
        addSection("Others", others)

        notifyDataSetChanged()
    }

    private fun addSection(title: String, items: List<InventoryItem>) {
        if (items.isNotEmpty()) {
            displayList.add("$title (${items.size})")
            displayList.addAll(items.sortedBy { it.name })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val title = displayList[position] as String
            holder.tvHeader.text = title
            holder.tvHeader.setTypeface(null, Typeface.BOLD)
            holder.tvHeader.setTextColor(Color.parseColor("#C8102E"))
            holder.tvHeader.setPadding(45, 50, 40, 20)
            holder.tvHeader.setBackgroundColor(Color.parseColor("#F5F5F5"))

        } else if (holder is ItemViewHolder) {
            val item = displayList[position] as InventoryItem

            holder.tvName.text = item.name
            holder.tvSku.text = "ID: ${item.itemID}"
            holder.tvQty.text = item.quantity.toString()

            // --- FEATURE: LOAD PRODUCT IMAGE FROM DATABASE ---
            if (item.imageUrl.isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.ivItemImage.setImageBitmap(bitmap)
                    holder.ivItemImage.imageTintList = null // Remove any icon tints
                } catch (e: Exception) {
                    holder.ivItemImage.setImageResource(R.drawable.ic_tag) // Fallback on error
                }
            } else {
                holder.ivItemImage.setImageResource(R.drawable.ic_tag)
            }

            if (isSelectionMode) {
                holder.cbSelect.visibility = View.VISIBLE
                holder.cbSelect.isChecked = selectedItems.contains(item)

                val toggleAction = {
                    if (selectedItems.contains(item)) selectedItems.remove(item)
                    else selectedItems.add(item)
                    notifyItemChanged(position)
                }

                holder.cbSelect.setOnClickListener { toggleAction() }
                holder.itemView.setOnClickListener { toggleAction() }
            } else {
                holder.cbSelect.visibility = View.GONE
                holder.itemView.setOnClickListener(null)
            }
        }
    }

    override fun getItemCount(): Int = displayList.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader: TextView = view.findViewById(android.R.id.text1)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage) // Bind new ImageView
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvSku: TextView = view.findViewById(R.id.tvItemSku)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    fun updateList(newList: List<InventoryItem>) {
        allItems = newList.toMutableList()
        fullList = ArrayList(newList)
        organizeData(allItems)
    }

    fun filterList(query: String) {
        val searchText = query.lowercase(Locale.getDefault())
        val filtered = if (searchText.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                        it.itemID.lowercase(Locale.getDefault()).contains(searchText)
            }
        }
        organizeData(filtered)
    }

    fun toggleSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun deleteSelectedItems() {
        allItems.removeAll(selectedItems)
        fullList.removeAll(selectedItems)
        selectedItems.clear()
        isSelectionMode = false
        organizeData(allItems)
    }

    fun deleteAllItems() {
        allItems.clear()
        fullList.clear()
        selectedItems.clear()
        organizeData(allItems)
    }

    fun getSelectedItems(): List<InventoryItem> = selectedItems.toList()
    fun isEmpty(): Boolean = allItems.isEmpty()
}
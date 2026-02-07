package com.example.visionstock.item

data class InventoryItem(
    val documentId: String = "", // Needed for Deletion
    val itemID: String = "",     // e.g. "INV-001"
    val name: String = "",       // e.g. "Steel Sheets"
    val category: String = "",   // e.g. "Raw Materials"
    val quantity: Int = 0,
    val location: String = "",   // e.g. "Shelf A-1"
    val imageUrl: String = ""    // URL for picture
)
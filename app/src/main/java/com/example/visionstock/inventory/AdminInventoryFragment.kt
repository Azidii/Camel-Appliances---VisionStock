package com.example.visionstock.inventory

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminInventoryFragment : Fragment(R.layout.fragment_admin_inventory) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. BACK BUTTON
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. RECYCLER VIEW (Same logic as User)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Using the same Adapter and Dummy Data for now
        val dummyList = listOf(
            InventoryItem("Organic Quinoa", "SKU-91823", 45),
            InventoryItem("Apple iPhone 15", "SKU-11002", 12),
            InventoryItem("Logitech Mouse", "SKU-55421", 89)
        )
        recyclerView.adapter = InventoryAdapter(dummyList)

        // 3. ADD BUTTON LOGIC
        // 3. ADD BUTTON LOGIC
        view.findViewById<FloatingActionButton>(R.id.btnAddItem).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_up, R.anim.fade_out,
                    R.anim.fade_in, R.anim.slide_out_down
                )
                .replace(R.id.content_frame, AddItemFragment()) // Opens the new UI
                .addToBackStack(null)
                .commit()
        }
    }
}
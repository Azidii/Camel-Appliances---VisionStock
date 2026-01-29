package com.example.visionstock.mainpage

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.visionstock.HomeFragment
import com.example.visionstock.R
import com.example.visionstock.login.LoginActivity
import com.example.visionstock.scanner.ScannerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Track the current tab ID
    private var lastTabId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 1. Startup Logic
        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "NONE")
            bottomNav.selectedItemId = R.id.nav_home
            lastTabId = R.id.nav_home
        }

        // 2. Uniform Navigation Logic
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == lastTabId) return@setOnItemSelectedListener true

            // Get the "Order" of the tabs (0, 1, 2)
            val currentOrder = getTabOrder(lastTabId)
            val newOrder = getTabOrder(item.itemId)

            // Determine Fragment to load
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_scanner -> ScannerFragment()
                R.id.nav_menu -> MenuFragment()
                else -> HomeFragment()
            }

            // Determine Direction:
            // If New > Current (e.g. 0 -> 1), we are moving "Forward" (Slide Left)
            // If New < Current (e.g. 1 -> 0), we are moving "Backward" (Slide Right)
            val direction = if (newOrder > currentOrder) "FORWARD" else "BACKWARD"

            loadFragment(fragment, direction)
            lastTabId = item.itemId
            true
        }
    }

    // Helper to define the physical order of tabs
    private fun getTabOrder(tabId: Int): Int {
        return when (tabId) {
            R.id.nav_home -> 0      // Left
            R.id.nav_scanner -> 1   // Center
            R.id.nav_menu -> 2      // Right
            else -> 0
        }
    }

    private fun loadFragment(fragment: Fragment, animType: String) {
        val transaction = supportFragmentManager.beginTransaction()

        when (animType) {
            "FORWARD" -> {
                // Moving Right (Enter from Right, Exit to Left)
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            "BACKWARD" -> {
                // Moving Left (Enter from Left, Exit to Right)
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            "NONE" -> {
                
            }
        }

        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
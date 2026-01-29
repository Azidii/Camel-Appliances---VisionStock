package com.example.visionstock.mainpage

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.example.visionstock.inventory.AdminInventoryFragment // Import Admin Fragment
import com.example.visionstock.inventory.InventoryFragment
import com.example.visionstock.result.HistoryFragment

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Logout
        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            (activity as? MainActivity)?.showLogoutDialog()
        }

        // 2. History
        view.findViewById<View>(R.id.menuHistory).setOnClickListener {
            openFragment(HistoryFragment())
        }

        // 3. INVENTORY (The Logic Switch)
        view.findViewById<View>(R.id.menuInventory).setOnClickListener {

            if (isAdmin()) {
                // GO TO ADMIN INVENTORY (Has '+' Button)
                openFragment(AdminInventoryFragment())
            } else {
                // GO TO REGULAR INVENTORY (View Only)
                openFragment(InventoryFragment())
            }
        }
    }

    // HELPER: Checks SharedPreferences for user type
    private fun isAdmin(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user") // Default to "user"
        return role == "admin"
    }

    // HELPER: To make the code cleaner
    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_up, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_down
            )
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }
}
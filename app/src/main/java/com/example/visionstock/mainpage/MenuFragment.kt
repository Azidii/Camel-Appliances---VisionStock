package com.example.visionstock.mainpage

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.example.visionstock.inventory.AdminInventoryFragment
import com.example.visionstock.mainpage.InventoryFragment
import com.example.visionstock.history.HistoryFragment
import com.example.visionstock.mainpage.UsersFragment

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. DISPLAY LOGGED-IN USERNAME & EMAIL ---
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = view.findViewById<TextView>(R.id.tvUserEmail) // <--- NEW: Find Email View

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Retrieve data saved during LoginActivity
        val username = sharedPref.getString("username", "User Name")
        val email = sharedPref.getString("email", "user@email.com") // <--- NEW: Get Email

        tvUserName.text = username
        tvEmail.text = email // <--- NEW: Set Email Text

        // 2. INVENTORY (Admin vs User logic)
        view.findViewById<View>(R.id.menuInventory).setOnClickListener {
            if (isAdmin()) {
                openFragment(AdminInventoryFragment())
            } else {
                openFragment(InventoryFragment())
            }
        }

        // 3. HISTORY
        view.findViewById<View>(R.id.menuHistory).setOnClickListener {
            openFragment(HistoryFragment())
        }

        // 4. MANAGE USERS (Only visible if role is "admin")
        val btnUsers = view.findViewById<View>(R.id.menuUsers)
        if (isAdmin()) {
            btnUsers.visibility = View.VISIBLE
            btnUsers.setOnClickListener {
                openFragment(UsersFragment())
            }
        } else {
            btnUsers.visibility = View.GONE
        }

        // 5. LOGOUT
        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            (activity as? MainActivity)?.showLogoutDialog()
        }
    }

    private fun isAdmin(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user")
        return role == "admin"
    }

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
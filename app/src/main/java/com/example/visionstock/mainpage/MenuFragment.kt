package com.example.visionstock.mainpage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.example.visionstock.result.HistoryFragment // Import your HistoryFragment

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Handle Logout
        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            (activity as? MainActivity)?.showLogoutDialog()
        }

        // 2. Handle History (UPDATED ANIMATION)
        view.findViewById<View>(R.id.menuHistory).setOnClickListener {
            parentFragmentManager.beginTransaction()
                // 1. Enter: History slides UP
                // 2. Exit: Menu fades OUT
                // 3. PopEnter: Menu fades IN (when clicking back)
                // 4. PopExit: History slides DOWN (when clicking back)
                .setCustomAnimations(
                    R.anim.slide_in_up,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out_down
                )
                .replace(R.id.content_frame, HistoryFragment())
                .addToBackStack(null) // Ensures the Back button works
                .commit()
        }
    }
}
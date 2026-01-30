package com.example.visionstock.mainpage

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.visionstock.R
import com.example.visionstock.history.HistoryFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. SET DYNAMIC DATE (World Time / Device Time)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        // 2. VIEW ALL CLICK -> OPEN HISTORY
        view.findViewById<View>(R.id.tvViewAll).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.content_frame, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
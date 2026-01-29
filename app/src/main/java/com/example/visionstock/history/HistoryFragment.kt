package com.example.visionstock.result

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.visionstock.R

// Notice: We inherit from Fragment, not AppCompatActivity
class HistoryFragment : Fragment(R.layout.fragment_history) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle Back Button
        // Instead of finish(), we use popBackStack() to go back to the Menu Fragment
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
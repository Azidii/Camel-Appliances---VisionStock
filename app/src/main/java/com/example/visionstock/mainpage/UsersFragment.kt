package com.example.visionstock.mainpage

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.adapter.UsersAdapter
import com.example.visionstock.item.UserItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersFragment : Fragment(R.layout.fragment_users) {

    private lateinit var adapter: UsersAdapter
    private var usersList: MutableList<UserItem> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private var isSelectionMode = false
    private var isMenuOpen = false
    private var currentAction = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvUsers = view.findViewById<RecyclerView>(R.id.rvUsers)
        val etSearch = view.findViewById<EditText>(R.id.etSearchUsers)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnCancel = view.findViewById<ImageView>(R.id.btnCancelDelete)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        val fabMain = view.findViewById<FloatingActionButton>(R.id.fabMain)
        val fabBan = view.findViewById<ExtendedFloatingActionButton>(R.id.fabBan)
        val fabUnban = view.findViewById<ExtendedFloatingActionButton>(R.id.fabUnban)

        adapter = UsersAdapter(usersList)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
        fetchUsersFromDatabase()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        })

        fabMain.setOnClickListener {
            if (isSelectionMode) {
                val selected = adapter.getSelectedItemsList()
                if (selected.isEmpty()) {
                    DialogHelper.showError(requireContext(), "Selection Required", "Please select users first.")
                } else {
                    confirmBatchAction(selected)
                }
            } else {
                toggleFabMenu(fabBan, fabUnban)
            }
        }

        fabBan.setOnClickListener {
            if (adapter.isEmpty()) return@setOnClickListener
            adapter.filterByStatus("active")
            currentAction = "BAN"
            enterSelectionMode(tvTitle, btnBack, btnCancel, fabMain, "Select Users to Ban")
            toggleFabMenu(fabBan, fabUnban)
        }

        fabUnban.setOnClickListener {
            if (adapter.isEmpty()) return@setOnClickListener
            adapter.filterByStatus("banned")
            currentAction = "UNBAN"
            enterSelectionMode(tvTitle, btnBack, btnCancel, fabMain, "Select Users to Unban")
            toggleFabMenu(fabBan, fabUnban)
        }

        btnCancel.setOnClickListener {
            exitSelectionMode(tvTitle, btnBack, btnCancel, fabMain)
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    // --- HELPER: Convert DP to Pixels (Ensures consistent spacing on all screens) ---
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    // --- ANIMATION LOGIC (Updated with wider spacing) ---
    private fun toggleFabMenu(fabBan: View, fabUnban: View) {
        if (isMenuOpen) {
            // CLOSE MENU (Slide Down)
            fabBan.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                fabBan.visibility = View.INVISIBLE
            }.start()

            fabUnban.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                fabUnban.visibility = View.INVISIBLE
            }.start()

            isMenuOpen = false
        } else {
            // OPEN MENU (Slide Up)
            fabBan.visibility = View.VISIBLE
            fabUnban.visibility = View.VISIBLE

            // Reset to starting position (hidden behind main button)
            fabBan.alpha = 0f
            fabUnban.alpha = 0f
            fabBan.translationY = 0f
            fabUnban.translationY = 0f

            // --- ANIMATION VALUES ---
            // Middle Button (BAN) - Moves up 80dp
            fabBan.animate()
                .translationY(-dpToPx(80f))
                .alpha(1f)
                .setDuration(300)
                .start()

            // Top Button (UNBAN) - Moves up 150dp (80 + 70 gap)
            fabUnban.animate()
                .translationY(-dpToPx(150f))
                .alpha(1f)
                .setDuration(300)
                .start()

            isMenuOpen = true
        }
    }

    private fun enterSelectionMode(tvTitle: TextView, btnBack: View, btnCancel: View, fabMain: FloatingActionButton, title: String) {
        isSelectionMode = true
        tvTitle.text = title
        btnBack.visibility = View.GONE
        btnCancel.visibility = View.VISIBLE
        fabMain.setImageResource(R.drawable.ic_check_circle)
        fabMain.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        adapter.toggleSelectionMode(true)
    }

    private fun exitSelectionMode(tvTitle: TextView, btnBack: View, btnCancel: View, fabMain: FloatingActionButton) {
        isSelectionMode = false
        currentAction = ""
        tvTitle.text = "Manage Users"
        btnBack.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        fabMain.setImageResource(R.drawable.ic_users)
        fabMain.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#212121"))
        adapter.toggleSelectionMode(false)
        adapter.resetFilter()
    }

    private fun confirmBatchAction(users: List<UserItem>) {
        val newStatus = if (currentAction == "BAN") "banned" else "active"
        val actionVerb = if (currentAction == "BAN") "Ban" else "Unban"
        val message = "Are you sure you want to $actionVerb ${users.size} users?"

        AlertDialog.Builder(requireContext())
            .setTitle("$actionVerb Users")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                val batch = db.batch()
                for (user in users) {
                    val ref = db.collection("users").document(user.userId)
                    batch.update(ref, "status", newStatus)
                }
                batch.commit()
                    .addOnSuccessListener {
                        DialogHelper.showSuccess(requireContext(), "Success", "Users updated.")
                        view?.let { v ->
                            exitSelectionMode(
                                v.findViewById(R.id.tvTitle),
                                v.findViewById(R.id.btnBack),
                                v.findViewById(R.id.btnCancelDelete),
                                v.findViewById(R.id.fabMain)
                            )
                        }
                        fetchUsersFromDatabase()
                    }
                    .addOnFailureListener { e ->
                        DialogHelper.showError(requireContext(), "Error", "Failed: ${e.message}")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchUsersFromDatabase() {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val newList = mutableListOf<UserItem>()
                for (document in result) {
                    newList.add(UserItem(
                        userId = document.id,
                        username = document.getString("username") ?: "Unknown",
                        password = "********",
                        status = document.getString("status") ?: "active"
                    ))
                }
                adapter.updateList(newList)
                val emptyView = view?.findViewById<LinearLayout>(R.id.emptyState)
                val rv = view?.findViewById<RecyclerView>(R.id.rvUsers)
                if (newList.isEmpty()) {
                    emptyView?.visibility = View.VISIBLE
                    rv?.visibility = View.GONE
                } else {
                    emptyView?.visibility = View.GONE
                    rv?.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                DialogHelper.showError(requireContext(), "Error", e.message ?: "Unknown error")
            }
    }
}
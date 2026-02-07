package com.example.visionstock.mainpage

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.adapter.UsersAdapter
import com.example.visionstock.item.UserItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class UsersFragment : Fragment(R.layout.fragment_users) {

    private lateinit var adapter: UsersAdapter
    private var usersList: MutableList<UserItem> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private var isSelectionMode = false
    private var isMenuOpen = false
    private var currentAction = ""

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: ImageView
    private lateinit var fabMain: FloatingActionButton
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Bind Views
        val rvUsers = view.findViewById<RecyclerView>(R.id.rvUsers)
        val etSearch = view.findViewById<EditText>(R.id.etSearchUsers)
        val fabBan = view.findViewById<ExtendedFloatingActionButton>(R.id.fabBan)
        val fabUnban = view.findViewById<ExtendedFloatingActionButton>(R.id.fabUnban)

        tvTitle = view.findViewById(R.id.tvTitle)
        btnBack = view.findViewById(R.id.btnBack)
        btnCancel = view.findViewById(R.id.btnCancelDelete)
        fabMain = view.findViewById(R.id.fabMain)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyState = view.findViewById(R.id.emptyState)

        // 2. Setup Adapter
        adapter = UsersAdapter(usersList)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter

        fetchUsersFromDatabase()

        // 3. Handle Device Back Button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        // 4. Refresh Logic
        swipeRefresh.setOnRefreshListener {
            fetchUsersFromDatabase()
        }

        // 5. Search Logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        })

        // 6. Button Listeners

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
            enterSelectionMode("Select Users to Ban")
            toggleFabMenu(fabBan, fabUnban)
        }

        fabUnban.setOnClickListener {
            if (adapter.isEmpty()) return@setOnClickListener
            adapter.filterByStatus("banned")
            currentAction = "UNBAN"
            enterSelectionMode("Select Users to Unban")
            toggleFabMenu(fabBan, fabUnban)
        }

        btnCancel.setOnClickListener {
            exitSelectionMode()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // --- HELPER FUNCTIONS ---

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    // --- FIXED: DRASTICALLY REDUCED VALUES FOR TIGHT STACK ---
    private fun toggleFabMenu(fabBan: View, fabUnban: View) {
        if (isMenuOpen) {
            // CLOSE MENU (Slide Down)
            fabBan.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                fabBan.visibility = View.INVISIBLE
            }.start()

            fabUnban.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                fabUnban.visibility = View.INVISIBLE
            }.start()

            fabMain.setImageResource(R.drawable.ic_users)
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

            // --- NEW VALUES (Compensating for XML margins) ---

            // 1. RED BUTTON (BAN) - Moves up only 15dp
            // This pulls it drastically closer to the X button.
            fabBan.animate()
                .translationY(-dpToPx(2f))
                .alpha(1f)
                .setDuration(300)
                .start()

            // 2. GREEN BUTTON (UNBAN) - Moves up 70dp
            // 15dp (Red Pos) + 55dp (Height + Gap)
            fabUnban.animate()
                .translationY(-dpToPx(70f))
                .alpha(1f)
                .setDuration(300)
                .start()

            // Change Main Icon to 'X'
            fabMain.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            isMenuOpen = true
        }
    }

    private fun enterSelectionMode(title: String) {
        isSelectionMode = true
        tvTitle.text = title

        btnBack.visibility = View.GONE
        btnCancel.visibility = View.VISIBLE

        // Force Icon to X
        btnCancel.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

        // Force Bottom Icon to Checkmark
        fabMain.setImageResource(R.drawable.ic_check_circle)
        fabMain.backgroundTintList = ColorStateList.valueOf(Color.BLACK)

        adapter.toggleSelectionMode(true)
    }

    private fun exitSelectionMode() {
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
                DialogHelper.showLoading(requireContext(), "Processing...")

                val batch = db.batch()
                for (user in users) {
                    val ref = db.collection("users").document(user.userId)
                    batch.update(ref, "status", newStatus)
                }
                batch.commit()
                    .addOnSuccessListener {
                        DialogHelper.hideLoading()
                        DialogHelper.showSuccess(requireContext(), "Success", "Users updated.") {
                            exitSelectionMode()
                            fetchUsersFromDatabase()
                        }
                    }
                    .addOnFailureListener { e ->
                        DialogHelper.hideLoading()
                        DialogHelper.showError(requireContext(), "Error", "Failed: ${e.message}")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchUsersFromDatabase() {
        if (!swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = true
        }

        db.collection("users").get()
            .addOnSuccessListener { result ->
                val newList = mutableListOf<UserItem>()
                for (document in result) {
                    newList.add(UserItem(
                        userId = document.id,
                        username = document.getString("username") ?: "Unknown",
                        password = document.getString("password") ?: "********",
                        status = document.getString("status") ?: "active"
                    ))
                }

                adapter.updateList(newList)

                // CRITICAL FIX: Re-apply filter if in selection mode to prevent reset
                if (isSelectionMode) {
                    if (currentAction == "BAN") adapter.filterByStatus("active")
                    else if (currentAction == "UNBAN") adapter.filterByStatus("banned")
                }

                val rv = view?.findViewById<RecyclerView>(R.id.rvUsers)
                if (newList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rv?.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rv?.visibility = View.VISIBLE
                }

                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { e ->
                swipeRefresh.isRefreshing = false
                DialogHelper.showError(requireContext(), "Error", e.message ?: "Unknown error")
            }
    }
}
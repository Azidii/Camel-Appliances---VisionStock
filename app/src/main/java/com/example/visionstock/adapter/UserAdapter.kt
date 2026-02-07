package com.example.visionstock.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.item.UserItem
import com.google.android.material.card.MaterialCardView

private const val TYPE_HEADER = 0
private const val TYPE_USER = 1

class UsersAdapter(
    private var allUsers: MutableList<UserItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Organized Data Lists
    private var displayList: MutableList<Any> = mutableListOf()
    private var activeUsers: MutableList<UserItem> = mutableListOf()
    private var bannedUsers: MutableList<UserItem> = mutableListOf()

    // Selection Mode
    var isSelectionMode = false
    private val selectedItems = mutableSetOf<UserItem>()

    init {
        organizeData(allUsers)
    }

    // --- LOGIC: Organize Users into Groups ---
    private fun organizeData(users: List<UserItem>) {
        activeUsers.clear()
        bannedUsers.clear()
        displayList.clear()

        // 1. Split users
        for (user in users) {
            if (user.status == "banned") bannedUsers.add(user) else activeUsers.add(user)
        }

        // 2. Build Display List
        if (activeUsers.isNotEmpty()) {
            displayList.add("Active Users (${activeUsers.size})")
            displayList.addAll(activeUsers)
        }

        if (bannedUsers.isNotEmpty()) {
            displayList.add("Banned Users (${bannedUsers.size})")
            displayList.addAll(bannedUsers)
        }
    }

    // --- ADAPTER OVERRIDES ---

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is String) TYPE_HEADER else TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            UserViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            // --- BIND HEADER ---
            val title = displayList[position] as String
            holder.tvHeader.text = title
            holder.tvHeader.setTypeface(null, Typeface.BOLD)
            holder.tvHeader.setTextColor(Color.DKGRAY)
            holder.tvHeader.setBackgroundColor(Color.TRANSPARENT)

            // --- ADDED PADDING HERE ---
            // Left, Top, Right, Bottom (in Pixels)
            // This moves the "Active Users" text away from the edge
            holder.tvHeader.setPadding(40, 50, 40, 20)

        } else if (holder is UserViewHolder) {
            // --- BIND USER ---
            val user = displayList[position] as UserItem

            holder.tvUsername.text = user.username
            holder.tvUserId.text = "ID: ${user.userId}"
            holder.tvPassword.text = "Pass: *******"

            // 1. PILL LOGIC
            if (user.status == "banned") {
                holder.tvStatusPill.text = "BANNED"
                holder.tvStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
            } else {
                holder.tvStatusPill.text = "ACTIVE"
                holder.tvStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }

            // 2. SELECTION LOGIC
            if (isSelectionMode) {
                holder.cbSelectUser.visibility = View.VISIBLE
                holder.cbSelectUser.isChecked = selectedItems.contains(user)
                holder.tvStatusPill.visibility = View.GONE

                if (selectedItems.contains(user)) {
                    holder.cardView.strokeWidth = 6
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                } else {
                    holder.cardView.strokeWidth = 0
                    holder.cardView.setCardBackgroundColor(Color.WHITE)
                }
            } else {
                holder.cbSelectUser.visibility = View.GONE
                holder.tvStatusPill.visibility = View.VISIBLE
                holder.cardView.strokeWidth = 0
                holder.cardView.setCardBackgroundColor(Color.WHITE)
            }

            // 3. CLICKS
            holder.itemView.setOnClickListener {
                if (isSelectionMode) holder.cbSelectUser.performClick()
            }
            holder.cbSelectUser.setOnClickListener {
                if (holder.cbSelectUser.isChecked) selectedItems.add(user) else selectedItems.remove(user)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = displayList.size

    // --- VIEW HOLDERS ---
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeader: TextView = itemView.findViewById(android.R.id.text1)
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)
        val tvStatusPill: TextView = itemView.findViewById(R.id.tvStatusPill)
        val cbSelectUser: CheckBox = itemView.findViewById(R.id.cbSelectUser)
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardUser)
    }

    // --- HELPER FUNCTIONS ---

    fun updateList(newList: List<UserItem>) {
        allUsers = newList.toMutableList()
        organizeData(allUsers)
        notifyDataSetChanged()
    }

    fun filterByStatus(status: String) {
        displayList.clear()
        if (status == "active") {
            displayList.add("Select Active Users")
            displayList.addAll(activeUsers)
        } else {
            displayList.add("Select Banned Users")
            displayList.addAll(bannedUsers)
        }
        notifyDataSetChanged()
    }

    fun resetFilter() {
        organizeData(allUsers)
        notifyDataSetChanged()
    }

    fun toggleSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemsList(): List<UserItem> = selectedItems.toList()
    fun isEmpty(): Boolean = allUsers.isEmpty()

    fun filter(text: String) {
        val query = text.lowercase().trim()
        val filtered = if (query.isEmpty()) allUsers else allUsers.filter { it.username.lowercase().contains(query) }
        organizeData(filtered)
        notifyDataSetChanged()
    }
}
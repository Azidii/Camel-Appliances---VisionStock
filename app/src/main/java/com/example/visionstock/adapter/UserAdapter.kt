package com.example.visionstock.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import com.example.visionstock.item.UserItem

class UsersAdapter(
    private var users: MutableList<UserItem>
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<UserItem>()
    private var usersFull: List<UserItem> = ArrayList(users)

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)
        val checkBox: CheckBox = itemView.findViewById(R.id.cbSelectUser)
        val ivUserIcon: ImageView = itemView.findViewById(R.id.ivUserIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        if (user.status == "banned") {
            holder.tvUsername.text = "${user.username} (BANNED)"
            holder.tvUsername.setTextColor(Color.RED)
        } else {
            holder.tvUsername.text = user.username
            holder.tvUsername.setTextColor(Color.BLACK)
        }

        holder.tvUserId.text = "ID: ${user.userId}"
        holder.tvPassword.text = "Pass: ${user.password}"
        holder.ivUserIcon.setImageResource(R.drawable.ic_users)

        holder.checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = selectedItems.contains(user)

        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked) selectedItems.add(user) else selectedItems.remove(user)
        }
        holder.itemView.setOnClickListener { if (isSelectionMode) holder.checkBox.performClick() }
    }

    override fun getItemCount(): Int = users.size

    fun updateList(newList: List<UserItem>) {
        users = newList.toMutableList()
        usersFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(text: String) {
        val query = text.lowercase().trim()
        users = if (query.isEmpty()) {
            usersFull.toMutableList()
        } else {
            usersFull.filter { it.username.lowercase().contains(query) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun filterByStatus(statusToKeep: String) {
        users.clear()
        if (statusToKeep == "active") {
            users.addAll(usersFull.filter { it.status != "banned" })
        } else {
            users.addAll(usersFull.filter { it.status == "banned" })
        }
        notifyDataSetChanged()
    }

    fun resetFilter() {
        users.clear()
        users.addAll(usersFull)
        notifyDataSetChanged()
    }

    fun toggleSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemsList(): List<UserItem> = selectedItems.toList()
    fun isEmpty(): Boolean = users.isEmpty()

    fun deleteSelectedItems() {
        users.removeAll(selectedItems)
        usersFull = usersFull.filterNot { selectedItems.contains(it) }
        selectedItems.clear()
        notifyDataSetChanged()
    }
}
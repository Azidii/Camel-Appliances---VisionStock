package com.example.visionstock.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.visionstock.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanHistoryItem(
    val name: String = "",
    val category: String = "",
    val timestamp: Long = 0L,
    val itemId: String = "",
    val location: String = "",
    val quantity: Int = 0,
    val imageUrl: String = ""
)

class RecentScanAdapter(
    private var scanList: List<ScanHistoryItem>
) : RecyclerView.Adapter<RecentScanAdapter.ScanViewHolder>() {

    class ScanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivScanImage: ImageView = view.findViewById(R.id.ivScanImage)
        val tvScanName: TextView = view.findViewById(R.id.tvScanName)
        val tvScanCategory: TextView = view.findViewById(R.id.tvScanCategory)
        val tvScanTime: TextView = view.findViewById(R.id.tvScanTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_scan, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val item = scanList[position]
        holder.tvScanName.text = item.name
        holder.tvScanCategory.text = item.category

        // Format timestamp to readable time
        if (item.timestamp > 0) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            holder.tvScanTime.text = sdf.format(Date(item.timestamp))
        } else {
            holder.tvScanTime.text = ""
        }

        // Load image (Base64, file URI, or fallback to logo)
        loadImage(holder.ivScanImage, item.imageUrl)
    }

    override fun getItemCount(): Int = scanList.size

    fun updateList(newList: List<ScanHistoryItem>) {
        scanList = newList
        notifyDataSetChanged()
    }

    private fun loadImage(imageView: ImageView, imageString: String) {
        if (imageString.isEmpty()) {
            imageView.setImageResource(R.drawable.logo)
            return
        }

        try {
            when {
                imageString.startsWith("file://") || imageString.startsWith("content://") -> {
                    // Load from URI
                    val uri = Uri.parse(imageString)
                    val inputStream = imageView.context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(R.drawable.logo)
                    }
                }
                imageString.startsWith("http") -> {
                    // Web URL — fallback to logo (no Glide/Picasso)
                    imageView.setImageResource(R.drawable.logo)
                }
                else -> {
                    // Decode as Base64
                    var cleanBase64 = imageString
                    val commaIndex = cleanBase64.indexOf(",")
                    if (commaIndex != -1) {
                        cleanBase64 = cleanBase64.substring(commaIndex + 1)
                    }
                    val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(R.drawable.logo)
                    }
                }
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.logo)
        }
    }
}

package com.example.visionstock

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

object DialogHelper {

    fun showSuccess(context: Context, title: String, message: String, onDismiss: () -> Unit = {}) {
        showDialog(context, title, message, true, onDismiss)
    }

    fun showError(context: Context, title: String, message: String) {
        showDialog(context, title, message, false)
    }

    private fun showDialog(context: Context, title: String, message: String, isSuccess: Boolean, onDismiss: () -> Unit = {}) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_status)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false) // User must click OK

        // Bind Views
        val tvTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        val btnOk = dialog.findViewById<Button>(R.id.btnDialogOk)
        val icon = dialog.findViewById<ImageView>(R.id.dialogIcon)

        // Set Text
        tvTitle.text = title
        tvMessage.text = message

        // Style based on Type
        if (isSuccess) {
            icon.setImageResource(R.drawable.ic_check_circle) // Ensure this drawable exists
            icon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            btnOk.setBackgroundColor(Color.parseColor("#4CAF50")) // Green Button
        } else {
            icon.setImageResource(R.drawable.ic_error) // Create an error icon drawable
            icon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            btnOk.setBackgroundColor(Color.parseColor("#C8102E")) // Red Button
        }

        btnOk.setOnClickListener {
            dialog.dismiss()
            onDismiss() // Run code after closing (e.g., move to next screen)
        }

        dialog.show()
    }
}
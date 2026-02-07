package com.example.visionstock.helper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.visionstock.R

object DialogHelper {

    private var loadingDialog: AlertDialog? = null

    // --- LOADING DIALOG (Fixed for Crashes) ---
    fun showLoading(context: Context, message: String) {
        try {
            // 1. Clean up old dialogs to prevent "Window Leaked" errors
            hideLoading()

            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setMessage(message)

            // 2. Padding for the spinner (As you requested)
            val progressBar = ProgressBar(context)
            progressBar.setPadding(50, 50, 50, 50) // Increased padding for better look
            builder.setView(progressBar)

            loadingDialog = builder.create()
            loadingDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace() // Log error but don't crash app
        }
    }

    fun hideLoading() {
        try {
            if (loadingDialog != null && loadingDialog!!.isShowing) {
                loadingDialog?.dismiss()
            }
            loadingDialog = null
        } catch (e: Exception) {
            loadingDialog = null
        }
    }

    // --- SUCCESS / ERROR DIALOGS ---

    fun showSuccess(context: Context, title: String, message: String, onDismiss: () -> Unit = {}) {
        showDialog(context, title, message, true, onDismiss)
    }

    fun showError(context: Context, title: String, message: String) {
        showDialog(context, title, message, false)
    }

    private fun showDialog(context: Context, title: String, message: String, isSuccess: Boolean, onDismiss: () -> Unit = {}) {
        try {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_status) // Ensure this XML exists!
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)

            // Use '?' (Safe Call) to prevent crash if ID is missing in XML
            val tvTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            val tvMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
            val btnOk = dialog.findViewById<Button>(R.id.btnDialogOk)
            val icon = dialog.findViewById<ImageView>(R.id.dialogIcon)

            tvTitle?.text = title
            tvMessage?.text = message

            if (isSuccess) {
                // SUCCESS: Green Theme
                icon?.setImageResource(R.drawable.ic_check_circle)
                icon?.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                btnOk?.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            } else {
                // ERROR: Red Theme
                icon?.setImageResource(R.drawable.ic_error)
                icon?.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                btnOk?.setBackgroundColor(Color.parseColor("#C8102E")) // Red
            }

            btnOk?.setOnClickListener {
                dialog.dismiss()
                onDismiss()
            }

            dialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: If custom dialog fails, show a standard system alert so the user still sees the message
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                    onDismiss()
                }
                .show()
        }
    }
}
package com.example.visionstock.helper

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.visionstock.R

object DialogHelper {
    private var loadingDialog: AlertDialog? = null

    fun showLoading(context: Context, message: String) {
        try {
            hideLoading()
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setMessage(message)
            val progressBar = ProgressBar(context)
            progressBar.setPadding(50, 50, 50, 50)
            builder.setView(progressBar)
            loadingDialog = builder.create()
            loadingDialog?.show()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun hideLoading() {
        try {
            if (loadingDialog != null && loadingDialog!!.isShowing) loadingDialog?.dismiss()
            loadingDialog = null
        } catch (e: Exception) { loadingDialog = null }
    }

    fun showSuccess(context: Context, title: String, message: String, onDismiss: () -> Unit = {}) {
        showDialog(context, title, message, true, onDismiss)
    }

    fun showError(context: Context, title: String, message: String, onDismiss: () -> Unit = {}) {
        showDialog(context, title, message, false, onDismiss)
    }

    private fun showDialog(context: Context, title: String, message: String, isSuccess: Boolean, onDismiss: () -> Unit = {}) {
        try {
            val dp = { value: Int ->
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
            }
            val accentColor = if (isSuccess) Color.parseColor("#4CAF50") else Color.parseColor("#C8102E")

            val container = LinearLayout(context)
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER
            container.setPadding(dp(28), dp(32), dp(28), dp(24))
            val cardBg = GradientDrawable()
            cardBg.setColor(Color.WHITE)
            cardBg.cornerRadius = dp(20).toFloat()
            container.background = cardBg

            val iconView = ImageView(context)
            val iconParams = LinearLayout.LayoutParams(dp(56), dp(56))
            iconParams.bottomMargin = dp(16)
            iconParams.gravity = Gravity.CENTER
            iconView.layoutParams = iconParams
            try {
                if (isSuccess) iconView.setImageResource(R.drawable.ic_check_circle)
                else iconView.setImageResource(R.drawable.ic_error)
                iconView.setColorFilter(accentColor)
            } catch (e: Exception) { }
            container.addView(iconView)

            val titleView = TextView(context)
            titleView.text = title
            titleView.setTextColor(Color.parseColor("#212121"))
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            titleView.setTypeface(null, Typeface.BOLD)
            titleView.gravity = Gravity.CENTER
            titleView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            container.addView(titleView)

            val messageView = TextView(context)
            messageView.text = message
            messageView.setTextColor(Color.parseColor("#757575"))
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            messageView.gravity = Gravity.CENTER
            val msgParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            msgParams.topMargin = dp(8)
            messageView.layoutParams = msgParams
            container.addView(messageView)

            val btnBg = GradientDrawable()
            btnBg.setColor(accentColor)
            btnBg.cornerRadius = dp(12).toFloat()
            val button = TextView(context)
            button.text = "Okay"
            button.setTextColor(Color.WHITE)
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            button.setTypeface(null, Typeface.BOLD)
            button.gravity = Gravity.CENTER
            button.background = btnBg
            button.setPadding(dp(16), dp(14), dp(16), dp(14))
            val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            btnParams.topMargin = dp(24)
            button.layoutParams = btnParams
            container.addView(button)

            val builder = AlertDialog.Builder(context)
            builder.setView(container)
            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            button.setOnClickListener { dialog.dismiss(); onDismiss() }
            dialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { d, _ -> d.dismiss(); onDismiss() }
                .show()
        }
    }
}

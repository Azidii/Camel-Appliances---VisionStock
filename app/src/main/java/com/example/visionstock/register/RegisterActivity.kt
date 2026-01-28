package com.example.visionstock.register

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.visionstock.R // Important: Manually imports R so it can see your layout

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Initialize Views
        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        // 2. Handle Register Button Click
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation Logic
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else {
                // Success
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                finish() // Closes Register page and returns to Login
            }
        }

        // 3. Handle Login Link Click
        tvLoginLink.setOnClickListener {
            finish() // Closes this page to reveal the Login page underneath
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
        }
    }
}
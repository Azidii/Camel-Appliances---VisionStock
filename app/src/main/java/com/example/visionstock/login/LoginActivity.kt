package com.example.visionstock.login // Keep your package name

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.mainpage.MainActivity
import com.example.visionstock.R
import com.google.android.material.textfield.TextInputEditText


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Initialize Views
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        // 2. Handle Login Button Click
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both Username and Password", Toast.LENGTH_SHORT).show()
            }
            else if (username == "alvin" && password == "12345") {
                // Success! Credentials match.
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Go to Main Activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close login page

                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
            }
            else {
                // Success! Navigate to Main App
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Closes Login page so user can't go back
            }
        }

        // 3. Handle "Create Account" Link Click
        tvCreateAccount.setOnClickListener {
            tvCreateAccount.setOnClickListener {
                val intent =
                    Intent(this, com.example.visionstock.register.RegisterActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
            }
        }
    }
}
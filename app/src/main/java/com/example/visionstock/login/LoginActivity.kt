package com.example.visionstock.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.mainpage.MainActivity
import com.example.visionstock.R
import com.example.visionstock.register.RegisterActivity
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

            // VALIDATION 1: Check for Empty Fields
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both Username and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CHECK 1: ADMIN ACCOUNT
            if (username == "admin" && password == "alain121004") {
                Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show()
                saveUserSession("admin") // Save role as Admin
                goToMainPage()
            }
            // CHECK 2: USER ACCOUNT (Hardcoded for now)
            else if (username == "alvin" && password == "12345") {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                saveUserSession("user") // Save role as User
                goToMainPage()
            }
            // VALIDATION 2: ACCOUNT DOES NOT EXIST
            else {
                // This stops the user from entering if credentials are wrong
                Toast.makeText(this, "Account does not exist or invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Handle "Create Account" Link Click
        tvCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
        }
    }

    // --- HELPER: Save User Role ---
    private fun saveUserSession(role: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("role", role)
        editor.apply()
    }

    // --- HELPER: Go to Main Page ---
    private fun goToMainPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
    }
}
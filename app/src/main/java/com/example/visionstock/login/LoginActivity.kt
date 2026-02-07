package com.example.visionstock.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.helper.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.mainpage.MainActivity
import com.example.visionstock.register.RegisterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        btnLogin.setOnClickListener {
            val usernameInput = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 1. INPUT VALIDATION (With Popup)
            if (usernameInput.isEmpty() || password.isEmpty()) {
                DialogHelper.showError(this, "Missing Info", "Please enter both username and password.")
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("username", usernameInput)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        DialogHelper.showError(this, "Login Failed", "Username not found.")
                    } else {
                        val document = documents.documents[0]
                        val email = document.getString("email") ?: ""
                        val role = document.getString("role") ?: "user"
                        val username = document.getString("username") ?: "User Name"

                        // 2. CHECK IF BANNED
                        val status = document.getString("status")
                        if (status == "banned") {
                            DialogHelper.showError(this, "Access Denied", "Your account has been suspended by the admin.")
                            return@addOnSuccessListener // Stop here
                        }

                        // 3. ATTEMPT LOGIN
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // 4. SUCCESS POPUP
                                    DialogHelper.showSuccess(this, "Welcome Back!", "Login Successful.") {
                                        // Run this ONLY after user clicks "Okay"
                                        saveUserSession(role, username, email)
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                } else {
                                    DialogHelper.showError(this, "Login Failed", "Invalid Password.")
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    DialogHelper.showError(this, "Connection Error", e.message ?: "Unknown error")
                }
        }

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // --- ADDED ANIMATION HERE ---
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun saveUserSession(role: String, username: String, email: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("role", role)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.apply()
    }
}
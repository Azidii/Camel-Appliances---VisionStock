package com.example.visionstock.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.DialogHelper
import com.example.visionstock.R
import com.example.visionstock.login.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            // 1. INPUT VALIDATION (With Popup)
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                DialogHelper.showError(this, "Missing Info", "Please fill in all fields.")
                return@setOnClickListener
            }

            // 2. PASSWORD CHECK (With Popup)
            if (password != confirm) {
                DialogHelper.showError(this, "Password Mismatch", "Passwords do not match.")
                return@setOnClickListener
            }

            // Disable button to prevent double-clicks
            btnRegister.isEnabled = false

            // 3. ATTEMPT REGISTRATION
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserToFirestore(username, email)
                    } else {
                        btnRegister.isEnabled = true
                        val errorMessage = task.exception?.message ?: "Registration Failed"
                        DialogHelper.showError(this, "Registration Failed", errorMessage)
                    }
                }
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            // --- ADDED ANIMATION HERE ---
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun saveUserToFirestore(username: String, email: String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "role" to "user",
            "status" to "active" // Default status so they aren't banned immediately
        )

        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                // 4. SUCCESS POPUP
                DialogHelper.showSuccess(this, "Success!", "Account created successfully!") {
                    // Navigate to Login ONLY after they click "Okay"
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                findViewById<Button>(R.id.btnRegister).isEnabled = true
                DialogHelper.showError(this, "Database Error", e.message ?: "Unknown error")
            }
    }
}
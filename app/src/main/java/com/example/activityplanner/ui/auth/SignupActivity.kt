package com.example.activityplanner.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.example.activityplanner.PasswordUtils

/**
 * SignupActivity handles user registration.
 * - Inserts new user into SQLite users table.
 * - Validates input (email, password length).
 */
class SignupActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabaseHelper
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var btnCreateAccount: Button

    private lateinit var btnGoLogin: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        db = TaskDatabaseHelper(this)

        // Initialize views

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etName = findViewById(R.id.etName) // Add this field in XML
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnGoLogin = findViewById(R.id.btnGoLogin)



        // Create account button
        btnCreateAccount.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = PasswordUtils.hashPassword(password)
            val result = db.insertUser(email, hashedPassword, name)
            if (result != -1L) {
                Toast.makeText(this, "Account created. Please login.", Toast.LENGTH_SHORT).show()
                finish() // Return to LoginActivity
            } else {
                Toast.makeText(this, "Signup failed. Email may already exist.", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to Login
        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}

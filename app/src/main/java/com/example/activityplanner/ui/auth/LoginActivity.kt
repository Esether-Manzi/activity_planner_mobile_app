package com.example.activityplanner.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.activityplanner.R
import com.example.activityplanner.db.TaskDatabaseHelper
import com.example.activityplanner.ui.tasks.TaskListActivity
import com.google.android.material.appbar.MaterialToolbar
import com.example.activityplanner.ui.ToolbarUtils
import com.example.activityplanner.session.SessionManager
import com.example.activityplanner.PasswordUtils

/**
 * LoginActivity handles user login.
 * - Validates credentials against SQLite users table.
 * - Navigates to TaskListActivity on success.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabaseHelper
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = TaskDatabaseHelper(this)

        // Initialize views

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoSignup = findViewById(R.id.btnGoSignup)



        // Login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = PasswordUtils.hashPassword(password)
            val valid = db.validateUser(email, hashedPassword)
            if (valid) {
                val user = db.getUserByEmail(email)
                if (user != null) {
                    val session = SessionManager(this)
                    session.saveUserSession(user["id"]!!.toInt(), email)
                }
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, TaskListActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }

        }

        // Go to signup
        btnGoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }
}

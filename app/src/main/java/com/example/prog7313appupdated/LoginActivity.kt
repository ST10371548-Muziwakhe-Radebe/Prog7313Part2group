package com.example.prog7313appupdated

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import com.example.prog7313appupdated.database.entities.User
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Tag for logging
    private val TAG = "LoginActivity"

    // UI elements
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "LoginActivity created")

        // Initialise UI elements
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Handle login button click
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            Log.d(TAG, "Login attempted for username: $username")

            // Validate input fields
            if (username.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Login failed - empty fields")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Query database for user credentials
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val user = db.userDao().login(username, password)

                runOnUiThread {
                    if (user != null) {
                        Log.d(TAG, "Login successful for userId: ${user.userId}")
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to main dashboard
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("userId", user.userId)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w(TAG, "Login failed - invalid credentials")
                        Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Handle register button click
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            Log.d(TAG, "Registration attempted for username: $username")

            // Validate input fields
            if (username.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Registration failed - empty fields")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if username already exists
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val existingUser = db.userDao().getUserByUsername(username)

                runOnUiThread {
                    if (existingUser != null) {
                        Log.w(TAG, "Registration failed - username already exists")
                        Toast.makeText(this@LoginActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        // Insert new user into database
                        lifecycleScope.launch {
                            db.userDao().insertUser(User(username = username, password = password))
                            Log.d(TAG, "New user registered: $username")
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example.prog7313appupdated

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

class SignupActivity : AppCompatActivity() {

    private val TAG = "SignupActivity"
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etUsername = findViewById(R.id.etSignupUsername)
        etPassword = findViewById(R.id.etSignupPassword)
        btnSignup = findViewById(R.id.btnSignup)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val existingUser = db.userDao().getUserByUsername(username)

                if (existingUser != null) {
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    db.userDao().insertUser(User(username = username, password = password))
                    Log.d(TAG, "New user registered: $username")
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }
}
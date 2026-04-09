package com.example.prog7313appupdated

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private val PREFS_NAME = "MoneyGoalsPrefs"
    private val KEY_USER_ID = "userId"
    private val KEY_REMEMBER_ME = "rememberMe"

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var cbRememberMe: CheckBox
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check if user is already logged in
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
            if (userId != -1) {
                navigateToDashboard(userId)
                return
            }
        }

        setContentView(R.layout.activity_login)
        Log.d(TAG, "LoginActivity created")

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = com.example.prog7313appupdated.utils.EncryptionUtils.hashPassword(password)

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val user = db.userDao().login(username, hashedPassword)

                if (user != null) {
                    if (cbRememberMe.isChecked) {
                        sharedPreferences.edit().apply {
                            putInt(KEY_USER_ID, user.userId)
                            putBoolean(KEY_REMEMBER_ME, true)
                            apply()
                        }
                    }
                    runOnUiThread {
                        navigateToDashboard(user.userId)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToDashboard(userId: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }
}
package com.example.prog7313appupdated

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnReset: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etUsername = findViewById(R.id.etResetUsername)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnReset = findViewById(R.id.btnResetPassword)
        btnBack = findViewById(R.id.btnBackToLoginFromReset)

        btnReset.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            if (username.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val user = db.userDao().getUserByUsername(username)

                if (user != null) {
                    val updatedUser = user.copy(password = newPassword)
                    db.userDao().updateUser(updatedUser)
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
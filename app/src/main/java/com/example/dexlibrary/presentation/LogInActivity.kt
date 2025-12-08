package com.example.dexlibrary.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.LoginRequest
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.data.storage.TokenManager
import kotlinx.coroutines.launch
import java.io.IOException

class LogInActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "LogInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)

        tokenManager = TokenManager(applicationContext)

        val userLogin = findViewById<EditText>(R.id.loginEditText)
        val userPassword = findViewById<EditText>(R.id.passwordEditText)
        val button = findViewById<Button>(R.id.loginButton)
        val signUpText = findViewById<TextView>(R.id.registerPromptTextView)

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()
            if (login.isNotEmpty() && password.isNotEmpty()) {
                Log.i(TAG, "Login button clicked for user: $login")
                val loginRequest = LoginRequest(username = login, password = password)
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Attempting to login...")
                        val response = RetrofitClient.apiService.login(loginRequest)
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            if (authResponse != null) {
                                tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                                DataManager.fetchAllData(authResponse.access)

                                val intent = Intent(this@LogInActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)

                                Toast.makeText(this@LogInActivity, "Login Successful: ${authResponse.message}", Toast.LENGTH_LONG).show()
                            } else {
                                Log.e(TAG, "Login failed: Response body is null")
                                Toast.makeText(this@LogInActivity, "Login failed: Empty response", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Login failed with code ${response.code()}: $errorBody")
                            Toast.makeText(this@LogInActivity, "Login failed: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Network error during login", e)
                        Toast.makeText(this@LogInActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "An unexpected error occurred during login", e)
                        Toast.makeText(this@LogInActivity, "An unexpected error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.w(TAG, "Login attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
        }
    }
}

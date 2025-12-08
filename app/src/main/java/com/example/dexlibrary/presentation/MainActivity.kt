package com.example.dexlibrary.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dexlibrary.R
import com.example.dexlibrary.data.DataManager
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(applicationContext)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_search, R.id.navigation_main_menu, R.id.navigation_favorites, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        loadAllData()
    }

    private fun loadAllData() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                Log.e(TAG, "Access token is null")
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                val booksResponse = RetrofitClient.apiService.getBooks(authHeader)
                if (booksResponse.isSuccessful) {
                    DataManager.books.clear()
                    DataManager.books.addAll(booksResponse.body() ?: emptyList())
                }

                val reservationsResponse = RetrofitClient.apiService.getMyReservations(authHeader)
                if (reservationsResponse.isSuccessful) {
                    DataManager.reservations.clear()
                    DataManager.reservations.addAll(reservationsResponse.body() ?: emptyList())
                }

                val borrowsResponse = RetrofitClient.apiService.getMyBorrows(authHeader)
                if (borrowsResponse.isSuccessful) {
                    DataManager.borrows.clear()
                    DataManager.borrows.addAll(borrowsResponse.body() ?: emptyList())
                }

                val favoritesResponse = RetrofitClient.apiService.getFavorites(authHeader)
                if (favoritesResponse.isSuccessful) {
                    DataManager.favorites.clear()
                    DataManager.favorites.addAll(favoritesResponse.body() ?: emptyList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
            }
        }
    }
}

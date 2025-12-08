package com.example.dexlibrary.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.DataManager
import com.example.dexlibrary.data.model.Favorite
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.presentation.adapters.FavoriteAdapter
import kotlinx.coroutines.launch
import java.io.IOException

class FavoritesFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val TAG = "FavoritesFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        tokenManager = TokenManager(requireContext())
        setupRecyclerView(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        favoriteAdapter.updateFavorites(DataManager.favorites)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.favorites_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoriteAdapter = FavoriteAdapter(mutableListOf()) { favorite ->
            handleFavoriteClick(favorite)
        }
        recyclerView.adapter = favoriteAdapter
    }

    private fun handleFavoriteClick(favorite: Favorite) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                Toast.makeText(requireContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                val response = RetrofitClient.apiService.removeFromFavorites(authHeader, favorite.book.id)
                if (response.isSuccessful) {
                    DataManager.favorites.remove(favorite)
                    val bookInGeneralList = DataManager.books.find { it.id == favorite.book.id }
                    bookInGeneralList?.is_favorite = false
                    favoriteAdapter.updateFavorites(DataManager.favorites)
                }
            } catch (e: IOException) {e.printStackTrace() }
        }
    }
}
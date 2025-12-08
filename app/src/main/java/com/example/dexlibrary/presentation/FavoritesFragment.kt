package com.example.dexlibrary.presentation

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
import com.example.dexlibrary.data.model.Favorite
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.presentation.adapters.FavoriteAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var tokenManager: TokenManager
    private val TAG = "FavoritesFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        recyclerView = view.findViewById(R.id.recycler_view_favorites)

        favoriteAdapter = FavoriteAdapter(
            mutableListOf(),
            onFavoriteClick = { favorite -> handleFavoriteClick(favorite) },
            onItemClick = { favorite -> showBookDetails(favorite) }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = favoriteAdapter

        observeFavorites()
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.favorites.collectLatest { favorites ->
                favoriteAdapter.updateFavorites(favorites)
            }
        }
    }

    private fun showBookDetails(favorite: Favorite) {
        val dialogFragment = BookDetailDialogFragment.newInstance(favorite.book)
        dialogFragment.show(parentFragmentManager, "BookDetailDialog")
    }

    private fun handleFavoriteClick(favorite: Favorite) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                val response = RetrofitClient.apiService.removeFromFavorites(authHeader, favorite.book.id)
                if (response.isSuccessful) {
                    Log.i(TAG, "Book ${favorite.book.id} removed from favorites")
                    DataManager.refreshBooksAndFavorites(token)
                } else {
                    Log.e(TAG, "Failed to remove from favorites: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling favorite click", e)
            }
        }
    }
}

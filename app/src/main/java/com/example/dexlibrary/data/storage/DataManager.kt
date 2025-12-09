package com.example.dexlibrary.data.storage

import android.util.Log
import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.Borrow
import com.example.dexlibrary.data.model.Favorite
import com.example.dexlibrary.data.model.Reservation
import com.example.dexlibrary.data.model.User
import com.example.dexlibrary.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DataManager {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books = _books.asStateFlow()

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations = _reservations.asStateFlow()

    private val _borrows = MutableStateFlow<List<Borrow>>(emptyList())
    val borrows = _borrows.asStateFlow()

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites = _favorites.asStateFlow()

    suspend fun fetchAllData(token: String) {
        val authHeader = "Bearer $token"
        coroutineScope {
            try {
                val userDeferred = async { RetrofitClient.apiService.getProfile(authHeader) }
                val booksDeferred = async { RetrofitClient.apiService.getBooks(authHeader) }
                val reservationsDeferred = async { RetrofitClient.apiService.getMyReservations(authHeader) }
                val borrowsDeferred = async { RetrofitClient.apiService.getMyBorrows(authHeader) }
                val favoritesDeferred = async { RetrofitClient.apiService.getFavorites(authHeader) }

                val userResponse = userDeferred.await()
                val booksResponse = booksDeferred.await()
                val reservationsResponse = reservationsDeferred.await()
                val borrowsResponse = borrowsDeferred.await()
                val favoritesResponse = favoritesDeferred.await()

                if (userResponse.isSuccessful) _user.value = userResponse.body()
                if (reservationsResponse.isSuccessful) _reservations.value = reservationsResponse.body() ?: emptyList()
                if (borrowsResponse.isSuccessful) _borrows.value = borrowsResponse.body() ?: emptyList()

                if (booksResponse.isSuccessful && favoritesResponse.isSuccessful) {
                    val allBooks = booksResponse.body() ?: emptyList()
                    val favoriteItems = favoritesResponse.body() ?: emptyList()
                    _favorites.value = favoriteItems

                    val favoriteBookIds = favoriteItems.map { it.book.id }.toSet()

                    val correctedBooks = allBooks.map {
                        it.copy(is_favorite = favoriteBookIds.contains(it.id))
                    }
                    _books.value = correctedBooks
                } else {
                    Log.e("DataManager", "Failed to fetch books or favorites during full fetch")
                }

            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data", e)
            }
        }
    }

    suspend fun refreshBooksAndFavorites(token: String) {
        val authHeader = "Bearer $token"
        coroutineScope {
            try {
                val booksDeferred = async { RetrofitClient.apiService.getBooks(authHeader) }
                val favoritesDeferred = async { RetrofitClient.apiService.getFavorites(authHeader) }

                val booksResponse = booksDeferred.await()
                val favoritesResponse = favoritesDeferred.await()

                if (booksResponse.isSuccessful && favoritesResponse.isSuccessful) {
                    val allBooks = booksResponse.body() ?: emptyList()
                    val favoriteItems = favoritesResponse.body() ?: emptyList()
                    _favorites.value = favoriteItems

                    val favoriteBookIds = favoriteItems.map { it.book.id }.toSet()

                    val correctedBooks = allBooks.map { book ->
                        book.copy(is_favorite = favoriteBookIds.contains(book.id))
                    }
                    _books.value = correctedBooks
                } else {
                    Log.e("DataManager", "Failed to refresh books or favorites")
                }
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing books and favorites", e)
            }
        }
    }

    fun clearData() {
        _user.value = null
        _books.value = emptyList()
        _reservations.value = emptyList()
        _borrows.value = emptyList()
        _favorites.value = emptyList()
    }
}

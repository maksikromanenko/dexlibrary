package com.example.dexlibrary.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.FavoriteRequestBody
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.presentation.adapters.BookAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var isbnSearchButton: Button
    private var allBooks: List<Book> = listOf()
    private val TAG = "SearchFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.recycler_view_books)
        isbnSearchButton = view.findViewById(R.id.isbn_search_button)

        bookAdapter = BookAdapter(mutableListOf(),
            onFavoriteClick = { book -> handleFavoriteClick(book) },
            onItemClick = { book -> showBookDetails(book) }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = bookAdapter

        isbnSearchButton.setOnClickListener { showIsbnSearchDialog() }

        setupSearch()
        observeBooks()
    }

    private fun showIsbnSearchDialog() {
        val dialog = IsbnSearchDialogFragment()
        dialog.show(parentFragmentManager, "IsbnSearchDialog")
    }

    private fun showBookDetails(book: Book) {
        val dialogFragment = BookDetailDialogFragment.newInstance(book)
        dialogFragment.show(parentFragmentManager, "BookDetailDialog")
    }

    private fun handleFavoriteClick(book: Book) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                if (book.is_favorite) {
                    val response = RetrofitClient.apiService.removeFromFavorites(authHeader, book.id)
                    if (response.isSuccessful) {
                        Log.i(TAG, "Book ${book.id} removed from favorites")
                        DataManager.refreshBooksAndFavorites(token)
                    } else {
                        Log.e(TAG, "Failed to remove from favorites: ${response.errorBody()?.string()}")
                    }
                } else {
                    val response = RetrofitClient.apiService.addToFavorites(authHeader, FavoriteRequestBody(book.id))
                    if (response.isSuccessful) {
                        Log.i(TAG, "Book ${book.id} added to favorites")
                        DataManager.refreshBooksAndFavorites(token)
                    } else {
                        Log.e(TAG, "Failed to add to favorites: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling favorite click", e)
            }
        }
    }


    private fun observeBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.books.collectLatest { books ->
                allBooks = books
                filterBooks(searchView.query.toString())
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBooks(newText)
                return true
            }
        })
    }

    private fun filterBooks(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allBooks
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            allBooks.filter {
                it.title.lowercase().contains(lowerCaseQuery) || it.author_name?.lowercase()?.contains(lowerCaseQuery) == true
            }
        }
        bookAdapter.updateBooks(filteredList)
    }
}

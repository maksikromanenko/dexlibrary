package com.example.dexlibrary.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.DataManager
import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.FavoriteRequestBody
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.presentation.adapters.BookAdapter
import kotlinx.coroutines.launch
import java.io.IOException

class SearchFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchEditText: EditText
    private val TAG = "SearchFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        tokenManager = TokenManager(requireContext())
        searchEditText = view.findViewById(R.id.search_edit_text)
        setupRecyclerView(view)
        setupSearch()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update books from DataManager, as favorites might have changed
        bookAdapter.updateBooks(DataManager.books.sortedBy { it.title })
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.books_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(
            mutableListOf(),
            { book -> handleFavoriteClick(book) },
            { book -> handleItemClick(book) }
        )
        recyclerView.adapter = bookAdapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBooks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterBooks(query: String) {
        val filteredBooks = DataManager.books.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
                    book.author_name.contains(query, ignoreCase = true)
        }.sortedBy { it.title }
        bookAdapter.updateBooks(filteredBooks)
    }

    private fun handleItemClick(book: Book) {
        val action = SearchFragmentDirections.actionSearchFragmentToBookDetailDialogFragment(book)
        findNavController().navigate(action)
    }

    private fun handleFavoriteClick(book: Book) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                Toast.makeText(requireContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                if (book.is_favorite) {
                    // Remove from favorites
                    val response = RetrofitClient.apiService.removeFromFavorites(authHeader, book.id)
                    if (response.isSuccessful) {
                        book.is_favorite = false
                        DataManager.favorites.removeAll { it.book.id == book.id }
                        updateAdapter(book)
                    }
                } else {
                    // Add to favorites
                    val response = RetrofitClient.apiService.addToFavorites(authHeader, FavoriteRequestBody(book.id))
                    if (response.isSuccessful && response.body() != null) {
                        book.is_favorite = true
                        DataManager.favorites.add(response.body()!!)
                        updateAdapter(book)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error on favorite click", e)
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred on favorite click", e)
            }
        }
    }
    
    private fun updateAdapter(book: Book) {
        val currentList = bookAdapter.books
        val bookIndex = currentList.indexOfFirst { it.id == book.id }
        if (bookIndex != -1) {
            bookAdapter.notifyItemChanged(bookIndex)
        }
    }
}
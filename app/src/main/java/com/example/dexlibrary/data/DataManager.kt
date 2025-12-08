package com.example.dexlibrary.data

import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.Borrow
import com.example.dexlibrary.data.model.Favorite
import com.example.dexlibrary.data.model.Reservation

object DataManager {
    val books = mutableListOf<Book>()
    val reservations = mutableListOf<Reservation>()
    val borrows = mutableListOf<Borrow>()
    val favorites = mutableListOf<Favorite>()

    fun findFavoriteByBookId(bookId: Int): Favorite? {
        return favorites.find { it.book.id == bookId }
    }
}
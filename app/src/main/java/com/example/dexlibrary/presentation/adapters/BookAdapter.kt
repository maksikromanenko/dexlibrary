package com.example.dexlibrary.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Book

class BookAdapter(
    val books: MutableList<Book>,
    private val onFavoriteClick: (Book) -> Unit,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book, onFavoriteClick, onItemClick)
    }

    override fun getItemCount() = books.size

    fun updateBooks(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.book_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.book_author)
        private val publisherTextView: TextView = itemView.findViewById(R.id.book_publisher)
        private val copiesTextView: TextView = itemView.findViewById(R.id.book_copies)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
        private val coverImageView: ImageView = itemView.findViewById(R.id.book_cover)

        fun bind(book: Book, onFavoriteClick: (Book) -> Unit, onItemClick: (Book) -> Unit) {
            itemView.setOnClickListener { onItemClick(book) }
            titleTextView.text = book.title
            authorTextView.text = book.author_name
            publisherTextView.text = book.publisher
            copiesTextView.text = "- ${book.total_copies} копий"

            Glide.with(itemView.context)
                .load(book.logo_url)
                .into(coverImageView)

            val favoriteIcon = if (book.is_favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            favoriteButton.setImageResource(favoriteIcon)

            favoriteButton.setOnClickListener {
                onFavoriteClick(book)
            }
        }
    }
}
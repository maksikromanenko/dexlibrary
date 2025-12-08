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
import com.example.dexlibrary.data.model.Favorite

class FavoriteAdapter(
    val favorites: MutableList<Favorite>,
    private val onFavoriteClick: (Favorite) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favorites[position]
        holder.bind(favorite, onFavoriteClick)
    }

    override fun getItemCount() = favorites.size

    fun updateFavorites(newFavorites: List<Favorite>) {
        favorites.clear()
        favorites.addAll(newFavorites)
        notifyDataSetChanged()
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.book_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.book_author)
        private val publisherTextView: TextView = itemView.findViewById(R.id.book_publisher)
        private val copiesTextView: TextView = itemView.findViewById(R.id.book_copies)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
        private val coverImageView: ImageView = itemView.findViewById(R.id.book_cover)

        fun bind(favorite: Favorite, onFavoriteClick: (Favorite) -> Unit) {
            val book = favorite.book
            titleTextView.text = book.title
            authorTextView.text = book.author_name
            publisherTextView.text = book.publisher
            copiesTextView.text = "- ${book.total_copies} копий"

            Glide.with(itemView.context)
                .load(book.logo_url)
                .into(coverImageView)

            favoriteButton.setImageResource(R.drawable.ic_favorite)

            favoriteButton.setOnClickListener {
                onFavoriteClick(favorite)
            }
        }
    }
}
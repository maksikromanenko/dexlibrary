package com.example.dexlibrary.presentation.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Book
import jp.wasabeef.glide.transformations.BlurTransformation

class BookDetailDialogFragment : DialogFragment() {

    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        book = arguments?.getParcelable("book")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_book_detail, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val blurredBackground: ImageView = view.findViewById(R.id.blurred_background)
        val bookCover: ImageView = view.findViewById(R.id.book_cover)
        val bookTitle: TextView = view.findViewById(R.id.book_title)
        val bookAuthor: TextView = view.findViewById(R.id.book_author)
        val bookPublisher: TextView = view.findViewById(R.id.book_publisher)
        val reserveButton: Button = view.findViewById(R.id.reserve_button)

        Glide.with(this)
            .load(book.logo_url)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            .into(blurredBackground)

        Glide.with(this)
            .load(book.logo_url)
            .into(bookCover)

        bookTitle.text = book.title
        bookAuthor.text = book.author_name
        bookPublisher.text = book.publisher

        // TODO: Implement reserve logic
    }

    companion object {
        fun newInstance(book: Book): BookDetailDialogFragment {
            val args = Bundle()
            args.putParcelable("book", book)
            val fragment = BookDetailDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
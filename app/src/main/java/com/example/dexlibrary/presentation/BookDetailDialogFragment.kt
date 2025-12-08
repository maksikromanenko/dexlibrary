package com.example.dexlibrary.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.ReservationRequestBody
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.data.storage.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BookDetailDialogFragment : DialogFragment() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "BookDetailDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        val book = arguments?.getParcelable<Book>(ARG_BOOK)
        if (book == null) {
            dismiss()
            return
        }

        val titleTextView: TextView = view.findViewById(R.id.book_title)
        val authorTextView: TextView = view.findViewById(R.id.book_author)
        val publisherTextView: TextView = view.findViewById(R.id.book_publisher)
        val coverImageView: ImageView = view.findViewById(R.id.book_cover)
        val reserveButton: Button = view.findViewById(R.id.reserve_button)

        titleTextView.text = book.title
        authorTextView.text = book.author_name
        publisherTextView.text = book.publisher

        Glide.with(this)
            .load(book.logo_url)
            .into(coverImageView)

        reserveButton.setOnClickListener {
            handleReservation(book)
        }
    }

    private fun handleReservation(book: Book) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            try {
                val requestBody = ReservationRequestBody(book = book.id, status = "активна")
                val response = RetrofitClient.apiService.reserveBook(authHeader, requestBody)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Книга успешно забронирована!", Toast.LENGTH_SHORT).show()
                    // Обновляем все данные, чтобы увидеть бронирование в HomeFragment
                    DataManager.fetchAllData(token)
                    dismiss()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(context, "Не удалось забронировать: $error", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Reservation failed: $error")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error during reservation", e)
            }
        }
    }

    companion object {
        private const val ARG_BOOK = "book"

        fun newInstance(book: Book): BookDetailDialogFragment {
            val fragment = BookDetailDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_BOOK, book)
            fragment.arguments = args
            return fragment
        }
    }
}

package com.example.dexlibrary.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.dexlibrary.R
import com.example.dexlibrary.data.storage.DataManager
import com.google.android.material.textfield.TextInputEditText

class IsbnSearchDialogFragment : DialogFragment() {

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
        return inflater.inflate(R.layout.dialog_isbn_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isbnEditText = view.findViewById<TextInputEditText>(R.id.isbn_edit_text)
        val findButton = view.findViewById<Button>(R.id.find_by_isbn_button)

        findButton.setOnClickListener {
            val isbn = isbnEditText.text.toString().trim()
            if (isbn.isEmpty()) {
                Toast.makeText(context, "Введите ISBN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findBookByIsbn(isbn)
        }
    }

    private fun findBookByIsbn(isbn: String) {
        // Ищем книгу в DataManager
        val book = DataManager.books.value.find { it.isbn == isbn }

        if (book != null) {
            // Книга найдена, открываем детали
            val detailFragment = BookDetailDialogFragment.newInstance(book)
            detailFragment.show(parentFragmentManager, "BookDetailDialog")
            // Закрываем текущий диалог
            dismiss()
        } else {
            // Книга не найдена
            Toast.makeText(context, "Книга не найдена", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.dexlibrary.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Borrow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BorrowAdapter(val borrows: MutableList<Borrow>) : RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_borrow, parent, false)
        return BorrowViewHolder(view)
    }

    override fun onBindViewHolder(holder: BorrowViewHolder, position: Int) {
        val borrow = borrows[position]
        holder.bind(borrow)
    }

    override fun getItemCount() = borrows.size

    fun updateBorrows(newBorrows: List<Borrow>) {
        borrows.clear()
        borrows.addAll(newBorrows)
        notifyDataSetChanged()
    }

    class BorrowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.borrow_book_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.borrow_book_author)
        private val borrowDateTextView: TextView = itemView.findViewById(R.id.borrow_date)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.borrow_due_date)
        private val statusTextView: TextView = itemView.findViewById(R.id.borrow_status)

        fun bind(borrow: Borrow) {
            titleTextView.text = borrow.book_title
            authorTextView.text = borrow.book_author
            borrowDateTextView.text = "Дата взятия: ${formatDateString(borrow.borrow_date)}"
            dueDateTextView.text = "Вернуть до: ${formatDateString(borrow.due_date)}"
            statusTextView.text = "Статус: ${borrow.status}"
        }

        private fun formatDateString(dateString: String): String {
            return try {
                val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE // "YYYY-MM-DD"
                val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
                val date = LocalDate.parse(dateString, inputFormatter)
                date.format(outputFormatter)
            } catch (e: Exception) {
                dateString // В случае ошибки возвращаем исходную строку
            }
        }
    }
}
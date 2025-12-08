package com.example.dexlibrary.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.Reservation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReservationAdapter(val reservations: MutableList<Reservation>) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.bind(reservation)
    }

    override fun getItemCount() = reservations.size

    fun updateReservations(newReservations: List<Reservation>) {
        reservations.clear()
        reservations.addAll(newReservations)
        notifyDataSetChanged()
    }

    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.reservation_book_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.reservation_book_author)
        private val dateTextView: TextView = itemView.findViewById(R.id.reservation_date)
        private val statusTextView: TextView = itemView.findViewById(R.id.reservation_status)

        fun bind(reservation: Reservation) {
            titleTextView.text = reservation.book_title
            authorTextView.text = reservation.book_author
            dateTextView.text = "Дата бронирования: ${formatDateString(reservation.reserve_date)}"
            statusTextView.text = "Статус: ${reservation.status}"
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
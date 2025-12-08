package com.example.dexlibrary.data.model

data class Reservation(
    val id: Int,
    val book: Int,
    val book_title: String,
    val book_author: String,
    val reserve_date: String,
    val status: String
)
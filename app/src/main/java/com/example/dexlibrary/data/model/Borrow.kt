package com.example.dexlibrary.data.model

data class Borrow(
    val id: Int,
    val book: Int,
    val book_title: String,
    val book_author: String,
    val borrow_date: String,
    val due_date: String,
    val return_date: String?,
    val status: String,
    val is_overdue: Boolean
)
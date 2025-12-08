package com.example.dexlibrary.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: Int,
    val title: String,
    val author_name: String,
    val publisher: String,
    val total_copies: Int,
    var is_favorite: Boolean,
    @SerializedName("image")
    val logo_url: String?
) : Parcelable
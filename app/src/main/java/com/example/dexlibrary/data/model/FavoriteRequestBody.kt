package com.example.dexlibrary.data.model

import com.google.gson.annotations.SerializedName

data class FavoriteRequestBody(
    @SerializedName("book")
    val bookId: Int
)
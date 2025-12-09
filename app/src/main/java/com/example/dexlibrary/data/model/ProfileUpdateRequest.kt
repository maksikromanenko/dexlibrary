package com.example.dexlibrary.data.model

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String
)
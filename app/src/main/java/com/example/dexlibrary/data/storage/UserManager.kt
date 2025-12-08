package com.example.dexlibrary.data.storage

import com.example.dexlibrary.data.model.Address
import com.example.dexlibrary.data.model.User

object UserManager {
    var currentUser: User? = null
    var userAddress: List<Address>? = null

    fun clearUser() {
        currentUser = null
        userAddress = null
    }
}

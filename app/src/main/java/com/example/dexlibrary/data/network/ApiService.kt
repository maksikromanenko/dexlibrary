package com.example.dexlibrary.data.network

import com.example.dexlibrary.data.model.AuthResponse
import com.example.dexlibrary.data.model.Book
import com.example.dexlibrary.data.model.Borrow
import com.example.dexlibrary.data.model.ChangePasswordRequest
import com.example.dexlibrary.data.model.CheckTokenRequest
import com.example.dexlibrary.data.model.CheckTokenResponse
import com.example.dexlibrary.data.model.Favorite
import com.example.dexlibrary.data.model.FavoriteRequestBody
import com.example.dexlibrary.data.model.LoginRequest
import com.example.dexlibrary.data.model.ProfileUpdateRequest
import com.example.dexlibrary.data.model.RefreshTokenRequest
import com.example.dexlibrary.data.model.RefreshTokenResponse
import com.example.dexlibrary.data.model.RegisterRequest
import com.example.dexlibrary.data.model.Reservation
import com.example.dexlibrary.data.model.ReservationRequestBody
import com.example.dexlibrary.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/users/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/users/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/users/check-tokens/")
    suspend fun checkTokens(@Body request: CheckTokenRequest): Response<CheckTokenResponse>

    @POST("/api/users/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("/api/users/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @PATCH("/api/users/profile/")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<User>

    @PATCH("/api/users/change-password/")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit> // Assuming the server returns no body on success

    @GET("/api/catalog/books/")
    suspend fun getBooks(@Header("Authorization") token: String): Response<List<Book>>

    @GET("/api/circulation/my-reservations/")
    suspend fun getMyReservations(@Header("Authorization") token: String): Response<List<Reservation>>

    @GET("/api/circulation/my-borrows/")
    suspend fun getMyBorrows(@Header("Authorization") token: String): Response<List<Borrow>>

    @GET("/api/catalog/favorites/")
    suspend fun getFavorites(@Header("Authorization") token: String): Response<List<Favorite>>

    @POST("/api/catalog/favorites/")
    suspend fun addToFavorites(
        @Header("Authorization") token: String,
        @Body body: FavoriteRequestBody
    ): Response<Favorite>

    @DELETE("/api/catalog/favorites/{book_id}/")
    suspend fun removeFromFavorites(
        @Header("Authorization") token: String,
        @Path("book_id") bookId: Int
    ): Response<Unit>

    @POST("/api/circulation/reserve/")
    suspend fun reserveBook(
        @Header("Authorization") token: String,
        @Body body: ReservationRequestBody
    ): Response<Reservation>
}

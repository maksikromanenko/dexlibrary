package com.example.dexlibrary.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.presentation.adapters.BorrowAdapter
import com.example.dexlibrary.presentation.adapters.ReservationAdapter
import kotlinx.coroutines.launch
import java.io.IOException

class MainMenuFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var reservationAdapter: ReservationAdapter
    private lateinit var borrowAdapter: BorrowAdapter
    private val TAG = "MainMenuFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        tokenManager = TokenManager(requireContext())
        setupRecyclerViews(view)
        loadReservations()
        loadBorrows()

        return view
    }

    private fun setupRecyclerViews(view: View) {
        val reservationsRecyclerView = view.findViewById<RecyclerView>(R.id.reservations_recycler_view)
        reservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reservationAdapter = ReservationAdapter(mutableListOf())
        reservationsRecyclerView.adapter = reservationAdapter

        val borrowsRecyclerView = view.findViewById<RecyclerView>(R.id.borrows_recycler_view)
        borrowsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        borrowAdapter = BorrowAdapter(mutableListOf())
        borrowsRecyclerView.adapter = borrowAdapter
    }

    private fun loadReservations() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val authHeader = "Bearer $token"
                    val response = RetrofitClient.apiService.getMyReservations(authHeader)
                    if (response.isSuccessful) {
                        val reservations = response.body() ?: emptyList()
                        reservationAdapter.updateReservations(reservations)
                    } else {
                        Log.e(TAG, "Failed to fetch reservations: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.e(TAG, "Access token is null")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error while fetching reservations", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred", e)
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBorrows() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    val authHeader = "Bearer $token"
                    val response = RetrofitClient.apiService.getMyBorrows(authHeader)
                    if (response.isSuccessful) {
                        val borrows = response.body() ?: emptyList()
                        borrowAdapter.updateBorrows(borrows)
                    } else {
                        Log.e(TAG, "Failed to fetch borrows: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.e(TAG, "Access token is null")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error while fetching borrows", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred", e)
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
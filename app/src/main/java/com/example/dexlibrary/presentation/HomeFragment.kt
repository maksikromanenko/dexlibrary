package com.example.dexlibrary.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dexlibrary.R
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.presentation.adapters.BorrowAdapter
import com.example.dexlibrary.presentation.adapters.ReservationAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var borrowsRecyclerView: RecyclerView
    private lateinit var reservationsRecyclerView: RecyclerView
    private lateinit var borrowAdapter: BorrowAdapter
    private lateinit var reservationAdapter: ReservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        borrowsRecyclerView = view.findViewById(R.id.recycler_view_borrows)
        reservationsRecyclerView = view.findViewById(R.id.recycler_view_reservations)

        borrowAdapter = BorrowAdapter(mutableListOf())
        reservationAdapter = ReservationAdapter(mutableListOf())

        borrowsRecyclerView.layoutManager = LinearLayoutManager(context)
        borrowsRecyclerView.adapter = borrowAdapter

        reservationsRecyclerView.layoutManager = LinearLayoutManager(context)
        reservationsRecyclerView.adapter = reservationAdapter

        observeData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.borrows.collectLatest { borrows ->
                borrowAdapter.updateBorrows(borrows)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.reservations.collectLatest { reservations ->
                reservationAdapter.updateReservations(reservations)
            }
        }
    }
}
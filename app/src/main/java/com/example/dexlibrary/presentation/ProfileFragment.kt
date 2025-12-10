package com.example.dexlibrary.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.ProfileUpdateRequest
import com.example.dexlibrary.data.model.User
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.DataManager
import com.example.dexlibrary.data.storage.TokenManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(), OnChartValueSelectedListener {

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var actionButton: Button
    private lateinit var changePasswordButton: ImageButton
    private lateinit var logoutButton: ImageButton
    private lateinit var borrowsStat: TextView
    private lateinit var reservationsStat: TextView
    private lateinit var favoritesStat: TextView
    private lateinit var authorPieChart: PieChart

    private lateinit var tokenManager: TokenManager
    private var isEditMode = false
    private val TAG = "ProfileFragment"

    private var originalChartColors: List<Int>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        firstNameEditText = view.findViewById(R.id.first_name_edit_text)
        lastNameEditText = view.findViewById(R.id.last_name_edit_text)
        emailEditText = view.findViewById(R.id.email_edit_text)
        actionButton = view.findViewById(R.id.profile_action_button)
        changePasswordButton = view.findViewById(R.id.change_password_button)
        logoutButton = view.findViewById(R.id.logout_button)
        borrowsStat = view.findViewById(R.id.borrows_stat)
        reservationsStat = view.findViewById(R.id.reservations_stat)
        favoritesStat = view.findViewById(R.id.favorites_stat)
        authorPieChart = view.findViewById(R.id.author_pie_chart)

        actionButton.setOnClickListener { toggleMode() }
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        logoutButton.setOnClickListener { showLogoutConfirmationDialog() }

        observeData()
        setupPieChart()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.user.collectLatest { user ->
                user?.let { populateUserData(it) }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.borrows.collectLatest { borrows ->
                borrowsStat.text = "Взято: ${borrows.size}"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.reservations.collectLatest { reservations ->
                reservationsStat.text = "В Брони: ${reservations.size}"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.favorites.collectLatest { favorites ->
                favoritesStat.text = "В избранном: ${favorites.size}"
                updatePieChart(favorites.mapNotNull { it.book.author_name })
            }
        }
    }

    private fun setupPieChart() {
        authorPieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setHoleRadius(40f)
            setTransparentCircleRadius(40f)
            setUsePercentValues(true)
            setDrawEntryLabels(false)

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                isWordWrapEnabled = true
            }

            setOnChartValueSelectedListener(this@ProfileFragment)
        }
    }

    private fun updatePieChart(authors: List<String>) {
        if (authors.isEmpty()) {
            authorPieChart.visibility = View.GONE
            return
        }
        authorPieChart.visibility = View.VISIBLE

        val authorCounts = authors.groupingBy { it }.eachCount()
        val entries = authorCounts.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "")
        
        val colors = mutableListOf<Int>()
        colors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
        colors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
        colors.addAll(ColorTemplate.JOYFUL_COLORS.toList())
        originalChartColors = colors
        dataSet.colors = originalChartColors

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(authorPieChart))
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.WHITE)

        authorPieChart.data = pieData
        authorPieChart.invalidate()
    }

    override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
        if (h == null || originalChartColors == null) return

        val selectedIndex = h.x.toInt()
        val newColors = originalChartColors!!.mapIndexed { index, color ->
            if (index == selectedIndex) color else Color.LTGRAY
        }

        (authorPieChart.data.dataSet as PieDataSet).colors = newColors
        authorPieChart.invalidate()
    }

    override fun onNothingSelected() {
        if (originalChartColors != null) {
            (authorPieChart.data.dataSet as PieDataSet).colors = originalChartColors
            authorPieChart.invalidate()
        }
    }

    private fun populateUserData(user: User) {
        firstNameEditText.setText(user.firstName)
        lastNameEditText.setText(user.lastName)
        emailEditText.setText(user.email)
    }

    private fun toggleMode() {
        isEditMode = !isEditMode
        if (isEditMode) {
            setFieldsEnabled(true)
            actionButton.text = "Сохранить"
        } else {
            saveProfileChanges()
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        firstNameEditText.isEnabled = enabled
        lastNameEditText.isEnabled = enabled
        emailEditText.isEnabled = enabled
    }

    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialogFragment()
        dialog.show(parentFragmentManager, "ChangePasswordDialog")
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход из аккаунта")
            .setMessage("Вы действительно хотите выйти?")
            .setPositiveButton("Да") { _, _ -> logout() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            DataManager.clearData()
            tokenManager.clearTokens()

            val intent = Intent(activity, LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun saveProfileChanges() {
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val email = emailEditText.text.toString()

        setFieldsEnabled(false)
        actionButton.text = "Изменить профиль"

        viewLifecycleOwner.lifecycleScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val authHeader = "Bearer $token"

            val request = ProfileUpdateRequest(firstName, lastName, email)

            try {
                val response = RetrofitClient.apiService.updateProfile(authHeader, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show()
                    DataManager.fetchAllData(token)
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(context, "Ошибка: $error", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Profile update failed: $error")
                    DataManager.user.value?.let { populateUserData(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                Toast.makeText(context, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                DataManager.user.value?.let { populateUserData(it) }
            }
        }
    }
}
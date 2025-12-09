package com.example.dexlibrary.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
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
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var actionButton: Button
    private lateinit var changePasswordButton: ImageButton
    private lateinit var logoutButton: ImageButton

    private lateinit var tokenManager: TokenManager
    private var isEditMode = false
    private val TAG = "ProfileFragment"

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

        actionButton.setOnClickListener { toggleMode() }
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        logoutButton.setOnClickListener { showLogoutConfirmationDialog() }

        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataManager.user.collectLatest { user ->
                user?.let { populateUserData(it) }
            }
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
            // 1. Очищаем все сессионные данные
            DataManager.clearData()
            // 2. Стираем токены
            tokenManager.clearTokens()

            // 3. Перенаправляем на экран входа
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

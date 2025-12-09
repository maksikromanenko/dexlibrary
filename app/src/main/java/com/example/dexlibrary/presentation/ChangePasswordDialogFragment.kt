package com.example.dexlibrary.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.ChangePasswordRequest
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ChangePasswordDialogFragment : DialogFragment() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "ChangePasswordDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentDialog) // Применяем наш прозрачный стиль
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        val oldPasswordEditText = view.findViewById<TextInputEditText>(R.id.old_password_edit_text)
        val newPasswordEditText = view.findViewById<TextInputEditText>(R.id.new_password_edit_text)
        val newPasswordConfirmEditText = view.findViewById<TextInputEditText>(R.id.new_password_confirm_edit_text)
        val submitButton = view.findViewById<Button>(R.id.submit_change_password_button)

        submitButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val newPasswordConfirm = newPasswordConfirmEditText.text.toString()

            if (newPassword != newPasswordConfirm) {
                Toast.makeText(context, "Новые пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(context, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(oldPassword, newPassword, newPasswordConfirm)
        }
    }

    private fun changePassword(old: String, new: String, confirm: String) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val request = ChangePasswordRequest(old, new, confirm)
            try {
                val response = RetrofitClient.apiService.changePassword("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(context, "Ошибка: $error", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Password change failed: $error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password", e)
                Toast.makeText(context, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
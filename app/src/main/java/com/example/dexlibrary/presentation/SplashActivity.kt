package com.example.dexlibrary.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.dexlibrary.R
import com.example.dexlibrary.data.model.CheckTokenRequest
import com.example.dexlibrary.data.model.RefreshTokenRequest
import com.example.dexlibrary.data.network.RetrofitClient
import com.example.dexlibrary.data.storage.TokenManager
import com.example.dexlibrary.data.storage.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "SplashActivity"

    private enum class NavigationTarget {
        MAIN_ACTIVITY,
        LOGIN_ACTIVITY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.isAppearanceLightStatusBars = true
        insetsController?.isAppearanceLightNavigationBars = true
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        tokenManager = TokenManager(applicationContext)

        lifecycleScope.launch {
            val navigationTargetJob = async { decideNextScreen() }

            delay(1000)

            val destination = navigationTargetJob.await()
            when (destination) {
                NavigationTarget.MAIN_ACTIVITY -> navigateToMain()
                NavigationTarget.LOGIN_ACTIVITY -> navigateToLogin()
            }
        }
    }

    private suspend fun decideNextScreen(): NavigationTarget {
        val accessToken = tokenManager.getAccessToken().first()
        val refreshToken = tokenManager.getRefreshToken().first()

        if (accessToken == null || refreshToken == null) {
            Log.i(TAG, "Токены не найдены, переход на LogInActivity")
            return NavigationTarget.LOGIN_ACTIVITY
        }

        Log.d(TAG, "Токены найдены, проверка валидности...")
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.checkTokens(CheckTokenRequest(accessToken, refreshToken))
                if (!response.isSuccessful) {
                    Log.e(TAG, "Запрос checkTokens завершился с ошибкой ${response.code()}, переход на Login.")
                    return@withContext NavigationTarget.LOGIN_ACTIVITY
                }

                val checkResponse = response.body()!!
                var currentAccessToken = accessToken

                if (!checkResponse.accessValid && checkResponse.refreshValid) {
                    Log.i(TAG, "Access токен невалиден, refresh токен валиден. Обновление...")
                    val refreshResponse = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
                    if (refreshResponse.isSuccessful) {
                        val newAccessToken = refreshResponse.body()!!.access
                        tokenManager.saveTokens(newAccessToken, refreshToken)
                        currentAccessToken = newAccessToken
                        Log.i(TAG, "Токен успешно обновлен.")
                    } else {
                        Log.e(TAG, "Не удалось обновить токен, код: ${refreshResponse.code()}. Переход на Login.")
                        return@withContext NavigationTarget.LOGIN_ACTIVITY
                    }
                } else if (!checkResponse.accessValid && !checkResponse.refreshValid) {
                    Log.i(TAG, "Оба токена невалидны. Переход на Login.")
                    return@withContext NavigationTarget.LOGIN_ACTIVITY
                }

                Log.i(TAG, "Токен валиден. Загрузка начальных данных...")
                if (fetchData(currentAccessToken)) {
                    Log.i(TAG, "Данные успешно загружены. Переход на MainActivity.")
                    NavigationTarget.MAIN_ACTIVITY
                } else {
                    Log.e(TAG, "Не удалось загрузить начальные данные. Переход на Login.")
                    NavigationTarget.LOGIN_ACTIVITY
                }
            } catch (e: Exception) {
                Log.e(TAG, "Произошла ошибка во время проверки токена или загрузки данных.", e)
                NavigationTarget.LOGIN_ACTIVITY
            }
        }
    }

    private suspend fun fetchData(accessToken: String): Boolean {
        val authHeader = "Bearer $accessToken"
        return try {
            val profileResponse = RetrofitClient.apiService.getProfile(authHeader)
            if (profileResponse.isSuccessful) {
                UserManager.currentUser = profileResponse.body()
                Log.i(TAG, "Профиль успешно загружен")
                true
            } else {
                Log.e(TAG, "Не удалось загрузить профиль: ${profileResponse.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Сетевая ошибка при загрузке данных", e)
            false
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

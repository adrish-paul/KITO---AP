package com.kito.core.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class ESP(context: Context) {
    private companion object {
        const val KEY_SAP_PASSWORD = "sap_password"
        const val KEY_LOGGED_IN = "logged_in"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {

            // 🔥 Delete corrupted prefs file
            context.deleteSharedPreferences("secure_prefs")

            // 🔥 Recreate clean master key
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    suspend fun saveSapPassword(password: String): Boolean =
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_SAP_PASSWORD, password)
                .putBoolean(KEY_LOGGED_IN, true)
                .commit()
        }

    suspend fun getSapPassword(): String =
        withContext(Dispatchers.IO) {
            prefs.getString(KEY_SAP_PASSWORD, "") ?: ""
        }

    val isLoggedInFlow: Flow<Boolean> = callbackFlow {
        val sendValue = {
            trySend(prefs.getBoolean(KEY_LOGGED_IN, false))
        }

        sendValue()

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LOGGED_IN) {
                sendValue()
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    suspend fun clearSapPassword(): Boolean =
        withContext(Dispatchers.IO) {
            prefs.edit()
                .remove(KEY_SAP_PASSWORD)
                .putBoolean(KEY_LOGGED_IN, false)
                .commit()
        }
}
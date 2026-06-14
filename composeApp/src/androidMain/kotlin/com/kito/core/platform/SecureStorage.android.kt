package com.kito.core.platform

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by
preferencesDataStore(name = "encrypted_data_store")

actual class SecureStorage(private val context: Context) {

    actual constructor() : this(PlatformContext.applicationContext ?: throw IllegalStateException("PlatformContext not initialized"))

    private companion object {
        private val KEY_SAP_PASSWORD = stringPreferencesKey("sap_password")
        const val KEYSET_NAME = "kito_master_keyset"
        const val PREFERENCE_FILE = "__androidx_security_crypto_encrypted_prefs__"
        private const val ASSOCIATED_DATA = "sap_password"
    }

    private val aead: Aead by lazy {
        createAead()
    }

    private fun createAead(): Aead {
        return try {
            AeadConfig.register()

            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://kito_master_key")
                .build()
                .keysetHandle

            keysetHandle.getPrimitive(Aead::class.java)

        } catch (e: Exception) {

            // 🔥 Self-heal corrupted keyset
            context.deleteSharedPreferences(PREFERENCE_FILE)

            AeadConfig.register()

            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://kito_master_key")
                .build()
                .keysetHandle

            keysetHandle.getPrimitive(Aead::class.java)
        }
    }

    private fun encrypt(plainText: String): String {
        val cipherText = aead.encrypt(
            plainText.toByteArray(Charsets.UTF_8),
            ASSOCIATED_DATA.toByteArray()
        )
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    private fun decrypt(cipherTextBase64: String): String {
        val cipherText = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
        val plainText = aead.decrypt(
            cipherText,
            ASSOCIATED_DATA.toByteArray()
        )
        return String(plainText, Charsets.UTF_8)
    }

    actual suspend fun saveSapPassword(password: String): Boolean {
        val encryptedPassword = encrypt(password)
        context.dataStore.edit { prefs ->
            prefs[KEY_SAP_PASSWORD] = encryptedPassword
        }
        return true
    }

    actual suspend fun getSapPassword(): String {
        val encrypted = context.dataStore.data.first()[KEY_SAP_PASSWORD]
            ?: return ""
        try {
            return decrypt(encrypted)
        } catch (_: Exception) {
        }

        return try {
            val legacyPlain = decryptLegacy(encrypted)

            val reEncrypted = encrypt(legacyPlain)
            context.dataStore.edit { prefs ->
                prefs[KEY_SAP_PASSWORD] = reEncrypted
            }

            legacyPlain
        } catch (_: Exception) {
            clearSapPassword()
            ""
        }
    }

    private fun decryptLegacy(cipherTextBase64: String): String {
        val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)

        val plainText = aead.decrypt(
            cipherText,
            null
        )

        return String(plainText, Charsets.UTF_8)
    }

    actual val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            !prefs[KEY_SAP_PASSWORD].isNullOrBlank()
        }

    actual suspend fun clearSapPassword(): Boolean {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_SAP_PASSWORD)
        }
        return true
    }
}
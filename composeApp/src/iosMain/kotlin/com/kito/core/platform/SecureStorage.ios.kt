package com.kito.core.platform

import com.kito.core.datastore.IosPrefsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SecureStorage : KoinComponent {
    actual suspend fun saveKayaPassword(password: String): Boolean {
        KeychainHelper.save(service, "kaya_password", password)
        return KeychainHelper.read(service, "kaya_password") == password
    }
    actual suspend fun getKayaPassword(): String = KeychainHelper.read(service, "kaya_password") ?: ""
    actual suspend fun clearKayaPassword(): Boolean {
        KeychainHelper.delete(service, "kaya_password")
        return KeychainHelper.read(service, "kaya_password") == null
    }
    
    private val iosPrefsRepository: IosPrefsRepository by inject()
    
    private val service = "com.kito.app.secure"
    private val account = "sap_password"

    actual val isLoggedInFlow: Flow<Boolean> = iosPrefsRepository.isLoggedInFlow

    actual suspend fun saveSapPassword(password: String): Boolean {
        KeychainHelper.save(service, account, password)
        iosPrefsRepository.setLoggedIn(true)
        return true
    }

    actual suspend fun getSapPassword(): String {
        return KeychainHelper.read(service, account) ?: ""
    }

    actual suspend fun clearSapPassword(): Boolean {
        KeychainHelper.delete(service, account)
        iosPrefsRepository.setLoggedIn(false)
        return true
    }
}

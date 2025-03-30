package com.emberiot.emberiot

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class EmberIotApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "EmberIotKeys",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        app = this
    }

    companion object {
        var encryptedSharedPreferences: SharedPreferences? = null
            private set

        var initCallback: (() -> Unit)? = null

        lateinit var app: EmberIotApp
        var firebaseInit = false

        enum class PreferenceKey {
            APPID,
            APIKEY,
            DBURL,
        }

        fun hasCreds(): Boolean {
            val appId = encryptedSharedPreferences!!.getString(PreferenceKey.APPID.name, null)
            val apiKey = encryptedSharedPreferences!!.getString(PreferenceKey.APIKEY.name, null)
            val dbUrl = encryptedSharedPreferences!!.getString(PreferenceKey.DBURL.name, null)
            return !appId.isNullOrBlank() && !apiKey.isNullOrBlank() && !dbUrl.isNullOrBlank()
        }

        fun setCreds(appId: String?, apiKey: String?, dbUrl: String?) {
            encryptedSharedPreferences!!.edit().let {
                it.putString(PreferenceKey.APPID.name, appId);
                it.putString(PreferenceKey.APIKEY.name, apiKey);
                it.putString(PreferenceKey.DBURL.name, dbUrl);
                it.apply()
            }
        }

        fun initFirebase(context: ViewModelStoreOwner): Boolean {
            if (firebaseInit) {
                return true
            }

            val appId = encryptedSharedPreferences!!.getString(PreferenceKey.APPID.name, null)
            val apiKey = encryptedSharedPreferences!!.getString(PreferenceKey.APIKEY.name, null)
            val dbUrl = encryptedSharedPreferences!!.getString(PreferenceKey.DBURL.name, null)

            if (appId != null && apiKey != null && dbUrl != null) {
                FirebaseApp.initializeApp(app, FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setDatabaseUrl(dbUrl)
                    .build())
                firebaseInit = true

                val loginViewModel = ViewModelProvider(context)[LoginViewModel::class.java]
                loginViewModel.checkIsLogged()
                initCallback?.invoke()
                return true
            } else {
                return false
            }
        }

        fun deinitFirebase() {
            if (!firebaseInit) {
                return
            }

            firebaseInit = false
            encryptedSharedPreferences!!.edit { clear() }
            FirebaseApp.getInstance().delete()
        }
    }
}
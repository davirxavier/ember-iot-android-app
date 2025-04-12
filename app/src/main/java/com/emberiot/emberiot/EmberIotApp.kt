package com.emberiot.emberiot

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.emberiot.emberiot.data.UserData
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack

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
        loadIconPack()
    }

    private fun loadIconPack() {
        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        EmberIotApp.iconPack = iconPack
    }

    companion object {
        var iconPack: IconPack? = null

        var encryptedSharedPreferences: SharedPreferences? = null
            private set

        private val _firebaseInit = MutableLiveData(false)
        val firebaseInit: LiveData<Boolean> get() = _firebaseInit

        lateinit var app: EmberIotApp

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
            if (firebaseInit.value == true) {
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

                _firebaseInit.value = true

                val loginViewModel = ViewModelProvider(context)[LoginViewModel::class.java]
                loginViewModel.checkIsLogged()
                return true
            } else {
                return false
            }
        }

        fun deinitFirebase() {
            if (firebaseInit.value == false) {
                return
            }

            _firebaseInit.value = false
            encryptedSharedPreferences!!.edit { clear() }
            FirebaseApp.getInstance().delete()
        }
    }
}
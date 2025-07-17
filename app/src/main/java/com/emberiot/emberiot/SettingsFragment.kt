package com.emberiot.emberiot

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.emberiot.emberiot.view_model.LoginViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val loginViewModel by lazy {
        ViewModelProvider(requireActivity())[LoginViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            onLogoutClicked()
            true
        }

        findPreference<SwitchPreferenceCompat>("notificationsEnabled")?.let {
            if (!EmberIotApp.hasCred(EmberIotApp.Companion.PreferenceKey.GCMSENDERID)) {
                it.isChecked = false

                it.setOnPreferenceChangeListener { preference, newValue ->
                    (newValue as? Boolean)?.let { value ->
                        if (value) {
                            showNoGcmDialog()
                            it.isChecked = false
                        }
                    }

                    true
                }
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.isChecked = hasNotifPerm()

                it.setOnPreferenceChangeListener { preference, newValue ->
                    (newValue as? Boolean)?.let { value ->
                        if (value && !hasNotifPerm()) {
                            showNotificationPermissionDialog(true)
                            return@setOnPreferenceChangeListener false
                        }

                        if (!value && hasNotifPerm()) {
                            showNotificationPermissionDialog(false)
                            return@setOnPreferenceChangeListener false
                        }
                    }

                    true
                }
            } else {
                it.isChecked = true
                it.isEnabled = false
            }
        }
    }

    override fun onResume() {
        super.onResume()

        findPreference<SwitchPreferenceCompat>("notificationsEnabled")?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.isChecked = hasNotifPerm()
            }
        }
    }

    private fun hasNotifPerm(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showNoGcmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.notifications_title)
            .setMessage(R.string.cant_enable_notif_dialog)
            .setNegativeButton(R.string.close, null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showNotificationPermissionDialog(enable: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.notifications_title)
            .setMessage(if (enable) R.string.enable_notif_dialog else R.string.disable_notif_dialog)
            .setPositiveButton(R.string.goto_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun onLogoutClicked() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.confirm)
            setMessage(R.string.logout_confirm)

            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.confirm) { _, _ ->
                loginViewModel.logout()
            }
        }.create().show()
    }
}
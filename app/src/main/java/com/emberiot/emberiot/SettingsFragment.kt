package com.emberiot.emberiot

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
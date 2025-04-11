package com.emberiot.emberiot.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.emberiot.emberiot.EmberIotApp
import com.emberiot.emberiot.R
import com.emberiot.emberiot.databinding.FragmentLoginBinding
import com.emberiot.emberiot.util.OnPrevCallback
import com.emberiot.emberiot.view_model.LoginViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader

class LoginFragment : Fragment(), OnPrevCallback {

    companion object {
        enum class Steps {
            INSTRUCTIONS, CONFIG, PROGRESS, LOGIN_INSTRUCTIONS, LOGIN, PROGRESS_LOGIN
        }
    }

    private lateinit var binding: FragmentLoginBinding
     var auth: FirebaseAuth? = null
    private var currentStep: Steps = Steps.INSTRUCTIONS
    private var init = false

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(requireActivity())[LoginViewModel::class.java]
    }

    private val requestFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    onFileChosen(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.doneBtn.setOnClickListener { onNext() }
        binding.uploadBtn.setOnClickListener { onPickJson() }
        binding.manualBtn.setOnClickListener { onManualSend() }
        binding.backBtn.setOnClickListener { onDeleteApp() }
        binding.signInBtn.setOnClickListener { onLogin() }
        binding.doneBtnLogin.setOnClickListener { onNext() }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onPrev()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        EmberIotApp.firebaseInit.observe(viewLifecycleOwner) { init ->
            if (init == false) {
                return@observe
            }

            loginViewModel.currentUser.observe(viewLifecycleOwner) {
                currentStep = Steps.PROGRESS_LOGIN
                updateStep()

                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
                    val isApiKey = (it.exception as? FirebaseException)?.message?.contains("API key not valid") ?: false
                    if (isApiKey) {
                        Toast.makeText(requireContext(), R.string.invalid_key, Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        EmberIotApp.deinitFirebase()
                        currentStep = Steps.INSTRUCTIONS
                        updateStep()
                    } else {
                        redirectHome()
                    }
                }
            }
        }

        return binding.root;
    }

    private fun onLogin() {
        val email = binding.emailText.text.toString()
        val pass = binding.passText.text.toString()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.login_invalid), Toast.LENGTH_SHORT).show()
            return
        }

        onNext()

        viewLifecycleOwner.lifecycleScope.launch {
            if (loginViewModel.login(email, pass) == null) {
                redirectHome()
            } else {
                currentStep = Steps.LOGIN
                updateStep()
                Toast.makeText(requireContext(), getString(R.string.login_invalid), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectHome() {
        findNavController().navigate(R.id.nav_devices, null, NavOptions.Builder().apply {
            setPopUpTo(R.id.nav_login, true)
        }.build())
    }

    private fun onDeleteApp() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.login_confirmation_title)
            setMessage(R.string.login_confirmation_message)
            setPositiveButton(R.string.confirm_deletion) { dialog, _ ->
                EmberIotApp.deinitFirebase()
                currentStep = Steps.INSTRUCTIONS
                updateStep()

                dialog.dismiss()
            }
            setNegativeButton(R.string.cancel) { _, _ -> }
        }.show()
    }

    private fun onManualSend() {
        val appId = binding.appIdText.text.toString()
        val apiKey = binding.apiKeyText.text.toString()
        val dbUrl = binding.urlText.text.toString()

        if (appId.isNotBlank() && apiKey.isNotBlank() && dbUrl.isNotBlank()) {
            EmberIotApp.setCreds(appId, apiKey, dbUrl)
            currentStep = Steps.PROGRESS
            updateStep()
        }
    }

    private fun onFileChosen(uri: Uri) {
        readJson(uri)?.let { jsonObject ->
            val dbUrl = jsonObject.optJSONObject("project_info")?.optString("firebase_url")

            val client = jsonObject.optJSONArray("client")?.optJSONObject(0)
            val appId = client?.optJSONObject("client_info")?.optString("mobilesdk_app_id")
            val apiKey = client?.optJSONArray("api_key")?.optJSONObject(0)?.optString("current_key")

            if (dbUrl != null && appId != null && apiKey != null) {
                EmberIotApp.setCreds(appId, apiKey, dbUrl)
                onNext()
            } else {
                Toast.makeText(requireContext(), getString(R.string.login_incomplete_json), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readJson(uri: Uri): JSONObject? {
        return try {
            val inputStream: InputStream = requireActivity().contentResolver.openInputStream(uri) ?: return null
            val reader = InputStreamReader(inputStream)
            val stringBuilder = StringBuilder()
            var character: Int
            while (reader.read().also { character = it } != -1) {
                stringBuilder.append(character.toChar())
            }
            reader.close()
            JSONObject(stringBuilder.toString())
        } catch (e: Exception) {
            Log.e("SelectedFile", "Error reading file: ${e.message}")
            Toast.makeText(requireContext(), getString(R.string.login_invalid_json), Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun onPickJson() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/json"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        requestFileLauncher.launch(intent)
    }

    private fun onNext() {
        currentStep = when (currentStep) {
            Steps.INSTRUCTIONS -> Steps.CONFIG
            Steps.CONFIG -> Steps.PROGRESS
            Steps.PROGRESS -> Steps.LOGIN_INSTRUCTIONS
            Steps.LOGIN_INSTRUCTIONS -> Steps.LOGIN
            Steps.LOGIN -> Steps.PROGRESS_LOGIN
            Steps.PROGRESS_LOGIN -> Steps.PROGRESS_LOGIN
        }

        updateStep()
    }

    override fun onPrev() {
        if (currentStep == Steps.PROGRESS || currentStep == Steps.PROGRESS_LOGIN) {
            return
        }

        currentStep = when (currentStep) {
            Steps.LOGIN -> Steps.LOGIN_INSTRUCTIONS
            Steps.PROGRESS -> Steps.PROGRESS
            Steps.CONFIG -> Steps.INSTRUCTIONS
            Steps.INSTRUCTIONS -> Steps.INSTRUCTIONS
            Steps.PROGRESS_LOGIN -> Steps.PROGRESS_LOGIN
            Steps.LOGIN_INSTRUCTIONS -> Steps.LOGIN_INSTRUCTIONS
        }

        updateStep()
    }

    private fun updateStep() {
        binding.instruction1Panel.visibility = if (currentStep == Steps.INSTRUCTIONS) View.VISIBLE else View.GONE
        binding.infoPanel.visibility = if (currentStep == Steps.CONFIG) View.VISIBLE else View.GONE
        binding.progressBarPanel.visibility = if (currentStep == Steps.PROGRESS) View.VISIBLE else View.GONE
        binding.loginPanel.visibility = if (currentStep == Steps.LOGIN) View.VISIBLE else View.GONE
        binding.progressBarLogin.visibility = if (currentStep == Steps.PROGRESS_LOGIN) View.VISIBLE else View.GONE
        binding.instructionLoginPanel.visibility = if (currentStep == Steps.LOGIN_INSTRUCTIONS) View.VISIBLE else View.GONE

        if (currentStep != Steps.INSTRUCTIONS) {
            getActionBar().show()
        }
        else {
            getActionBar().hide()
        }

        if (currentStep == Steps.PROGRESS && EmberIotApp.initFirebase(requireActivity())) {
            onNext()
        }
    }

    override fun onResume() {
        super.onResume()

        if (currentStep == Steps.INSTRUCTIONS) {
            getActionBar().hide()
        }

        if (!init && EmberIotApp.hasCreds()) {
            init = true
            currentStep = Steps.PROGRESS
            updateStep()
        }
    }

    override fun onStop() {
        super.onStop()
        getActionBar().show()
    }

    private fun getActionBar(): ActionBar {
        return (activity as AppCompatActivity).supportActionBar!!
    }
}
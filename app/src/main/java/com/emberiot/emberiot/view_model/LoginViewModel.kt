package com.emberiot.emberiot.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.emberiot.emberiot.data.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val _currentUser = MutableLiveData<UserData>()
    val currentUser: LiveData<UserData> get() = _currentUser

    private fun setCurrentUser(user: UserData) {
        _currentUser.value = user
    }

    fun checkIsLogged() {
        FirebaseAuth.getInstance().currentUser?.let {
            setCurrentUser(UserData(it))
        }
    }

    suspend fun login(username: String, password: String): AuthError? {
        return try {
            val user = FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).await().user
            setCurrentUser(UserData(user!!))
            return null
        } catch (_: Exception) {
            return AuthError.INVALID_CREDENTIALS
        }
    }

    companion object {
        enum class AuthError {
            INVALID_CREDENTIALS
        }
    }
}
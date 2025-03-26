package com.example.fyp_androidapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.repository.AuthRepository
import com.example.fyp_androidapp.database.dao.UserDao
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SimpleUser(
    val uid: String,
    val email: String?,
    val displayName: String?
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<SimpleUser>>(emptyList())
    val accounts: StateFlow<List<SimpleUser>> = _accounts

    fun signInWithGoogle(uid: String, email: String?, displayName: String?, idToken: String?, authCode: String?) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(uid, email, displayName, idToken, authCode)
            _accounts.value = _accounts.value + SimpleUser(uid, email, displayName)
        }
    }

    fun signOut(userIndex: Int) {
        viewModelScope.launch {
            val user = _accounts.value.getOrNull(userIndex)
            if (user != null) {
                authRepository.signOut(user.uid)
                _accounts.value = _accounts.value.toMutableList().apply { removeAt(userIndex) }
            }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        return authRepository.getGoogleSignInClient(context)
    }
}

package com.example.fyp_androidapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _accounts = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val accounts: StateFlow<List<FirebaseUser>> = _accounts

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _accounts.value = listOf(user)
        }
    }

    fun signInWithGoogle(idToken: String?, authCode: String?) {
        viewModelScope.launch {
            val user = authRepository.signInWithGoogle(idToken)
            if (user != null) {
                _accounts.value = _accounts.value + user
                authRepository.sendTokenToBackend(user.uid, user.email, user.displayName, idToken, authCode)
            }
        }
    }

    fun signOut(userIndex: Int) {
        viewModelScope.launch {
            authRepository.signOut()
            _accounts.value = _accounts.value.toMutableList().apply { removeAt(userIndex) }
        }
    }
}

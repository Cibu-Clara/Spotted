package com.example.onspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onspot.data.repository.UserRepository
import com.example.onspot.data.repository.UserRepositoryImpl
import com.example.onspot.ui.states.ChangePasswordState
import com.example.onspot.ui.states.DeleteAccountState
import com.example.onspot.ui.states.SignOutState
import com.example.onspot.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _deleteAccountState = Channel<DeleteAccountState>()
    val deleteAccountState = _deleteAccountState.receiveAsFlow()

    private val _logoutState = Channel<SignOutState>()
    val logoutState = _logoutState.receiveAsFlow()

    private val _changePasswordState = Channel<ChangePasswordState>()
    val changePasswordState = _changePasswordState.receiveAsFlow()

    fun logoutUser(googleSignInClient: GoogleSignInClient?) = viewModelScope.launch {
        userRepository.logoutUser(googleSignInClient).collect { result ->
            when(result) {
                is Resource.Loading -> { _logoutState.send(SignOutState(isLoading = true)) }
                is Resource.Success -> { _logoutState.send(SignOutState(isSuccess = "Logged out successfully")) }
                is Resource.Error -> { _logoutState.send(SignOutState(isError = result.message)) }
            }
        }
    }

    fun deleteUserAccount(googleSignInClient: GoogleSignInClient?) = viewModelScope.launch {
        userRepository.deleteUserAccount(googleSignInClient).collect { result ->
            when(result) {
                is Resource.Loading -> { _deleteAccountState.send(DeleteAccountState(isLoading = true)) }
                is Resource.Success -> { _deleteAccountState.send(DeleteAccountState(isSuccess = "Account deleted successfully")) }
                is Resource.Error -> { _deleteAccountState.send(DeleteAccountState(isError = result.message)) }
            }
        }
    }

    fun verifyPassword(password: String, callback: (Boolean) -> Unit) = viewModelScope.launch {
        val isPasswordCorrect = userRepository.verifyPassword(password)
        callback(isPasswordCorrect)
    }

    fun changeUserPassword(currentPassword: String, newPassword: String) = viewModelScope.launch {
        userRepository.changePassword(currentPassword, newPassword).collect { result ->
            when(result) {
                is Resource.Loading -> { _changePasswordState.send(ChangePasswordState(isLoading = true)) }
                is Resource.Success -> { _changePasswordState.send(ChangePasswordState(isSuccess = "Password changed successfully")) }
                is Resource.Error -> { _changePasswordState.send(ChangePasswordState(isError = result.message)) }
            }
        }
    }

    fun navigateForPasswordChange(callback: (Boolean, String?) -> Unit) = viewModelScope.launch {
        when (userRepository.getCurrentUserAuthProvider()) {
            "password" -> { callback(true, null) }
            "google.com" -> { callback(false, "For security reasons, please change your password directly via Google's account management.") }
            "facebook.com" -> { callback(false, "For security reasons, please change your password directly via Facebook's account management.") }
        }
    }

    fun verifyAuthProvider(callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (userRepository.getCurrentUserAuthProvider()) {
            "password" -> { callback(true) }
            else -> { callback(false) }
        }
    }
}
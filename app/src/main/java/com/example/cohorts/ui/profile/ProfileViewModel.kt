package com.example.cohorts.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.user.UserRepo
import com.example.cohorts.core.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepo
) : ViewModel() {

    init {
        Timber.d("init")
    }

    private val _currentUser = MutableLiveData(getCurrentUser())
    val currentUser: LiveData<User> = _currentUser

    private val _errorMessage = MutableLiveData<String>()
    private val errorMessage: LiveData<String> = _errorMessage

    private fun getCurrentUser(): User {
        val result = userRepository.getCurrentUser()
        return if (result.succeeded) {
            val user = (result as Result.Success).data
            Timber.d("user = $user")
            user
        } else {
            User()
        }
    }

}
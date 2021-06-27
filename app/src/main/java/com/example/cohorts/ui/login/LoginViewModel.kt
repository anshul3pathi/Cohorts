package com.example.cohorts.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.NetworkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _userRegistered = MutableLiveData(NetworkRequest.LOADING)
    val userRegistered: LiveData<NetworkRequest> = _userRegistered

    fun registerCurrentUser() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = repository.registerCurrentUser()
            if (result.succeeded) {
                _userRegistered.postValue(NetworkRequest.SUCCESS)
            } else {
                _userRegistered.postValue(NetworkRequest.FAILURE)
                result as Result.Error
                Timber.e(result.exception)
            }
        }
    }

}
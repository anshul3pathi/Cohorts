package com.example.cohorts.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    fun fetchChatReference(cohortUid: String): DatabaseReference? {
        val result = repository.fetchChatReference(cohortUid)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            _errorMessage.postValue((result as Result.Error).exception.message)
            null
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = repository.getCurrentUser()
            if (result.succeeded) {
                _currentUser.postValue((result as Result.Success).data!!)
            } else {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

    fun sendNewMessage(textMessage: String, cohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val newMessage = ChatMessage(
                text = textMessage,
                name = _currentUser.value!!.userName,
                userUid = _currentUser.value!!.uid,
                chatOfCohort = cohortUid
            )
            val result = repository.sendNewChatMessage(newMessage)
            if (!result.succeeded) {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

}
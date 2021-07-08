package com.example.cohorts.ui.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.chat.ChatRepo
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.repository.meeting.MeetingRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.initJitsi
import com.example.cohorts.jitsi.launchJitsi
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val chatRepository: ChatRepo,
    private val meetingRepository: MeetingRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User> = _currentUser

    private val _cohortDeleted = MutableLiveData(false)
    val cohortDeleted: LiveData<Boolean> = _cohortDeleted

    fun fetchChatReference(cohortUid: String): DatabaseReference? {
        val result = chatRepository.fetchChatReference(cohortUid)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            _errorMessage.postValue((result as Result.Error).exception.message)
            null
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = cohortsRepository.getCurrentUser()
            if (result.succeeded) {
                _currentUser.postValue((result as Result.Success).data)
            } else {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

    fun startNewMeeting(ofCohort: Cohort, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = meetingRepository.startNewMeeting(ofCohort.cohortUid)
            if (result.succeeded) {
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                result as Result.Error
                _errorMessage.postValue(result.exception.message)
            }
        }
    }

    fun initialiseJitsi(broadcastReceiver: BroadcastReceiver, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val currentUser = cohortsRepository.getCurrentUser()
            if (currentUser.succeeded) {
                currentUser as Result.Success
                initJitsi(currentUser.data, broadcastReceiver, context)
            }
        }
    }

    fun deleteThisCohort(cohort: Cohort) {
        GlobalScope.launch(coroutineDispatcher) {
            val result = cohortsRepository.deleteThisCohort(cohort)
            if (result.succeeded) {
                _cohortDeleted.postValue(true)
            } else {
                val exception = (result as Result.Error).exception
                _cohortDeleted.postValue(false)
                _errorMessage.postValue(exception.message)
            }
        }
    }

    fun sendNewMessage(textMessage: String, cohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val newMessage = ChatMessage(
                text = textMessage,
                name = _currentUser.value!!.userName,
                userUid = _currentUser.value!!.uid,
                chatOfCohort = cohortUid,
                photoUrl = _currentUser.value!!.photoUrl
            )
            Timber.d("$newMessage")
            Timber.d("${_currentUser.value!!.photoUrl}")
            val result = chatRepository.sendNewChatMessage(newMessage)
            if (!result.succeeded) {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

    fun sendImageMessage(imageUri: Uri?, cohortUid: String) {
        val newImageMessage = ChatMessage(
            text = null,
            name = _currentUser.value!!.userName,
            userUid = _currentUser.value!!.uid,
            chatOfCohort = cohortUid,
            photoUrl = _currentUser.value!!.photoUrl
        )
        GlobalScope.launch(coroutineDispatcher) {
            val result = chatRepository.sendImageMessage(newImageMessage, imageUri)
            if (!result.succeeded) {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

}
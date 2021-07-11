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
import com.example.cohorts.core.repository.user.UserRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.initJitsi
import com.example.cohorts.jitsi.launchJitsi
import com.example.cohorts.utils.Event
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject
/**
 * ViewModel for the Chat list screen
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val chatRepository: ChatRepo,
    private val meetingRepository: MeetingRepo,
    private val userRepository: UserRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User> = _currentUser

    private val _cohortDeleted = MutableLiveData<Event<Boolean>>()
    val cohortDeleted: LiveData<Event<Boolean>> = _cohortDeleted

    /**
     * @param [cohortUid] uid of the cohort whose chat is required
     */
    fun fetchChatReference(cohortUid: String): DatabaseReference? {
        val result = chatRepository.fetchChatReference(cohortUid)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            val exception = (result as Result.Error).exception
            Timber.e(exception, "error fetching chat reference")
            _snackbarMessage.postValue(Event("Error fetching chat reference!"))
            null
        }
    }

    /**
     * Gets the data of current user and posts the value in currentUser liveData
     */
    fun getCurrentUser() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = cohortsRepository.getCurrentUser()
            if (result.succeeded) {
                _currentUser.postValue((result as Result.Success).data)
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "Error getting current user!")
                _snackbarMessage.postValue(
                    Event("There was some error. Check you internet or try again later")
                )
            }
        }
    }

    /**
     * Starts a new meeting in given cohort
     *
     * @param ofCohort Cohort object containing the data of cohort whose new meeting is started
     * @param context [Context]
     */
    fun startNewMeeting(ofCohort: Cohort, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = meetingRepository.startNewMeeting(ofCohort.cohortUid)
            if (result.succeeded) {
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error starting a new meeting.")
                _snackbarMessage.postValue(
                    Event("Couldn't start a new meeting. Check your internet or try again later.")
                )
            }
        }
    }

    /**
     * Initialises Jitsi with broadcastReceiver and the context
     *
     * @param broadcastReceiver for listening the broadcast events
     * @param context [Context]
     */
    fun initialiseJitsi(broadcastReceiver: BroadcastReceiver, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser.succeeded) {
                currentUser as Result.Success
                initJitsi(currentUser.data, broadcastReceiver, context)
            } else {
                val exception = (currentUser as Result.Error).exception
                Timber.e(exception, "error getting current user.")
                _snackbarMessage.postValue(Event("There was some error."))
            }
        }
    }

    /**
     * Deletes the given cohort from database
     *
     * @param cohort object containing data data of [Cohort] to be deleted
     */
    fun deleteThisCohort(cohort: Cohort) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = cohortsRepository.deleteThisCohort(cohort)
            if (result.succeeded) {
                _cohortDeleted.postValue(Event(true))
                _snackbarMessage.postValue(Event("Cohort deleted successfully!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error deleting cohort.")
                _snackbarMessage.postValue(Event("There was an error deleting the cohort."))
            }
        }
    }

    /**
     * Sends a  new text message in [Cohort]
     *
     * @param textMessage the text to be sent
     * @param cohortUid uid of the [Cohort] in which the text is sent
     */
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
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error sending message")
                _snackbarMessage.postValue(Event("There was an error sending the message"))
            }
        }
    }

    /**
     * Sends a  new image message in [Cohort]
     *
     * @param imageUri uri of the image to be sent
     * @param cohortUid uid of the [Cohort] in which the image is sent
     */
    fun sendImageMessage(imageUri: Uri?, cohortUid: String) {
        val newImageMessage = ChatMessage(
            text = null,
            name = _currentUser.value!!.userName,
            userUid = _currentUser.value!!.uid,
            chatOfCohort = cohortUid,
            photoUrl = _currentUser.value!!.photoUrl
        )
        /**
         * CoroutineScope ensures that the image message is saved in database
         * even if the [ChatViewModel] is destroyed.
         */
        CoroutineScope(coroutineDispatcher).launch {
            val result = chatRepository.sendImageMessage(newImageMessage, imageUri)
            if (!result.succeeded) {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error sending image")
                _snackbarMessage.postValue(Event("There was an error sending the image"))
            }
        }
    }

}
package com.example.cohorts.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.repository.meeting.MeetingRepo
import com.example.cohorts.core.repository.theme.ThemeRepo
import com.example.cohorts.core.repository.user.UserRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.destroyJitsi
import com.example.cohorts.utils.Theme
import com.example.cohorts.utils.toTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeRepository: ThemeRepo,
    private val meetingRepository: MeetingRepo,
    private val userRepository: UserRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentAppTheme = MutableLiveData(getAppTheme())
    val currentAppTheme: LiveData<Theme> = _currentAppTheme

    fun terminateOngoingMeeting(context: Context, broadcastReceiver: BroadcastReceiver) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = meetingRepository.leaveOngoingMeeting()
            destroyJitsi(context, broadcastReceiver)
            if (result.succeeded) {
                Timber.i("left the meeting")
            } else {
                val exception = (result as Result.Error).exception
                _errorMessage.postValue(exception.message)
                Timber.e(exception)
            }
        }
    }

    fun onDestroy(context: Context, broadcastReceiver: BroadcastReceiver) {
        destroyJitsi(context, broadcastReceiver)
        CoroutineScope(coroutineDispatcher).launch {
            meetingRepository.onDestroy()
        }
    }

    fun changeAppTheme(value: Int) {
        val theme = value.toTheme()
        _currentAppTheme.postValue(theme)
        themeRepository.saveAppTheme(theme)
    }

    fun signOut() {
        Timber.d("Signing out!")
        CoroutineScope(coroutineDispatcher).launch {
            userRepository.signOut()
        }
    }

    private fun getAppTheme() = themeRepository.getAppTheme()

}
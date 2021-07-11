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

/**
 * ViewModel for MainActivity
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeRepository: ThemeRepo,
    private val meetingRepository: MeetingRepo,
    private val userRepository: UserRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    init {
        // load the data of current user as the user is now logged in
        userRepository.initialiseCurrentUser()
    }

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentAppTheme = MutableLiveData(getAppTheme())
    val currentAppTheme: LiveData<Theme> = _currentAppTheme

    /**
     * Terminate the ongoing meeting
     *
     * Detaches the broadcast receiver from Jitsi and removes the user from the ongoing
     * meeting
     *
     * @param context [Context]
     * @param broadcastReceiver broadcast receiver listening to Jitsi events
     */
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

    /**
     * Remove user from any ongoing meeting and terminate the meetings
     *
     * Removes the user from meetings of any cohort the user maybe in and
     * detaches the broadcast receiver from Jitsi
     *
     * @param context [Context]
     * @param broadcastReceiver listens to the event broadcasts by Jitsi
     * */
    fun onDestroy(context: Context, broadcastReceiver: BroadcastReceiver) {
        destroyJitsi(context, broadcastReceiver)
        CoroutineScope(coroutineDispatcher).launch {
            meetingRepository.onDestroy()
        }
    }

    /**
     * Change the app theme
     *
     * Changes the current theme value of _currentTheme liveData and saves the updated
     * theme
     *
     * @param value [Int] value that will be mapped to app theme
     */
    fun changeAppTheme(value: Int) {
        val theme = value.toTheme()
        _currentAppTheme.postValue(theme)
        themeRepository.saveAppTheme(theme)
    }

    /**
     * Sign the current user out
     */
    fun signOut() {
        Timber.d("Signing out!")
        CoroutineScope(coroutineDispatcher).launch {
            userRepository.signOut()
        }
    }

    private fun getAppTheme() = themeRepository.getAppTheme()

}
package com.example.cohorts.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.repository.ThemeRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.destroyJitsi
import com.example.cohorts.utils.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val themeRepository: ThemeRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentAppTheme = MutableLiveData(getCurrentAppTheme())
    val currentAppTheme: LiveData<Theme> = _currentAppTheme

    fun terminateOngoingMeeting(context: Context, broadcastReceiver: BroadcastReceiver) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = cohortsRepository.leaveOngoingMeeting()
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

    fun changeAppTheme(value: Int) {
        val theme = when (value) {
            0 -> Theme.LIGHT
            1 -> Theme.DARK
            else -> Theme.SYSTEM_DEFAULT
        }
        _currentAppTheme.postValue(theme)
        themeRepository.saveAppTheme(theme)
    }

    private fun getCurrentAppTheme() = themeRepository.getAppTheme()

}
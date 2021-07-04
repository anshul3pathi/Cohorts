package com.example.cohorts.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.destroyJitsi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun terminateOngoingMeeting(context: Context, broadcastReceiver: BroadcastReceiver) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = repository.leaveOngoingMeeting()
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

}
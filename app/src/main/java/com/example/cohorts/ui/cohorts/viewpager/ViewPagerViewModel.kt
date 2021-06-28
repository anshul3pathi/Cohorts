package com.example.cohorts.ui.cohorts.viewpager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.destroyJitsi
import com.example.cohorts.jitsi.initJitsi
import com.example.cohorts.jitsi.launchJitsi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.BroadcastReceiver
import javax.inject.Inject

@HiltViewModel
class ViewPagerViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _inMeeting = MutableLiveData(false)
    val inMeeting: LiveData<Boolean> = _inMeeting

    private val _errorOccurred = MutableLiveData("")
    val errorOccurred: LiveData<String> = _errorOccurred

    fun startNewMeeting(ofCohort: Cohort, context: Context) {
        viewModelScope.launch(dispatcher) {
            val result = repository.startNewMeeting(ofCohort)
            if (result.succeeded) {
                _inMeeting.postValue(true)
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                result as Result.Error
                _errorOccurred.postValue(result.exception.message)
            }
        }
    }

    fun initialiseJitsi(broadcastReceiver: BroadcastReceiver, context: Context) {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser.succeeded) {
                currentUser as Result.Success
                initJitsi(currentUser.data, broadcastReceiver, context)
            }
        }
    }

    fun terminateOngoingMeeting(context: Context, broadcastReceiver: BroadcastReceiver) {
        viewModelScope.launch(dispatcher) {
            repository.leaveOngoingMeeting()
            destroyJitsi(context, broadcastReceiver)
        }
    }

}
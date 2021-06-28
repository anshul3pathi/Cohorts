package com.example.cohorts.ui.cohorts.viewpager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewPagerViewModel @Inject constructor(
    private val repository: CohortsRepo
) : ViewModel() {

    private val _inMeeting = MutableLiveData(false)
    val inMeeting: LiveData<Boolean> = _inMeeting

    private val _errorOccurred = MutableLiveData("")
    val errorOccurred: LiveData<String> = _errorOccurred

    fun startNewMeeting(cohort: Cohort) {
        viewModelScope.launch {
            val result = repository.startNewMeeting(cohort)
            if (result.succeeded) {
                _inMeeting.postValue(true)
            } else {
                result as Result.Error
                _inMeeting.postValue(false)
                _errorOccurred.postValue(result.exception.message)
            }
        }
    }

}
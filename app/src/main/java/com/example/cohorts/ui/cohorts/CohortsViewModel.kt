package com.example.cohorts.ui.cohorts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CohortsViewModel @Inject constructor(
    private val repository: CohortsRepo
) : ViewModel() {

    private val _userAddedToMeeting = MutableLiveData(false)
    val userAddedToMeeting: LiveData<Boolean> = _userAddedToMeeting

    private val _errorAddingUserToMeeting = MutableLiveData(false)
    val errorAddingUserToMeeting: LiveData<Boolean> = _errorAddingUserToMeeting

    private var _cohort: Cohort? = null
    val cohort: Cohort? = _cohort

    fun fetchCohortsQuery(): Query {
        val query = repository.fetchCohortsQuery()
        query as Result.Success
        return query.data
    }

    fun addCurrentUserToMeeting(cohort: Cohort) {
        viewModelScope.launch {
            val result = repository.addCurrentUserToMeeting(cohort)
            if (result.succeeded) {
                _userAddedToMeeting.postValue(true)
            } else {
                _errorAddingUserToMeeting.postValue(true)
            }
        }
    }

    fun resetUserAddedToMeeting() {
        _userAddedToMeeting.postValue(false)
    }

    fun resetErrorAddingUserToMeeting() {
        _errorAddingUserToMeeting.postValue(false)
    }

}
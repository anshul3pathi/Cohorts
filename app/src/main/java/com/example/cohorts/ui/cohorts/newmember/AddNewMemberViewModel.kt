package com.example.cohorts.ui.cohorts.newmember

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for AddNewMember screen
 */
@HiltViewModel
class AddNewMemberViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    /**
     * Add new member to given [Cohort]
     *
     * @param cohort object containing [Cohort] info
     * @param userEmail email of the user to be added
     */
    fun addNewMemberToCohort(cohort: Cohort, userEmail: String) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = repository.addNewMemberToCohort(cohort, userEmail)
            if (result.succeeded) {
                val user = (result as Result.Success).data.toString()
                _snackbarMessage.postValue(
                    Event("$user added successfully to ${cohort.cohortName}")
                )
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error adding member to cohort")
                _snackbarMessage.postValue(
                    Event("Cannot add user to cohort because - ${exception.message}")
                )
            }
        }
    }

}
package com.example.cohorts.ui.cohorts.newcohort

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for AddNewCohort screen*/
@HiltViewModel
class AddNewCohortViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    /**
     * Adds new Cohort to firestore database
     *
     * @param newCohort object containing the data of the newCohort to be added
     */
    fun addNewCohort(newCohort: Cohort) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = repository.addNewCohort(newCohort)
            if (result.succeeded) {
                _snackbarMessage.postValue(Event("Cohort was added successfully!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error adding new cohort")
                _snackbarMessage.postValue(
                    Event("There was some error in creating new cohort")
                )
            }
        }
    }

}
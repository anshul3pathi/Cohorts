package com.example.cohorts.ui.cohorts.newcohort

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewCohortViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _cohortAddedSuccessfully = MutableLiveData(false)
    val cohortAddedSuccessfully: LiveData<Boolean> = _cohortAddedSuccessfully

    fun addNewCohort(newCohort: Cohort) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = repository.addNewCohort(newCohort)
            if (result.succeeded) {
                _cohortAddedSuccessfully.postValue(true)
            } else {
                result as Result.Error
                _errorMessage.postValue(result.exception.message)
            }
        }
    }

}
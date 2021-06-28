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
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    fun addNewCohort(newCohort: Cohort) {
        viewModelScope.launch(dispatcher) {
            val currentUser = repository.getCurrentUser()
            if (currentUser.succeeded) {
                currentUser as Result.Success
                // adding currently logged user to the new Cohort
                newCohort.numberOfMembers += 1
                newCohort.cohortMembers.add(currentUser.data.uid!!)
            } else {
                _errorMessage.postValue("User not logged in!")
                return@launch
            }
            val result = repository.saveCohort(newCohort)
            if (!result.succeeded) {
                val error = (result as Result.Error).exception
                _errorMessage.postValue(error.message)
            }
        }
    }

}
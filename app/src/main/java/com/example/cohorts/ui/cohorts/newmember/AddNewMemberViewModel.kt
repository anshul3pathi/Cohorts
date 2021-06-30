package com.example.cohorts.ui.cohorts.newmember

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewMemberViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _userAddedSuccessfully = MutableLiveData("")
    val userAddedSuccessfully: LiveData<String> = _userAddedSuccessfully

    fun addNewMemberToCohort(cohort: Cohort, userEmail: String) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = repository.addNewMemberToCohort(cohort, userEmail)
            if (result.succeeded) {
                _userAddedSuccessfully.postValue((result as Result.Success).data.toString())
            } else {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

}
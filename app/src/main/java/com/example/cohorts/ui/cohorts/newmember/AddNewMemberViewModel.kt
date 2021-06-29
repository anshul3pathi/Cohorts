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

    private val _userAddedSuccessfully = MutableLiveData(false)
    val userAddedSuccessfully: LiveData<Boolean> = _userAddedSuccessfully

    fun addNewMemberToCohort(cohort: Cohort, userEmail: String) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = repository.getUserByEmail(userEmail)
            if (result.succeeded) {
                val user = (result as Result.Success).data
                if (user.uid!! !in cohort.cohortMembers) {
                    cohort.cohortMembers.add(user.uid!!)
                    cohort.numberOfMembers += 1
                    repository.saveCohort(cohort)
                    _userAddedSuccessfully.postValue(true)
                } else {
                    _errorMessage.postValue("User is already in Cohort!")
                }
            } else {
                val exception = (result as Result.Error).exception
                _errorMessage.postValue(exception.message)
            }
        }
    }

}
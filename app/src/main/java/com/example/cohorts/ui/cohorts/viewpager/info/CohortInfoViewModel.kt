package com.example.cohorts.ui.cohorts.viewpager.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.repository.user.UserRepo
import com.example.cohorts.core.succeeded
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CohortInfoViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val userRepository: UserRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _userSuccessfullyRemovedMessage = MutableLiveData<String>()
    val userSuccessfullyRemovedMessage: LiveData<String> = _userSuccessfullyRemovedMessage

    private val _cohortInfoUpdatedMessage = MutableLiveData<String>()
    val cohortInfoUpdatedMessage: LiveData<String> = _cohortInfoUpdatedMessage

    fun fetchUsersQuery(cohortUid: String): Query {
        val result = cohortsRepository.fetchUsersQuery(cohortUid)
        return (result as Result.Success).data
    }

    fun getCurrentUser() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = userRepository.getCurrentUser()
            if (result.succeeded) {
                Timber.d("got current user - ${(result as Result.Success).data}")
                _currentUser.postValue(result.data!!)
            } else {
                Timber.e((result as Result.Error).exception, "couldn't get current user")
                _errorMessage.postValue("There was some error removing the user.")
            }
        }
    }

    fun removeThisUserFromCohort(user: User, cohort: Cohort) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = cohortsRepository.removeThisUserFromCohort(user, cohort)
            if (result.succeeded) {
                _userSuccessfullyRemovedMessage.postValue((result as Result.Success).data as String)
            } else {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

    fun updateThisCohort(cohort: Cohort) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = cohortsRepository.saveCohort(cohort)
            if (result.succeeded) {
                _cohortInfoUpdatedMessage.postValue("Cohort info was updated!")
            } else {
                _errorMessage.postValue((result as Result.Error).exception.message)
            }
        }
    }

}
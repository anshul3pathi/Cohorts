package com.example.cohorts.ui.tasks

import androidx.lifecycle.*
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Task
import com.example.cohorts.core.repository.tasks.TasksRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.Event
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    fun fetchTaskReference(cohortUid: String): DatabaseReference? {
        val result = tasksRepository.fetchTaskReference(cohortUid)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            val errorMessage = (result as Result.Error).exception.message
            _snackbarMessage.postValue(Event("There was some error."))
            null
        }
    }

    fun markTaskCompleteOrActive(task: Task) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.markTaskCompleteOrActive(task)
            if (!result.succeeded) {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "Could not change status of task")
                _snackbarMessage.postValue(Event("Couldn't change status of task!"))
            }
        }
    }

    fun clearCompletedTasks(ofCohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.clearCompletedTasks(ofCohortUid)
            if (result.succeeded) {
                _snackbarMessage
                    .postValue(Event("Completed tasks are deleted."))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "clear completed tasks failed.")
                _snackbarMessage.postValue(Event("Couldn't delete completed tasks!"))
            }
        }
    }

    fun clearAllTasks(ofCohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.clearAllTasks(ofCohortUid)
            if (result.succeeded) {
                _snackbarMessage.postValue(Event("All tasks are deleted."))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "clear all tasks failed.")
                _snackbarMessage.postValue(Event("Couldn't clear all tasks!"))
            }
        }
    }

}
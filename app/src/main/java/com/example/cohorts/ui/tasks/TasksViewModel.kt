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

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val _allTasksDeletedMessage = MutableLiveData<Event<String>>()
    val allTasksDeletedMessage: LiveData<Event<String>> = _allTasksDeletedMessage

    fun fetchChatReference(cohortUid: String): DatabaseReference? {
        val result = tasksRepository.fetchTaskReference(cohortUid)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            val errorMessage = (result as Result.Error).exception.message
            _errorMessage.postValue(Event(errorMessage?: ""))
            null
        }
    }

    fun markTaskCompleteOrActive(task: Task) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.markTaskCompleteOrActive(task)
            if (!result.succeeded) {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "Could not change status of task")
                _errorMessage.postValue(Event(exception.message?: ""))
            }
        }
    }

    fun clearAllTasks(ofCohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.clearAllTasks(ofCohortUid)
            if (result.succeeded) {
                _allTasksDeletedMessage.postValue(Event("All tasks are deleted."))
            }
        }
    }

}
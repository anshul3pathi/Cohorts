package com.example.cohorts.ui.tasks.taskdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Task
import com.example.cohorts.core.repository.tasks.TasksRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val tasksRepository: TasksRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    fun saveChangesToTask(task: Task) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = tasksRepository.updateTask(task)
            if (result.succeeded) {
                _snackbarMessage.postValue(Event("Task was edited!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "Couldn't edit task.")
                _snackbarMessage.postValue(Event("Couldn't edit task!"))
            }
        }
    }

    fun deleteTask(task: Task) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = tasksRepository.deleteTask(task)
            if (result.succeeded) {
                _snackbarMessage.postValue(Event("Task was deleted!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error deleting task.")
                _snackbarMessage.postValue(Event("There was some error deleting task."))
            }
        }
    }

    fun markTaskCompletedOrActive(task: Task) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = tasksRepository.markTaskCompleteOrActive(task)
            if (!result.succeeded) {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "cannot change status of task.")
                _snackbarMessage.postValue(Event("There was some error!"))
            }
        }
    }

}
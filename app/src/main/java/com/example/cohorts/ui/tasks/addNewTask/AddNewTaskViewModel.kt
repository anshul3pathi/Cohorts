package com.example.cohorts.ui.tasks.addNewTask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.Task
import com.example.cohorts.core.repository.tasks.TasksRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddNewTaskViewModel @Inject constructor(
    private val tasksRepository: TasksRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    fun addNewTask(newTask: Task, cohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.addNewTask(newTask, cohortUid)
            if (result.succeeded) {
                _snackbarMessage.postValue(Event("Task added successfully!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "error adding new task")
                _snackbarMessage.postValue(Event("Couldn't add new task."))
            }
        }
    }

}
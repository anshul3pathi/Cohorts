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

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val _taskEditedMessage = MutableLiveData<Event<String>>()
    val taskEditedMessage: LiveData<Event<String>> = _taskEditedMessage

    fun saveChangesToTask(task: Task) {
        CoroutineScope(coroutineDispatcher).launch {
            val result = tasksRepository.updateTask(task)
            if (result.succeeded) {
                _taskEditedMessage.postValue(Event("Task was edited!"))
            } else {
                val exception = (result as Result.Error).exception
                Timber.e(exception, "Couldn't edit task.")
                _errorMessage.postValue(Event("Couldn't edit task!"))
            }
        }
    }

}
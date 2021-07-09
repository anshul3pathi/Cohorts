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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewTaskViewModel @Inject constructor(
    private val tasksRepository: TasksRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _taskAddedSuccessfully = MutableLiveData(false)
    val taskAddedSuccessfully: LiveData<Boolean> = _taskAddedSuccessfully

    fun addNewCohort(newTask: Task, cohortUid: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = tasksRepository.addNewTask(newTask, cohortUid)
            if (result.succeeded) {
                _taskAddedSuccessfully.postValue(true)
            } else {
                result as Result.Error
                _errorMessage.postValue(result.exception.message)
            }
        }
    }

}
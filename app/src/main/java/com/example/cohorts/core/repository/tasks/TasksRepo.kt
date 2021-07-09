package com.example.cohorts.core.repository.tasks

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Task
import com.google.firebase.database.DatabaseReference


interface TasksRepo {

    fun fetchTaskReference(cohortUid: String): Result<DatabaseReference>
    suspend fun addNewTask(task: Task, cohortUid: String): Result<Any>
    suspend fun markTaskCompleteOrActive(task: Task): Result<Any>
    suspend fun clearAllTasks(ofCohortUid: String): Result<Any>

}
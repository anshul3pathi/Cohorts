package com.example.cohorts.core.repository.tasks

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Task
import com.google.firebase.database.DatabaseReference

/**
 * Interface for interacting with the [Task] firebase database layer
 */
interface TasksRepo {

    fun fetchTaskReference(cohortUid: String): Result<DatabaseReference>

    suspend fun addNewTask(task: Task, cohortUid: String): Result<Any>

    suspend fun markTaskCompleteOrActive(task: Task): Result<Any>

    suspend fun clearAllTasks(ofCohortUid: String): Result<Any>

    suspend fun clearCompletedTasks(ofCohortUid: String): Result<Any>

    suspend fun updateTask(task: Task): Result<Any>

    suspend fun deleteTask(task: Task): Result<Any>

}
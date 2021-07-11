package com.example.cohorts.core.repository.tasks

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Task
import com.example.cohorts.utils.safeCall
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Concrete implementation of [TasksRepo] for dealing with the [Task] firebase
 * database layer
 */
class TasksRepository @Inject constructor(
    firebaseDatabase: FirebaseDatabase
) : TasksRepo {

    companion object {
        private const val TASKS_CHILD = "tasks"
    }

    private val tasksReference = firebaseDatabase.reference.child(TASKS_CHILD)

    /**
     * Fetch the reference to the tasks of a cohort
     *
     * @param cohortUid uid of the cohort whose [Task]s are required
     * @return [DatabaseReference] of the [Task]s in firebase database, wrapped
     * in [Result]
     */
    override fun fetchTaskReference(cohortUid: String): Result<DatabaseReference> {
        return safeCall {
            Result.Success(tasksReference.child(cohortUid))
        }
    }

    /**
     * Add a new [Task] to a cohort
     *
     * @param task object containing the data of a task
     * @param cohortUid uid of the cohort to which given task belongs
     * @return [Any] wrapped in [Result]
     */
    override suspend fun addNewTask(task: Task, cohortUid: String): Result<Any> {
        return safeCall {
            tasksReference.child(cohortUid).child(task.taskId).setValue(task).await()
            Result.Success(Any())
        }
    }

    /**
     * Toggle the status of a given task
     *
     * If the given [Task] is active, then mark it complete and if it is
     * completed, then mark it active
     *
     * @param task object containing the data of the task
     * @return [Any] wrapped in [Result]
     */
    override suspend fun markTaskCompleteOrActive(task: Task): Result<Any> {
        return safeCall {
            task.isCompleted = !task.isCompleted
            tasksReference.child(task.taskOfCohort!!).child(task.taskId).setValue(task).await()
            Result.Success(Any())
        }
    }

    /**
     * Delete all the tasks of a cohort
     *
     * @param ofCohortUid uid of the cohort whose tasks are to be deleted
     * @return [Any] wrapped in [Result]
     */
    override suspend fun clearAllTasks(ofCohortUid: String): Result<Any> {
        return safeCall {
            tasksReference.child(ofCohortUid).removeValue().await()
            Result.Success(Any())
        }
    }

    /**
     * Delete all the tasks that are completed in a cohort
     *
     * @param ofCohortUid uid of the cohort whose completed tasks are to be cleared
     * @return [Any] wrapped in [Result]
     */
    override suspend fun clearCompletedTasks(ofCohortUid: String): Result<Any> {
        return safeCall {
            val tasksSnapshot = tasksReference.child(ofCohortUid).get().await()
            for (snapshot in tasksSnapshot.children) {
                if (snapshot.exists()) {
                    val task = snapshot.getValue<Task>()!!
                    if (task.isCompleted) {
                        snapshot.ref.removeValue()
                    }
                }
            }
            Result.Success(Any())
        }
    }

    /**
     * Update the information of a given [Task]
     *
     * @param task object containing the updated info of a task for saving
     * @return [Any] wrapped in [Result]
     */
    override suspend fun updateTask(task: Task): Result<Any> {
        return safeCall {
            Timber.d("updating task - $task")
            tasksReference.child(task.taskOfCohort!!).child(task.taskId).setValue(task).await()
            Result.Success(Any())
        }
    }

    /**
     * Delete a given task form firebase database
     *
     * @param task task to be deleted
     * @return [Any] wrapped in [Result]
     */
    override suspend fun deleteTask(task: Task): Result<Any> {
        return safeCall {
            tasksReference.child(task.taskOfCohort!!).child(task.taskId).removeValue().await()
            Result.Success(Any())
        }
    }
}
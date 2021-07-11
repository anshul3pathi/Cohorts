package com.example.cohorts.core.model

import com.example.cohorts.utils.generateRandomString
import java.io.Serializable

/**
 * Model class of a task
 *
 * @param title title of the task
 * @param description description of the task
 * @param isCompleted true if task completed otherwise false
 * @param taskId unique id of this task
 * @param taskOfCohort unique id of the cohort this task belongs to
 */
data class Task(
    var title: String? = null,
    var description: String? = null,
    var isCompleted: Boolean = false,
    val taskId: String = generateRandomString(20),
    val taskOfCohort: String? = null // Uid of cohort to which this task belongs
) : Serializable
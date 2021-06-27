package com.example.cohorts.ui.cohorts

import android.view.View
import com.example.cohorts.core.model.Cohort

interface CohortClickListener {

    fun cohortItemClicked(cohort: Cohort)
    fun joinVideoCallButtonClicked(view: View, cohort: Cohort)

}
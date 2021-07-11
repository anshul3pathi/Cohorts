package com.example.cohorts.core.repository.theme

import com.example.cohorts.utils.Theme


/**
 * Interface for acting as a layer between Storage and UI layer
 */
interface ThemeRepo {

    fun saveAppTheme(theme: Theme)

    fun getAppTheme(): Theme

}
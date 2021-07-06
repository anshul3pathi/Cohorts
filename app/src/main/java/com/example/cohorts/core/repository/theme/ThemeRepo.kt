package com.example.cohorts.core.repository.theme

import com.example.cohorts.utils.Theme

interface ThemeRepo {

    fun saveAppTheme(theme: Theme)
    fun getAppTheme(): Theme

}
package com.example.cohorts.core.repository

import com.example.cohorts.utils.Theme

interface ThemeRepo {

    fun saveAppTheme(theme: Theme)
    fun getAppTheme(): Theme

}
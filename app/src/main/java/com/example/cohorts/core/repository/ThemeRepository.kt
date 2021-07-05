package com.example.cohorts.core.repository

import com.example.cohorts.core.storage.Storage
import com.example.cohorts.utils.Theme
import javax.inject.Inject

class ThemeRepository @Inject constructor(private val storage: Storage) : ThemeRepo {

    override fun saveAppTheme(theme: Theme) {
        val themeValue = when (theme) {
            Theme.LIGHT -> 0
            Theme.DARK -> 1
            else -> 2
        }
        storage.setTheme(themeValue)
    }

    override fun getAppTheme(): Theme {
        return when (storage.getTheme()) {
            0 -> Theme.LIGHT
            1 -> Theme.DARK
            else -> Theme.SYSTEM_DEFAULT
        }
    }

}
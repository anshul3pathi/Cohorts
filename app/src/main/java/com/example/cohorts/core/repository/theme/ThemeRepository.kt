package com.example.cohorts.core.repository.theme

import com.example.cohorts.core.storage.Storage
import com.example.cohorts.utils.Theme
import com.example.cohorts.utils.fromThemeToInt
import com.example.cohorts.utils.toTheme
import javax.inject.Inject

/**
 * Concrete implementation of the [ThemeRepo]
 */
class ThemeRepository @Inject constructor(private val storage: Storage) : ThemeRepo {

    /**
     * Save the current app theme
     *
     * @param theme current theme of the app
     */
    override fun saveAppTheme(theme: Theme) {
        val themeValue = theme.fromThemeToInt()

        storage.setTheme(themeValue)
    }

    /**
     * Get the current app theme
     *
     * @return current theme of the app
     */
    override fun getAppTheme(): Theme {
        return storage.getTheme().toTheme()
    }

}
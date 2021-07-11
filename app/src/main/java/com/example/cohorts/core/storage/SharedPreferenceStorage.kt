package com.example.cohorts.core.storage

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.cohorts.utils.Theme

/**
 * Concrete implementation of [Storage]
 *
 * Stores and retrieves the current app theme from [SharedPreferenceStorage]
 */
class SharedPreferenceStorage @Inject constructor(
    @ApplicationContext context: Context
) : Storage {

    companion object {
        private const val SHARED_PREFERENCE_KEY = "COHORTS_SHARED_PREFERENCE"
        private const val THEME_KEY = "APP_THEME"
    }

    private val sharedPref = context.getSharedPreferences(
        SHARED_PREFERENCE_KEY,
        Context.MODE_PRIVATE
    )

    /**
     * Save the current app theme value as an [Int]
     *
     * @param value integer corresponding to the value of [Theme]
     */
    override fun setTheme(value: Int) {
        sharedPref.edit {
            putInt(THEME_KEY, value)
        }
    }

    /**
     * Get the current app theme that was saved as an [Int]
     *
     * @return integer value corresponding to the [Theme]*/
    override fun getTheme(): Int {
        return sharedPref.getInt(THEME_KEY, 0)
    }

}
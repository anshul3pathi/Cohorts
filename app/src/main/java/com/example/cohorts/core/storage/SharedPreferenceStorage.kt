package com.example.cohorts.core.storage

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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

    override fun setTheme(value: Int) {
        sharedPref.edit {
            putInt(THEME_KEY, value)
        }
    }

    override fun getTheme(): Int {
        return sharedPref.getInt(THEME_KEY, 0)
    }

}
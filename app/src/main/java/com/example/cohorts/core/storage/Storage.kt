package com.example.cohorts.core.storage

/**
 * Interface that acts as a layer between SharedPreferences and the app
 */
interface Storage {

    fun setTheme(value: Int)

    fun getTheme(): Int

}
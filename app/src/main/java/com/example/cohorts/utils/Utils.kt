package com.example.cohorts.utils


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import com.example.cohorts.core.Result
import com.google.android.material.snackbar.Snackbar

/**
 * Generated a random string of given length
 *
 * @param length length of string required
 */
fun generateRandomString(length: Int = 10): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map{ allowedChars.random() }
        .joinToString("")
}

/**
 * This function takes a lambda and calls it inside a try and catch block
 *
 * @param T
 * @param action () -> Result<T>
 * @return T wrapped in [Result]
 */
inline fun <T> safeCall(action: () -> Result<T>): Result<T> {
    return try {
        action()
    } catch (e: Exception) {
        Result.Error(e)
    }
}

/**
 * View extension function for showing a snackbar
 */
fun View.snackbar(message: String, lengthShort: Boolean = false) {
    Snackbar.make(
        this,
        message, if (lengthShort) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_LONG
    ).show()
}

/**
 * Theme Enum class
 */
enum class Theme {
    LIGHT, DARK, SYSTEM_DEFAULT
}

/**
 * Extension function on [Theme] which converts [Theme] to [Int]
 *
 * @return [Int]
 */
fun Theme.fromThemeToInt(): Int {
    return when (this) {
        Theme.LIGHT -> 0
        Theme.DARK -> 1
        Theme.SYSTEM_DEFAULT -> 2
    }
}

/**
 * Extension function on [Int] which converts an [Int] to [Theme]
 *
 * @return [Theme]
 */
fun Int.toTheme(): Theme {
    return when (this) {
        0 -> Theme.LIGHT
        1 -> Theme.DARK
        else -> Theme.SYSTEM_DEFAULT
    }
}

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}
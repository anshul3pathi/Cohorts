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

fun generateRandomString(length: Int = 10): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map{ allowedChars.random() }
        .joinToString("")
}

inline fun <T> safeCall(action: () -> Result<T>): Result<T> {
    return try {
        action()
    } catch (e: Exception) {
        Result.Error(e)
    }
}

fun View.snackbar(message: String, lengthShort: Boolean = false) {
    Snackbar.make(
        this,
        message, if (lengthShort) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_LONG
    ).show()
}

fun snackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

enum class Theme {
    LIGHT, DARK, SYSTEM_DEFAULT
}

fun Theme.fromThemeToInt(): Int {
    return when (this) {
        Theme.LIGHT -> 0
        Theme.DARK -> 1
        Theme.SYSTEM_DEFAULT -> 2
    }
}

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
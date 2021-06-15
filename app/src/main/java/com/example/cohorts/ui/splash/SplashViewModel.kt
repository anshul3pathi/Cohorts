package com.example.cohorts.ui.splash

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _navigateToMainActivity = MutableLiveData(false)
    val navigateToLiveData: LiveData<Boolean>
        get() = _navigateToMainActivity

    init {
        startTimer()
    }

    private fun startTimer() {
        object: CountDownTimer(3500L, 100L) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                _navigateToMainActivity.value = true
            }
        }.start()
    }

}
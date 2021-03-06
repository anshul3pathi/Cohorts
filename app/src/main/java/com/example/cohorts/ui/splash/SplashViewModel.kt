package com.example.cohorts.ui.splash

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.repository.theme.ThemeRepo
import com.example.cohorts.core.repository.user.UserRepo
import com.example.cohorts.utils.Event
import com.example.cohorts.utils.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Splash screen
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val themeRepository: ThemeRepo,
    private val userRepository: UserRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _navigate = MutableLiveData<Event<Boolean>>()
    val navigate: LiveData<Event<Boolean>> = _navigate

    private val _appTheme = MutableLiveData(getAppTheme())
    val appTheme: LiveData<Theme> = _appTheme

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    init {
        startTimer()
        checkIfUserLoggedIn()
    }

    fun initialiseCurrentUser() {
        userRepository.initialiseCurrentUser()
    }

    /**
     * Start the timer and navigate to either MainActivity or LoginActivity
     */
    private fun startTimer() {
        object: CountDownTimer(1500L, 500L) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                _navigate.postValue(Event(true))
            }
        }.start()
    }

    private fun getAppTheme() = themeRepository.getAppTheme()

    private fun checkIfUserLoggedIn() {
        viewModelScope.launch(coroutineDispatcher) {
            val isLoggedIn = userRepository.isUserLoggedIn()
            if (isLoggedIn) {
                _isUserLoggedIn.postValue(true)
            } else {
                _isUserLoggedIn.postValue(false)
            }
        }
    }
}
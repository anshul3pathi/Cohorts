package com.example.cohorts.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivitySplashScreenBinding
import com.example.cohorts.ui.main.MainActivity
import com.example.cohorts.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        auth = FirebaseAuth.getInstance()

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        viewModel.navigateToLiveData.observe(this, { navigateToMainActivity ->
            if (navigateToMainActivity) {
                if (auth.currentUser != null) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        })
    }
}
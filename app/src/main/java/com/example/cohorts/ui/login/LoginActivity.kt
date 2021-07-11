package com.example.cohorts.ui.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityLoginBinding
import com.example.cohorts.ui.main.MainActivity
import com.example.cohorts.utils.snackbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Activity for logging the user in using the Firebase's AuthUI
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 1
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        subscribeToObservers()

        launchSignInFlow()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                loginViewModel.registerCurrentUser()
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()
            } else {
                // user pressed back button or there was an error
                Timber.e("onActivityResult: ${response?.error}")
                finish()
            }
        }
    }

    /**
     * Launch the firebase's AuthUI
     */
    private fun launchSignInFlow() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.cohorts_signin_logo)
                .setTheme(R.style.Theme_Cohorts)
                .build(),
            RC_SIGN_IN
        )
    }

    private fun subscribeToObservers() {
        loginViewModel.snackbarMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.loginRootLayout.snackbar(it)
            }
        })
    }

}
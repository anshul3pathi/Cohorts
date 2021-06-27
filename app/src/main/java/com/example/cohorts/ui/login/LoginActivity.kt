package com.example.cohorts.ui.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityLoginBinding
import com.example.cohorts.core.model.User
import com.example.cohorts.ui.MainActivity
import com.example.cohorts.utils.NetworkRequest
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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
        loginViewModel.userRegistered.observe(this, { userRegistered ->
            if (userRegistered == NetworkRequest.SUCCESS) {
                Timber.d("Logged in!")
                Snackbar.make(binding.loginRootLayout, "Signed in!", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                Timber.d("Error saving user")
                Snackbar
                    .make(binding.loginRootLayout, "Error saving user", Snackbar.LENGTH_LONG)
                    .show()
            }
        })

        launchSignInFlow()

    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Timber.d("onActivityResult: Successfully signed in")
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

}
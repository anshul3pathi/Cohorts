package com.example.cohorts.ui.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityLoginBinding
import com.example.cohorts.model.User
import com.example.cohorts.ui.MainActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 1
        private const val TAG = "LogInActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore

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
                val user = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, "onActivityResult: user: ${user?.displayName}")
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                saveUserInDatabase()
                startActivity(mainActivityIntent)
                finish()
                // user pressed back button or there was an error
            } else {
                Log.e(TAG, "onActivityResult: ${response?.error}")
                finish()
            }
        }
    }

    private fun saveUserInDatabase() {
        val user = User(
            userName = auth.currentUser!!.displayName,
            userEmail = auth.currentUser!!.email,
            uid = auth.currentUser!!.uid
        )
        firestore.collection("users").document(user.uid!!).set(user)
    }

}
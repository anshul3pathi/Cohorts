package com.example.cohorts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityMainBinding
import com.example.cohorts.ui.login.LoginActivity
import com.firebase.ui.auth.AuthUI
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val hiddenDestination = listOf(
        R.id.addNewMemberFragment,
        R.id.viewPagerFragment
    )

    // overridden functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = this.findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.cohortsFragment,
            R.id.chatFragment,
            R.id.filesFragment
        ))

        // Hide bottom navigation bar inside cohorts
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (shouldHideBottomNav(destination.id)) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setBottomNavigationMenu(navController)
//        startTimer()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_sign_out -> {
                signOut()
                true
            }  else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // private functions

    private fun setBottomNavigationMenu(navController: NavController) {
        val bottomNavigation = binding.bottomNavigation
        bottomNavigation.setupWithNavController(navController)
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    private fun shouldHideBottomNav(id: Int): Boolean {
        for (item in hiddenDestination) {
            if (item == id) return true
        }
        return false
    }

}
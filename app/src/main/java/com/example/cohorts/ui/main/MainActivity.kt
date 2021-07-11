package com.example.cohorts.ui.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityMainBinding
import com.example.cohorts.ui.cohorts.CohortsFragmentDirections
import com.example.cohorts.ui.login.LoginActivity
import com.example.cohorts.utils.Theme
import com.example.cohorts.utils.snackbar
import com.example.cohorts.utils.toTheme
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber

/**
 * Activity that hosts the navHost Fragment and listens to broadcast events from Jitsi
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val APP_THEME_EXTRA = "app_theme"
    }

    val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver(this.applicationContext) {
            override fun onReceive(context: Context?, intent: Intent?) {
                onBroadcastReceived(intent)
            }
        }
    }

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appTheme: Theme
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var themeChangeItemClicked = false

    // overridden functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = this.findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.cohortsFragment
        ))

        subscribeToObservers()

        appTheme = intent.getIntExtra(APP_THEME_EXTRA, 1).toTheme()

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onDestroy() {
        mainViewModel.onDestroy(this, broadcastReceiver)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_profile -> {
                navController.navigate(
                    CohortsFragmentDirections.actionCohortToProfile()
                )
                true
            } R.id.item_sign_out -> {
                signOut()
                true
            } R.id.item_theme -> {
                themeChangeItemClicked = true
                showChangeThemeDialog()
                true
            }  else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // private functions

    /**
     * Launch Firebase's AuthUI for signing out the user
     */
    private fun signOut() {
        mainViewModel.signOut()
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    private fun subscribeToObservers() {
        mainViewModel.errorMessage.observe(this, { errorMessage ->
            if (errorMessage != "Index: 0, Size: 0") {
                binding.mainActivityRootLayout.snackbar(errorMessage, false)
            }
        })
        mainViewModel.currentAppTheme.observe(this, { appTheme ->
            this.appTheme = appTheme
            if (themeChangeItemClicked) {
                changeAppTheme(appTheme)
                themeChangeItemClicked = false
            }
        })
    }

    /**
     * Shows the change theme dialog and changes the theme with the theme selected
     */
    private fun showChangeThemeDialog() {
        val singleItems = arrayOf("Light", "Dark", "System Default")
        val checkedItem = when (appTheme) {
            Theme.LIGHT -> 0
            Theme.DARK -> 1
            else -> 2
        }
        var itemChecked = checkedItem
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Done") { _, _, ->
                mainViewModel.changeAppTheme(itemChecked)
            }
            .setSingleChoiceItems(singleItems, checkedItem) { _, which ->
                Timber.d("item chosen = $which")
                itemChecked = which
            }.show()
    }

    private fun changeAppTheme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Receives the broadcast events from Jitsi and decides what to do with them
     */
    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {

                // Meeting joined, show Toast
                BroadcastEvent.Type.CONFERENCE_JOINED -> Toast.makeText(
                    this, "Meeting joined", Toast.LENGTH_LONG
                ).show()

                // Participant joined, show their name
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Toast.makeText(
                    this, "${event.data["name"]} joined the meeting", Toast.LENGTH_LONG
                ).show()

                // User left the meeting
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    Toast.makeText(
                    this, "You left the meeting.", Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.terminateOngoingMeeting(this, broadcastReceiver)
                } else -> {
                    if (event.data?.get("muted") == "6.0") {
                        // if the close button is pressed on the picture in picture screen

                        // show meeting left Toast
                        Toast.makeText(
                            this, "You left the meeting.", Toast.LENGTH_LONG
                        ).show()
                        mainViewModel.terminateOngoingMeeting(this, broadcastReceiver)
                    }
                }
            }
        }
    }

}
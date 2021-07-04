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
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.ActivityMainBinding
import com.example.cohorts.ui.login.LoginActivity
import com.example.cohorts.utils.snackbar
import com.firebase.ui.auth.AuthUI
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver(this.applicationContext) {
            override fun onReceive(context: Context?, intent: Intent?) {
                onBroadcastReceived(intent)
            }
        }
    }

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

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

        subscribeToObservers()

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
        Timber.d("onDestroy called!")
        mainViewModel.terminateOngoingMeeting(this, broadcastReceiver)
        super.onDestroy()
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

    private fun signOut() {
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
            snackbar(binding.mainActivityRootLayout, errorMessage)
        })
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Toast.makeText(
                    this, "Conference joined main", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Toast.makeText(
                    this, "User joined main - ${event.data["name"]}", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    Timber.d("on going conference terminated!")
                    mainViewModel.terminateOngoingMeeting(this, broadcastReceiver)
                }
                BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> {
                    Timber.d("pip exited!")
                }
                else -> {
                    if (event.data.get("muted") == "6.0") {
                        Timber.d("Meeting terminated from pip")
                        mainViewModel.terminateOngoingMeeting(this, broadcastReceiver)
                    }
                    Timber.d( "Event - ${event.data}")
                    Timber.d("muted = ${event.data.get("muted")}")
                }
            }
        }
    }

}
package com.example.cohorts.jitsi

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.*
import timber.log.Timber
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@ActivityScoped
class Jitsi @Inject constructor(
    private val context: Context,
    private val repository: CohortsRepo
) {

    companion object {
        private const val JITSI_SERVER = "https://meet.jit.si"
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver(context) {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }
    private val coroutineJob = Job()
    private val jitsiScope = CoroutineScope(coroutineJob)
    private var currentUser: User? = null
    private lateinit var cohortUid: String

    // Example for handling different JitsiMeetSDK events
    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Toast.makeText(
                    context, "Conference joined", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Toast.makeText(
                    context, "User joined - ${event.data["name"]}", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> hangUp()
                else -> Timber.d( "Event - ${event.data}")
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private fun hangUp() {
        Timber.d( "hangUp: Conference call terminated")
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(hangupBroadcastIntent)

        unregisterBroadcastReceiver()

        jitsiScope.launch {
            val cohort = repository.getCohortById(cohortUid)
            if (cohort.succeeded) {
                cohort as Result.Success
                cohort.data.membersInMeeting.remove(currentUser!!.uid)
                if (cohort.data.membersInMeeting.size == 0) {
                    cohort.data.isCallOngoing = false
                }
                val saved = repository.saveCohort(cohort.data)
                if (!saved.succeeded) {
                    saved as Result.Error
                    Timber.e(saved.exception)
                }
            } else {
                cohort as Result.Error
                Timber.e(cohort.exception)
            }
        }

//        firestore.collection("cohorts").document(cohort.cohortUid)
//            .get()
//            .addOnSuccessListener {
//                val newCohort = it.toObject(Cohort::class.java)!!
//                newCohort.membersInMeeting.remove(currentUser.uid)
//                if (newCohort.membersInMeeting.size == 0) {
//                    newCohort.isCallOngoing = false
//                    firestore.collection("cohorts").document(newCohort.cohortUid)
//                        .set(newCohort)
//                } else {
//                    firestore.collection("cohorts").document(newCohort.cohortUid)
//                        .set(newCohort)
//                }
//            }
    }

    private fun unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.action);
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.action);
                ... other events
         */
        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private suspend fun initUserInfo() {
        currentUser = withContext(Dispatchers.IO) {
            val result = repository.getCurrentUser()
            if (result.succeeded) {
                result as Result.Success
                result.data
            } else null
        }
    }

    fun initJitsi(cohortUid: String) {
        val serverURL = try {
            URL(JITSI_SERVER)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        this.cohortUid = cohortUid

        jitsiScope.launch {
            initUserInfo()

            withContext(Dispatchers.Main) {
                val userBundle = Bundle()
                userBundle.apply {
                    putString("displayName", currentUser?.userName)
                    putString("email", currentUser?.userEmail)
                }
                val userInfo = JitsiMeetUserInfo(userBundle)

                val defaultJitsiOptions = JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setWelcomePageEnabled(false)
                    .setFeatureFlag("chat.enabled", false)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("call-integration.enabled", false)
                    .setUserInfo(userInfo)
                    .build()
                JitsiMeet.setDefaultConferenceOptions(defaultJitsiOptions)
            }
        }
        registerForBroadcastMessages()
    }

    fun launchJitsi(cohortRoomCode: String) {
        val jitsiMeetOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom(cohortRoomCode)
            .build()
        JitsiMeetActivity.launch(context, jitsiMeetOptions)
    }

    fun destroyJitsi() {
        hangUp()
        unregisterBroadcastReceiver()
    }

}
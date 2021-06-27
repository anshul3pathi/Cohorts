package com.example.cohorts.jitsi

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.cohorts.core.model.Cohort
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.jitsi.meet.sdk.*
import timber.log.Timber
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL

class Jitsi(
    private val context: Context,
    private val cohort: Cohort,
    private val firestore: FirebaseFirestore,
    private val currentUser: FirebaseUser
) {

    companion object {
        private const val TAG = "JitsiClass"
        private const val JITSI_SERVER = "https://meet.jit.si"
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver(context) {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

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

        firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .addOnSuccessListener {
                val newCohort = it.toObject(Cohort::class.java)!!
                newCohort.membersInMeeting.remove(currentUser.uid)
                if (newCohort.membersInMeeting.size == 0) {
                    newCohort.isCallOngoing = false
                    firestore.collection("cohorts").document(newCohort.cohortUid)
                        .set(newCohort)
                } else {
                    firestore.collection("cohorts").document(newCohort.cohortUid)
                        .set(newCohort)
                }
            }
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

    fun initJitsi() {
        val serverURL = try {
            URL(JITSI_SERVER)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }

        val userBundle = Bundle()
        userBundle.apply {
            putString("displayName", currentUser.displayName)
            putString("email", currentUser.email)
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
        registerForBroadcastMessages()
    }

    fun launchJitsi() {
        val jitsiMeetOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom(cohort.cohortRoomCode)
            .build()
        JitsiMeetActivity.launch(context, jitsiMeetOptions)
    }

    fun destroyJitsi() {
        hangUp()
        unregisterBroadcastReceiver()
    }

}
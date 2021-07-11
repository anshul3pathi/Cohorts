package com.example.cohorts.jitsi

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.cohorts.core.model.User
import org.jitsi.meet.sdk.*
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL

/**
 * This module provides all the methods that help in integrating Jitsi in the app
 */


private const val JITSI_SERVER = "https://meet.jit.si"


/**
 * Initialise the Jitsi severs URL and builds default Jitsi Meeting options
 * with the info of current user and other default settings.
 *
 * @param user object containing info about the current user
 * @param broadcastReceiver for listening to the event broadcasts by Jitsi
 * @param context [Context] object
 */
fun initJitsi(user: User, broadcastReceiver: BroadcastReceiver, context: Context) {
    val serverURL = try {
        URL(JITSI_SERVER)
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        throw RuntimeException("Invalid server URL!")
    }

    val userBundle = Bundle()
    userBundle.apply {
        putString("displayName", user.userName)
        putString("email", user.userEmail)
    }
    val userInfo = JitsiMeetUserInfo(userBundle)

    val defaultJitsiOptions = JitsiMeetConferenceOptions.Builder()
        .setServerURL(serverURL)
        .setWelcomePageEnabled(false)
        .setFeatureFlag("chat.enabled", false)
        .setFeatureFlag("invite.enabled", false)
        .setFeatureFlag("call-integration.enabled", false)
        .setAudioMuted(true)
        .setVideoMuted(true)
        .setUserInfo(userInfo)
        .build()
    JitsiMeet.setDefaultConferenceOptions(defaultJitsiOptions)
    registerForBroadcastMessages(broadcastReceiver, context)
}

/**
 * Launch a new Jitsi meeting with the given room code
 *
 * @param context [Context] object
 * @param cohortRoomCode room code of the meeting
 */
fun launchJitsi(context: Context, cohortRoomCode: String) {
    val jitsiMeetOptions = JitsiMeetConferenceOptions.Builder()
        .setRoom(cohortRoomCode)
        .build()
    JitsiMeetActivity.launch(context, jitsiMeetOptions)
}

/**
 * Destroys the links between Jitsi and the app by unregistering the [BroadcastReceiver]
 * and also hangs up the ongoing call.
 *
 * @param context [Context] object
 * @param broadcastReceiver that was listening to broadcast events from Jitsi till now
 */
fun destroyJitsi(context: Context, broadcastReceiver: BroadcastReceiver) {
    hangUp()
    unregisterBroadcastReceiver(context, broadcastReceiver)
}

/**
 * This registers for every possible event sent back by Jitsi
 *
 * @param broadcastReceiver for listening to the broadcast events
 * @param context [Context] object
 */
private fun registerForBroadcastMessages(broadcastReceiver: BroadcastReceiver, context: Context) {
    val intentFilter = IntentFilter()

    /* If only some of the events are needed, the for loop can be replaced
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

/**
 * Unregisters the given broadcastReceiver
 *
 * @param context [Context] object
 * @param broadcastReceiver that was listening to the broadcast events till now
 */
private fun unregisterBroadcastReceiver(context: Context, broadcastReceiver: BroadcastReceiver) {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
}

/**
 * Hang up the ongoing call
 */
private fun hangUp() {
    val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
    LocalBroadcastManager
        .getInstance(org.webrtc.ContextUtils.getApplicationContext())
        .sendBroadcast(hangupBroadcastIntent)
}
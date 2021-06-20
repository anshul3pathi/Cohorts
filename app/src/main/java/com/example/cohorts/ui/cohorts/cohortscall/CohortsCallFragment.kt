package com.example.cohorts.ui.cohorts.cohortscall

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsBinding
import com.example.cohorts.databinding.FragmentCohortsCallBinding
import com.example.cohorts.ui.cohorts.CohortsFragment
import com.google.firebase.auth.FirebaseAuth
import org.jitsi.meet.sdk.*
import java.net.MalformedURLException
import java.net.URL

class CohortsCallFragment : Fragment() {

    companion object {
        private const val TAG = "CohortsCallFragment"
        private const val JITSI_SERVER = "https://meet.jit.si"
    }

    private lateinit var binding: FragmentCohortsCallBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsCallBinding.inflate(inflater)

        auth = FirebaseAuth.getInstance()

        val serverUrl: URL = try {
            URL(JITSI_SERVER)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid Server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            // When using JaaS, set the obtained JWT here
            //.setToken("MyJWT")
            // Different features flags can be set
            //.setFeatureFlag("toolbox.enabled", false)
            //.setFeatureFlag("filmstrip.enabled", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("minParticipants", 2)
            .setWelcomePageEnabled(true)
            .setAudioMuted(true)
            .setVideoMuted(true)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        binding.startVideoCallButton.setOnClickListener {
            startMeeting()
        }

        return binding.root
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        hangUp()
        super.onDestroy()
    }

    override fun onResume() {
        registerForBroadcastMessages()
        super.onResume()
    }

    private fun startMeeting() {
        val roomCode = binding.roomCodeET.editText?.text.toString()
        val userBundle = Bundle()
        userBundle.putString("displayName", auth.currentUser!!.displayName)
        userBundle.putString("email", auth.currentUser!!.email)
        val userInfo = JitsiMeetUserInfo(userBundle)
        if (roomCode.isNotEmpty()) {
            val options = JitsiMeetConferenceOptions.Builder()
                .setRoom(roomCode)
                .setUserInfo(userInfo)
                .build()
            JitsiMeetActivity.launch(requireContext(), options)
        }
    }

    private val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver(requireContext()) {
            override fun onReceive(context: Context?, intent: Intent?) {
                onBroadcastReceived(intent)
            }
        }
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

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Toast.makeText(requireContext(), "Meeting joined with url ${event.data["url"]}", Toast.LENGTH_LONG).show()
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Toast.makeText(requireContext(), "Participant joined - ${event.data["name"]}", Toast.LENGTH_LONG).show()
                else -> Log.e(TAG, "else branch")
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private fun hangUp() {
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(org.webrtc.ContextUtils.getApplicationContext()).sendBroadcast(hangupBroadcastIntent)
    }

}
package com.example.cohorts.ui.cohorts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.R
import com.example.cohorts.adapters.CohortsAdapter
import com.example.cohorts.databinding.FragmentCohortsBinding
import com.example.cohorts.jitsi.Jitsi
import com.example.cohorts.model.Cohort
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL

class CohortsFragment : Fragment(), CohortClickListener {

    companion object {
        private const val TAG = "CohortsFragment"
    }

    private lateinit var binding: FragmentCohortsBinding
    private lateinit var navController: NavController
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cohortsAdapter: CohortsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        val cohortCollection = firestore.collection("cohorts")
        val query = cohortCollection
            .whereNotEqualTo("cohortName", null)

        val cohortAdapterOptions = FirestoreRecyclerOptions.Builder<Cohort>()
            .setQuery(query, Cohort::class.java)
            .build()

        cohortsAdapter = CohortsAdapter(cohortAdapterOptions, this)
        binding.cohortsRcv.apply {
            adapter = cohortsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.addCohortsFab.setOnClickListener {
            navController.navigate(R.id.cohorts_to_addNewCohorts)
        }
    }

    override fun onStart() {
        cohortsAdapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        cohortsAdapter.stopListening()
        super.onStop()
    }

    override fun cohortItemClicked(cohort: Cohort) {
        Log.d(TAG, "cohort clicked with id: $cohort")
        val action = CohortsFragmentDirections.actionCohortsToViewPager(cohort)
        navController.navigate(action)
    }

    override fun joinVideoCallButtonClicked(view: View, cohort: Cohort) {
        if (auth.currentUser!!.uid in cohort.membersInMeeting) {
            (view as Button).isEnabled = false
        } else {
            Log.d(TAG, "joinVideoCallButtonClicked: trying to join meeting")

            cohort.membersInMeeting.add(auth.currentUser!!.uid)
            firestore.collection("cohorts").document(cohort.cohortUid)
                .set(cohort)
            val jitsi = Jitsi(requireContext(), cohort, firestore, auth.currentUser!!)
            jitsi.initJitsi()
            jitsi.launchJitsi()
        }
    }
}
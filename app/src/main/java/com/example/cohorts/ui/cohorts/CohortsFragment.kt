package com.example.cohorts.ui.cohorts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.ui.main.MainActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CohortsFragment : Fragment(), CohortClickListener {

    companion object {
        private const val TAG = "CohortsFragment"
    }

    private lateinit var binding: FragmentCohortsBinding
    private lateinit var navController: NavController
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cohortsAdapter: CohortsAdapter
    private val cohortsViewModel: CohortsViewModel by viewModels()

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

        cohortsViewModel.userAddedToMeeting.observe(viewLifecycleOwner, { userAddedToMeeting ->
            if (userAddedToMeeting) {
                Timber.d("cohort to join - ${cohortsViewModel.cohort}")
                cohortsViewModel.resetUserAddedToMeeting()
            }
        })
        cohortsViewModel.errorAddingUserToMeeting.observe(viewLifecycleOwner, { errorAddingUser ->
            if (errorAddingUser) {
                Snackbar.make(
                    binding.cohortsFragmentRootLayout,
                    "Error adding you to meeting.",
                    Snackbar.LENGTH_LONG
                ).show()
                cohortsViewModel.resetErrorAddingUserToMeeting()
            }
        })

        val query = cohortsViewModel.fetchCohortsQuery()

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
        Timber.d( "cohort clicked with id: $cohort")
        val action = CohortsFragmentDirections.actionCohortsToViewPager(cohort)
        navController.navigate(action)
    }

    override fun joinVideoCallButtonClicked(view: View, cohort: Cohort) {
        if (auth.currentUser!!.uid in cohort.membersInMeeting) {
            (view as Button).isEnabled = false
        } else {
            Timber.d("joinVideoCallButtonClicked: trying to join cohort - $cohort")

            cohortsViewModel.addCurrentUserToOngoingMeeting(
                cohort,
                (activity as MainActivity).broadcastReceiver,
                requireContext()
            )
        }
    }
}

package com.example.cohorts.ui.cohorts

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentViewPagerBinding
import com.example.cohorts.jitsi.Jitsi
import com.example.cohorts.model.Cohort
import com.example.cohorts.ui.cohorts.cohortschat.CohortsChatFragment
import com.example.cohorts.ui.cohorts.cohortsfile.CohortsFilesFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jitsi.meet.sdk.*
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL

class ViewPagerFragment : Fragment() {

    companion object {
        private const val TAG = "ViewPagerFragment"
    }

    private lateinit var binding: FragmentViewPagerBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private val fragmentList = listOf(CohortsChatFragment(), CohortsFilesFragment())
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var cohort: Cohort
    private var buttonClicked = false
    private lateinit var jitsi: Jitsi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentViewPagerBinding.inflate(inflater)
        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        navController = findNavController()

        arguments?.let {
            cohort = ViewPagerFragmentArgs.fromBundle(it).cohort!!
            (activity as AppCompatActivity).supportActionBar?.title = cohort.cohortName
        }
        jitsi = Jitsi(requireContext(), cohort, firestore, auth.currentUser!!)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")

        viewPagerAdapter = ViewPagerAdapter(
            fragmentList,
            this.childFragmentManager,
            lifecycle
        )

        val tabTitles = listOf("Chat", "Files")
        binding.viewpager2Cohorts.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayoutCohorts, binding.viewpager2Cohorts) { tab, position ->
            tab.text = tabTitles[position]
            binding.viewpager2Cohorts.setCurrentItem(tab.position, true)
        }.attach()

        jitsi.initJitsi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.viewpager_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_new_member_menu_item -> {
                val action = ViewPagerFragmentDirections
                    .actionViewPagerFragmentToAddNewMemberFragment(cohort)
                navController.navigate(action)
                true
            } R.id.start_video_call_menu_button -> {
                Log.d(TAG, "onOptionsItemSelected: start video call button clicked")
                startMeeting()
                true
            } else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        jitsi.destroyJitsi()
        super.onDestroy()
    }

    private fun startMeeting() {
        if (!cohort.isCallOngoing) {
            cohort.isCallOngoing = true
            cohort.membersInMeeting.add(auth.currentUser!!.uid)
            firestore.collection("cohorts").document(cohort.cohortUid)
                .set(cohort)
            jitsi.launchJitsi()
        }
    }

}
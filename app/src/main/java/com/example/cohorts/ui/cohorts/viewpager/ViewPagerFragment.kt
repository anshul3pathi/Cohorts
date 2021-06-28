package com.example.cohorts.ui.cohorts.viewpager

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentViewPagerBinding
import com.example.cohorts.jitsi.Jitsi
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.ui.cohorts.cohortschat.CohortsChatFragment
import com.example.cohorts.ui.cohorts.cohortsfile.CohortsFilesFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    companion object {
        private const val TAG = "ViewPagerFragment"
    }

    private lateinit var binding: FragmentViewPagerBinding
//    private lateinit var firestore: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private val fragmentList = listOf(CohortsChatFragment(), CohortsFilesFragment())
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var cohortArgument: Cohort
    private var buttonClicked = false
    @Inject lateinit var jitsi: Jitsi
    private val viewPagerViewModel: ViewPagerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d( "onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentViewPagerBinding.inflate(inflater)
//        firestore = Firebase.firestore
//        auth = FirebaseAuth.getInstance()
        navController = findNavController()

        arguments?.let {
            cohortArgument = ViewPagerFragmentArgs.fromBundle(it).cohort!!
            (activity as AppCompatActivity).supportActionBar?.title = cohortArgument.cohortName
        }
//        jitsi = Jitsi(requireContext(), cohortArgument, firestore, auth.currentUser!!)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d( "onViewCreated")

        viewPagerAdapter = ViewPagerAdapter(
            fragmentList,
            this.childFragmentManager,
            lifecycle
        )

        viewPagerViewModel.inMeeting.observe(viewLifecycleOwner, { inMeeting ->
            if (inMeeting) {
                Timber.d("You are in a new meeting!")
            }
        })
        viewPagerViewModel.errorOccurred.observe(viewLifecycleOwner, { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Snackbar.make(
                    binding.rootLayoutViewPagerFragment,
                    errorMessage,
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        })

        val tabTitles = listOf("Chat", "Files")
        binding.viewpager2Cohorts.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayoutCohorts, binding.viewpager2Cohorts) { tab, position ->
            tab.text = tabTitles[position]
            binding.viewpager2Cohorts.setCurrentItem(tab.position, true)
        }.attach()

//        jitsi.initJitsi()
        jitsi.initJitsi(cohortArgument.cohortUid)
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
                    .actionViewPagerFragmentToAddNewMemberFragment(cohortArgument)
                navController.navigate(action)
                true
            } R.id.start_video_call_menu_button -> {
                Timber.d( "onOptionsItemSelected: start video call button clicked")
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
//        if (!cohortArgument.isCallOngoing) {
//            cohortArgument.isCallOngoing = true
//            cohortArgument.membersInMeeting.add(auth.currentUser!!.uid)
//            firestore.collection("cohorts").document(cohortArgument.cohortUid)
//                .set(cohortArgument)
//            jitsi.launchJitsi()
//        }
        viewPagerViewModel.startNewMeeting(cohortArgument)
        jitsi.launchJitsi(cohortArgument.cohortRoomCode)
    }

}
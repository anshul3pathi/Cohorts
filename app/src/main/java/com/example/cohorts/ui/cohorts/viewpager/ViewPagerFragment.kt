package com.example.cohorts.ui.cohorts.viewpager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentViewPagerBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.ui.cohorts.cohortschat.CohortsChatFragment
import com.example.cohorts.ui.cohorts.cohortsfile.CohortsFilesFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    companion object {
        private const val TAG = "ViewPagerFragment"
    }

    private lateinit var binding: FragmentViewPagerBinding
    private lateinit var navController: NavController
    private val fragmentList = listOf(CohortsChatFragment(), CohortsFilesFragment())
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var cohortArgument: Cohort
//    @Inject lateinit var jitsi: Jitsi
    private val viewPagerViewModel: ViewPagerViewModel by viewModels()

    private val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver(context) {
            override fun onReceive(context: Context?, intent: Intent?) {
                onBroadcastReceived(intent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d( "onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentViewPagerBinding.inflate(inflater)
        navController = findNavController()

        arguments?.let {
            cohortArgument = ViewPagerFragmentArgs.fromBundle(it).cohort!!
            (activity as AppCompatActivity).supportActionBar?.title = cohortArgument.cohortName
        }

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
        viewPagerViewModel.cohortDeleted.observe(viewLifecycleOwner, { cohortDeleted ->
            if (cohortDeleted) {
                Snackbar.make(
                    binding.rootLayoutViewPagerFragment,
                    "This cohort was deleted!",
                    Snackbar.LENGTH_LONG
                ).show()
                object : CountDownTimer(1500L, 500L) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        navController.navigateUp()
                    }
                }.start()
            }
        })

        val tabTitles = listOf("Chat", "Files")
        binding.viewpager2Cohorts.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayoutCohorts, binding.viewpager2Cohorts) { tab, position ->
            tab.text = tabTitles[position]
            binding.viewpager2Cohorts.setCurrentItem(tab.position, true)
        }.attach()

//        jitsi.initJitsi(cohortArgument.cohortUid)
        viewPagerViewModel.initialiseJitsi(broadcastReceiver, requireContext())
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
            } R.id.delete_cohort_menu_item -> {
                deleteThisCohort()
                true
            } else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        viewPagerViewModel.terminateOngoingMeeting(requireContext(), broadcastReceiver)
        super.onDestroy()
    }

    private fun deleteThisCohort() {
        viewPagerViewModel.deleteThisCohort(cohortArgument)
    }

    private fun startMeeting() {
        viewPagerViewModel.startNewMeeting(cohortArgument, requireContext())
//        jitsi.launchJitsi(cohortArgument.cohortRoomCode)
    }

    // Example for handling different JitsiMeetSDK events
    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Toast.makeText(
                    context, "Conference joined", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.PARTICIPANT_JOINED ->  Toast.makeText(
                    context, "User joined - ${event.data["name"]}", Toast.LENGTH_LONG
                ).show()
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    Timber.d("on going conference terminated!")
                    viewPagerViewModel
                        .terminateOngoingMeeting(requireContext(), broadcastReceiver)
                }
                else -> Timber.d( "Event - ${event.data}")
            }
        }
    }
}

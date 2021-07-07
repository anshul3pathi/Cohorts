package com.example.cohorts.ui.cohorts.viewpager

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentViewPagerBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.ui.main.MainActivity
import com.example.cohorts.ui.chat.ChatFragment
import com.example.cohorts.ui.files.FilesFragment
import com.example.cohorts.utils.themeColor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding
    private lateinit var navController: NavController
    private val fragmentList = listOf(ChatFragment(), FilesFragment())
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var cohortArgument: Cohort
    private val viewPagerViewModel: ViewPagerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d( "onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentViewPagerBinding.inflate(inflater)
        navController = findNavController()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }

        arguments?.let {
            cohortArgument = ViewPagerFragmentArgs.fromBundle(it).cohort!!
            (activity as AppCompatActivity).supportActionBar?.title = cohortArgument.cohortName
            (activity as AppCompatActivity).supportActionBar?.subtitle =
                cohortArgument.cohortDescription
        }

        binding.apply {
            rootLayoutViewPagerFragment.transitionName = cohortArgument.cohortUid
        }

        val bundle = Bundle()
        bundle.putString("cohortUid", cohortArgument.cohortUid)
        fragmentList[0].arguments = bundle

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
                    "Cohort deleted!",
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
            } R.id.cohort_info_menu_item -> {
                navController.navigate(
                    ViewPagerFragmentDirections
                        .actionViewPagerFragmentToCohortInfoFragment(cohortArgument)
                )
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

    private fun deleteThisCohort() {
        val context = requireContext()
        MaterialAlertDialogBuilder(context)
            .setTitle("Are you sure you want to delete ${cohortArgument.cohortName}?")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") {_, _ ->
                viewPagerViewModel.deleteThisCohort(cohortArgument)
            }.show()
    }

    private fun startMeeting() {
        viewPagerViewModel.initialiseJitsi(
            (activity as MainActivity).broadcastReceiver,
            requireContext()
        )

        viewPagerViewModel.startNewMeeting(
            cohortArgument,
            requireContext()
        )
    }

}

package com.example.cohorts.ui.cohorts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsBinding
import com.example.cohorts.databinding.FragmentViewPagerBinding
import com.example.cohorts.ui.cohorts.cohortscall.CohortsCallFragment
import com.example.cohorts.ui.cohorts.cohortschat.CohortsChatFragment
import com.example.cohorts.ui.cohorts.cohortsfile.CohortsFilesFragment
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding
    private val fragmentList =
        listOf(CohortsCallFragment(), CohortsChatFragment(), CohortsFilesFragment())
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewPagerBinding.inflate(inflater)

        viewPagerAdapter = ViewPagerAdapter(
            fragmentList,
            this.childFragmentManager,
            lifecycle
        )

        val tabTitles = listOf("Call", "Chat", "Files")
        binding.viewpager2Cohorts.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayoutCohorts, binding.viewpager2Cohorts) { tab, position ->
            tab.text = tabTitles[position]
            binding.viewpager2Cohorts.setCurrentItem(tab.position, true)
        }.attach()

        return binding.root
    }

}
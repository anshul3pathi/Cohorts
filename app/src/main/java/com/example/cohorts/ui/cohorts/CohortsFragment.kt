package com.example.cohorts.ui.cohorts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsBinding
import com.example.cohorts.ui.cohorts.cohortscall.CohortsCallFragment
import com.example.cohorts.ui.cohorts.cohortschat.CohortsChatFragment
import com.example.cohorts.ui.cohorts.cohortsfile.CohortsFilesFragment
import com.google.android.material.tabs.TabLayoutMediator

class CohortsFragment : Fragment() {

    companion object {
        private const val TAG = "CohortsFragment"
    }

    private lateinit var binding: FragmentCohortsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsBinding.inflate(inflater)

        binding.navigateButtonCohorts.setOnClickListener {
            findNavController().navigate(R.id.action_cohortsTo_viewPager)
        }

        return binding.root
    }

}
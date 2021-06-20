package com.example.cohorts.ui.cohorts.cohortschat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsChatBinding

class CohortsChatFragment : Fragment() {

    private lateinit var binding: FragmentCohortsChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsChatBinding.inflate(inflater)

        return binding.root
    }
}
package com.example.cohorts.ui.cohorts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsBinding

class CohortsFragment : Fragment() {

    private lateinit var binding: FragmentCohortsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCohortsBinding.inflate(inflater)

        return binding.root
    }
}
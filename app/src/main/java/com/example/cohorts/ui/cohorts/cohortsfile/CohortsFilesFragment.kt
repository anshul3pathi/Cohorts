package com.example.cohorts.ui.cohorts.cohortsfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.databinding.FragmentCohortsFilesBinding

class CohortsFilesFragment : Fragment() {

    private lateinit var binding: FragmentCohortsFilesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsFilesBinding.inflate(inflater)

        return binding.root
    }

}
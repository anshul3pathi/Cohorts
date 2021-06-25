package com.example.cohorts.ui.cohorts.cohortschat

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentCohortsChatBinding

class CohortsChatFragment : Fragment() {

    companion object {
        private const val TAG = "CohortChatFragment"
    }

    private lateinit var binding: FragmentCohortsChatBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortsChatBinding.inflate(inflater)
        navController = findNavController()
        setHasOptionsMenu(true)

        return binding.root
    }

}
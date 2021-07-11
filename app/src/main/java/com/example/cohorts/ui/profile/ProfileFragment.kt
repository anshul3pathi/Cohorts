package com.example.cohorts.ui.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.cohorts.databinding.FragmentProfileBinding
import com.example.cohorts.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Displays the Profile screen
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // clear the MainActivity's menu
        menu.clear()
    }

    private fun subscribeToObservers() {
        profileViewModel.currentUser.observe(viewLifecycleOwner, { currentUser ->
            binding.user = currentUser
        })

        profileViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.profileRootMcv.snackbar(it)
            }
        })
    }

}
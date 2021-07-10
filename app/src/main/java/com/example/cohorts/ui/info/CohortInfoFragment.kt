package com.example.cohorts.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.databinding.FragmentCohortInfoBinding
import com.example.cohorts.utils.snackbar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CohortInfoFragment : Fragment() {

    private lateinit var binding: FragmentCohortInfoBinding
    private lateinit var cohortArgument: Cohort
    private val cohortInfoViewModel: CohortInfoViewModel by viewModels()
    private lateinit var userInfoAdapter: UserInfoAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCohortInfoBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments.let {
            cohortArgument = CohortInfoFragmentArgs.fromBundle(it!!).cohort!!
            binding.cohort = cohortArgument
            (activity as AppCompatActivity).supportActionBar?.subtitle = ""
            Timber.d("Cohort - $cohortArgument")
        }

        binding.infoEditOrDoneButton.setOnClickListener { button ->
            button as MaterialButton
            if (button.text == "Edit") {
                binding.apply {
                    infoEditCohortInfoContainer.visibility = View.VISIBLE
                    infoCohortInfoLayout.visibility = View.GONE
                }
            } else {
                binding.apply {
                    infoEditCohortInfoContainer.visibility = View.GONE
                    infoCohortInfoLayout.visibility = View.VISIBLE
                    cohortArgument.cohortName = infoCohortNameEt.text.toString()
                    cohortArgument.cohortDescription = infoCohortDescriptionEt.text.toString()
                }
                updateCohortInformation(cohortArgument)
                binding.cohort = cohortArgument
            }
            button.text = if (button.text == "Edit") "Done" else "Edit"
        }

        setUpUserInfoRcv()
        subscribeToObservers()

    }

    override fun onStart() {
        Timber.d("onStart")
        userInfoAdapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        userInfoAdapter.stopListening()
        super.onStop()
    }

    private fun subscribeToObservers() {
        cohortInfoViewModel.currentUser.observe(viewLifecycleOwner, { currentUser ->
            userInfoAdapter.setCurrentUser(currentUser)
        })

        cohortInfoViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.infoRootLayout.snackbar(it)
            }
        })
    }

    private fun setUpUserInfoRcv() {
        val userQuery = cohortInfoViewModel.fetchUsersQuery(cohortArgument.cohortUid)

        userQuery.get().addOnSuccessListener {
            Timber.d("${it.toObjects(User::class.java)}")
        }

        val userAdapterOptions = FirestoreRecyclerOptions.Builder<User>()
            .setQuery(userQuery, User::class.java)
            .build()

        userInfoAdapter = UserInfoAdapter(userAdapterOptions)
        binding.infoUserRcv.apply {
            adapter = userInfoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        cohortInfoViewModel.getCurrentUser()

        userInfoAdapter.setRemoveButtonClickListener { user ->
            cohortInfoViewModel.removeThisUserFromCohort(user, cohortArgument)
        }
    }

    private fun updateCohortInformation(cohort: Cohort) {
        Timber.d("$cohort")
        cohortInfoViewModel.updateThisCohort(cohort)
    }

}
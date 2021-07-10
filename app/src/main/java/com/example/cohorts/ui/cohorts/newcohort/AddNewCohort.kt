package com.example.cohorts.ui.cohorts.newcohort

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentAddNewCohortBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.utils.snackbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AddNewCohort : Fragment() {

    private lateinit var binding: FragmentAddNewCohortBinding
    private lateinit var navController: NavController
    private val addNewCohortViewModel: AddNewCohortViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddNewCohortBinding.inflate(inflater)
        navController = findNavController()

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToObservers()

        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cancel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.add_cohort_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.done_add_cohort_button -> {
                Timber.d( "added new Cohort - ${binding.cohortNameEt.text}")
                addNewCohortToDatabase()
                true
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addNewCohortToDatabase() {
        val newCohort = Cohort(
            cohortName = binding.cohortNameEt.text.toString(),
            cohortDescription = binding.cohortDescriptionEt.text.toString()
        )
        addNewCohortViewModel.addNewCohort(newCohort)

        object: CountDownTimer(1500L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                navController.popBackStack()
            }
        }.start()
    }

    private fun subscribeToObservers() {
        addNewCohortViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.addNewCohortFragmentRoot.snackbar(it)
            }
        })
    }

}
package com.example.cohorts.ui.cohorts.newcohort

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentAddNewCohortBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Displays the page for adding a new Cohort
 */
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

            // change the icon of up button
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

        // wait till the confirmation or error snackbar is displayed, then pop out of this screen
        object: CountDownTimer(3000L, 1000L) {
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
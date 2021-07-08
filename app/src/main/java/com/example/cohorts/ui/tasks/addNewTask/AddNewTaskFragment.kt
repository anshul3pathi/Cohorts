package com.example.cohorts.ui.tasks.addNewTask

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.Task
import com.example.cohorts.databinding.FragmentAddNewTaskBinding
import com.example.cohorts.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AddNewTaskFragment : Fragment() {

    private lateinit var binding: FragmentAddNewTaskBinding
    private lateinit var navController: NavController
    private val addNewTaskViewModel: AddNewTaskViewModel by viewModels()
    private lateinit var cohortArgument: Cohort

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNewTaskBinding.inflate(inflater)

        navController = findNavController()

        arguments.let {
            cohortArgument = AddNewTaskFragmentArgs.fromBundle(it!!).cohort!!
        }

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
                addNewTask()
                object: CountDownTimer(3000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        navController.popBackStack()
                    }
                }.start()
                true
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addNewTask() {
        val newTask = Task(
            taskOfCohort =cohortArgument.cohortUid,
            title = binding.cohortNameEt.text.toString(),
            description = binding.cohortDescriptionEt.text.toString()
        )
        addNewTaskViewModel.addNewCohort(newTask, cohortArgument.cohortUid)
    }

    private fun subscribeToObservers() {
        addNewTaskViewModel.errorMessage.observe(viewLifecycleOwner, { errorMessage ->
            snackbar(binding.addNewTaskRootLayout, errorMessage)
        })

        addNewTaskViewModel.taskAddedSuccessfully.observe(viewLifecycleOwner, { taskAdded ->
            if (taskAdded) {
                snackbar(binding.addNewTaskRootLayout, "Task added successfully!")
            }
        })
    }

}
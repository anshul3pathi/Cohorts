package com.example.cohorts.ui.tasks

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.R
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.Task
import com.example.cohorts.databinding.FragmentTasksBinding
import com.example.cohorts.utils.EventObserver
import com.example.cohorts.utils.snackbar
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Displays the list of [Task]
 */
@AndroidEntryPoint
class TasksFragment : Fragment() {

    private lateinit var binding: FragmentTasksBinding
    private val taskViewModel: TasksViewModel by viewModels()
    private lateinit var cohortArgument: Cohort
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater)

        // initialise the cohortArgument member variable from safeArgs
        arguments?.let {
            cohortArgument = TasksFragmentArgs.fromBundle(it).cohort!!
            Timber.d("cohort Argument = $cohortArgument")
            (activity as AppCompatActivity).supportActionBar?.subtitle = cohortArgument.cohortName
        }

        navController = findNavController()

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpTaskRcv()

        subscribeToObservers()

        binding.addTaskFab.setOnClickListener {
            navController.navigate(
                TasksFragmentDirections.actionTaskToAddTask(cohortArgument)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // clear the MainActivity's menu and inflate TasksFragment's menu
        menu.clear()
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_task_clear_completed -> {
                taskViewModel.clearCompletedTasks(cohortArgument.cohortUid)
                true
            } R.id.item_task_clear_all -> {
                taskViewModel.clearAllTasks(cohortArgument.cohortUid)
                true
            } else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        tasksAdapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        tasksAdapter.stopListening()
        super.onStop()
    }

    private fun setUpTaskRcv() {
        val taskRef = taskViewModel.fetchTaskReference(cohortArgument.cohortUid)!!

        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    /*
                     * if tasks exist, show tasks list and hide not task message
                     */
                    Timber.d("tasks exist")
                    binding.tasksRcv.visibility = View.VISIBLE
                    binding.noTasksLayout.visibility = View.INVISIBLE
                } else {
                    /*
                     * if tasks don't exist, hide tasks list and show no tasks message
                     */
                    binding.tasksRcv.visibility = View.INVISIBLE
                    binding.noTasksLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val options = FirebaseRecyclerOptions.Builder<Task>()
            .setQuery(taskRef, Task::class.java)
            .build()

        tasksAdapter = TasksAdapter(options) { task ->
            taskViewModel.markTaskCompleteOrActive(task)
        }

        tasksAdapter.setTaskItemClickListener { task, view ->
            val extras = FragmentNavigatorExtras(
                view to task.taskId
            )
            navController.navigate(
                TasksFragmentDirections.actionTaskToTaskDetail(task),
                extras
            )
        }

        binding.tasksRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }

    private fun subscribeToObservers() {
        taskViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.tasksFragmentRootLayout.snackbar(it)
            }
        })
    }

}
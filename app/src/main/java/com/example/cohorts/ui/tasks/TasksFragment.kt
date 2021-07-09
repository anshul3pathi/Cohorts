package com.example.cohorts.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.Task
import com.example.cohorts.databinding.FragmentTasksBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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

        arguments.let {
            cohortArgument = TasksFragmentArgs.fromBundle(it!!).cohort!!
            Timber.d("cohort Argument = $cohortArgument")
            (activity as AppCompatActivity).supportActionBar?.subtitle = cohortArgument.cohortName
        }

        navController = findNavController()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpTaskRcv()

        binding.addTaskFab.setOnClickListener {
            navController.navigate(
                TasksFragmentDirections.actionTaskToAddTask(cohortArgument)
            )
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
        val taskRef = taskViewModel.fetchChatReference(cohortArgument.cohortUid)!!

        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Timber.d("tasks exist")
                    binding.tasksRcv.visibility = View.VISIBLE
                    binding.noTasksLayout.visibility = View.INVISIBLE
                } else {
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

        binding.tasksRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }

}
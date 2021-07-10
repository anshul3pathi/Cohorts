package com.example.cohorts.ui.tasks.taskdetail

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.core.model.Task
import com.example.cohorts.databinding.FragmentTaskDetailBinding
import com.example.cohorts.utils.snackbar
import com.example.cohorts.utils.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailBinding
    private var editing = false
    private val taskDetailViewModel: TaskDetailViewModel by viewModels()
    private lateinit var taskArgument: Task
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailBinding.inflate(inflater)

        navController = findNavController()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }

        arguments?.let {
            taskArgument = TaskDetailFragmentArgs.fromBundle(it).task!!
            binding.apply {
                task = taskArgument
                isEditing = editing
                taskDetailRootLayout.transitionName = taskArgument.taskId
            }
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToObservers()

        binding.apply {
            editTaskFab.setOnClickListener {
                editTask()
            }
            taskDetailCompleteCheckbox.setOnClickListener {
                taskDetailViewModel.markTaskCompletedOrActive(taskArgument.copy())
                taskArgument.isCompleted = !taskArgument.isCompleted
                this.task = taskArgument
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.task_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.item_task_detail_delete -> {
                deleteTask()
                taskDetailViewModel.deleteTask(taskArgument)
                true
            } else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun subscribeToObservers() {
        taskDetailViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.taskDetailRootLayout.snackbar(it)
            }
        })
    }

    private fun editTask() {
        editing = !editing

        binding.apply {
            isEditing = editing
            executePendingBindings()
        }

        if (!editing) {
            Timber.d("done editing! save changes.")
            if (binding.editTaskTitleEt.text.toString().isNotEmpty()) {
                taskArgument.title = binding.editTaskTitleEt.text.toString()
                taskArgument.description = binding.editTaskDescriptionEt.text.toString()
                taskDetailViewModel.saveChangesToTask(taskArgument)
                binding.task = taskArgument
                binding.executePendingBindings()
            } else {
                binding.taskDetailRootLayout.snackbar("Title cannot be empty!")
            }
        }
    }

    private fun deleteTask() {
        taskDetailViewModel.deleteTask(taskArgument)

        object: CountDownTimer(1500L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                navController.popBackStack()
            }
        }.start()
    }

}
package com.example.cohorts.ui.tasks.taskdetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailBinding.inflate(inflater)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToObservers()

        binding.editTaskFab.setOnClickListener {
            editTask()
        }
    }

    private fun subscribeToObservers() {
        taskDetailViewModel.errorMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                snackbar(binding.taskDetailRootLayout, it)
            }
        })

        taskDetailViewModel.taskEditedMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                snackbar(binding.taskDetailRootLayout, it)
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
                snackbar(binding.taskDetailRootLayout, "Title cannot be empty!")
            }
        }
    }

}
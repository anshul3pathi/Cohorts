package com.example.cohorts.ui.tasks.taskdetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentTaskDetailBinding
import com.example.cohorts.utils.themeColor
import com.google.android.material.transition.MaterialContainerTransform

class TaskDetailFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailBinding
    private var editing = false

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
            val taskArg = TaskDetailFragmentArgs.fromBundle(it).task!!
            binding.apply {
                task = taskArg
                isEditing = editing
                taskDetailRootLayout.transitionName = taskArg.taskId
            }
        }

        return binding.root
    }
}
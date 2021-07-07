package com.example.cohorts.ui.zoomedimage

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentZoomedImageBinding
import com.example.cohorts.utils.themeColor
import com.google.android.material.transition.MaterialContainerTransform

class ZoomedImageFragment : Fragment() {

    private lateinit var binding: FragmentZoomedImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZoomedImageBinding.inflate(inflater)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColor(R.color.black))
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments.let {
            val imageUrl = ZoomedImageFragmentArgs.fromBundle(it!!).imageUrl
            binding.imageUrl = imageUrl
            binding.zoomedImageView.transitionName = imageUrl
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
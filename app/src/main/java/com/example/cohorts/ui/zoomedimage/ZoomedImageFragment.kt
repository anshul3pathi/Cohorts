package com.example.cohorts.ui.zoomedimage

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentZoomedImageBinding
import com.example.cohorts.utils.themeColor
import com.google.android.material.transition.MaterialContainerTransform

/**
 * Fragment that contains the ZoomedImageView for displaying chat images enlarged
 */
class ZoomedImageFragment : Fragment() {

    private lateinit var binding: FragmentZoomedImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZoomedImageBinding.inflate(inflater)

        // initialise the transition
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
        arguments?.let {
            val imageUrl = ZoomedImageFragmentArgs.fromBundle(it).imageUrl
            binding.imageUrl = imageUrl
            binding.zoomedImageView.transitionName = imageUrl
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // clear the default menu of ActivityMain
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
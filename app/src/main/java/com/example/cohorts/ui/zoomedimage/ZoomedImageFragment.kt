package com.example.cohorts.ui.zoomedimage

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.cohorts.databinding.FragmentZoomedImageBinding

class ZoomedImageFragment : Fragment() {

    private lateinit var binding: FragmentZoomedImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZoomedImageBinding.inflate(inflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments.let {
            val imageUrl = ZoomedImageFragmentArgs.fromBundle(it!!).imageUrl
            binding.imageUrl = imageUrl
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
package com.example.cohorts.ui.files

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentFilesBinding

class FilesFragment : Fragment() {

    private lateinit var binding: FragmentFilesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFilesBinding.inflate(inflater)

        return binding.root
    }

}
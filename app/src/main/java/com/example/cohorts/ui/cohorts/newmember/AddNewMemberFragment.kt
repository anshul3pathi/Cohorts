package com.example.cohorts.ui.cohorts.newmember

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.cohorts.databinding.FragmentAddNewMemberBinding
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.utils.snackbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Bottom Sheet for taking userEmail as input and adding new user to [Cohort]
 */
@AndroidEntryPoint
class AddNewMemberFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddNewMemberBinding
    private lateinit var cohort: Cohort
    private val addNewMemberViewModel: AddNewMemberViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        Timber.d("onCreateView")
        binding = FragmentAddNewMemberBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        binding.apply {
            doneAddMemberButton.setOnClickListener {
                addUserToCohort()
            }
            cancelAddMemberButton.setOnClickListener {
                dismiss()
            }
        }
        arguments?.let {
            cohort = AddNewMemberFragmentArgs.fromBundle(it).cohort!!
        }

        addNewMemberViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.fragmentAddNewMemberRootLayout.snackbar(it)
            }
        })

    }

    private fun addUserToCohort() {
        val userEmail = binding.enterEmailEt.text.toString()
        if (userEmail.isNotEmpty()) {
            Timber.d("addUserToCohort")
            addNewMemberViewModel.addNewMemberToCohort(cohort, userEmail)

            // display a snackbar then dismiss the dialog
            object : CountDownTimer(3000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    dismiss()
                }
            }.start()
        }
    }

}
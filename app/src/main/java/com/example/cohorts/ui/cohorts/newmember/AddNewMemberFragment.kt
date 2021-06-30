package com.example.cohorts.ui.cohorts.newmember

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentAddNewMemberBinding
import com.example.cohorts.core.model.Cohort
import com.facebook.react.uimanager.util.ReactFindViewUtil.findView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class AddNewMemberFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "AddNewMemberBottomSheet"
    }

    private lateinit var binding: FragmentAddNewMemberBinding
//    private lateinit var firestore: FirebaseFirestore
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
//        firestore = Firebase.firestore
        binding.apply {
            doneAddMemberButton.setOnClickListener {
                addUserToCohort()
                object : CountDownTimer(3000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        dismiss()
                    }
                }.start()
            }
            cancelAddMemberButton.setOnClickListener {
                dismiss()
            }
        }
        arguments?.let {
            cohort = AddNewMemberFragmentArgs.fromBundle(it).cohort!!
        }

        addNewMemberViewModel.userAddedSuccessfully.observe(viewLifecycleOwner, { userAdded ->
            if (userAdded.isNotEmpty()) {
                Snackbar.make(
                    binding.fragmentAddNewMemberRootLayout,
                    userAdded,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })

        addNewMemberViewModel.errorMessage.observe(viewLifecycleOwner, { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Timber.e(errorMessage)
                Snackbar.make(
                    binding.fragmentAddNewMemberRootLayout,
                    errorMessage,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun addUserToCohort() {
        val userEmail = binding.enterEmailEt.text.toString()
        if (userEmail.isNotEmpty()) {
            Timber.d("addUserToCohort")
            addNewMemberViewModel.addNewMemberToCohort(cohort, userEmail)
        }
    }

//    private fun getUserFromDb() {
//        val memberEmail = binding.enterEmailEt.text.toString()
//        if (memberEmail.isNotBlank()) {
//            firestore.collection("users")
//                .whereEqualTo("userEmail", memberEmail)
//                .get()
//                .addOnSuccessListener {
//                    addSearchedUserToCohort(it.documents)
//                }
//                .addOnFailureListener { e ->
//                    Timber.e("user search failed - $e")
//                }
//        }
//    }

//    private fun addSearchedUserToCohort(documents: MutableList<DocumentSnapshot>) {
//        if (documents.size > 1) {
//            Timber.e("multiple users with the given email found")
//        } else if (documents.size == 0) {
//            Timber.d( "user not found")
//        } else {
//            val user = documents[0].data!!
//            Timber.d("User found with name: ${user["userName"]}")
//            if ((user["uid"] as String) in cohort.cohortMembers) {
//                Timber.d("user is already in cohort")
//            } else {
//                cohort.cohortMembers.add(user["uid"] as String)
//                cohort.numberOfMembers += 1
//                firestore.collection("cohorts").document(cohort.cohortUid)
//                    .set(cohort)
//            }
//        }
//    }

}
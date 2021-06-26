package com.example.cohorts.ui.cohorts.newcohort

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.cohorts.R
import com.example.cohorts.databinding.FragmentAddNewCohortBinding
import com.example.cohorts.model.Cohort
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class AddNewCohort : Fragment() {

    companion object {
        private const val TAG = "AddNewFragment"
    }

    private lateinit var binding: FragmentAddNewCohortBinding
    private lateinit var navController: NavController
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddNewCohortBinding.inflate(inflater)
        navController = findNavController()
        firestore = Firebase.firestore
        currentUser = FirebaseAuth.getInstance().currentUser!!

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.add_cohort_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.done_add_cohort_button -> {
                Timber.d( "added new Cohort - ${binding.cohortNameEt.text}")
                addNewCohortToDatabase()
                navController.popBackStack()
                true
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addNewCohortToDatabase() {
        val newCohort = Cohort(
            cohortName = binding.cohortNameEt.text.toString(),
            cohortDescription = binding.cohortDescriptionEt.text.toString()
        )
        // adding currently logged user to the new Cohort
        newCohort.cohortMembers.add(currentUser.uid)
        newCohort.numberOfMembers += 1

        firestore.collection("cohorts")
            .document(newCohort.cohortUid).set(newCohort)
    }

}
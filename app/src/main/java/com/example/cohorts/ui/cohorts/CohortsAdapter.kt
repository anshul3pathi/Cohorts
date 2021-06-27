package com.example.cohorts.ui.cohorts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cohorts.databinding.ItemCohortBinding
import com.example.cohorts.core.model.Cohort
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CohortsAdapter(
    options: FirestoreRecyclerOptions<Cohort>,
    private val itemClickListener: CohortClickListener
) : FirestoreRecyclerAdapter<Cohort, ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCohortBinding.inflate(inflater, parent, false)
        return CohortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Cohort) {
        (holder as CohortViewHolder).bind(model)
    }

    inner class CohortViewHolder(private val binding: ItemCohortBinding): ViewHolder(binding.root) {

        init {
            binding.apply {
                cohortItemMcv.setOnClickListener {
                    itemClickListener.cohortItemClicked(getItem(layoutPosition))
                }
                joinVideoCallButton.setOnClickListener { button ->
                    itemClickListener.joinVideoCallButtonClicked(button, getItem(layoutPosition))
                }
            }
        }

        fun bind(cohortItem: Cohort) {
            binding.apply {
                itemCohortNameTv.text = cohortItem.cohortName
                itemCohortDescriptionTv.text = cohortItem.cohortDescription
                itemCohortMemberTv.text = itemCohortMemberTv.context.getString(
                    R.string.member_number, cohortItem.numberOfMembers.toString()
                )
                joinVideoCallButton.visibility =
                     if (cohortItem.isCallOngoing) View.VISIBLE else View.GONE
            }
        }
    }

}
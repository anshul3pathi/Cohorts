package com.example.cohorts.ui.cohorts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cohorts.databinding.ItemCohortBinding
import com.example.cohorts.core.model.Cohort
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import timber.log.Timber

class CohortsAdapter(
    options: FirestoreRecyclerOptions<Cohort>,
) : FirestoreRecyclerAdapter<Cohort, ViewHolder>(options) {

    private var cohortItemClickListener: ((Cohort) -> Unit)? = null
    private var outlineJoinButtonClickListener: (() -> Unit)? = null
    private var containedJoinButtonClickListener: ((Cohort) -> Unit)? = null

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
//                    itemClickListener.cohortItemClicked(getItem(layoutPosition))
                    cohortItemClickListener!!(getItem(layoutPosition))
                }
                joinVideoCallContainedButton.setOnClickListener {
                    containedJoinButtonClickListener!!(getItem(layoutPosition))
                }
                joinVideoCallOutlineButton.setOnClickListener {
                    outlineJoinButtonClickListener!!()
                }
            }
        }

        fun bind(cohortItem: Cohort) {
            binding.cohort = cohortItem
        }
    }

    fun setCohortItemClickListener(listener: (Cohort) -> Unit) {
        cohortItemClickListener = listener
    }

    fun setOutlineJoinButtonClickListener(listener: () -> Unit) {
        outlineJoinButtonClickListener = listener
    }

    fun setContainedJoinButtonClickListener(listener: (Cohort) -> Unit) {
        containedJoinButtonClickListener = listener
    }

}

class ExtendedFloatingActionButtonScrollListener(
    private val extendedFab: ExtendedFloatingActionButton
) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE
            && !extendedFab.isExtended && recyclerView.computeVerticalScrollOffset() == 0) {
            Timber.d("scroll state changed")
            extendedFab.extend()
        }
        super.onScrollStateChanged(recyclerView, newState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0 && extendedFab.isExtended) {
            extendedFab.shrink()
        } else if (dy < 0  && !extendedFab.isExtended) {
            extendedFab.extend()
        }
        super.onScrolled(recyclerView, dx, dy)
    }

}
package com.example.cohorts.ui.cohorts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cohorts.databinding.ItemCohortBinding
import com.example.cohorts.core.model.Cohort
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import timber.log.Timber

/**
 * Adapter for displaying list of [Cohort]s
 * @param options [FirestoreRecyclerOptions] for displaying realtime list of [Cohort]s
 */
class CohortsAdapter(
    options: FirestoreRecyclerOptions<Cohort>,
) : FirestoreRecyclerAdapter<Cohort, ViewHolder>(options) {

    // listener for item click events
    private var cohortItemClickListener: ((Cohort, View) -> Unit)? = null

    // listener for joinButton click events
    private var joinButtonClickListener: ((Cohort) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCohortBinding.inflate(inflater, parent, false)
        return CohortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Cohort) {
        (holder as CohortViewHolder).bind(model)
    }

    inner class CohortViewHolder(private val binding: ItemCohortBinding):
        ViewHolder(binding.root) {

        fun bind(cohortItem: Cohort) {
            binding.apply {
                cohortItemMcv.apply {
                    setOnClickListener { mcv ->
                        cohortItemClickListener!!(getItem(layoutPosition), mcv)
                    }
                    transitionName = cohortItem.cohortUid
                }
                joinVideoCallContainedButton.setOnClickListener {
                    joinButtonClickListener!!(getItem(layoutPosition))
                }
            }
            binding.cohort = cohortItem
        }
    }

    fun setCohortItemClickListener(listener: (Cohort, View) -> Unit) {
        cohortItemClickListener = listener
    }

    fun setJoinButtonClickListener(listener: (Cohort) -> Unit) {
        joinButtonClickListener = listener
    }

}

/**
 * RecyclerView.OnScrollListener() for extending, shrinking and hiding
 * the add new cohort [ExtendedFloatingActionButton]
 *
 * @param extendedFab this [ExtendedFloatingActionButton] will change shape and appearance
 * according to [RecyclerView]'s scroll state
 */
class ExtendedFloatingActionButtonScrollListener(
    private val extendedFab: ExtendedFloatingActionButton
) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE
            && !extendedFab.isExtended && recyclerView.computeVerticalScrollOffset() == 0) {
            /*
            * if the top most item is visible and list items are not being scrolled
            * then extend the fab
            */
            extendedFab.extend()
        }
        if (!recyclerView.canScrollVertically(1)
            && newState == RecyclerView.SCROLL_STATE_IDLE) {
            /*
            * if the list items can't be scrolled vertically and they are not being scrolled
            * right now then hide the fab so that the last list item can be viewed
            * and clicked easily
            */
            extendedFab.hide()
        } else {
            /*
            * otherwise show the fab
            */
            extendedFab.show()
        }
        super.onScrollStateChanged(recyclerView, newState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0 && extendedFab.isExtended) {
            // if the list items are scrolled vertically upwards then shrink the fab
            extendedFab.shrink()
        } else if (dy < 0  && !extendedFab.isExtended) {
            // if the list items are scrolled vertically downwards then extend the fab
            extendedFab.extend()
        }
        super.onScrolled(recyclerView, dx, dy)
    }

}
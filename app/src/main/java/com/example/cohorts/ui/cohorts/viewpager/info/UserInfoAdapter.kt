package com.example.cohorts.ui.cohorts.viewpager.info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.cohorts.core.model.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.databinding.ItemInfoUserBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import timber.log.Timber

class UserInfoAdapter(
    options: FirestoreRecyclerOptions<User>
) : FirestoreRecyclerAdapter<User, ViewHolder>(options) {

    private lateinit var currentUser: User
    private var removeButtonOnClickListener: ((User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Timber.d("onCreateViewHolder")
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInfoUserBinding.inflate(inflater, parent, false)
        return UserInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: User) {
        (holder as UserInfoViewHolder).bind(model)
    }

    inner class UserInfoViewHolder(
        private val binding: ItemInfoUserBinding
    ) : ViewHolder(binding.root) {

        init {
            binding.itemInfoRemoveButton.setOnClickListener {
                removeButtonOnClickListener!!(getItem(layoutPosition))
            }
        }

        fun bind(user: User) {
            binding.user = user
            binding.currentUser = currentUser
            Timber.d("User - $user")
        }

    }

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun setRemoveButtonClickListener(listener: (User) -> Unit) {
        removeButtonOnClickListener = listener
    }

}
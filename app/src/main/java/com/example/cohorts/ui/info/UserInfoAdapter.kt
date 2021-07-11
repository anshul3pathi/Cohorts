package com.example.cohorts.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.cohorts.core.model.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.databinding.ItemInfoUserBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import timber.log.Timber

/**
 * Adapter for displaying list of [User]s
 * @param options for displaying the list of [User]s in realtime
 */
class UserInfoAdapter(
    options: FirestoreRecyclerOptions<User>
) : FirestoreRecyclerAdapter<User, ViewHolder>(options) {

    private lateinit var mCurrentUser: User
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

        fun bind(user: User) {
            binding.apply {
                Timber.d("current user while binding - $mCurrentUser")
                this.user = user
                this.currentUser = mCurrentUser

                itemInfoRemoveButton.setOnClickListener {
                    removeButtonOnClickListener!!(user)
                }
            }
            Timber.d("User - $user")
        }

    }

    /**
     * Sets the currentUser member variable with the given [User] object
     *
     * @param user object with data of user
     */
    fun setCurrentUser(user: User) {
        mCurrentUser = user
        Timber.d("setCurrentUser - $mCurrentUser")

    }

    /**
     * Sets removeButtonClickListener member variable
     *
     * @param listener click listener lambda function
     */
    fun setRemoveButtonClickListener(listener: (User) -> Unit) {
        removeButtonOnClickListener = listener
    }

}
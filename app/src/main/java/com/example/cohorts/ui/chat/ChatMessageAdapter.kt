package com.example.cohorts.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.R
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.User
import com.example.cohorts.databinding.ItemChatImageBinding
import com.example.cohorts.databinding.ItemChatReceivedBinding
import com.example.cohorts.databinding.ItemChatSentBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ChatMessageAdapter(
    private val options: FirebaseRecyclerOptions<ChatMessage>
) : FirebaseRecyclerAdapter<ChatMessage, ViewHolder>(options) {

    companion object {
        private const val VIEW_TYPE_TEXT_SENT = 69
        private const val VIEW_TYPE_TEXT_RECEIVED = 696969
        private const val VIEW_TYPE_IMAGE = 6969
    }

    private lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT_SENT -> {
                val binding = ItemChatSentBinding.inflate(inflater, parent, false)
                return SentTextViewHolder(binding)
            }
            VIEW_TYPE_TEXT_RECEIVED -> {
                val binding = ItemChatReceivedBinding.inflate(inflater, parent, false)
                return ReceivedTextViewHolder(binding)
            }
            else -> {
                val binding = ItemChatImageBinding.inflate(inflater, parent, false)
                ImageMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ChatMessage) {
        if (options.snapshots[position].text != null) {
            if (options.snapshots[position].userUid == currentUser.uid) {
                (holder as SentTextViewHolder).bind(model)
            } else {
                (holder as ReceivedTextViewHolder).bind(model)
            }
        } else {
            (holder as ImageMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].text != null) {
            if (options.snapshots[position].userUid == currentUser.uid) {
                VIEW_TYPE_TEXT_SENT
            } else {
                VIEW_TYPE_TEXT_RECEIVED
            }
        } else {
            VIEW_TYPE_IMAGE
        }
    }

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    inner class ReceivedTextViewHolder(private val binding: ItemChatReceivedBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class SentTextViewHolder(private val binding: ItemChatSentBinding) : ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class ImageMessageViewHolder(private val binding: ItemChatImageBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            loadImageIntoView(binding.itemChatImageIv, chat.imageUrl!!)
            if (chat.photoUrl != null) {
                loadImageIntoView(binding.itemChatImageIv, chat.imageUrl!!)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }

        private fun loadImageIntoView(view: ImageView, url: String) {
            TODO()
        }

    }

}
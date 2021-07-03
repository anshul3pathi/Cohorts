package com.example.cohorts.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.User
import com.example.cohorts.databinding.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ChatMessageAdapter(
    private val options: FirebaseRecyclerOptions<ChatMessage>
) : FirebaseRecyclerAdapter<ChatMessage, ViewHolder>(options) {

    companion object {
        private const val VIEW_TYPE_TEXT_SENT = 6
        private const val VIEW_TYPE_TEXT_RECEIVED = 69
        private const val VIEW_TYPE_IMAGE_SENT = 696
        private const val VIEW_TYPE_IMAGE_RECEIVED = 6969
    }

    private lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT_SENT -> {
                val binding = ItemChatTextSentBinding.inflate(inflater, parent, false)
                return SentTextViewHolder(binding)
            }
            VIEW_TYPE_TEXT_RECEIVED -> {
                val binding =
                    ItemChatTextReceivedBinding.inflate(inflater, parent, false)
                return ReceivedTextViewHolder(binding)
            }
            VIEW_TYPE_IMAGE_RECEIVED -> {
                val binding =
                    ItemChatImageReceivedBinding.inflate(inflater, parent, false)
                return ImageMessageReceivedViewHolder(binding)
            }
            else -> {
                val binding = ItemChatImageSentBinding.inflate(inflater, parent, false)
                ImageMessageSentViewHolder(binding)
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
            if (options.snapshots[position].userUid == currentUser.uid) {
                (holder as ImageMessageSentViewHolder).bind(model)
            } else {
                (holder as ImageMessageReceivedViewHolder).bind(model)
            }
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
            if (options.snapshots[position].userUid == currentUser.uid) {
                VIEW_TYPE_IMAGE_SENT
            } else {
                VIEW_TYPE_IMAGE_RECEIVED
            }
        }
    }

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    inner class ReceivedTextViewHolder(private val binding: ItemChatTextReceivedBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class SentTextViewHolder(private val binding: ItemChatTextSentBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class ImageMessageReceivedViewHolder(private val binding: ItemChatImageReceivedBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class ImageMessageSentViewHolder(private val binding: ItemChatImageSentBinding) :
        ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

}
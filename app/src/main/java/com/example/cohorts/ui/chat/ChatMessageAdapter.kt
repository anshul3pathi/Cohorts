package com.example.cohorts.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.User
import com.example.cohorts.databinding.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.ObservableSnapshotArray
import timber.log.Timber

/**
 * Adapter for displaying list of [ChatMessage]
 *
 * @param options [FirebaseRecyclerOptions] For displaying chats in realtime
 * @param progressBar [ProgressBar] For displaying chats loading
 * @param imageClickListener Click listener for image message click event
 */
class ChatMessageAdapter(
    private val options: FirebaseRecyclerOptions<ChatMessage>,
    private val progressBar: ProgressBar,
    private val imageClickListener: (String?, View) -> Unit
) : FirebaseRecyclerAdapter<ChatMessage, ViewHolder>(options) {

    companion object {
        private const val VIEW_TYPE_TEXT_SENT = 6
        private const val VIEW_TYPE_TEXT_RECEIVED = 69
        private const val VIEW_TYPE_IMAGE_SENT = 696
        private const val VIEW_TYPE_IMAGE_RECEIVED = 6969
    }

    private lateinit var currentUser: User
    private var isProgressBarVisible = true

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
                /*
                * if the item is of type text and the person who sent the text is current user
                * then bind the data to SentTextViewHolder
                */
                (holder as SentTextViewHolder).bind(model)
            } else {
                /*
                * if the item is of type text but the person who sent the text is not current user
                * then bind data to ReceivedTextViewHolder
                */
                (holder as ReceivedTextViewHolder).bind(model)
            }
        } else {
            if (options.snapshots[position].userUid == currentUser.uid) {
                /*
                * if the item is of type image and is sent by the current user then bind the data
                * to ImageMessageSentViewHolder
                */
                (holder as ImageMessageSentViewHolder).bind(model)
            } else {
                /*
                * if the item is of type image and is not sent by the current user then bind the
                *  data to ImageMessageReceivedViewHolder
                */
                (holder as ImageMessageReceivedViewHolder).bind(model)
            }
        }
    }

    /**
     * Depending upon the item being of text type of image type and if they are sent by
     * the current user or not, returns the View type as a constant
     */
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

    /**
     * Initialises the currentUser member variable
     * @param user the user object containing the data of the current user
     */
    fun setCurrentUser(user: User) {
        currentUser = user
    }

    inner class ReceivedTextViewHolder(private val binding: ItemChatTextReceivedBinding) :
        ViewHolder(binding.root) {

        init {
            // hide the progress bar since the chat has loaded and list views are inflated
            hideProgressBar()
        }

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class SentTextViewHolder(private val binding: ItemChatTextSentBinding) :
        ViewHolder(binding.root) {

        init {
            hideProgressBar()
        }

        fun bind(chat: ChatMessage) {
            binding.chat = chat
        }

    }

    inner class ImageMessageReceivedViewHolder(private val binding: ItemChatImageReceivedBinding) :
        ViewHolder(binding.root) {

        init {
            hideProgressBar()
        }

        fun bind(chat: ChatMessage) {
            binding.itemChatImageIv.apply {
                val imageUrl = getItem(layoutPosition).imageUrl
                setOnClickListener { view ->
                    imageClickListener(getItem(layoutPosition).imageUrl, view)
                }
                transitionName = imageUrl
            }
            binding.chat = chat
        }

    }

    inner class ImageMessageSentViewHolder(private val binding: ItemChatImageSentBinding) :
        ViewHolder(binding.root) {

        init {
            hideProgressBar()
        }

        fun bind(chat: ChatMessage) {
            binding.itemChatImageSentIv.apply {
                val imageUrl = getItem(layoutPosition).imageUrl
                setOnClickListener { view ->
                    imageClickListener(getItem(layoutPosition).imageUrl, view)
                }
                transitionName = imageUrl
            }
            binding.chat = chat
        }

    }

    /**
     * Hides the chat loading progress bar if it is not already hidden
     */
    private fun hideProgressBar() {
        if (isProgressBarVisible) {
            progressBar.visibility = View.INVISIBLE
            isProgressBarVisible = false
        }
    }

}
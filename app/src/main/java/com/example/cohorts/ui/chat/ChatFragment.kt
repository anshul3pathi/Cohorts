package com.example.cohorts.ui.chat

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.R
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.databinding.FragmentChatBinding
import com.example.cohorts.ui.cohorts.viewpager.ViewPagerFragmentDirections
import com.example.cohorts.utils.snackbar
import com.firebase.ui.database.FirebaseRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var cohortUid: String
    private lateinit var manager: LinearLayoutManager
    private lateinit var navController: NavController
    private val chatViewModel: ChatViewModel by viewModels()

    companion object {
        private const val RC_PHOTO_PICKER = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater)
        cohortUid = arguments?.getString("cohortUid").toString()
        Timber.d("cohortUid - $cohortUid")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        setupChatRcv()
        subscribeToObservers()
        binding.messageEditText.addTextChangedListener(ChatTextObserver(binding.sendButton))
        binding.addMessageImageView.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, RC_PHOTO_PICKER)
        }
        binding.sendButton.setOnClickListener {
            val textMessage = binding.messageEditText.text.toString()
            chatViewModel.sendNewMessage(textMessage, cohortUid)
            binding.messageEditText.setText("")
        }
        binding.chatProgressBar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                Timber.d("Uri: ${uri.toString()}")
                chatViewModel.sendImageMessage(uri, cohortUid)
            }
        }
    }

    override fun onStart() {
        chatMessageAdapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        chatMessageAdapter.stopListening()
        super.onStop()
    }

    private fun subscribeToObservers() {
        chatViewModel.errorMessage.observe(viewLifecycleOwner, { errorMessage ->
            snackbar(binding.chatRootLayout, errorMessage)
        })
        chatViewModel.currentUser.observe(viewLifecycleOwner, { currentUser ->
            chatMessageAdapter.setCurrentUser(currentUser)
        })
    }

    private fun setupChatRcv() {
        val chatRef = chatViewModel.fetchChatReference(cohortUid)
        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(chatRef!!, ChatMessage::class.java)
            .build()
        chatMessageAdapter = ChatMessageAdapter(options) { imageUrl ->
            navController.navigate(
                ViewPagerFragmentDirections.actionZoomedImage(imageUrl)
            )
        }
        chatViewModel.getCurrentUser()
        binding.chatRcv.apply {
            manager = LinearLayoutManager(requireContext())
            manager.stackFromEnd = true
            adapter = chatMessageAdapter
            layoutManager = manager
        }
        chatMessageAdapter.registerAdapterDataObserver(
            ScrollToBottomObserver(binding.chatRcv, chatMessageAdapter, manager)
        )
    }
}
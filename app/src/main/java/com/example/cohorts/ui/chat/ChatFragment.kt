package com.example.cohorts.ui.chat

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cohorts.R
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.databinding.FragmentChatBinding
import com.example.cohorts.ui.main.MainActivity
import com.example.cohorts.utils.snackbar
import com.example.cohorts.utils.themeColor
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Displays a list of [ChatMessage] in the form of group chat.
 */

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var cohortArgument: Cohort
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

        // receive the cohort argument passed from CohortsFragment
        arguments.let {
            cohortArgument = ChatFragmentArgs.fromBundle(it!!).cohort!!

            (activity as AppCompatActivity).supportActionBar?.title = cohortArgument.cohortName
            (activity as AppCompatActivity).supportActionBar?.subtitle =
                cohortArgument.cohortDescription
        }

        // initialise the sharedElementTransition
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        binding.chatRootLayout.transitionName = cohortArgument.cohortUid

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()

        setupChatRcv()

        subscribeToObservers()

        binding.apply {
            messageEditText.addTextChangedListener(ChatTextObserver(binding.sendButton))

            addMessageImageView.setOnClickListener {
                // code for opening the photo picker intent
                val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, RC_PHOTO_PICKER)
            }

            sendButton.setOnClickListener {
                val textMessage = binding.messageEditText.text.toString()
                chatViewModel.sendNewMessage(textMessage, cohortArgument.cohortUid)
                binding.messageEditText.setText("")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                // If photo was picked successfully, then send this image as message
                val uri = data.data
                Timber.d("Uri: ${uri.toString()}")
                chatViewModel.sendImageMessage(uri, cohortArgument.cohortUid)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // clear the default menu of MainActivity
        menu.clear()

        // inflate menu specific to this fragment
        inflater.inflate(R.menu.chat_fragment_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_new_member_menu_item -> {
                // navigate to AddNewMemberFragment
                val action = ChatFragmentDirections
                    .actionChatToAddNewMember(cohortArgument)
                navController.navigate(action)
                true
            } R.id.cohort_info_menu_item -> {
                // navigate to CohortInfoFragment
                navController.navigate(
                    ChatFragmentDirections
                        .actionChatToCohortInfo(cohortArgument)
                )
                true
            } R.id.item_go_to_tasks -> {
                // navigate to TasksFragment
                navController.navigate(
                    ChatFragmentDirections.actionChatToTasks(cohortArgument)
                )
                true
            } R.id.start_video_call_menu_button -> {
                // start a new video meeting
                startMeeting()
                true
            } R.id.delete_cohort_menu_item -> {
                deleteThisCohort()
                true
            } else -> return super.onOptionsItemSelected(item)
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

    private fun deleteThisCohort() {
        val context = requireContext()
        MaterialAlertDialogBuilder(context)
            .setTitle("Are you sure you want to delete ${cohortArgument.cohortName}?")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") {_, _ ->
                chatViewModel.deleteThisCohort(cohortArgument)
            }.show()
    }

    private fun startMeeting() {
        // initialise Jitsi with context and broadcastReceiver
        chatViewModel.initialiseJitsi(
            (activity as MainActivity).broadcastReceiver,
            requireContext()
        )

        chatViewModel.startNewMeeting(
            cohortArgument,
            requireContext()
        )
    }

    private fun subscribeToObservers() {
        chatViewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                binding.chatRootLayout.snackbar(it)
            }
        })

        chatViewModel.currentUser.observe(viewLifecycleOwner, { currentUser ->
            chatMessageAdapter.setCurrentUser(currentUser)
        })

        chatViewModel.cohortDeleted.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    /*
                    * if the cohort is deleted then display a confirmation snackbar
                    * and pop out of this fragment
                    */
                    object : CountDownTimer(3000L, 500L) {
                        override fun onTick(millisUntilFinished: Long) {}

                        override fun onFinish() {
                            navController.navigateUp()
                        }
                    }.start()
                }
            }
        })
    }

    private fun setupChatRcv() {
        val chatRef = chatViewModel.fetchChatReference(cohortArgument.cohortUid)!!

        // see if chats exist
        // if chats exist then show them in recycler view otherwise show the start new chat TV
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // chats exist, show them in recycler view and hide start new chat message
                    binding.chatStartChatTv.visibility = View.INVISIBLE
                } else {
                    // chats do no exist hide the progress bar and show start chat message
                    binding.chatStartChatTv.visibility = View.VISIBLE
                    binding.chatProgressBar.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // build options for FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(chatRef, ChatMessage::class.java)
            .build()

        chatMessageAdapter =
            ChatMessageAdapter(options, binding.chatProgressBar) { imageUrl, view ->
            val extras = FragmentNavigatorExtras(
                view to imageUrl!!
            )
            navController.navigate(
                ChatFragmentDirections.actionChatToDetailedImage(imageUrl),
                extras
            )
        }

        // get the data of current user and save it in chatViewModel as liveData
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
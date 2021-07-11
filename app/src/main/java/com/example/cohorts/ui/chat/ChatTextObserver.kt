package com.example.cohorts.ui.chat

import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import com.example.cohorts.R

/**
 * TextWatcher for observing text changes in messageEditText
 */
class ChatTextObserver(private val button: ImageView) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        /**
         * if the messageEditText has some text, enable the sendButton
         * otherwise disable it
         */
        if (s.toString().trim().isNotEmpty()) {
            button.isEnabled = true
            button.setImageResource(R.drawable.outline_send_24)
        } else {
            button.isEnabled = false
            button.setImageResource(R.drawable.outline_send_gray_24)
        }
    }

    override fun afterTextChanged(s: Editable?) {}
}
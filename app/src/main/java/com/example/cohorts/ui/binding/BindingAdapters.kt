package com.example.cohorts.ui.binding

import android.view.View
import androidx.databinding.BindingAdapter
import com.example.cohorts.core.model.User
import com.google.android.material.button.MaterialButton

@BindingAdapter(value = ["bind:user", "bind:currentUser"], requireAll = true)
fun setUserInfoButtonText(view: View, user: User, currentUser: User) {
    (view as MaterialButton).text = if (user.uid == currentUser.uid) "Leave" else "Remove"
}
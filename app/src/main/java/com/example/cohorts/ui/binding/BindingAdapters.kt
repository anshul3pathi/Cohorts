package com.example.cohorts.ui.binding

import android.media.Image
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.cohorts.R
import com.example.cohorts.core.model.User
import com.google.android.material.button.MaterialButton

@BindingAdapter(value = ["bind:user", "bind:currentUser"], requireAll = true)
fun setUserInfoButtonText(view: View, user: User, currentUser: User) {
    (view as MaterialButton).text = if (user.uid == currentUser.uid) "Leave" else "Remove"
}

@BindingAdapter("photoUrl")
fun photoUrl(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    } else {
        Glide.with(view.context).load(R.drawable.ic_account_circle_black_36dp).into(view)
    }
}

@BindingAdapter("imageUrl")
fun imageUrl(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    }
}

@BindingAdapter("numberOfMembers")
fun numberOfMembers(view: TextView, numberOfMembers: Int) {
    view.text = view.context.getString(R.string.member_number, numberOfMembers.toString())
}

@BindingAdapter("viewGoneOrVisible")
fun viewGoneOrVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}
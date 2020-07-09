package com.arjun.covid19tracker.util

import android.app.Activity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

fun View.visibility(constraint: Boolean) {
    visibility = if (constraint)
        View.VISIBLE
    else
        View.GONE
}

fun TextView.drawableEnd(@DrawableRes id: Int = 0) {
    this.setCompoundDrawablesWithIntrinsicBounds(0, 0, id, 0)
}

fun Fragment.showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, length).show()
}

fun Activity.showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, length).show()
}
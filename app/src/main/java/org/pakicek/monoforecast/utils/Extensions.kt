package org.pakicek.monoforecast.utils

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(message: String, backgroundColorRes: Int, textColorRes: Int) {
    val snack = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    val context = this.context

    snack.setBackgroundTint(ContextCompat.getColor(context, backgroundColorRes))
    snack.setTextColor(ContextCompat.getColor(context, textColorRes))

    snack.show()
}

fun View.showSnackbar(message: String) {
    val snack = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

    snack.show()
}
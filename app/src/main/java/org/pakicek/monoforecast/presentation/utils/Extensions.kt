package org.pakicek.monoforecast.presentation.utils

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(message: String, backgroundColorRes: Int? = null, textColorRes: Int? = null) {
    val snack = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    val context = this.context

    if (backgroundColorRes != null) {
        snack.setBackgroundTint(ContextCompat.getColor(context, backgroundColorRes))
    }
    if (textColorRes != null) {
        snack.setTextColor(ContextCompat.getColor(context, textColorRes))
    }

    snack.show()
}
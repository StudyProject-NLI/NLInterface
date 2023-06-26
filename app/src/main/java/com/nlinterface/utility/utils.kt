package com.nlinterface.utility

import android.content.res.Resources
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

fun setViewRelativeWidth(view: View, relWidth: Double) {
    val width: Int = (Resources.getSystem().displayMetrics.widthPixels * relWidth).toInt()
    view.layoutParams.width = width
}

fun setViewRelativeHeight(view: View, relHeight: Double) {
    val height = (Resources.getSystem().displayMetrics.heightPixels * relHeight).toInt()
    view.layoutParams.height = height
}

fun setViewRelativeSize(view: View, relWidth:Double, relHeight: Double) {
    val height = (Resources.getSystem().displayMetrics.heightPixels * relHeight).toInt()
    val width = (Resources.getSystem().displayMetrics.widthPixels * relWidth).toInt()
    view.layoutParams = ConstraintLayout.LayoutParams(width, height)
}
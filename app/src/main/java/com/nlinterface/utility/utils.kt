package com.nlinterface.utility

import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.dataclasses.GroceryItem
import java.io.BufferedReader
import java.io.File
import java.util.Dictionary
import java.util.Locale

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

fun getLocaleType() : LocaleType {

    return if (Locale.getDefault().toString().contains("en", true)) {
        LocaleType.EN
    } else if (Locale.getDefault().toString().contains("de", true)) {
        LocaleType.DE
    } else {
        LocaleType.OTHER
    }

}

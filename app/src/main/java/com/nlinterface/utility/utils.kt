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

fun cleanSTTInput(input: String) : String {

    val punctuationRegex = Regex("[^\\w\\süäöß]")

    var cleanInput = input.lowercase()
    cleanInput = cleanInput.replace(punctuationRegex, "")

    cleanInput = if (getLocaleType() == LocaleType.EN) {
        cleanVoiceInputEN(cleanInput)
    } else if (getLocaleType() == LocaleType.DE) {
        cleanVoiceInputDE(cleanInput)
    } else {
        Log.println(Log.DEBUG, "cleanVoiceInput", "Locale = Locale.OTHER")
        cleanVoiceInputEN(cleanInput)
    }

    return cleanInput

}

private fun cleanVoiceInputEN(input: String): String {

    val word2Digit = mapOf(
        "a" to 1,
        "an" to 1,
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6
    )

    return input.replace(word2Digit)

}

private fun cleanVoiceInputDE(input: String): String {

    val word2Digit = mapOf(
        "ein" to 1,
        "eine" to 1,
        "einen" to 1,
        "zwei" to 2,
        "drei" to 3,
        "view" to 4,
        "fünf" to 5,
        "sechs" to 6)

    return input.replace(word2Digit)

}

private fun String.replace(map: Map<String, Int>): String {
    var result = this
    map.forEach { (k, v) -> result = result.replace(k, v) }
    return result
}

private fun String.replace(k: String, v: Int): String {
    val result = this
    result.replace(k, v.toString())
    return result
}

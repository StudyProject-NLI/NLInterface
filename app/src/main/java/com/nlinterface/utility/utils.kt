package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.nlinterface.activities.BarcodeSettingsActivity
import com.nlinterface.activities.ClassificationActivity
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.activities.MainActivity
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.SettingsActivity
import java.util.Locale

/**
 * Resize a View's width relative to the screen size.
 *
 * @param view: View to be resized
 * @param relWidth: Double, the desired relative width
 */
fun setViewRelativeWidth(view: View, relWidth: Double) {
    val width: Int = (Resources.getSystem().displayMetrics.widthPixels * relWidth).toInt()
    view.layoutParams.width = width
}

/**
 * Resize a View's height relative to the screen size.
 *
 * @param view: View to be resized
 * @param relHeight: Double, the desired relative height
 */
fun setViewRelativeHeight(view: View, relHeight: Double) {
    val height = (Resources.getSystem().displayMetrics.heightPixels * relHeight).toInt()
    view.layoutParams.height = height
}

/**
 * Resize a View's width and height relative to the screen size.
 *
 * @param view: View to be resized
 * @param relWidth: Double, the desired relative width
 * @param relHeight: Double, the desired relative height
 */
fun setViewRelativeSize(view: View, relWidth: Double, relHeight: Double) {
    val height = (Resources.getSystem().displayMetrics.heightPixels * relHeight).toInt()
    val width = (Resources.getSystem().displayMetrics.widthPixels * relWidth).toInt()
    view.layoutParams = ConstraintLayout.LayoutParams(width, height)
}

/**
 * Evaluate the currently used Locale to a Locale Type
 *
 * @return LocaleType.EN, if default is english, LocaleType.DE, if default is german,
 * else LocaleType.OTHER
 */
fun getLocaleType(): LocaleType {
    
    return if (Locale.getDefault().toString().contains("en", true)) {
        LocaleType.EN
    } else if (Locale.getDefault().toString().contains("de", true)) {
        LocaleType.DE
    } else {
        LocaleType.OTHER
    }
    
}

/**
 * Handles navigation to next activity.
 *
 * @param context
 * @param activity: ActivityType, Enum specifying the activity
 */
fun navToActivity(context: Context, activity: ActivityType) {
    
    when (activity) {
        
        ActivityType.MAIN ->
            context.startActivity(Intent(context, MainActivity::class.java))
    
        ActivityType.GROCERYLIST ->
            context.startActivity(Intent(context, GroceryListActivity::class.java))
        
        ActivityType.PLACEDETAILS ->
            context.startActivity(Intent(context, PlaceDetailsActivity::class.java))

        ActivityType.CLASSIFICATION ->
            context.startActivity(Intent(context, ClassificationActivity::class.java))
        
        ActivityType.SETTINGS ->
            context.startActivity(Intent(context, SettingsActivity::class.java))

        ActivityType.BARCODESETTINGS ->
            context.startActivity(Intent(context, BarcodeSettingsActivity::class.java))
    }
    
}
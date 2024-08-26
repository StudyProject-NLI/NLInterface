package com.nlinterface.utility

import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * The Swipe Touch Interceptor allows the app to check for vertical swipes. The viewPager is very
 * sensitive to horizontal swipes. Therefore is is intercepted to first check for vertical ones.
 * If the Interceptor detects a vertical swipe, it intercepts the viewPager and delegates to the
 * Fragments functionalities.
 */
class OnSwipeTouchInterceptor(
    private val swipeAction: SwipeAction
) : RecyclerView.OnItemTouchListener {


    private var initialXValue: Float = 0f
    private var initialYValue: Float = 0f
    private val horizontalSwipeThreshold = 50
    private val verticalSwipeThreshold = 25

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                initialXValue = e.x
                initialYValue = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                val diffX = e.x - initialXValue
                val diffY = e.y - initialYValue
                if (abs(diffY) > abs(diffX) && abs(diffY) > verticalSwipeThreshold) {
                    // Handle vertical swipe
                    if (diffY > 0) {
                        swipeAction.onSwipeDown()
                    } else {
                        swipeAction.onSwipeUp()
                    }
                    Log.d("SwipeAction","Vertical Swipe Action detected")
                    return true
                } else if (abs(diffX) > horizontalSwipeThreshold) {
                    return false
                }
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
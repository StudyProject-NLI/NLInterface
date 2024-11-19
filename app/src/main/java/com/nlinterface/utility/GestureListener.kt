package com.nlinterface.utility

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * A class that implements the usage of swipe and touch navigation. It differentiates between a
 * swipe to the left, right, top, down and a double tap as well as holding.
 */
open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val swipeThreshold = 25
        private val swipeVelocityThreshold = 25

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    return true
                }
            } else {
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY > 0) {
                        onSwipeDown()
                    } else {
                        onSwipeUp()
                    }
                    return true
                }
            }
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPress()
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap()
            return true
        }
    }

    /**
     * Just empty bodies to allow each fragment its own functionalities when receiving the
     * corresponding inout.
     */
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onLongPress() {}
    open fun onDoubleTap() {}
}

/**
 * Interface that is passed in Fragment Creation to implement functionalities in the fragment itself
 */
interface SwipeAction {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeUp()
    fun onSwipeDown()
    fun onLongPress()
    fun onDoubleTap()
}

/**
 * Class that allows fragment creation with a SwipeNavigationListener class.
 */
class SwipeNavigationListener(
    context: Context,
    private var swipeAction: SwipeAction?
) : OnSwipeTouchListener(context) {

    override fun onSwipeRight() {
        swipeAction?.onSwipeRight()
    }

    override fun onSwipeLeft() {
        swipeAction?.onSwipeLeft()
    }

    override fun onSwipeUp() {
        swipeAction?.onSwipeUp()

    }

    override fun onSwipeDown() {
        swipeAction?.onSwipeDown()
    }

    override fun onLongPress() {
        swipeAction?.onLongPress()
    }

    override fun onDoubleTap() {
        swipeAction?.onDoubleTap()
    }
}
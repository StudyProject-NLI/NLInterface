package com.nlinterface.utility

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

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
            val diffY = e2!!.y - e1!!.y
            val diffX = e2.x - e1.x
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > swipeThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    return true
                }
            } else {
                if (Math.abs(diffY) > swipeThreshold && Math.abs(velocityY) > swipeVelocityThreshold) {
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

    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onLongPress() {}
    open fun onDoubleTap() {}
}

interface SwipeAction {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeUp()
    fun onSwipeDown()
    fun onLongPress()
    fun onDoubleTap()
}

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
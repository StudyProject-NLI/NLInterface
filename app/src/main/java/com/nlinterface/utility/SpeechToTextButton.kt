package com.nlinterface.utility

import android.R
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat

class SpeechToTextButton : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        setImageResource(com.nlinterface.R.drawable.ic_mic_white)
    }
}
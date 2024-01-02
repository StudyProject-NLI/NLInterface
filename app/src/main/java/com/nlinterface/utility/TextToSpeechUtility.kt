package com.nlinterface.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechUtility(context: Context, listener: OnInitListener)
    : TextToSpeech(context, listener) {

    private var textToSpeechEngine: TextToSpeech = TextToSpeech(context, listener)

    fun setLocale(locale: Locale = Locale.getDefault()) {
        textToSpeechEngine.language = locale
    }

    fun setSpeedRate(speedRate: Float = 1.2F) {
        textToSpeechEngine.setSpeechRate(speedRate)
    }

    fun say(text: String, queueMode: Int) {
        textToSpeechEngine.speak(text, queueMode, null, "tts1")
    }

    fun onPause() {
        textToSpeechEngine.stop()
    }

    fun onDestroy() {
        textToSpeechEngine.shutdown()
    }
}

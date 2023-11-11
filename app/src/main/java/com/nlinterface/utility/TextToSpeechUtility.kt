package com.nlinterface.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechUtility(context: Context, listener: OnInitListener)
    : TextToSpeech(context, listener) {

    private var textToSpeechEngine: TextToSpeech = TextToSpeech(context, listener)

    fun setLocale(locale: Locale = Locale.US) {
        textToSpeechEngine.language = locale
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

// USE CASE EXAMPLE
/*
class SpeechToTextActivity : AppCompatActivity() {
    private val textToSpeechUtility = TextToSpeechUtility()


    fun onCreate() {
        ...
        textToSpeechUtility.createTTSEngine(this, Locale.UK)
        ...
    }
    your_button.setOnClickListener {
        ...
        textToSpeechUtility.say("Hello World!")
        ...
    }

}

*/

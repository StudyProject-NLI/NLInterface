package com.nlinterface.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechUtility {

    private var textToSpeechEngine: TextToSpeech? = null
    fun createTTSEngine(context: Context, language: Locale = Locale.UK) {
        textToSpeechEngine = TextToSpeech(context
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine?.language = language
            }
        }
    }

    fun say(text: String) {
        textToSpeechEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    fun onPause() {
        textToSpeechEngine?.stop()
    }

    fun onDestroy() {
        textToSpeechEngine?.shutdown()
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

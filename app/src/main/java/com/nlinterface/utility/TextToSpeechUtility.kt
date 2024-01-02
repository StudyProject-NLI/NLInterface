package com.nlinterface.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * /**
 *  * Helper class for android.speech.tts.TextToSpeech.
 *  */
 */
class TextToSpeechUtility(context: Context, listener: OnInitListener) :
    TextToSpeech(context, listener) {

    private var textToSpeechEngine: TextToSpeech = TextToSpeech(context, listener)

    /**
     * Set the engine language to the given locale.
     *
     * @param locale: Locale, the Locale to be used
     */
    fun setLocale(locale: Locale = Locale.getDefault()) {
        textToSpeechEngine.language = locale
    }

    /**
     * Sets the engine speech speed rate.
     *
     * @param speedRate: Float, the desired speed rate
     */
    fun setSpeedRate(speedRate: Float = 1.2F) {
        textToSpeechEngine.setSpeechRate(speedRate)
    }

    /**
     * Reads a string out loud.
     *
     * @param text: String to be read out loud
     * @param queueMode: Int, defines whether to append the text to the speaking queue (QUEUE.ADD)
     * or to overwrite the queue with the text (QUEUE.FLUSH)
     */
    fun say(text: String, queueMode: Int) {
        textToSpeechEngine.speak(text, queueMode, null, "tts1")
    }

    /**
     * Stops the speech engine if the activity is paused.
     */
    fun onPause() {
        textToSpeechEngine.stop()
    }

    /**
     * Destroys the speech engine at the end of the activity lifecycle.
     */
    fun onDestroy() {
        textToSpeechEngine.shutdown()
    }
}

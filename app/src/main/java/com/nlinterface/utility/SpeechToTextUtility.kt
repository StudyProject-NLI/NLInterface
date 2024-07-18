package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Helper class for android.speech.SpeechRecognizer.
 */
class SpeechToTextUtility {

    private lateinit var speechRecognizer: SpeechRecognizer


    /**
     * Calls speechRecognizer to begin listening to voice input.
     *
     * @param locale: Locale, the language that should be used for speech recognition
     *
     * TODO: error handling
     */
    fun handleSpeechBegin(locale: Locale = Locale.getDefault()) {
        if (this::speechRecognizer.isInitialized) {
            speechRecognizer.startListening(createIntent(locale))
        }
    }

    /**
     * Cancels the listening process of the speechRecognizer.
     *
     * TODO: error handling
     */
    fun cancelListening() {
        if (this::speechRecognizer.isInitialized) {
            speechRecognizer.cancel()
        }
    }

    /**
     * Initializes the SpeechRecognizer and defines the callbacks.
     *
     * @param context: Context, the activity context
     */
    fun createSpeechRecognizer(context: Context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }
    
    /**
     * Defines the callbacks for the SpeechRecognitionListener.
     *
     * @param onResults: lambda, handles the processing of speech recognition results
     * @param onEndOfSpeech: lambda, handles the end of speech
     * @param onError: lambda, error handling
     */
    fun setSpeechRecognitionListener(
        onResults: (results: Bundle) -> Unit,
        onEndOfSpeech: () -> Unit,
        onError: (p0: Int) -> Unit
    ) {
    
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
        
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                onEndOfSpeech
            }

            override fun onError(p0: Int) {
                onError
            }
        
            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
            override fun onResults(results: Bundle) {
                onResults(results)
            }
        
        })
        
    }

    /**
     * Creates an Intent for the SpeechRecognizer. Carries the information that the action is to be
     * speech recognition, which language model to use, that partial results are accepted and which
     * language (locale) to use.
     *
     * @param locale: Locale, the language to be used for speech recognition
     *
     * @return a constructed Intent
     */
    private fun createIntent(locale: Locale = Locale.getDefault()): Intent {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)

        return intent
    }
}

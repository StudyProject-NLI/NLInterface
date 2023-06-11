package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.TextView

class SpeechToTextUtility {
    private var speechRecognizer: SpeechRecognizer? = null

    fun handleSpeechBegin(output: TextView, button: SpeechToTextButton) {
        output!!.setText(com.nlinterface.R.string.placeholder_text)
        speechRecognizer!!.startListening(createIntent())
        button!!.setImageResource(com.nlinterface.R.drawable.ic_mic_green)
    }

    fun handleSpeechEnd(output: TextView, button: SpeechToTextButton) {
        output!!.setText(com.nlinterface.R.string.stt_output_content)
        speechRecognizer!!.cancel()
        button!!.setImageResource(com.nlinterface.R.drawable.ic_mic)
    }

    fun createSpeechRecognizer(context: Context, output: TextView) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {}

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // The results are added in decreasing order of confidence to the list
                    val result = matches[0]
                    output!!.text = result
                }
            }

            override fun onPartialResults(partialResults: Bundle) {
                val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // handle partial speech results
                    val partialResult = matches[0]
                    output!!.text = partialResult
                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {}
        })
    }

    private fun createIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de") // remove this line for english version; TODO: global setting for language
        return intent
    }
}
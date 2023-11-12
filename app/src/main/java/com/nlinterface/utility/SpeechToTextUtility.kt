package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageButton
import androidx.lifecycle.MutableLiveData
import com.nlinterface.R
import java.util.Locale


class SpeechToTextUtility {

    private var speechRecognizer: SpeechRecognizer? = null

    fun handleSpeechBegin() {
        speechRecognizer!!.startListening(createIntent())
    }

    fun cancelListening() {
        speechRecognizer!!.cancel()
    }

    fun createSpeechRecognizer(context: Context, onResults: (results: Bundle) -> Unit, onEndOfSpeech: () -> Unit) {

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {onEndOfSpeech}
            override fun onError(p0: Int) {}
            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
            override fun onResults(results: Bundle) {onResults(results) }
        })
    }

    private fun createIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        return intent
    }
}

// USE CASE EXAMPLE
/*
class SpeechToTextActivity : AppCompatActivity() {

    private var isListening = false
    private var outputText: TextView? = null
    private var sttTrigger: SpeechToTextButton? = null
    private val speechToTextUtility = SpeechToTextUtility()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_to_text_example)

        outputText = findViewById(R.id.outputTV)

        sttTrigger = findViewById(R.id.stt_btn)
        sttTrigger!!.setOnClickListener {
            if (isListening) {
                speechToTextUtility.handleSpeechEnd(outputText!!, sttTrigger!!)
                isListening = false
            } else {
                speechToTextUtility.handleSpeechBegin(outputText!!, sttTrigger!!)
                isListening = true
            }
        }

        speechToTextUtility.createSpeechRecognizer(this, outputText!!)
    }
}*/

package com.nlinterface.utility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.nlinterface.R
import java.util.Locale
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.activities.MainActivity
import org.w3c.dom.Text

class SpeechToTextUtility {
    private var speechRecognizer: SpeechRecognizer? = null
    var commandsList: MutableList<String>? = null

    // make it a Singleton
    companion object {
        private var mInstance: SpeechToTextUtility? = null

        @get:Synchronized
        val instance: SpeechToTextUtility?
            get() {
                if (null == mInstance) {
                    mInstance = SpeechToTextUtility()
                }
                return mInstance
            }
    }

    fun handleSpeechBegin(button: ImageButton) {
        button.setImageResource(R.drawable.ic_mic_green)
        speechRecognizer!!.startListening(createIntent())
    }

    fun createSpeechRecognizer(context: Context, button: ImageButton) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                button!!.setImageResource(R.drawable.ic_mic_white)
            }
            override fun onError(error: Int) {}

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // results are added in decreasing order of confidence to the list, so choose the first one
                    val result = matches[0]
                    button!!.setImageResource(R.drawable.ic_mic_white)
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                }
            }

            // handle partial speech results for dynamic speech to text recognition
            override fun onPartialResults(partialResults: Bundle) {}

            override fun onEvent(eventType: Int, params: Bundle) {}
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

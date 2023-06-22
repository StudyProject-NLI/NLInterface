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
    var currentContext: Context? = null

    fun handleSpeechBegin(output: TextView, button: SpeechToTextButton) {
        output!!.setText(com.nlinterface.R.string.placeholder_text)
        speechRecognizer!!.startListening(createIntent())
        button!!.setImageResource(com.nlinterface.R.drawable.ic_mic_green)
    }

    fun handleSpeechEnd(output: TextView, button: SpeechToTextButton) {
        output!!.setText(R.string.stt_output_content)
        speechRecognizer!!.cancel()
        button!!.setImageResource(com.nlinterface.R.drawable.ic_mic)
    }

    fun createSpeechRecognizer(context: Context, output: TextView) {
        currentContext = context
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
                    // results are added in decreasing order of confidence to the list, so choose the first one
                    val result = matches[0]
                    output!!.text = result
                    handleCommand(result, output)
                }
            }

            // handle partial speech results for dynamic speech to text recognition
            override fun onPartialResults(partialResults: Bundle) {
                val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        return intent
    }


    private fun handleCommand(command: String, output: TextView) {
        if (commandsList!!.contains(command)) {
            val intent = Intent(currentContext!!, GroceryListActivity::class.java)
            currentContext!!.startActivity(intent)
        } else {
            output.text = "$command cannot be recognized"
        }
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

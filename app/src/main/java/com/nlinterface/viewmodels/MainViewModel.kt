package com.nlinterface.viewmodels

import android.app.Application
import android.content.Intent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nlinterface.R
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.SettingsActivity
import com.nlinterface.utility.SpeechToTextUtility
import com.nlinterface.utility.TextToSpeechUtility
import java.util.Locale

class MainViewModel(
    application: Application
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeechUtility

    private val _ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val ttsInitialized: LiveData<Boolean>
        get() = _ttsInitialized

    private val _isListening: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val isListening: LiveData<Boolean>
        get() = _isListening

    private val _command: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    val command: LiveData<ArrayList<String>>
        get() = _command

    private val stt = SpeechToTextUtility()

    fun initTTS() {
        tts = TextToSpeechUtility(getApplication<Application>().applicationContext, this)
    }

    fun say(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (ttsInitialized.value == true) {
            tts.say(text, queueMode)
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.setLocale(Locale.getDefault())
            _ttsInitialized.value = true
        } else {
            Log.println(Log.ERROR, "tts onInit", "Couldn't initialize TTS Engine")
        }
    }

    fun initSTT() {
        stt.createSpeechRecognizer(getApplication<Application>().applicationContext,
            onResults = {
                cancelListening()
                val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // results are added in decreasing order of confidence to the list, so choose the first one
                    handleSpeechResult(matches[0])
            }
        }, onEndOfSpeech = {
            cancelListening()
        })
    }

    private fun handleSpeechResult(s: String?) {

        val string = s?.lowercase()
        val cmd = ArrayList<String>()

        if (string != null) {
            if (string.contains(Regex("go to grocery list"))) {
                cmd.add("GOTO")
                cmd.add("GL")
            } else if (string.contains(Regex("go to place details"))) {
                cmd.add("GOTO")
                cmd.add("PD")
            } else if (string.contains(Regex("go to settings"))) {
                cmd.add("GOTO")
                cmd.add("S")
            } else {
                cmd.add("")
                cmd.add("")
            }

            cmd.add("")

            _command.value = cmd
        }
    }

    fun handleSpeechBegin() {
        stt.handleSpeechBegin()
        _isListening.value = true
    }

    fun cancelListening() {
        stt.cancelListening()
        _isListening.value = false
    }
}
package com.nlinterface.viewmodels

import android.app.Application
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.R
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.utility.SpeechToTextUtility
import com.nlinterface.utility.TextToSpeechUtility
import java.io.BufferedReader
import java.io.File
import java.util.Locale


class GroceryListViewModel (
    application: Application
) : AndroidViewModel(application), OnInitListener {

    private val context = application
    private val groceryListFileName = "GroceryList.json"
    private val groceryListFile: File = File(context.filesDir, groceryListFileName)
    var groceryList: ArrayList<GroceryItem> = ArrayList<GroceryItem>()
    private var gson = Gson()

    private lateinit var tts: TextToSpeechUtility

    val ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

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

    fun fetchGroceryList() {

        if (!groceryListFile.exists()) {
            groceryListFile.createNewFile()
        }

        if (groceryListFile.length() > 0) {
            val bufferedReader: BufferedReader = groceryListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            groceryList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<GroceryItem?>?>() {}.type
            ) as ArrayList<GroceryItem>
        }

        Log.println(Log.DEBUG, "1", groceryList.toString())
    }

    fun addGroceryItem(itemName: String): ArrayList<GroceryItem> {
        groceryList.add(GroceryItem(itemName, groceryList.size, false))
        storeGroceryList()
        return groceryList
    }

    fun deleteGroceryItem(groceryItem: GroceryItem) {
        storeGroceryList()
        groceryList.remove(groceryItem)
    }

    fun placeGroceryItemInCart(groceryItem: GroceryItem): Boolean {
        storeGroceryList()
        groceryItem.inCart = !groceryItem.inCart

        return groceryItem.inCart
    }

    fun storeGroceryList() {
        val jsonString : String = gson.toJson(groceryList)
        groceryListFile.writeText(jsonString)
    }

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
            ttsInitialized.value = true
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
            if (string.contains(Regex("go to"))) {
                cmd.add("GOTO")
                if (string.contains(Regex("main menu"))) {
                    cmd.add("MM")
                } else if (string.contains(Regex("place details"))) {
                    cmd.add("PD")
                } else if (string.contains(Regex("settings"))) {
                    cmd.add("S")
                } else {
                    cmd.add("")
                }
            } else if (string.contains(Regex("add"))) {
                cmd.add("ADD")
            } else {
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
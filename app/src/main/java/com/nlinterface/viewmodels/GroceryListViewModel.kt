package com.nlinterface.viewmodels

import android.app.Application
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.utility.SpeechToTextUtility
import com.nlinterface.utility.TextToSpeechUtility
import java.io.BufferedReader
import java.io.File
import java.util.Locale


/**
 * The GroceryListViewModel interfaces the user interactions with the view (the activity) and the
 * model. It handles the adding/removing/placing into cart/taking out of cart functionality on the
 * data, fetches and stores the data and handles the TTS and STT systems.
 *
 */
class GroceryListViewModel(
    application: Application
) : AndroidViewModel(application), OnInitListener {

    private var gson = Gson()

    private lateinit var groceryListFile: File
    var groceryList: ArrayList<GroceryItem> = ArrayList()

    private lateinit var tts: TextToSpeechUtility
    private val stt = SpeechToTextUtility()

    // holds boolean, whether the TTS system has successfully been initialized.
    // private MutableLiveData, so that only the ViewModel has access to the setter
    private val _ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    // outward LiveData for _ttsInitialized. Immutable, so only access to the getter is given to
    // outside classes, and LiveData so that observers can be set.
    val ttsInitialized: LiveData<Boolean>
        get() = _ttsInitialized

    // holds boolean, whether the STT system is currently listening.
    private val _isListening: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    // outward immutable LiveData for _isListening.
    val isListening: LiveData<Boolean>
        get() = _isListening

    // holds the command extracted by the STT system
    private val _command: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    // outward immutable LiveData for _command.
    val command: LiveData<ArrayList<String>>
        get() = _command

    /**
     * Retrieves the stored grocery list from local phone storage, if available, and loads it into
     * the GroceryList ArrayList. Alternatively, creates a new file to which to later store the
     * list.
     */
    fun fetchGroceryList() {

        val groceryListFileName = "GroceryList.json"

        groceryListFile = File(
            getApplication<Application>().applicationContext.filesDir,
            groceryListFileName
        )

        // create new groceryListFile is none exists
        if (!groceryListFile.exists()) {
            groceryListFile.createNewFile()
        }

        // loads file contents into the GroceryList ArrayList, if file is not empty
        if (groceryListFile.length() > 0) {
            val bufferedReader: BufferedReader = groceryListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            groceryList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<GroceryItem?>?>() {}.type
            ) as ArrayList<GroceryItem>
        }

    }

    /**
     * Adds a new GroceryItem to the groceryList and saves the updated list to local storage.
     *
     * @param itemName: String, the name of the GroceryItem to be added.
     */
    fun addGroceryItem(itemName: String) {
        groceryList.add(GroceryItem(itemName, groceryList.size, false))
        storeGroceryList()
    }

    /**
     * Deletes a GroceryItem from the grocerList and saves the updated list to local storage.
     *
     * @param groceryItem: GroceryItem to be deleted
     */
    fun deleteGroceryItem(groceryItem: GroceryItem) {
        groceryList.remove(groceryItem)
        storeGroceryList()
    }

    /**
     * Places an item into or takes an item out of the shopping cart, depending on its previous
     * state and saves the updated list to local storage. Items in the cart are removed from it,
     * items not in the cart are placed in it.
     *
     * @param groceryItem: GroceryItem to be place into/taken out of cart
     *
     * @return true, if placed in cart, false, if taken out of cart
     */
    fun placeGroceryItemInCart(groceryItem: GroceryItem): Boolean {
        groceryItem.inCart = !groceryItem.inCart
        storeGroceryList()
        return groceryItem.inCart
    }

    /**
     * Saves the current groceryList to local storage as a JSON file.
     */
    fun storeGroceryList() {
        val jsonString: String = gson.toJson(groceryList)
        groceryListFile.writeText(jsonString)
    }

    /**
     * Initializes TTS variable. Must be done here (thus making tts late-init), because context can
     * only be accessed locally for a function.
     */
    fun initTTS() {
        tts = TextToSpeechUtility(getApplication<Application>().applicationContext, this)
    }

    /**
     * Calls the TTS system to read a given string out loud.
     *
     * @param text: String, the string to be read out loud.
     * @param queueMode: enum, defining how multiple speech outputs are queued.
     *                   TextToSpeech.QUEUE_FLUSH (default) overwrites the queue with the current
     *                   text, thus immediately reading it out loud
     *                   TextToSpeech.QUEUE_ADD appends the current text to the queue, only reading
     *                   it once all prior texts have been read out loud
     *
     * TODO: error handling
     */
    fun say(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (ttsInitialized.value == true) {
            tts.say(text, queueMode)
        }
    }

    /**
     * Overrides the TextToSpeech.OnInitListener's onInit function. Called, once the TTS engine
     * initialization is completed. If initialization was successful, the TTS engine's locale is
     * set to the user's phone locale and the speed rate is set to default. Finally, the
     * MutableLiveData _ttsInitialized is set to true. If initialization was unsuccessful, an error
     * message is printed to the console output.
     *
     * @param status: Int representing the success status
     *
     * TODO: improve error handling
     */
    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.setLocale(Locale.getDefault())
            tts.setSpeedRate()
            _ttsInitialized.value = true
        } else {
            Log.println(Log.ERROR, "tts onInit", "Couldn't initialize TTS Engine")
        }

    }

    /**
     * Initializes the STT system, by creating the SpeechRecognizer and passing it the functionality
     * to handle STT calls. Once results are returned by the STT recognizer, listening is cancelled,
     * matches are retrieved and their text output can then be handled by this ViewModel.
     * Listening is also cancelled once the speech ends or when an error occurs.
     *
     * TODO: improve error handling
     */
    fun initSTT() {
        stt.createSpeechRecognizer(getApplication<Application>().applicationContext,
            onResults = {
                cancelListening()
                val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // results are added in decreasing order of confidence to the list,
                    // so choose the first one
                    handleSpeechResult(matches[0])
                }
            }, onEndOfSpeech = {
                cancelListening()
            }, onError = {
                cancelListening()
            })
    }

    /**
     * Handles the processing of the results returned by the STT system.
     *
     * @param s: String, output of the STT system onResults
     *
     * TODO: streamline processing and command structure
     */
    private fun handleSpeechResult(s: String) {
        // TODO: implement
    }

    /**
     * Called when STT begins listening to voice input. Sets _isListening to true and calls on the
     * STT system for further handling.
     *
     * TODO: error handling (What if stt.handleSpeechBegin() is unsuccessful?)
     */
    fun handleSpeechBegin() {
        stt.handleSpeechBegin()
        _isListening.value = true
    }

    /**
     * Called when STT should stop listening. Calls on the STT system for further handling and sets
     * _isListening to false
     *
     * TODO: error handling (What if stt.cancelListening() is unsuccessful?)
     */
    fun cancelListening() {
        stt.cancelListening()
        _isListening.value = false
    }
}
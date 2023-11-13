package com.nlinterface.viewmodels

import android.app.Application
import android.content.Context
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.BuildConfig
import com.nlinterface.R
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.utility.SpeechToTextUtility
import com.nlinterface.utility.TextToSpeechUtility
import kotlinx.coroutines.CompletionHandler
import java.io.BufferedReader
import java.io.File
import java.util.Locale

class PlaceDetailsViewModel(
    application: Application
) : AndroidViewModel(application), OnInitListener {

    private val context = application
    private lateinit var placesClient: PlacesClient

    private val placeDetailsItemListFileName = "PlaceDetailsItemList.json"
    private val placeDetailsItemListFile: File = File(context.filesDir, placeDetailsItemListFileName)
    var placeDetailsItemList: ArrayList<PlaceDetailsItem> = ArrayList<PlaceDetailsItem> ()
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

    fun fetchPlaceDetailsItemList() {

        if (!placeDetailsItemListFile.exists()) {
            placeDetailsItemListFile.createNewFile()
        }

        if (placeDetailsItemListFile.length() > 0) {
            val bufferedReader: BufferedReader = placeDetailsItemListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            placeDetailsItemList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<PlaceDetailsItem?>?>() {}.type
            ) as ArrayList<PlaceDetailsItem>
        }

        Log.println(Log.DEBUG, "1", placeDetailsItemList.toString())
    }

    private fun addPlaceDetailsItem(placeID: String, storeName: String, openingHours: List<String>): ArrayList<PlaceDetailsItem> {
        placeDetailsItemList.add(PlaceDetailsItem(placeID, storeName, openingHours, false))
        storePlaceDetailsItemList()
        return placeDetailsItemList
    }

    fun initPlaceClient(context: Context) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(context)
    }

    fun onPlaceSelected(place: Place, completion: (success: Boolean) -> Unit) {
        val placeID = place.id
        fetchPlaceDetails(placeID) {
            completion(it)
        }
    }

    private fun fetchPlaceDetails(placeID: String?, completion: (success: Boolean) -> Unit) {

        // Specify the fields to return.
        val placeFields = listOf(
            Place.Field.NAME, Place.Field.OPENING_HOURS, Place.Field.ADDRESS
        )

        // Construct a request object, passing the place ID and fields array.
        val request = placeID?.let { FetchPlaceRequest.newInstance(it, placeFields) }

        if (request != null) {
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    addPlaceDetailsItem(placeID, response.place.name, response.place.openingHours.weekdayText)
                    completion(true)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Log.println(Log.DEBUG, "MAPSAPI", "failed")
                    }
                    completion(false)
                }
        }
    }

    fun onError(status: Status) {
        // TODO
    }

    fun storePlaceDetailsItemList() {
        val jsonString : String = gson.toJson(placeDetailsItemList)
        placeDetailsItemListFile.writeText(jsonString)
    }

    fun changeFavorite(item: PlaceDetailsItem): Boolean {
        item.favorite = !item.favorite
        storePlaceDetailsItemList()

        return item.favorite
    }

    fun deletePlaceDetailsItem(placeDetailsItem: PlaceDetailsItem): ArrayList<PlaceDetailsItem> {
        placeDetailsItemList.remove(placeDetailsItem)
        storePlaceDetailsItemList()

        return placeDetailsItemList
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
                } else if (string.contains(Regex("grocery list"))) {
                    cmd.add("GL")
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
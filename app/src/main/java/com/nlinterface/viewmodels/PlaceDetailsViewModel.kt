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
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.BuildConfig
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SpeechToTextUtility
import com.nlinterface.utility.TextToSpeechUtility
import com.nlinterface.utility.VoiceCommandUtilityOld
import java.io.BufferedReader
import java.io.File
import java.util.Locale

/**
 * The PlaceDetailsViewModel interfaces the user interactions with the view (the activity) and the
 * model. It handles
 * 1- the fetching and storing of data
 * 2- adding/deleting of PlaceDetailItems to the saved places list
 * 3- interaction with the PlaceClient (initialization, fetching Place Details and selecting places)
 * 4- TTS and STT systems.
 */
class PlaceDetailsViewModel(
    application: Application
) : AndroidViewModel(application), OnInitListener {

    private lateinit var placesClient: PlacesClient

    private lateinit var placeDetailsItemListFile: File

    var placeDetailsItemList: ArrayList<PlaceDetailsItem> = ArrayList()

    private var gson = Gson()

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
     * Retrieves the stored place details list from local phone storage, if available, and loads it
     * into the PlaceDetailsItem ArrayList. Alternatively, creates a new file to which to later
     * store the list.
     */
    fun fetchPlaceDetailsItemList() {

        val placeDetailsItemListFileName = "PlaceDetailsItemList.json"

        placeDetailsItemListFile = File(
            getApplication<Application>().applicationContext.filesDir,
            placeDetailsItemListFileName
        )

        // create new placeDetailsItemListFile is none exists
        if (!placeDetailsItemListFile.exists()) {
            placeDetailsItemListFile.createNewFile()
        }

        // loads file contents into the DetailsItem ArrayList, if file is not empty
        if (placeDetailsItemListFile.length() > 0) {
            val bufferedReader: BufferedReader = placeDetailsItemListFile.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            placeDetailsItemList = gson.fromJson(
                inputString,
                object : TypeToken<ArrayList<PlaceDetailsItem?>?>() {}.type
            ) as ArrayList<PlaceDetailsItem>
        }

    }

    /**
     * Adds a new PlaceDetailsItem to the placeDetailsItemList and saves the updated list to local
     * storage.
     *
     * @param placeID: String, the ID of the PlaceDetailsItem to be added.
     * @param storeName: String, the name of the PlaceDetailsItem to be added.
     * @param openingHours: List<String>, the openingHours of the PlaceDetailsItem to be added.
     */
    private fun addPlaceDetailsItem(
        placeID: String,
        storeName: String,
        openingHours: List<String>
    ) {
        placeDetailsItemList.add(PlaceDetailsItem(placeID, storeName, openingHours, false))
        storePlaceDetailsItemList()
    }

    /**
     * Initializes the PlaceClient required to retrieve details for a Place.
     *
     * @param context: Context
     */
    fun initPlaceClient(context: Context) {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(context)
    }

    /**
     * Handles the retrieving of Place Details after a place is selected.
     *
     * @param place: Place, the selected place
     * @param completion: Completion handler to notify on success
     */
    fun onPlaceSelected(place: Place, completion: (success: Boolean) -> Unit) {
        val placeID = place.id
        fetchPlaceDetails(placeID) {
            completion(it)
        }
    }

    /**
     * Retrieves the PlaceDetails (name, opening hours and address) for a Google PlaceID and adds a
     * newly constructed PlaceDetailsItem to the list.
     *
     * @param placeID: String?, the ID of the requested Place
     * @param completion: Completion handler to notify on success
     *
     * TODO: error handling (what if no name/opening hours/etc. exist for that ID?)
     * TODO: error handling on failure
     */
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
                    addPlaceDetailsItem(
                        placeID,
                        response.place.name,
                        response.place.openingHours.weekdayText
                    )
                    completion(true)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Log.println(Log.DEBUG, "MAPSAPI", "failed")
                    }
                    completion(false)
                }
        }
    }

    /**
     * Implements error handling for the AutocompleteFragment.
     *
     * @param status: Status, returned error status
     *
     * TODO: error handling
     */
    fun onError(status: Status) {
        // TODO: implement
    }

    /**
     * Saves the current placeDetailsItemList to local storage as a JSON file.
     */
    fun storePlaceDetailsItemList() {
        val jsonString: String = gson.toJson(placeDetailsItemList)
        placeDetailsItemListFile.writeText(jsonString)
    }

    /**
     * Update favorite value, depending on current value.
     *
     * @param item: PlaceDetailsItem, Item to be un-/favorite
     * @return new favorite value for the item
     */
    fun changeFavorite(item: PlaceDetailsItem): Boolean {
        item.favorite = !item.favorite
        storePlaceDetailsItemList()

        return item.favorite
    }

    /**
     * Deletes a PlaceDetailsItem from the placeDetailsItemList and saves the updated list to local
     * storage.
     *
     * @param placeDetailsItem: PlaceDetailsItem to be deleted
     */
    fun deletePlaceDetailsItem(placeDetailsItem: PlaceDetailsItem): ArrayList<PlaceDetailsItem> {
        placeDetailsItemList.remove(placeDetailsItem)
        storePlaceDetailsItemList()

        return placeDetailsItemList
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
        stt.createSpeechRecognizer(getApplication<Application>().applicationContext)
        setSpeechRecognitionListener(STTInputType.COMMAND)
    }
    
    fun setSpeechRecognitionListener(responseType: STTInputType = STTInputType.COMMAND) {
        stt.setSpeechRecognitionListener(
            onResults = {
                cancelListening()
                val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // results are added in decreasing order of confidence to the list,
                    // so choose the first one
                    handleSpeechResult(matches[0], responseType)
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
    private fun handleSpeechResult(s: String, responseType: STTInputType) {

        Log.println(Log.DEBUG, "handleSpeechResult", s)
        val voiceCommandUtilityOld = VoiceCommandUtilityOld()
        _command.value = voiceCommandUtilityOld.decodeVoiceCommand(s)

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
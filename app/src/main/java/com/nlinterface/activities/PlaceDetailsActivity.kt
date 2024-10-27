package com.nlinterface.activities

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.R
import com.nlinterface.adapters.PlaceDetailsAdapter
import com.nlinterface.adapters.PlaceDetailsFragmentAdapter
import com.nlinterface.databinding.ActivityPlaceDetailsBinding
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.fragments.PlaceDetailsListOverview
import com.nlinterface.fragments.PlaceDetailsScreen1
import com.nlinterface.fragments.PlaceDetailsScreenBase
import com.nlinterface.interfaces.PlaceDetailsItemCallback
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.OnSwipeTouchInterceptor
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.navToActivity
import com.nlinterface.viewmodels.PlaceDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar


/**
 * The PlaceDetailsActivity handles user interaction for the PlaceDetailsList.
 *
 * The PlaceDetailsActivity comprises a RecyclerView displaying a list of stored places with their
 * current opening hours and a b filled in star/outlined star icon visualizing if the place is a
 * favorite. It also features a VoiceActivationButton and a search fragment through which to search
 * for places that can then be added to the list. The user can physically interact with the activity
 * by searching for, selecting and adding places via the search fragment, removing places by swiping
 * right or left and by adding/removing places from their favorites via clicking the star icon.
 * Aside from searching for places, each action can also be accomplished via voice command and every
 * action performed is narrated.
 *
 * Possible Voice Commands:
 * - 'List all stored places'
 * - 'List my favorite places'
 * - 'List all open places' (begin with favorites)
 * - 'Remove a place' --> Which place? --> "Place X"
 * - 'State the opening hours' --> Of which place? --> "Place X"
 * - 'Add a place to favorites' --> Which place? --> "Place X"
 * - 'Remove a place from favorites' --> Which place? --> "Place X"
 *
 * TODO: Write own search fragment to allow for voice control
 * TODO: Add addresses
 * TODO: Add Google Maps navigation functionality
 * TODO: Sort by recency
 * TODO?: Allow for requesting opening times on other days
 */
class PlaceDetailsActivity : AppCompatActivity(), PlaceDetailsItemCallback {
    
    private lateinit var binding: ActivityPlaceDetailsBinding
    lateinit var viewModel: PlaceDetailsViewModel
    
    private lateinit var placeDetailsItemList: ArrayList<PlaceDetailsItem>
    private lateinit var placeDetailsAdapter: PlaceDetailsAdapter
    
    private lateinit var lastCommand: String
    private lateinit var lastResponse: String

    private lateinit var placeDetailsViewPager: ViewPager2
    lateinit var placeDetailsFragmentAdapter: PlaceDetailsFragmentAdapter

    val operationCompletedStatus = MutableLiveData<Boolean>()
    val operationCompleted: LiveData<Boolean> get() = operationCompletedStatus

    private lateinit var onResumeCheck:String

    /**
     * The onCreate function initializes the view by binding the Activity and the Layout and
     * retrieving the ViewModel. After calling the viewModel to load the place details list data and
     * to initialize the PlaceClient, the UI elements and TTS and STT systems are configured.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onResumeCheck = "true"

        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]
        viewModel.initPlaceClient(this)

        viewModel.fetchPlaceDetailsItemList()
        placeDetailsItemList = viewModel.placeDetailsItemList
        placeDetailsAdapter = PlaceDetailsAdapter(placeDetailsItemList, this)


        configureAutocompleteFragment()
        viewPagerSetUp()
        configureTTS()
        configureSTT()
    }


    /**
     * The Fragments in the Place Details activity are created dynamically, on Pause their state
     * is saved and on Resume they are restored. The check is needed to assure the onResume
     * functionality is only triggered, when it comes from a stoped state.
     */
    override fun onStop() {
        super.onStop()
        saveFragmentsState()  // Save the fragment state when the activity is paused
        onResumeCheck = "true"
    }

    override fun onResume() {
        super.onResume()
        if (onResumeCheck == "true") {
            restoreFragmentsState()  // Restore the fragment state when the activity is resumed
        }
        onResumeCheck = "false"
    }


    /**
     * Called by the onCreate Function and calls upon the ViewModel to initialize the TTS system. On
     * successful initialization, the Activity name is read aloud.
     */
    private fun configureTTS() {
        
        viewModel.initTTS()
        
        // once the TTS is successfully initialized, read out the activity name
        // required, since TTS initialization is asynchronous
        val ttsInitializedObserver = Observer<Boolean> { _ ->
            viewModel.say(resources.getString(R.string.place_details))
        }
        
        // observe LiveDate change, to be notified if TTS initialization is completed
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)
        
    }
    
    /**
     * Called by the onCreate function and calls upon the ViewModel to initialize the STT system.
     * The voiceActivationButton is configured to change it microphone color to green, if the STT
     * system is active and to change back to white, if it is not. Also retrieves the text output
     * of the voice input to the STT system, aka the 'command'
     */
    private fun configureSTT() {
        
        viewModel.initSTT()
        
        // if listening: microphone color green, else microphone color white
        val sttIsListeningObserver = Observer<Boolean> {
        }
        
        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)
        
        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<String> { command ->
            lastCommand = command
            executeCommand(command)
        }
        
        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)
    
        // if a response is successfully generated, process and execute it
        val responseObserver = Observer<String> { response ->
            lastResponse = response
            executeItemCommand(lastCommand, lastResponse)
        }
    
        // observe LiveData change to be notified when the STT returns a response
        viewModel.response.observe(this, responseObserver)
        
    }
    
    /**
     * Called once the STT system returns a command. It is then processed and, if valid,
     * finally executed by navigating to the next activity
     *
     * @param cmd: ArrayList<String>? containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun executeCommand(cmd: String) {
        
        val command = cmd.replace("favourites", "favorites")
        
        if (command.contains("go to")) {
            executeNavigationCommand(command)
            
        } else if (command.contains("list")) {
            
            executeListCommand(command)
            
        } else if (
            command.contains(resources.getString(R.string.remove_indicator)) ||
            command.contains(resources.getString(R.string.add_indicator)) ||
            command == resources.getString(R.string.tell_me_the_opening_hours_of_a_place)
        ) {
    
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                requestResponse(resources.getString(R.string.which_place))
            }
            
        } else if ((command == resources.getString(R.string.tell_me_my_options))) {
            
            viewModel.say(
                "${resources.getString(R.string.your_options_are)} " +
                        "${resources.getString(R.string.remove_a_place)}" +
                        "${resources.getString(R.string.add_a_place)}" +
                        "${resources.getString(R.string.add_a_place_to_favorites)}" +
                        "${resources.getString(R.string.remove_a_place_from_favorites)}" +
                        "${resources.getString(R.string.tell_me_the_opening_hours_of_a_place)}" +
                        "${resources.getString(R.string.list_all_saved_places)}" +
                        "${resources.getString(R.string.list_all_open_places)}" +
                        "${resources.getString(R.string.list_my_favorite_places)}" +
                        "${resources.getString(R.string.navigate_to_grocery_list)}," +
                        "${resources.getString(R.string.navigate_to_place_details)} and" +
                        "${resources.getString(R.string.navigate_to_settings)}." +
                        "${resources.getString(R.string.navigate_to_barcode_scanner_settings)}."
            )
            
        } else {
            viewModel.say(resources.getString(R.string.invalid_command))
        }
        
    }
    
    /**
     * Requests a response from the user by reading out the question and activating voice control
     * once the question has been read fully.
     *
     * @param question: String, the question to ask the user
     */
    private suspend fun requestResponse(question: String) {
        viewModel.sayAndAwait(question)
        viewModel.setSpeechRecognitionListener(STTInputType.ANSWER)
        viewModel.handleSpeechBegin()
    }
    
    /**
     * Called when the user has given a response to an item question.Depending on the previous
     * command and the given response, the action is executed.
     *
     * @param command: String, the command preceding the response
     * @param response: String, the response given by the user
     */
    private fun executeItemCommand(command: String, response: String) {
        
        Log.println(Log.DEBUG, "", command)
        
        if (response != resources.getString(R.string.cancel)) {
            
            when (command) {
    
                resources.getString(R.string.remove_a_place) -> {
                    deletePlaceDetailsItem(response)
                }
    
                resources.getString(R.string.tell_me_the_opening_hours_of_a_place) -> {
                    stateOpeningHours(response)
                }
    
                resources.getString(R.string.add_a_place_to_favorites) -> {
                    addToFavorites(response)
                }
    
                resources.getString(R.string.remove_a_place_from_favorites) -> {
                    removeFromFavorites(response)
                }
                
            }
            
        }
    
    }
    
    /**
     * Requests the ViewModel to add a new Place Details Item to the favorites by name. If no
     * Place Details Item exists for the given item name, this is stated. If the item is already a
     * favorite, this, too is stated.
     *
     * @param storeName: String, the name of the item to be added to favorites
     */
    private fun addToFavorites(storeName: String) {
        
        val placeDetailsItem = findPlaceByName(storeName)
    
        if (placeDetailsItem != null) {
            if (placeDetailsItem.favorite) {
                viewModel.say(resources.getString(R.string.STORENAME_is_a_favorite, storeName))
            } else {
                viewModel.changeFavorite(placeDetailsItem)
                placeDetailsAdapter.notifyItemChanged(placeDetailsItemList.indexOf(placeDetailsItem))
                viewModel.say(resources.getString(R.string.added_STORENAME_to_favorites, storeName))
            }
        } else {
            viewModel.say(
                resources.getString(R.string.STORENAME_is_not_on_the_list, storeName)
            )
        }
        
    }
    
    /**
     * Requests the ViewModel to remove a Place Details Item from the favorites by name. If no
     * Place Details Item exists for the given item name, this is stated. If the item is not a
     * favorite, this, too is stated.
     *
     * @param storeName: String, the name of the item to be removed from favorites
     */
    private fun removeFromFavorites(storeName: String) {
        
        val placeDetailsItem = findPlaceByName(storeName)
    
        if (placeDetailsItem != null) {
            if (!placeDetailsItem.favorite) {
                viewModel.say(resources.getString(R.string.STORENAME_is_not_a_favorite, storeName))
            } else {
                viewModel.changeFavorite(placeDetailsItem)
                placeDetailsAdapter.notifyItemChanged(placeDetailsItemList.indexOf(placeDetailsItem))
                viewModel.say(
                    resources.getString(R.string.deleted_STORENAME_from_favorites, storeName)
                )
            }
        } else {
            viewModel.say(
                resources.getString(R.string.STORENAME_is_not_on_the_list, storeName)
            )
        }
        
    }
    
    /**
     * Handles voice commands for which a list of place detail items must be read out loud.
     *
     * @param command: String, the command given by the user
     */
    private fun executeListCommand(command: String) {
        
        Log.println(Log.DEBUG, "exec", command)
        var exists = false
        
        when (command) {
    
            resources.getString(R.string.list_all_saved_places) -> {
                for (place in placeDetailsItemList) {
                    viewModel.say(place.storeName, TextToSpeech.QUEUE_ADD)
                }
    
                if (placeDetailsItemList.isEmpty()) {
                    viewModel.say(resources.getString(R.string.there_are_no_saved_places))
                }
            }
    
            resources.getString(R.string.list_my_favorite_places) -> {
                for ((_, storeName, _, _, favorite) in placeDetailsItemList) {
                    if (favorite) {
                        viewModel.say(storeName, TextToSpeech.QUEUE_ADD)
                        exists = true
                    }
                }
    
                if (placeDetailsItemList.isEmpty()) {
                    viewModel.say(resources.getString(R.string.there_are_no_saved_places))
                } else if (!exists) {
                    viewModel.say(resources.getString(R.string.there_are_no_favorites))
                }
                
            }
    
            resources.getString(R.string.list_all_open_places) -> {
                for ((_, storeName, openingHours, _) in placeDetailsItemList) {
                    if (isOpen(openingHours)) {
                        viewModel.say(storeName, TextToSpeech.QUEUE_ADD)
                        exists = true
                    }
                }
    
                if (placeDetailsItemList.isEmpty()) {
                    viewModel.say(resources.getString(R.string.there_are_no_saved_places))
                } else if (!exists) {
                    viewModel.say(resources.getString(R.string.there_are_no_open_places))
                }
                
            }
            
            else -> viewModel.say(resources.getString(R.string.invalid_command))
    
        }
        
    }
    
    /**
     * Reads today's Opening Hours for a given store name out loud. If the store name does not
     * correspond to a place details item on the list, state this.
     */
    fun stateOpeningHours(storeName: String) {
    
        val placeDetailsItem = findPlaceByName(storeName)
    
        if (placeDetailsItem != null) {
            val dayOfWeek = Math.floorMod(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2, 7)
            
            val openingHours = placeDetailsItem.openingHours
            val regexCurrentOpeningHours = "(?<=: \\[?)(\\w:?–? ?ö?)+( ?–?\\d?:?)+".toRegex()
            val currentOpeningHours = regexCurrentOpeningHours.find(openingHours[dayOfWeek])?.value
            
            viewModel.say(currentOpeningHours.toString(),TextToSpeech.QUEUE_ADD)
        } else {
            viewModel.say(
                resources.getString(R.string.STORENAME_is_not_on_the_list, storeName)
            )
        }
        
    }

    /**
     * Reads the address for a given store name out loud. If the store name does not
     * correspond to a place details item on the list, state this.
     */

    private fun stateAddress(storeName: String) {

        val placeDetailsItem = findPlaceByName(storeName)

        if (placeDetailsItem != null) {
            viewModel.say(placeDetailsItem.address,TextToSpeech.QUEUE_ADD)
        } else {
            viewModel.say(
                resources.getString(R.string.STORENAME_is_not_on_the_list, storeName)
            )
        }

    }

    /**
     * Checks whether a store is currently open, based on its opening Hours and the current time.
     *
     * @param openingHours: List<String> the opening hours of a store
     */
    private fun isOpen(openingHours: List<String>): Boolean {
        
        val dayOfWeek = Math.floorMod(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2, 7)
    
        val regexCurrentOpeningHours = "(?<=: \\[?)(\\w:?–? ?ö?)+( ?–?\\d?:?)+".toRegex()
        val currentOpeningHours = regexCurrentOpeningHours.find(openingHours[dayOfWeek])?.value
    
        val regexOpenClosedHours = "(\\d+:\\d+)".toRegex()
        val openClosedHoursSeq = currentOpeningHours?.let { regexOpenClosedHours.findAll(it) }
        val openClosedHoursString = openClosedHoursSeq?.map {
            it.groupValues[1]
        }?.joinToString("-")
    
        if (!openClosedHoursString.isNullOrEmpty()) {
    
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val min = Calendar.getInstance().get(Calendar.MINUTE)
    
            val openTime = openClosedHoursString.substringBefore("-")
                .replace(":","").replace(" ","").toInt()
            val closeTime = openClosedHoursString.substringAfter("-")
                .replace(":","").replace(" ","").toInt()
    
            //val currentTime = (hour.plus(min)).toInt()
            val currentTime = hour * 100 + min
            val maxTime = 2360
            val minTime = 0

            return (closeTime < openTime && (
                    currentTime in openTime until maxTime
                            || currentTime in minTime until closeTime
                    )) || (currentTime in openTime until closeTime)
        }
        
        return false
    }
    
    /**
     * Handles Navigation commands of the format "go to X". If the command is valid, navigate to
     * the desired activity.
     *
     * @param command: String, the command to be executed
     */
    private fun executeNavigationCommand(command: String) {
    
        when (command) {
            resources.getString(R.string.navigate_to_grocery_list) ->
                navToActivity(this, ActivityType.GROCERYLIST)
        
            resources.getString(R.string.navigate_to_place_details) ->
                navToActivity(this, ActivityType.PLACEDETAILS)
        
            resources.getString(R.string.navigate_to_settings) ->
                navToActivity(this, ActivityType.SETTINGS)
        
            resources.getString(R.string.navigate_to_main_menu) ->
                navToActivity(this, ActivityType.MAIN)

            resources.getString(R.string.navigate_to_barcode_scanner_settings) ->
                navToActivity(this, ActivityType.BARCODESETTINGS)
        
            else -> viewModel.say(resources.getString(R.string.invalid_command))
        }
        
    }

    /**
     * Calls the ViewModel to delete a PlaceDetailsItem from the places list and then removes it
     * from the UI and narrates the performed action to the user.
     *
     * @param placeDetailsItem: PlaceDetailsItem to be deleted
     * @param index: the index of the PlaceDetailsItem to be deleted
     */
    private fun deletePlaceDetailsItem(placeDetailsItem: PlaceDetailsItem, index: Int) {

        viewModel.deletePlaceDetailsItem(placeDetailsItem)
        placeDetailsAdapter.notifyItemRemoved(index)
        viewModel.say(
            resources.getString(
                R.string.deleted_ITEMNAME_from_saved_places, placeDetailsItem.storeName
            )
        )

    }
    
    /**
     * Checks if the desired Place Details Item exist in the list and deletes it. If it does not
     * exist, states this.
     *
     * @param itemName: name  to be deleted
     */
    fun deletePlaceDetailsItem(itemName: String) {
    
        val placeDetailsItem = findPlaceByName(itemName)
    
        if (placeDetailsItem != null) {
            deletePlaceDetailsItem(placeDetailsItem, placeDetailsItemList.indexOf(placeDetailsItem))
        } else {
            viewModel.say(
                resources.getString(R.string.STORENAME_is_not_on_the_list, itemName)
            )
        }
        
    }
    
    /**
     * Searches the list for a Place Detail Item by its name/id.
     *
     * @param storeName: String, the name of the place to be deleted
     *
     * @return The Place Details Item if it exists, else null
     */
    fun findPlaceByName(storeName: String): PlaceDetailsItem? {
        return placeDetailsItemList.find { it.storeName == storeName }
    }

    private fun findPlaceByID(placeID: String): PlaceDetailsItem? {
        return placeDetailsItemList.find {it.placeID == placeID}
    }

    /**
     * Three functions handling the google maps place search.
     */
    /**
     * Just launches the intent. It is  wrapped in function to be called from the different fragments.
     *
     * TODO: Make country flexible
     */
    fun startPlaceSearch(){
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        val searchInput = autocompleteFragment.view?.findViewById<View>(
            resources.getIdentifier("places_autocomplete_search_input", "id", packageName)
        )
        searchInput?.requestFocus()
        searchInput?.performClick()
    }

    /**
     * Once a place is selected it calls on the viewmodel to proceed with the onPlaceSelected.
     * Additionally it notifies the placeDetailsAdapter and gives feedback to the user.
     */

    private fun handlePlaceSelected(place: Place) {
        if(findPlaceByID(place.id!!) == null) {
            viewModel.onPlaceSelected(place) {
                if (it) {
                    placeDetailsAdapter.notifyItemInserted(placeDetailsItemList.size - 1)
                    operationCompletedStatus.value = true
                    val storeName = placeDetailsItemList.last().storeName
                    val address = placeDetailsItemList.last().address
                    viewModel.say(
                        resources.getString(
                            R.string.STORENAME_added_to_saved_places,
                            storeName
                        ), TextToSpeech.QUEUE_ADD
                    )
                    viewModel.say(
                        resources.getString(
                            R.string.STORENAME_address,
                            address
                        ), TextToSpeech.QUEUE_ADD
                    )
                }
            }
        }
        else{
            viewModel.say(resources.getString(R.string.place_on_list))
        }
    }

    /**
     * Sets up the AutocompleteFragment for the places search and sets a PlaceSelectionListener. The
     * search filters for supermarkets and returns the ID of a place. If a place is selected, it is
     * added to the saved places and the action is narrated.
     */
    private fun configureAutocompleteFragment() {

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.view?.visibility = View.GONE
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID))
        autocompleteFragment.setTypesFilter(listOf("supermarket"))
        autocompleteFragment.setCountries(listOf("DE"))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onError(status: Status) {
                viewModel.onError(status)
            }

            /**
             * When a place is selected, the ViewModel is called to handle fetching the place
             * details and adding a new PlaceDetailsItem to the list. On Success, the UI is updated
             * accordingly and the action is narrated.
             *
             * @param place: Place, the selected place
             */
            override fun onPlaceSelected(place: Place) {
                handlePlaceSelected(place)
            }
        })

    }


    /**
     * Called at the end of the activity lifecycle and saves the current PlaceDetailsItem list to
     * local storage.
     */
    override fun onDestroy() {
        super.onDestroy()
        viewModel.storePlaceDetailsItemList()
    }

    /**
     * Reads the respective store name out loud, if a card is clicked.
     *
     * @param item: PlaceDetailsItem, the item whose name is to be read out loud
     */
    override fun onCardClick(item: PlaceDetailsItem) {

        viewModel.say(item.storeName)
        stateOpeningHours(item.storeName)
        stateAddress(item.storeName)
    }

    /**
     * When the favorite icon of a PlaceDetailsItem is clicked, it is added to or removed from the
     * favorites list, depending on the current state. If favorited, it is removed, else it is
     * added.
     */
    override fun onFavoriteClick(item: PlaceDetailsItem) {

        val favorite = viewModel.changeFavorite(item)
        placeDetailsAdapter.notifyItemChanged(placeDetailsItemList.indexOf(item))

        if (favorite) {
            viewModel.say(
                resources.getString(
                    R.string.added_STORENAME_to_favorites,
                    item.storeName
                )
            )
        } else {
            viewModel.say(
                resources.getString(
                    R.string.deleted_STORENAME_from_favorites,
                    item.storeName
                )
            )
        }
    }

    /**
     * Necessary functions for dynamic fragments.
     */

    /**
     * Creates a new PlaceDetailsScreenBase fragment and adds it to the fragment placeDetailsAdapter.
     * Adjusts the viewPagers offScreenPageLimit to trigger the initialization of the new fragment,
     * so its variables can be accessed and used.
     */
    fun addNewFragment(placeTop: String, placeBottom: String) {
        placeDetailsViewPager.offscreenPageLimit = placeDetailsFragmentAdapter.itemCount -1
        val newFragment = PlaceDetailsScreenBase.newInstance(placeTop, placeBottom)
        placeDetailsFragmentAdapter.addFragment(newFragment)
    }

    /**
     * Saves the current state of the fragments. This is necessary to keep the newly created
     * fragments. The fragments are saved with their itemTop and itemBottom variable mapped onto
     * each-other. They are put into a json an d saved in the sharedPreferences.
     */
    private fun saveFragmentsState() {
        val sharedPreferences = getSharedPreferences(
            "place_details_fragment_state", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val fragmentData = placeDetailsFragmentAdapter.fragmentList
            .filterIsInstance<PlaceDetailsScreenBase>()
            .map { fragment ->
                fragment.placeTop to fragment.placeBottom
            }
        val gson = Gson()
        val json = gson.toJson(fragmentData)
        editor.putString("place_details_fragments", json)
        editor.apply()
        Log.i("Shared Preferences","FragmentState saved")
    }

    /**
     * Restores the fragments with their variables saved in the sharedPreferences. If there are
     * no saved Fragments a new one is created to makes sure, their is always at least one.
     */
    private fun restoreFragmentsState() {
        val sharedPreferences = getSharedPreferences(
            "place_details_fragment_state", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("place_details_fragments", null)

        if (json != null) {
            val fragmentData: List<Pair<String, String>> = gson.fromJson(json, object :
                TypeToken<List<Pair<String, String>>>() {}.type)
            fragmentData.forEach { (placeTop, placeBottom) ->
                val fragment = PlaceDetailsScreenBase.newInstance(placeTop, placeBottom)
                placeDetailsFragmentAdapter.addFragment(fragment)
            }
        }
        else{
            addNewFragment(
                resources.getString(R.string.add_a_place),
                resources.getString(R.string.add_a_place)
            )
        }
        Log.i("Shared Preferences","FragmentState restored")
    }

    /**
     * Function that sets and configures the viewPager2. ViewPager2 is a tool that can take multiple
     * fragments in a list and allows navigation between those fragments by swiping left and right.
     * To achieve this the placeDetailsFragmentAdapter is set as the viewpagers placeDetailsAdapter.
     */

    private fun viewPagerSetUp(){
        placeDetailsViewPager = findViewById(R.id.place_details_view_pager)
        placeDetailsFragmentAdapter = PlaceDetailsFragmentAdapter(this)
        placeDetailsViewPager.adapter = placeDetailsFragmentAdapter
        placeDetailsViewPager.setCurrentItem(1, false)
        /**
         * The swipe interceptor makes sure that vertical swipes are recognized more reliably.
         * In a default state a swipe to the top or bottom with the slightest movement left
         * or right will be recognized as a horizontal swipe. With the interceptor the app checks
         * first for a vertical swipe, which makes it more reliable and better to navigate.
         *
         * Overriding the onSwipeUp and onSwipeDown functions in the way it is done, is necessary
         * to assure the correct fragments onSwipe functions are referenced, since the activity is
         * shared among all the activities Fragment.
         *
         */

        val swipeInterceptor = OnSwipeTouchInterceptor(object : SwipeAction {
            override fun onSwipeLeft(){}
            override fun onSwipeRight(){}
            override fun onSwipeUp(){
                val currentPosition = placeDetailsViewPager.currentItem
                val currentFragment = placeDetailsFragmentAdapter.getCurrentFragment(currentPosition)
                when (currentPosition) {
                    0 -> {
                        (currentFragment as PlaceDetailsListOverview).onSwipeUp()
                    }
                    1 -> {
                        (currentFragment as PlaceDetailsScreen1).onSwipeUp()
                    }
                    else -> {
                        (currentFragment as PlaceDetailsScreenBase).onSwipeUp()
                    }
                }
            }
            override fun onSwipeDown(){
                val currentPosition = placeDetailsViewPager.currentItem
                val currentFragment = placeDetailsFragmentAdapter.getCurrentFragment(currentPosition)
                when (currentPosition) {
                    0 -> {
                        (currentFragment as PlaceDetailsListOverview).onSwipeDown()
                    }
                    1 -> {
                        (currentFragment as PlaceDetailsScreen1).onSwipeDown()
                    }
                    else -> {
                        (currentFragment as PlaceDetailsScreenBase).onSwipeDown()
                    }
                }
            }
            override fun onLongPress(){}
            override fun onDoubleTap() {}

        })

        placeDetailsViewPager.getChildAt(0).let { recyclerView ->
            if (recyclerView is RecyclerView) {
                recyclerView.addOnItemTouchListener(swipeInterceptor)
            }
        }
    }
    
}
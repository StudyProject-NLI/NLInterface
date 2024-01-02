package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.nlinterface.R
import com.nlinterface.adapters.PlaceDetailsAdapter
import com.nlinterface.databinding.ActivityPlaceDetailsBinding
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.interfaces.PlaceDetailsItemCallback
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.PlaceDetailsViewModel

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
 * - 'Delete Place' --> Which place? --> "Place X"
 * - 'State the opening hours' --> Of which place? --> "Place X"
 * - 'Mark a place as favorite' --> Which place? --> "Place X"
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
    private lateinit var viewModel: PlaceDetailsViewModel

    private lateinit var placeDetailsItemList: ArrayList<PlaceDetailsItem>

    private lateinit var adapter: PlaceDetailsAdapter

    private lateinit var voiceActivationButton: ImageButton

    /**
     * The onCreate function initializes the view by binding the Activity and the Layout and
     * retrieving the ViewModel. After calling the viewModel to load the place details list data and
     * to initialize the PlaceClient, the UI elements and TTS and STT systems are configured.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]
        viewModel.initPlaceClient(this)

        viewModel.fetchPlaceDetailsItemList()
        placeDetailsItemList = viewModel.placeDetailsItemList

        configureUI()
        configureAutocompleteFragment()
        configureTTS()
        configureSTT()

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
        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_green)
            } else {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_white)
            }
        }

        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)

        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<ArrayList<String>> { command ->
            executeCommand(command)
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)

    }

    /**
     * Called once the STT system returns a command. It is then processed and, if valid,
     * finally executed by navigating to the next activity
     *
     * @param command: ArrayList<String>? containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun executeCommand(command: ArrayList<String>?) {

        /*
        if (command != null && command.size == 3) {
            if (command[0] == "GOTO") {
                navToActivity(command[1])
            } else {
                viewModel.say(resources.getString(R.string.choose_activity_to_navigate_to))
            }
        }
         */

    }

    /**
     * Handles navigation to next activity. Called either by button click or by execution of the
     * voice command. If the called for activity is the current one, read out the activity name.
     *
     * @param activity: ActivityType, Enum specifying the activity
     */
    private fun navToActivity(activity: String) {

        when (activity) {

            ActivityType.PLACEDETAILS.toString() -> {
                viewModel.say(resources.getString(R.string.place_details))
            }

            ActivityType.MAIN.toString() -> {
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.GROCERYLIST.toString() -> {
                val intent = Intent(this, GroceryListActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.SETTINGS.toString() -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }

        }
    }

    /**
     * Sets up all UI elements, i.e. the voiceActivation buttons, their respective
     * onClick functionality and configures the recycler view.
     */
    private fun configureUI() {

        // set up voice activation button listener
        voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
        voiceActivationButton.setOnClickListener {
            onVoiceActivationButtonClick()
        }

        // resize Voice Activation Button to 1/3 of display size
        setViewRelativeSize(voiceActivationButton, 1.0, 0.33)

        configureRecyclerView()
    }

    /**
     * Initializes the PlaceDetailsAdapter and fills the recyclerview with the PlaceDetailsItems on
     * the Place Details List and configures the swipe to delete functionality in both directions
     * utilizing ItemTouchHelper
     */
    private fun configureRecyclerView() {

        adapter = PlaceDetailsAdapter(placeDetailsItemList, this)

        val rvPlaceDetails = findViewById<View>(R.id.place_details_rv) as RecyclerView
        rvPlaceDetails.adapter = adapter
        rvPlaceDetails.layoutManager = LinearLayoutManager(this)
        rvPlaceDetails.itemAnimator?.changeDuration = 0

        // implements swipe left to delete item functionality
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onSwipeToDelete(viewHolder)
            }
        }).attachToRecyclerView(rvPlaceDetails)

        // implements swipe right to delete item functionality
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onSwipeToDelete(viewHolder)
            }
        }).attachToRecyclerView(rvPlaceDetails)

    }

    /**
     * Called when user swipes left or right on an item in the recyclerview. The corresponding
     * PlaceDetailsItem is then removed.
     *
     * @param viewHolder: the RecyclerView ViewHolder
     */
    private fun onSwipeToDelete(viewHolder: RecyclerView.ViewHolder) {
        val placeDetailsItem: PlaceDetailsItem =
            placeDetailsItemList[viewHolder.adapterPosition]

        deletePlaceDetailsItem(placeDetailsItem, viewHolder.adapterPosition)
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
        adapter.notifyItemRemoved(index)
        viewModel.say(
            resources.getString(
                R.string.deleted_ITEMNAME_from_saved_places, placeDetailsItem.storeName
            )
        )

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

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID))
        autocompleteFragment.setTypesFilter(listOf("supermarket"))

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
                viewModel.onPlaceSelected(place) {
                    if (it) {

                        adapter.notifyItemInserted(placeDetailsItemList.size - 1)

                        val storeName = placeDetailsItemList.last().storeName
                        viewModel.say(
                            resources.getString(
                                R.string.STORENAME_added_to_saved_places,
                                storeName
                            )
                        )
                    }
                }
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
    }

    /**
     * When the favorite icon of a PlaceDetailsItem is clicked, it is added to or removed from the
     * favorites list, depending on the current state. If favorited, it is removed, else it is
     * added.
     */
    override fun onFavoriteClick(item: PlaceDetailsItem) {

        val favorite = viewModel.changeFavorite(item)
        adapter.notifyItemChanged(placeDetailsItemList.indexOf(item))

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
     * Called when voiceActivationButton is clicked and handles the result. If clicked while the
     * STT system is listening, call to viewModel to cancel listening. Else, call viewModel to begin
     * listening.
     */
    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

}
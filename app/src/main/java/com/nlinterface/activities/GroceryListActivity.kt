package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nlinterface.R
import com.nlinterface.adapters.GroceryListAdapter
import com.nlinterface.databinding.ActivityGroceryListBinding
import com.nlinterface.dataclasses.GroceryItem
import com.nlinterface.interfaces.GroceryListCallback
import com.nlinterface.utility.ActivityType
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.navToActivity
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.GroceryListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * The GroceryListActivity handles user interaction for the GroceryList.
 *
 * The GroceryListActivity is the view of a GroceryList, which displays a list of items and their
 * status. If greyed out, item has been added to the 'shopping cart', else it is still open.
 * Items can be added via a TextEdit interface or removed from the list via a swipe motion, added or
 * removed from the 'shopping cart' via long-click. The list is scrollable. Any touch functionality
 * can also be accomplished via voice commands entered through the voiceActivationButton and are
 * processed by a STT system. Every action completed via the touch interface is narrated by a TTS
 * system.
 *
 * Possible Voice Commands:
 * - 'Add Item to List' --> Which Item? --> 'Item X'
 * - 'Remove Item from List' --> Which Item? --> 'Item X'
 * - 'Add Item to Cart' --> Which Item? --> 'Item X'
 * - 'Remove Item from Cart' --> Which Item? --> 'Item X'
 * - 'Check if Item is on List' --> Which Item? --> 'Item X'
 * - 'Read out the List'
 *
 * TODO: voice control
 * TODO?: sorting, filtering
 * TODO?: quantities, categories
 */
class GroceryListActivity : AppCompatActivity(), GroceryListCallback {

    private lateinit var binding: ActivityGroceryListBinding
    private lateinit var viewModel: GroceryListViewModel

    private lateinit var groceryItemList: ArrayList<GroceryItem>

    private lateinit var adapter: GroceryListAdapter

    private lateinit var voiceActivationButton: ImageButton
    
    private lateinit var lastCommand: String
    private lateinit var lastResponse: String

    /**
     * The onCreate function initializes the view by binding the Activity and the Layout and
     * retrieving the ViewModel. After calling the viewModel to load the grocery list data, the UI
     * elements and TTS and STT systems are configured.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroceryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GroceryListViewModel::class.java]

        viewModel.fetchGroceryList()
        groceryItemList = viewModel.groceryList

        configureUI()
        configureTTS()
        configureSTT()
    }

    /**
     * Sets up all UI elements, i.e. the addItem and voiceActivation buttons, their respective
     * onClick functionality and configures the recycler view.
     */
    private fun configureUI() {

        // set up add item button listener
        val addItemButton: Button = findViewById<View>(R.id.add_item_bt) as Button
        addItemButton.setOnClickListener {
            onAddItemButtonClick()
        }

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
     * Initializes the GroceryListAdapter and fills the recyclerview with the GroceryItems on the
     * GroceryList and configures the swipe to delete functionality in both directions utilizing
     * ItemTouchHelper
     */
    private fun configureRecyclerView() {

        adapter = GroceryListAdapter(groceryItemList, this)

        val rvGroceryList = findViewById<View>(R.id.grocery_list_rv) as RecyclerView
        rvGroceryList.adapter = adapter
        rvGroceryList.layoutManager = LinearLayoutManager(this)

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
        }).attachToRecyclerView(rvGroceryList)

        // implements swipe right to delete functionality
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
        }).attachToRecyclerView(rvGroceryList)
    }

    /**
     * Called when user swipes left or right on an item in the recyclerview. The corresponding
     * GroceryItem is then removed.
     *
     * @param viewHolder: the RecyclerView ViewHolder
     */
    private fun onSwipeToDelete(viewHolder: RecyclerView.ViewHolder) {
        val groceryItem: GroceryItem =
            groceryItemList[viewHolder.adapterPosition]

        deleteGroceryItem(groceryItem, viewHolder.adapterPosition)
    }

    /**
     * Calls the ViewModel to delete a GroceryItem from the GroceryList and then removes it from the
     * UI and narrates the performed action to the user.
     *
     * @param groceryItem: GroceryItem to be deleted
     * @param index: the index of the GroceryItem to be deleted
     */
    private fun deleteGroceryItem(groceryItem: GroceryItem, index: Int) {

        viewModel.deleteGroceryItem(groceryItem)
        adapter.notifyItemRemoved(index)
        viewModel.say(
            resources.getString(R.string.deleted_ITEMNAME_from_grocery_list, groceryItem.itemName)
        )

    }
    
    /**
     * Deletes a grocery item from the grocery list given the name of the item. If it does not
     * exist, state that out loud.
     *
     * @param groceryItemName: String, the name of the Grocery Item to be deleted.
     */
    private fun deleteGroceryItem(groceryItemName: String) {
        
        val groceryItem = findGroceryItemByName(groceryItemName)
        
        if (groceryItem != null) {
            deleteGroceryItem(groceryItem, groceryItemList.indexOf(groceryItem))
        } else {
            viewModel.say(
                resources.getString(R.string.ITEMNAME_is_not_on_the_list, groceryItemName)
            )
        }
    }
    
    /**
     * Given the name, return the corresponding Grocery Item.
     *
     * @param groceryItemName: String, the name of the desired Grocery Item
     * @return the found Grocery Item or null if it does not exists
     */
    private fun findGroceryItemByName(groceryItemName: String): GroceryItem? {
        return groceryItemList.find { it.itemName.lowercase() == groceryItemName }
    }

    /**
     * Called when activity lifecycle is ended. Calls the ViewModel to store the grocery list in its
     * current state to save any changes made.
     */
    override fun onDestroy() {
        super.onDestroy()
        viewModel.storeGroceryList()
        viewModel.shutdownTTS()
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
            viewModel.say(resources.getString(R.string.grocery_list))
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
        val commandObserver = Observer<String> { command ->
            lastCommand = command
            executeCommand(lastCommand)
        }
    
        // observe LiveData change to be notified when the STT returns a response
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
     * @param command: ArrayList<String>? containing the deconstructed command
     *
     * TODO: streamline processing and command structure
     */
    private fun executeCommand(command: String) {
    
        var exists = false
        
        if (command.contains(resources.getString(R.string.go_to))) {
            executeNavigationCommand(command)
    
        } else if (
            command.contains(resources.getString(R.string.add_indicator)) ||
            command.contains(resources.getString(R.string.remove_indicator)) ||
            command == resources.getString(R.string.check_if_an_item_is_on_the_list)
            ) {
            
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                requestResponse(resources.getString(R.string.which_item))
            }
        
        } else if (command == resources.getString(R.string.list_all_grocery_items)) {
            
            if (groceryItemList.isEmpty()) {
                viewModel.say(resources.getString(R.string.there_are_no_items_on_the_list))
            }
    
            for ((itemName) in groceryItemList) {
                viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
            }
    
        } else if (command == resources.getString(R.string.list_all_items_in_cart)) {
    
            for ((itemName, _, inCart) in groceryItemList) {
                if (inCart) {
                    viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
                    exists = true
                }
            }
    
            if (groceryItemList.isEmpty()) {
                viewModel.say(resources.getString(R.string.there_are_no_items_on_the_list))
            } else if (!exists) {
                viewModel.say(resources.getString(R.string.there_are_no_items_in_the_cart))
            }
    
        } else if (command == resources.getString(R.string.list_all_items_not_in_cart)) {
    
            for ((itemName, _, inCart) in groceryItemList) {
                if (!inCart) {
                    viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
                    exists = true
                }
            }
    
            if (groceryItemList.isEmpty()) {
                viewModel.say(resources.getString(R.string.there_are_no_items_on_the_list))
            } else if (!exists) {
                viewModel.say(resources.getString(R.string.all_items_are_in_the_cart))
            }
    
        } else if(command == resources.getString(R.string.stop_speech)) {

            val intent = Intent("BarcodeInfo_Stop").apply {
                putExtra("stop_speech", true)
            }
            sendBroadcast(intent)

        } else if ((command == resources.getString(R.string.tell_me_my_options))) {
            
            viewModel.say(
                "${resources.getString(R.string.your_options_are)} " +
                        "${resources.getString(R.string.add_an_item)}," +
                        "${resources.getString(R.string.remove_an_item)}," +
                        "${resources.getString(R.string.add_an_item_to_the_cart)}," +
                        "${resources.getString(R.string.remove_an_item_from_the_cart)}," +
                        "${resources.getString(R.string.check_if_an_item_is_on_the_list)}," +
                        "${resources.getString(R.string.list_all_grocery_items)}," +
                        "${resources.getString(R.string.list_all_items_not_in_cart)}," +
                        "${resources.getString(R.string.list_all_items_in_cart)}," +
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
     * Called once the STT system returns a response by the user. Depending on the previous command
     * and the given response, the action is executed.
     *
     * @param command: String, the command preceding the response
     * @param response: String, the response given by the user
     */
    private fun executeItemCommand(command: String, response: String) {
        
        if (response != resources.getString(R.string.cancel)) {
            
            when (command) {
                
                resources.getString(R.string.add_an_item) -> {
                    addGroceryItem(response)
                }
                
                resources.getString(R.string.remove_an_item) -> {
                    deleteGroceryItem(response)
                }
                
                resources.getString(R.string.add_an_item_to_the_cart) -> {
                    addItemToCart(response)
                }
                
                resources.getString(R.string.remove_an_item_from_the_cart) -> {
                    removeItemFromCart(response)
                }
                
                resources.getString(R.string.check_if_an_item_is_on_the_list) -> {
                    checkItemOnList(response)
                }
                
            }
        }
    }
    
    /**
     * Requests the ViewModel to add a new Grocery Item to the cart by name. If no Grocery Item
     * exists for an input item name, this is stated. If the item is already in the cart, this, too,
     * is stated.
     *
     * @param itemName: String, the name of the item to be added to the cart
     */
    private fun addItemToCart(itemName: String) {
        
        val groceryItem = findGroceryItemByName(itemName)
        
        if (groceryItem != null) {
            
            if (groceryItem.inCart) {
                viewModel.say(resources.getString(R.string.ITEMNAME_is_in_the_cart, itemName))
            } else {
                viewModel.placeGroceryItemInCart(groceryItem)
                adapter.notifyItemChanged(groceryItemList.indexOf(groceryItem))
                viewModel.say(resources.getString(R.string.placed_ITEMNAME_into_cart, itemName))
            }
            
        } else {
            viewModel.say(resources.getString(R.string.ITEMNAME_is_not_on_the_list, itemName))
        }
        
    }
    
    /**
     * Requests the ViewModel to remove a new Grocery Item from the cart by name. If no Grocery Item
     * exists for an input item name, this is stated. If the item is already not in the cart, this,
     * too, is stated.
     *
     * @param itemName: String, the name of the item to be removed from the cart
     */
    private fun removeItemFromCart(itemName: String) {
        val groceryItem = findGroceryItemByName(itemName)
    
        if (groceryItem != null) {
        
            if (!groceryItem.inCart) {
                viewModel.say(resources.getString(R.string.ITEMNAME_is_not_in_the_cart, itemName))
            } else {
                viewModel.placeGroceryItemInCart(groceryItem)
                adapter.notifyItemChanged(groceryItemList.indexOf(groceryItem))
                viewModel.say(resources.getString(R.string.removed_ITEMNAME_from_cart, itemName))
            }
        
        } else {
            viewModel.say(resources.getString(R.string.ITEMNAME_is_not_on_the_list, itemName))
        }
    }
    
    /**
     * Checks and states whether an item is on the list by name. If the item is on the list, also
     * states if it is in the cart.
     *
     * @param itemName: String, the item name to check the list for
     */
    private fun checkItemOnList(itemName: String) {
    
        val groceryItem = findGroceryItemByName(itemName)
    
        if (groceryItem != null) {
            viewModel.say(
                resources.getString(R.string.ITEMNAME_is_on_the_list, itemName)
            )
            if (groceryItem.inCart) {
                viewModel.say(
                    resources.getString(R.string.ITEMNAME_is_in_the_cart, itemName),
                    TextToSpeech.QUEUE_ADD
                )
            } else {
                viewModel.say(
                    resources.getString(R.string.ITEMNAME_is_not_in_the_cart, itemName),
                    TextToSpeech.QUEUE_ADD
                )
            }
        } else {
            viewModel.say(
                resources.getString(R.string.ITEMNAME_is_not_on_the_list, itemName)
            )
        }
    
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
     * Called when voiceActivationButton is clicked and handles the result. If clicked while the
     * STT system is listening, call to viewModel to cancel listening. Else, call viewModel to begin
     * listening.
     */
    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    /**
     * Implements an alertDialog that opens when the addItemButton is clicked. The alertDialog
     * consists of an EditText, an 'Add' positive and a 'Cancel' negative button. If the positive
     * button is clicked, whatever string was entered into the EditText is added as a new
     * GroceryItem to the GroceryList. The alertDialog is closed either when the positive, negative
     * or the background activity is clicked. Every possible action is narrated to the user.
     */
    private fun onAddItemButtonClick() {

        val alertDialog: AlertDialog = this.let {
            val builder = MaterialAlertDialogBuilder(
                it, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background
            )

            val view = layoutInflater.inflate(R.layout.edit_text_dialog, null)

            builder.setView(view)
            builder.apply {
                setPositiveButton(R.string.add) { _, _ ->

                    val addItemEt = view.findViewById<EditText>(R.id.et)

                    addGroceryItem(addItemEt.text.toString())
                }

                setNegativeButton(R.string.cancel_cap) { _, _ ->
                    viewModel.say(resources.getString(R.string.cancelled_adding_item))
                }
            }
            // Set other dialog properties
            builder.setTitle(R.string.add_new_grocery_item)
            // Create the AlertDialog
            builder.create()
        }
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alertDialog.show()
        viewModel.say(resources.getString(R.string.enter_item_to_add))
    }

    /**
     * Calls the ViewModel to add a new GroceryItem from a string to the GroceryList, adds it to the
     * UI and narrates the action.
     *
     * @param newItemName: String the name of the GroceryItem to be added
     */
    private fun addGroceryItem(newItemName: String) {

        viewModel.addGroceryItem(newItemName)
        adapter.notifyItemInserted(groceryItemList.size - 1)

        viewModel.say(resources.getString(R.string.added_ITEMNAME_to_list, newItemName))

    }

    /**
     * Implements the functionality of placing a GroceryItem into the 'shopping cart' or taking it
     * out of the 'cart' if the item is long-clicked. It calls the ViewModel to add/remove the item
     * from the cart, updates the UI accordingly and narrates the action.
     */
    override fun onLongClick(item: GroceryItem) {

        val index = groceryItemList.indexOf(item)
        val inCart = viewModel.placeGroceryItemInCart(item)

        adapter.notifyItemChanged(index)

        if (inCart) {
            viewModel.say(resources.getString(R.string.placed_ITEMNAME_into_cart, item.itemName))
        } else {
            viewModel.say(resources.getString(R.string.removed_ITEMNAME_from_cart, item.itemName))
        }
    }

    /**
     * Reads the name of a GroceryItem out loud, if it is clicked.
     *
     * @param item: GroceryItem to be read out loud.
     */
    override fun onClick(item: GroceryItem) {
        viewModel.say(item.itemName)
    }
    
}
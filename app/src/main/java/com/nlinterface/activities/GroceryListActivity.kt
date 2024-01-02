package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.GroceryListViewModel


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
            resources.getString(
                R.string.deleted_ITEMNAME_from_grocery_list, groceryItem.itemName
            )
        )

    }

    /**
     * Called when activity lifecycle is ended. Calls the ViewModel to store the grocery list in its
     * current state to save any changes made.
     */
    override fun onDestroy() {
        super.onDestroy()
        viewModel.storeGroceryList()
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
            } else if (command[0] == ""){
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

        Log.println(Log.DEBUG, "navToActivity", activity)

        when (activity) {

            ActivityType.GROCERYLIST.toString() -> {
                viewModel.say(resources.getString(R.string.grocery_list))
            }

            ActivityType.MAIN.toString() -> {
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.PLACEDETAILS.toString() -> {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                this.startActivity(intent)
            }

            ActivityType.SETTINGS.toString() -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }

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

                setNegativeButton(R.string.cancel) { _, _ ->
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
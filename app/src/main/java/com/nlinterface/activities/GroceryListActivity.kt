package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
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
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.TextToSpeechUtility
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.GroceryListViewModel
import java.util.Locale


class GroceryListActivity : AppCompatActivity(), GroceryListCallback {

    private lateinit var binding: ActivityGroceryListBinding
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var adapter: GroceryListAdapter
    private lateinit var viewModel: GroceryListViewModel
    private lateinit var voiceActivationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroceryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GroceryListViewModel::class.java]

        viewModel.fetchGroceryList()

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch == GlobalParameters.KeepScreenOn.YES) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        configureUI()
        configureTTS()
        configureSTT()
    }

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

    private fun configureRecyclerView() {
        // RecyclerView
        val rvGroceryList = findViewById<View>(R.id.grocery_list_rv) as RecyclerView
        groceryItemList = viewModel.groceryList

        adapter = GroceryListAdapter(groceryItemList, this)
        rvGroceryList.adapter = adapter
        rvGroceryList.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {return false}

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val groceryItem: GroceryItem =
                    groceryItemList[viewHolder.adapterPosition]

                val index = viewHolder.adapterPosition

                viewModel.deleteGroceryItem(groceryItem)

                adapter.notifyItemRemoved(index)

                viewModel.say(resources.getString(R.string.deleted_ITEMNAME_from_grocery_list, groceryItem.itemName))
            }
        }).attachToRecyclerView(rvGroceryList)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {return false}

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val groceryItem: GroceryItem =
                    groceryItemList[viewHolder.adapterPosition]

                val index = viewHolder.adapterPosition

                viewModel.deleteGroceryItem(groceryItem)

                adapter.notifyItemRemoved(index)

                viewModel.say(resources.getString(R.string.deleted_ITEMNAME_from_grocery_list, groceryItem.itemName))
            }
        }).attachToRecyclerView(rvGroceryList)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.storeGroceryList()
    }

    private fun configureTTS() {

        viewModel.initTTS()

        val ttsInitializedObserver = Observer<Boolean> { _ ->
            viewModel.say(resources.getString(R.string.grocery_list))
        }

        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    private fun configureSTT() {

        viewModel.initSTT()

        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening) {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_green)
            } else {
                voiceActivationButton.setImageResource(R.drawable.ic_mic_white)
            }
        }

        viewModel.isListening.observe(this, sttIsListeningObserver)

        val commandObserver = Observer<ArrayList<String>> {command ->
            executeCommand(command)
        }

        viewModel.command.observe(this, commandObserver)

    }

    private fun executeCommand(command: ArrayList<String>?) {

        if (command != null && command.size == 3) {
            if (command[0] == "GOTO") {
                navToActivity(command[1])
            } else if (command[0] == ""){
                viewModel.say(resources.getString(R.string.choose_activity_to_navigate_to))
            }
        }

    }

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

    private fun onVoiceActivationButtonClick() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }

    private fun onAddItemButtonClick() {

        val alertDialog: AlertDialog = this.let {
            val builder = MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background)

            val view = layoutInflater.inflate(R.layout.edit_text_dialog, null)

            builder.setView(view)
            builder.apply {
                setPositiveButton(R.string.add) { _, _ ->

                    val addItemEt = view.findViewById<EditText>(R.id.et)
                    val newItemName = addItemEt.text.toString()

                    viewModel.addGroceryItem(newItemName)
                    adapter.notifyItemInserted(groceryItemList.size - 1)

                    viewModel.say(resources.getString(R.string.added_ITEMNAME_to_list, newItemName))
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
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show()
        viewModel.say(resources.getString(R.string.enter_item_to_add))
    }

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

    override fun onClick(item: GroceryItem) {
        viewModel.say(item.itemName)
    }

}
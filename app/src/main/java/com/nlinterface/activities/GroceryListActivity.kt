package com.nlinterface.activities

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
import com.nlinterface.utility.GlobalParameters
import com.nlinterface.utility.TextToSpeechUtility
import com.nlinterface.utility.setViewRelativeSize
import com.nlinterface.viewmodels.GroceryListViewModel
import java.util.Locale


class GroceryListActivity : AppCompatActivity(), GroceryListCallback, OnInitListener {

    private lateinit var binding: ActivityGroceryListBinding
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var adapter: GroceryListAdapter
    private lateinit var tts: TextToSpeechUtility
    private lateinit var viewModel: GroceryListViewModel

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

        initTTS()

        configureUI()
    }

    private fun configureUI() {

        // set up add item button listener
        val addItemButton: Button = findViewById<View>(R.id.add_item_bt) as Button
        addItemButton.setOnClickListener {
            onAddItemButtonClick()
        }

        // set up voice activation button listener
        val voiceActivationButton = findViewById<View>(R.id.voice_activation_bt) as ImageButton
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

                say("Deleted ${groceryItem.itemName} from list.")
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

                say("Deleted ${groceryItem.itemName} from list.")
            }
        }).attachToRecyclerView(rvGroceryList)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.storeGroceryList()
    }

    private fun initTTS() {

        val ttsInitializedObserver = Observer<Boolean> { _ ->
            say("Grocery List Activity")
        }
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

        tts = TextToSpeechUtility(this, this)

    }

    private fun say(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (viewModel.ttsInitialized.value == true) {
            tts.say(text, queueMode)
        }
    }

    private fun listActionOptions() {
        say("Add Items, Read Items on List, Read Items in Cart, Read All Items", TextToSpeech.QUEUE_ADD)
        say("Reading all Items", TextToSpeech.QUEUE_ADD)
        readItems(all = true)
        say("Reading only Items in Cart", TextToSpeech.QUEUE_ADD)
        readItems(inCart = true)
        say("Reading only Items still on List", TextToSpeech.QUEUE_ADD)
        readItems(onList = true)
    }

    private fun readItems(all: Boolean = false, inCart: Boolean = false, onList: Boolean = false) {

        var text = ""

        for (item in groceryItemList) {

            if (all) {
                text = text.plus(item.itemName)
            } else if (inCart && item.inCart) {
                text = text.plus(item.itemName)
            } else if (onList && !item.inCart) {
                text = text.plus(item.itemName)
            }
        }

        say(text , TextToSpeech.QUEUE_ADD)

    }

    private fun onVoiceActivationButtonClick() {
        listActionOptions()
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

                    say("Added $newItemName to List.")
                }

                setNegativeButton(R.string.cancel) { _, _ ->
                    say("Cancelled adding items.")
                }
            }
            // Set other dialog properties
            builder.setTitle(R.string.add_new_grocery_item)
            // Create the AlertDialog
            builder.create()
        }
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show()
        say("Type which item you would like to add.")
    }

    override fun onLongClick(item: GroceryItem) {
        val index = groceryItemList.indexOf(item)
        val inCart = viewModel.placeGroceryItemInCart(item)
        adapter.notifyItemChanged(index)

        if (inCart) {
            say("Placed ${item.itemName} into cart.")
        } else {
            say("Removed ${item.itemName} from cart.")
        }
    }

    override fun onClick(item: GroceryItem) {
        say(item.itemName)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.setLocale(Locale.US)
            viewModel.ttsInitialized.value = true
        } else {
            Log.println(Log.ERROR, "tts onInit", "Couldn't initialize TTS Engine")
        }

    }

}
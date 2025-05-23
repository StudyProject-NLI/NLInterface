package com.nlinterface.activities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nlinterface.dataclasses.GroceryItem
import java.io.File
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nlinterface.R
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.dataclasses.PlaceDetailsItem
import com.nlinterface.utility.LLMAppConnector
import com.nlinterface.utility.OnSwipeTouchListener
import com.nlinterface.viewmodels.VoiceOnlyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The Voice Only activity allows users to use the app solely by voice commands with the help of a
 * large language Model. The activity consist of three stages representing the three stages:
 * Listening, Processing and Speaking.
 *
 * TODO: Improve and Test
 */
class VoiceOnlyActivity: AppCompatActivity() {
    private var isProcessingCommand = false // State tracking
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VoiceOnlyViewModel
    private lateinit var viewFlipper: ViewFlipper

    /**
     * The onCreate function sets up the viewFlipper that changes the screens layout based on the
     * stage the speech system is in. It also sets up the OnTouchListener and starts in listening
     * mode.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_voice_only)

        viewModel = ViewModelProvider(this)[VoiceOnlyViewModel::class.java]

        viewFlipper = findViewById(R.id.viewSwitcher)

        val rootView: View = findViewById(android.R.id.content)

        /**
         * The OnSwipeTouchListener is simplified to two possible inputs.
         * DoubleTap navigates back to the Main Menu and exists the Voice Only Mode.
         * Pressing and holding starts listening. (Only necessary when an error occurs. Per default
         * the system should be listening without any additional input.)
         */

        rootView.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onDoubleTap() {
                val intent = Intent(this@VoiceOnlyActivity, MainActivity::class.java)
                startActivity(intent)
            }
            override fun onLongPress() {
                startListening()
            }
        })

        startAsynchronously()
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
            viewModel.say(resources.getString(R.string.voice_only_mode))
        }

        // observe LiveDate change, to be notified if TTS initialization is completed
        viewModel.ttsInitialized.observe(this, ttsInitializedObserver)

    }

    /**
     * Called by the onCreate function and calls upon the ViewModel to initialize the STT system.
     * It processed and observes the state of the activity to accurately represent it with the
     * corresponding screen and make sure the next processing state is started only when the prior
     * is finished.
     */
    private fun configureSTT() {
        viewModel.initSTT()

        val sttIsListeningObserver = Observer<Boolean> { isListening ->
            if (isListening && !isProcessingCommand) {
                showListeningStage()
            }
            else if (!isListening && !isProcessingCommand) {
                viewModel.handleSTTSpeechBegin()
            }
        }

        // observe LiveData change to be notified when the STT system is active(ly listening)
        viewModel.isListening.observe(this, sttIsListeningObserver)

        val processObserver = Observer<Boolean> { isProcessing ->
            if(isProcessing) {
                isProcessingCommand = true
                showProcessingStage()
            }
        }

        viewModel.isProcessing.observe(this, processObserver)

        // if a command is successfully generated, process and execute it
        val commandObserver = Observer<String> {command ->
            if (command.isNotEmpty()) {
                processVoiceInput(command)
            }
        }

        // observe LiveData change to be notified when the STT returns a command
        viewModel.command.observe(this, commandObserver)

    }

    /**
     * The following three functions handle the UI.
     */
    private fun showListeningStage() {
        if (viewFlipper.currentView.id != R.layout.mode_listening) {
            viewFlipper.displayedChild = 0
        }
    }

    private fun showProcessingStage() {
        if (viewFlipper.currentView.id != R.layout.mode_processing) {
            viewFlipper.displayedChild = 1
        }

    }

    private fun showSpeakingStage() {
        if (viewFlipper.currentView.id != R.layout.mode_speaking) {
            viewFlipper.displayedChild = 2
        }
    }

    private fun startAsynchronously() {
        configureTTS()
        firstListeningProcess()
    }

    /**
     * Checks for permission to use audio. If granted start listening. If not ask for permission
     * again and call itself again.
     */
    private fun firstListeningProcess(){
        lifecycleScope.launch{
            delay(2500)
            configureSTT()
            if (checkCallingOrSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            }
            else {
                ActivityCompat.requestPermissions(
                    this@VoiceOnlyActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MainActivity.STT_PERMISSION_REQUEST_CODE
                )
                firstListeningProcess()
            }
        }
    }

    private fun isValidCommand(label: String): Boolean {
        val supportedCommands = listOf(
            "grocery-list",
            "barcode-scanner",
            "navigation",
            "object-and-hand-recognition"
        )
        return supportedCommands.contains(label)
    }

    private fun processVoiceInput(command: String) {
        lifecycleScope.launch {
            showSpeakingStage()

            try {
                val llmConnector = LLMAppConnector.getInstance

                // Authenticate with the API
                val token = llmConnector.authenticate()

                // Add <User> prefix to the command
                val prefixedCommand = "<User>$command"

                // Send command to the LLM API
                val apiResponse = llmConnector.sendCommandToLLM(prefixedCommand, token)

                // Parse the response and get label and additional data requirement
                val (label, needsAdditionalData) = llmConnector.parseResponse(apiResponse)

                if (label != null) {
                    // If the label is found in the supported commands, execute it
                    if (isValidCommand(label)) {
                        executeCommand(label, needsAdditionalData)
                    } else {
                        // No valid command found, just say the response
                        viewModel.sayAndAwait(label)
                    }
                } else {
                    viewModel.sayAndAwait("I heard: $command, but couldn't process it.")
                }
            } catch (e: Exception) {
                viewModel.sayAndAwait("Sorry, I encountered an error: ${e.message}")
                e.printStackTrace()
            } finally {
                // Reset processing state after command completion
                isProcessingCommand = false
                //startListening()
            }
        }
    }

    /**
     * Gets the current grocery list formatted as a string.
     * Format: "[quantity]Item; [quantity]Item2; ..."
     * If the list is empty, returns "No items on the grocery list"
     */
    private fun getCurrentGroceryList(): String {
        val groceryListFile = File(applicationContext.filesDir, "GroceryList.json")
        if (!groceryListFile.exists() || groceryListFile.length() == 0L) {
            return "No items on the grocery list"
        }
        return try {
            val json = groceryListFile.readText()
            val type = object : TypeToken<ArrayList<GroceryItem>>() {}.type
            val groceryList: ArrayList<GroceryItem> = Gson().fromJson(json, type)
            if (groceryList.isEmpty()) {
                "No items on the grocery list"
            } else {
                groceryList.joinToString("; ") { "[1]\\${it.itemName}" }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "No items on the grocery list"
        }
    }

    /**
     * Executes the appropriate action based on the label from the LLM API.
     */
    private suspend fun executeCommand(label: String, needsAdditionalData: Boolean) {
        when (label) {
            "grocery-list" -> {
                if (needsAdditionalData) {
                    // Get the current grocery list
                    val groceryList = getCurrentGroceryList()

                    // Send the grocery list to the LLM with <App> prefix
                    val token = LLMAppConnector.getInstance.authenticate()
                    val prefixedData = "<App>$groceryList"
                    val apiResponse = LLMAppConnector.getInstance.sendCommandToLLM(prefixedData, token)

                    // Parse the response
                    val (response, _) = LLMAppConnector.getInstance.parseResponse(apiResponse)

                    if (response != null) {
                        // Process grocery list updates
                        processGroceryListUpdates(response)
                    } else {
                        viewModel.sayAndAwait("Sorry, I couldn't process your request.")
                    }
                } else {
                    viewModel.sayAndAwait(getString(R.string.navigate_to_grocery_list))
                    val intent = Intent(this@VoiceOnlyActivity, GroceryListActivity::class.java)
                    startActivity(intent)
                }
            }
            "barcode-scanner" -> {
                viewModel.sayAndAwait(getString(R.string.barcode_scanner))
                val intent = Intent(this@VoiceOnlyActivity, BarcodeSettingsActivity::class.java)
                startActivity(intent)
            }
            "navigation" -> {
                if (needsAdditionalData) {
                    // Retrieve current place details list as additional data (JSON)
                    val placeList = getCurrentPlaceList()
                    val token = LLMAppConnector.getInstance.authenticate()
                    val prefixedData = "<App>$placeList"
                    val apiResponse = LLMAppConnector.getInstance.sendCommandToLLM(prefixedData, token)
                    val (response, _) = LLMAppConnector.getInstance.parseResponse(apiResponse)
                    if (response != null) {
                        processNavigationUpdates(response)
                    } else {
                        viewModel.sayAndAwait("Sorry, I couldn't process your navigation request.")
                    }
                } else {
                    viewModel.sayAndAwait(getString(R.string.place_details))
                    val intent = Intent(this@VoiceOnlyActivity, PlaceDetailsActivity::class.java)
                    startActivity(intent)
                }
            }
            "object-and-hand-recognition" -> {
                viewModel.sayAndAwait(getString(R.string.classification))
                val intent = Intent(this@VoiceOnlyActivity, ClassificationActivity::class.java)
                startActivity(intent)
            }
            else -> {
                viewModel.sayAndAwait("I don't know how to handle: $label; additional-data-required=$needsAdditionalData")
            }
        }
    }

    /**
    * Process grocery list updates from LLM response
    * Expected format: "[+/-quantity]Item; [quantity]Item2; [-quantity]Item3"
    */
    private suspend fun processGroceryListUpdates(response: String) {
        // Find the first occurrence of '[' to ignore irrelevant prefixed text
        val startIndex = response.indexOf('[')
        if (startIndex == -1) {
            viewModel.sayAndAwait(response) // No valid grocery list format found, so print what the LLM said
            return
        }

        val itemsToProcess = response.substring(startIndex).split(";")
        val itemsToAdd = mutableListOf<String>()
        val itemsToRemove = mutableListOf<String>()

        // Parse the response to identify items to add or remove
        for (item in itemsToProcess) {
            val trimmedItem = item.trim()
            if (trimmedItem.isNotEmpty()) {
                // Parse the format [+/-quantity]Item
                val regex = """^\[([+\-]?\d+)](.+)$""".toRegex()
                val matchResult = regex.find(trimmedItem)

                if (matchResult != null) {
                    val (quantityStr, itemName) = matchResult.destructured

                    if (quantityStr.startsWith("+") || !quantityStr.startsWith("-")) {
                        // Add item
                        itemsToAdd.add(itemName.trim())
                    } else if (quantityStr.startsWith("-")) {
                        // Remove item
                        itemsToRemove.add(itemName.trim())
                    }
                }
            }
        }

        // Launch GroceryListActivity and pass the items to add/remove
        val intent = Intent(this, GroceryListActivity::class.java)
        intent.putExtra("ITEMS_TO_ADD", ArrayList(itemsToAdd))
        intent.putExtra("ITEMS_TO_REMOVE", ArrayList(itemsToRemove))
        intent.putExtra("FROM_VOICE_COMMAND", true)
        startActivity(intent)

        // Give feedback to user
        val addMessage = if (itemsToAdd.isNotEmpty())
            "Adding ${itemsToAdd.joinToString(", ")} to your grocery list. " else ""
        val removeMessage = if (itemsToRemove.isNotEmpty())
            "Removing ${itemsToRemove.joinToString(", ")} from your grocery list." else ""

        viewModel.sayAndAwait("$addMessage$removeMessage")
    }

    // Helper to get the current place list from the saved PlaceDetailsItem JSON file.
    private fun getCurrentPlaceList(): String {
        val placeListFile = File(applicationContext.filesDir, "PlaceDetailsItemList.json")
        if (!placeListFile.exists() || placeListFile.length() == 0L) {
            return "{}"
        }
        return try {
            placeListFile.readText()
        } catch (e: Exception) {
            "{}"
        }
    }

    private suspend fun processNavigationUpdates(response: String) {
        // Process only the content after the <LLM> tag if it exists.
        val processedResponse = if (response.contains("<LLM>")) {
            response.substringAfter("<LLM>")
        } else {
            response
        }

        // Regex to capture removals of the format: [-1]PlaceID
        val removalPattern = "\\[-1\\](\\S+)".toRegex()
        val removals = removalPattern.findAll(processedResponse).map { it.groupValues[1] }.toList()

        if (removals.isNotEmpty()) {
            // Start PlaceDetailsActivity with the removal IDs passed in the intent.
            val intent = Intent(this@VoiceOnlyActivity, PlaceDetailsActivity::class.java)
            intent.putStringArrayListExtra("PLACE_IDS_TO_REMOVE", ArrayList(removals))
            startActivity(intent)

            // Retrieve the currently stored PlaceDetailsItem list from JSON.
            val placeListJson = getCurrentPlaceList()
            val placeNames = try {
                // Using the PlaceDetailsItem list from PlaceDetailsActivity.
                val type = object : com.google.gson.reflect.TypeToken<List<PlaceDetailsItem>>() {}.type
                val places: List<PlaceDetailsItem> = com.google.gson.Gson().fromJson(placeListJson, type)
                // Map each removal ID to its corresponding store name (or fallback to the removal ID).
                removals.map { removalId ->
                    places.find { it.placeID == removalId }?.storeName ?: removalId
                }
            } catch (e: Exception) {
                removals // Fallback if JSON parsing fails.
            }

            // Announce removal by speaking only the supermarket names.
            viewModel.sayAndAwait("Removing supermarkets with names: ${placeNames.joinToString(", ")}.")
        } else {
            viewModel.sayAndAwait(response)
        }
    }

    private fun startListening() {
        viewModel.handleSTTSpeechBegin()
    }
}
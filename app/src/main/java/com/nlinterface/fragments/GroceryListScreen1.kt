package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nlinterface.R
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.GroceryListViewModel

/**
 * Fragment for the grocery list activity.
 */
class GroceryListScreen1 : Fragment(), SwipeAction {

    private lateinit var viewModel: GroceryListViewModel

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * On ViewCreated accesses the shared viewmodel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.grocerylist_screen1, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[GroceryListViewModel::class.java]

    }

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}

    override fun onSwipeRight() {}

    /**
     * Swiping down makes the app read out the current grocery list.
     */
    override fun onSwipeUp() {
        val activityViewmodel = (activity as GroceryListActivity).viewModel
        if (activityViewmodel.groceryList.isEmpty()){
            activityViewmodel.say(resources.getString(R.string.there_are_no_items_on_the_list))
        }
        else {
            for ((itemName) in activityViewmodel.groceryList) {
                (activity as GroceryListActivity).viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
            }
        }
    }

    /**
     * Swiping up clears the grocery list and removes all corresponding fragments.
     */
    override fun onSwipeDown() {
        for ((itemName, _, inCart) in (activity as GroceryListActivity).groceryItemList) {
            if (!inCart) {
                viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
            }
        }
    }

    /**
     * Navigates to the Voice Only Activity.
     */
    override fun onDoubleTap() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    /**
     * Activates the LLM to start listening.
     */
    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }
}
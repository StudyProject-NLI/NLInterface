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
class GroceryListScreen2 : Fragment(), SwipeAction {

    private lateinit var viewModel: GroceryListViewModel
    private lateinit var nextFragment: Fragment

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * On ViewCreated accesses the shared viewmodel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.grocerylist_screen2, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[GroceryListViewModel::class.java]
    }

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}

    override fun onSwipeRight() {    }

    /**
     * Swiping up opens up a text input allowing to add an item to the grocery list. It is
     * automatically added to the next open spot in the last fragment. If the last fragments
     * variables are set, a new fragment is created
     *
     * TODO: Add item to the next free slot and not only in the last fragment.
     */
    override fun onSwipeUp() {
        (activity as? GroceryListActivity)?.let {
            it.onAddItemButtonClick()
            it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                nextFragment = it.fragmentAdapter.fragmentList.last()
                if (isCompleted) {
                    if ((nextFragment as GroceryListScreenBase).itemTop ==
                        (nextFragment as GroceryListScreenBase).default){
                        (nextFragment as GroceryListScreenBase).itemTop =
                            it.viewModel.groceryList.last().itemName
                        (nextFragment as GroceryListScreenBase).viewModel.updateButtonTexts(
                            (nextFragment as GroceryListScreenBase).itemTop,
                            (nextFragment as GroceryListScreenBase).itemBottom
                        )
                        (nextFragment as GroceryListScreenBase).checkAndCreateNewFragment()
                        }
                    else if ((nextFragment as GroceryListScreenBase).itemBottom ==
                        (nextFragment as GroceryListScreenBase).default) {
                        (nextFragment as GroceryListScreenBase).itemBottom =
                            it.viewModel.groceryList.last().itemName
                        (nextFragment as GroceryListScreenBase).viewModel.updateButtonTexts(
                            (nextFragment as GroceryListScreenBase).itemTop,
                            (nextFragment as GroceryListScreenBase).itemBottom
                        )
                        (nextFragment as GroceryListScreenBase).checkAndCreateNewFragment()
                    }
                    it.operationCompleted.removeObservers(viewLifecycleOwner)
                    it.operationCompletedStatus.value = false
                }
            }
        }
    }

    /**
     * Swiping down makes the app read out all item that are not yet in the cart.
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
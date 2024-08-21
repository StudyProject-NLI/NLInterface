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

class GroceryListScreen2 : Fragment(), SwipeAction {

    private lateinit var viewModel: GroceryListViewModel
    private lateinit var nextFragment: Fragment

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

    override fun onSwipeLeft() {}

    override fun onSwipeRight() {    }

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

    override fun onSwipeDown() {
        for ((itemName, _, inCart) in (activity as GroceryListActivity).groceryItemList) {
            if (!inCart) {
                viewModel.say(itemName, TextToSpeech.QUEUE_ADD)
            }
        }
    }

    override fun onDoubleTap() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.setSpeechRecognitionListener(STTInputType.COMMAND)
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }
}
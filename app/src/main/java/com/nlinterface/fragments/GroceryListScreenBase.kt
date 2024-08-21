package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.nlinterface.R
import com.nlinterface.activities.GroceryListActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.GroceryListViewModel
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class GroceryListScreenBase : Fragment(), SwipeAction {

    lateinit var viewModel: GroceryListViewModel

    private lateinit var topButton: MaterialButton
    private lateinit var bottomButton: MaterialButton

    val default: String by lazy {
        resources.getString(R.string.add_an_item)
    }
    lateinit var itemTop: String
    lateinit var itemBottom: String


    companion object {
        private const val ARG_ITEM_TOP = "item_top"
        private const val ARG_ITEM_BOTTOM = "item_bottom"
        fun newInstance(itemTop: String, itemBottom: String): GroceryListScreenBase {
            val fragment = GroceryListScreenBase()
            val args = Bundle()
            args.putString(ARG_ITEM_TOP, itemTop)
            args.putString(ARG_ITEM_BOTTOM, itemBottom)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            itemTop = savedInstanceState.getString(ARG_ITEM_TOP, default)
            itemBottom = savedInstanceState.getString(ARG_ITEM_BOTTOM, default)
        } else {
            arguments.let {
                if (it != null) {
                    itemTop = it.getString(ARG_ITEM_TOP)!!
                }
                if (it != null) {
                    itemBottom = it.getString(ARG_ITEM_BOTTOM)!!
                }
            }
            Log.i("GroceryListScreenBase", "itemTop: $itemTop, itemBottom: $itemBottom")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.grocerylist_basescreen, container, false)
        topButton = view.findViewById(R.id.option_top)
        bottomButton = view.findViewById(R.id.option_bottom)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))

        viewModel = ViewModelProvider(this)[GroceryListViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topButton.text = itemTop
        bottomButton.text = itemBottom

        viewModel.topButtonText.observe(viewLifecycleOwner) { text ->
            topButton.text = text
        }

        viewModel.bottomButtonText.observe(viewLifecycleOwner) { text ->
            bottomButton.text = text
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_ITEM_TOP, itemTop)
        outState.putString(ARG_ITEM_BOTTOM, itemBottom)
    }
    override fun onSwipeLeft() {
    }

    override fun onSwipeRight() {
    }

    override fun onSwipeUp() {
        if (itemTop == default) {
            (activity as? GroceryListActivity)?.let {
                it.onAddItemButtonClick()
                it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                    if (isCompleted) {
                        Log.d("AddingItem", "Observer check")
                        itemTop = it.viewModel.groceryList.last().itemName
                        viewModel.updateButtonTexts(itemTop, itemBottom)
                        checkAndCreateNewFragment()
                        it.operationCompleted.removeObservers(viewLifecycleOwner)
                        it.operationCompletedStatus.value = false
                    }

                }
            }
        }
        else {
            Log.i("GroceryList", (activity as GroceryListActivity).viewModel.groceryList.toString())
            (activity as GroceryListActivity).addItemToCart(itemTop)
            (activity as GroceryListActivity).deleteGroceryItem(itemTop)
            Log.i("GroceryList", (activity as GroceryListActivity).viewModel.groceryList.toString())
            itemTop = default
            viewModel.updateButtonTexts(itemTop, itemBottom)
            if (itemTop == default  && itemBottom == default
                        && (activity as GroceryListActivity).fragmentAdapter.itemCount > 3){
                (activity as GroceryListActivity).fragmentAdapter.removeFragment(this)
            }
        }
    }

    override fun onSwipeDown() {
        if (itemBottom == default) {
            (activity as? GroceryListActivity)?.let {
                it.onAddItemButtonClick()
                it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                    if (isCompleted) {
                        itemBottom = it.viewModel.groceryList.last().itemName
                        viewModel.updateButtonTexts(itemTop, itemBottom)
                        checkAndCreateNewFragment()
                        it.operationCompleted.removeObservers(viewLifecycleOwner)
                        it.operationCompletedStatus.value = false
                    }
                }
            }
        }
        else {
            Log.i("GroceryList", (activity as GroceryListActivity).viewModel.groceryList.toString())
            (activity as GroceryListActivity).addItemToCart(itemTop)
            (activity as GroceryListActivity).deleteGroceryItem(itemBottom)
            Log.i("GroceryList", (activity as GroceryListActivity).viewModel.groceryList.toString())
            itemBottom = default
            viewModel.updateButtonTexts(itemTop, itemBottom)
            if (itemTop == default  && itemBottom == default
                    && (activity as GroceryListActivity).fragmentAdapter.itemCount > 3){
                (activity as GroceryListActivity).removeFragment(this)
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

    private fun areVariablesSet(): Boolean {
        return if (itemTop != default  && itemBottom != default) {
            TRUE
        } else {
            FALSE
        }
    }

    fun checkAndCreateNewFragment() {
        if (areVariablesSet()) {
            (activity as GroceryListActivity).addNewFragment(
                default, default
            )
            Log.println(Log.INFO, "GroceryFragment", "New Fragment created")
        }
    }

}
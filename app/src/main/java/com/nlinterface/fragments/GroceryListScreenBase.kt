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

/**
 * Fragment for the grocery list activity.
 */
class GroceryListScreenBase : Fragment(), SwipeAction {

    lateinit var viewModel: GroceryListViewModel

    private lateinit var topButton: MaterialButton
    private lateinit var bottomButton: MaterialButton

    val default: String by lazy {
        resources.getString(R.string.add_an_item)
    }
    lateinit var itemTop: String
    lateinit var itemBottom: String

    /**
     * Companion object used for fragment creation. Assures a dynamically created fragment comprises
     * the variables item top and item bottom and handles their initialization.
     */
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

    /**
     * On Create initializes the variables item top and item bottom.
     * On Create View creates the buttons for item top and item bottom. The buttons are for UI
     * purposes. (Allowing to change their text.)
     * On View created maps the variables item Top and item Bottom to their corresponding buttons
     * and initializes two observers to allow text changes on a UI level.
     */

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

    /**
     * Necessary to save fragment state, since they are dynamically created.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_ITEM_TOP, itemTop)
        outState.putString(ARG_ITEM_BOTTOM, itemBottom)
    }

    override fun onResume(){
        super.onResume()
        if ((activity as GroceryListActivity).groceryListFragmentAdapter.fragmentList.last()
            == this){
            checkAndCreateNewFragment()
        }
    }

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}

    /**
     * Opens up text input to add Grocery item to this slot. Updates the text in the UI as well.
     * If there already is an item in that slot, it is added to the cart and deleted from the list
     * and UI.
     */
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
            (activity as? GroceryListActivity)?.let {
                it.addItemToCart(itemTop)
                itemTop = default
                viewModel.updateButtonTexts(itemTop, itemBottom)
                val fragmentList = it.groceryListFragmentAdapter.fragmentList
                if (itemTop == default && itemBottom == default
                    && it.groceryListFragmentAdapter.itemCount > 3
                ) {
                    if (fragmentList.last() != this) {
                        it.groceryListFragmentAdapter.removeFragment(this)
                    }
                    else if (fragmentList.last() == this){
                        if (!(fragmentList[fragmentList.size-2] as GroceryListScreenBase).areVariablesSet()){
                            it.groceryListFragmentAdapter.removeFragment(this)
                        }
                    }
                }
            }
        }
    }

    /**
     * Opens up text input to add Grocery item to this slot. Updates the text in the UI as well.
     * If there already is an item in that slot, it is added to the cart and deleted from the list
     * and UI.
     */
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
            (activity as? GroceryListActivity)?.let {
                it.addItemToCart(itemBottom)
                itemBottom = default
                viewModel.updateButtonTexts(itemTop, itemBottom)
                val fragmentList = it.groceryListFragmentAdapter.fragmentList
                if (itemTop == default && itemBottom == default
                    && it.groceryListFragmentAdapter.itemCount > 3
                ) {
                    if (fragmentList.last() != this) {
                        it.groceryListFragmentAdapter.removeFragment(this)
                    }
                    else if (fragmentList.last() == this){
                        if (!(fragmentList[fragmentList.size-2] as GroceryListScreenBase).areVariablesSet()){
                            it.groceryListFragmentAdapter.removeFragment(this)
                        }
                    }
                }
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
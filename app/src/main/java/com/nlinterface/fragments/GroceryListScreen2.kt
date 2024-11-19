package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
    private lateinit var nextFragment: GroceryListScreenBase

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
                nextFragment = it.groceryListFragmentAdapter.fragmentList.last() as GroceryListScreenBase
                if (isCompleted) {
                    if (nextFragment.itemTop == nextFragment.default){
                        nextFragment.itemTop = it.viewModel.groceryList.last().itemName
                        nextFragment.viewModel.updateButtonTexts(
                            nextFragment.itemTop, nextFragment.itemBottom
                        )
                        (nextFragment).checkAndCreateNewFragment()
                        }
                    else if (nextFragment.itemBottom == nextFragment.default) {
                        nextFragment.itemBottom = it.viewModel.groceryList.last().itemName
                        nextFragment.viewModel.updateButtonTexts(
                            nextFragment.itemTop,nextFragment.itemBottom
                        )
                        nextFragment.checkAndCreateNewFragment()
                    }
                    it.operationCompleted.removeObservers(viewLifecycleOwner)
                    it.operationCompletedStatus.value = false
                }
            }
        }
    }

    /**
     * Swiping up clears the grocery list and removes all corresponding fragments.
     */
    override fun onSwipeDown() {
        (activity as? GroceryListActivity)?.let {
            Log.println(Log.DEBUG, "FragmentList", it.groceryListFragmentAdapter.fragmentList.toString())
            it.groceryListFragmentAdapter.clearFragments()
            Log.println(Log.DEBUG, "FragmentList", it.groceryListFragmentAdapter.fragmentList.toString())
            val time = it.viewModel.groceryList.size
            it.viewModel.groceryList.clear()
            it.viewModel.say(resources.getString(R.string.all_items_are_removed))
            Handler(Looper.getMainLooper()).postDelayed({
                it.addNewFragment(
                    resources.getString(R.string.add_an_item),
                    resources.getString(R.string.add_an_item)
                )
                Log.println(Log.INFO, "GroceryFragment", "New Fragment created")
            }, (time * 100).toLong())//necessary to assure the new fragment is created when grocery list is empty
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
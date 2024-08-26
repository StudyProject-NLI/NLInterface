package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
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
     * Swiping up clears the grocery list and removes all corresponding fragments.
     */
    override fun onSwipeUp() {
        (activity as? GroceryListActivity)?.let {
            Log.println(Log.DEBUG, "FragmentList", it.fragmentAdapter.fragmentList.toString())
            it.fragmentAdapter.clearFragments()
            Log.println(Log.DEBUG, "FragmentList", it.fragmentAdapter.fragmentList.toString())
            val time = it.viewModel.groceryList.size
            it.viewModel.groceryList.clear()
            it.viewModel.say(resources.getString(R.string.all_items_are_removed))
            Handler(Looper.getMainLooper()).postDelayed({
                it.addNewFragment(
                    resources.getString(R.string.add_an_item),
                    resources.getString(R.string.add_an_item)
                )
                Log.println(Log.INFO, "GroceryFragment", "New Fragment created")
            }, (time * 10).toLong())//necessary to assure the new fragment is created when grocery list is empty
        }
    }

    /**
     * Swiping down makes the app read out the current grocery list.
     */
    override fun onSwipeDown() {
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
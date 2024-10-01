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
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.PlaceDetailsViewModel

/**
 * Fragment for the place details activity.
 */
class PlaceDetailsScreen1 : Fragment(), SwipeAction {

    private lateinit var viewModel: PlaceDetailsViewModel
    private lateinit var nextFragment: PlaceDetailsScreenBase

    /**
     * On Create View creates the layout and sets up the swipe Navigation.
     * On ViewCreated accesses the shared viewmodel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.place_details_screen1, container, false)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[PlaceDetailsViewModel::class.java]

    }

    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */
    override fun onSwipeLeft() {}

    override fun onSwipeRight() {}

    override fun onSwipeUp() {
        (activity as PlaceDetailsActivity).let {
            it.startPlaceSearch()
            it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                nextFragment = it.placeDetailsFragmentAdapter.fragmentList.last() as PlaceDetailsScreenBase
                if (isCompleted) {
                    if (isCompleted) {
                        if (nextFragment.placeTop == nextFragment.default) {
                            nextFragment.placeTop =
                                it.viewModel.placeDetailsItemList.last().storeName
                            nextFragment.viewModel.updateButtonTexts(
                                nextFragment.placeTop, nextFragment.placeBottom
                            )
                            (nextFragment).checkAndCreateNewFragment()
                        } else if (nextFragment.placeBottom == nextFragment.default) {
                            nextFragment.placeBottom =
                                it.viewModel.placeDetailsItemList.last().storeName
                            nextFragment.viewModel.updateButtonTexts(
                                nextFragment.placeTop, nextFragment.placeBottom
                            )
                            nextFragment.checkAndCreateNewFragment()
                        }
                        it.operationCompleted.removeObservers(viewLifecycleOwner)
                        it.operationCompletedStatus.value = false
                    }
                }
            }
        }
    }

    override fun onSwipeDown() {
        val activityViewmodel = (activity as PlaceDetailsActivity).viewModel
        if (activityViewmodel.placeDetailsItemList.isEmpty()){
            activityViewmodel.say(resources.getString(R.string.there_are_no_saved_places))
        }
        else {
            for (place in activityViewmodel.placeDetailsItemList) {
                (activity as PlaceDetailsActivity).viewModel.say(place.storeName, TextToSpeech.QUEUE_ADD)
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
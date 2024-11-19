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
import com.nlinterface.activities.PlaceDetailsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.STTInputType
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.PlaceDetailsViewModel
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class PlaceDetailsScreenBase : Fragment(), SwipeAction {

    lateinit var viewModel: PlaceDetailsViewModel

    private lateinit var topButton: MaterialButton
    private lateinit var bottomButton: MaterialButton

    val default: String by lazy {
        resources.getString(R.string.add_a_place)
    }
    lateinit var placeTop: String
    lateinit var placeBottom: String

    /**
     * Companion object used for fragment creation. Assures a dynamically created fragment comprises
     * the variables item top and item bottom and handles their initialization.
     */
    companion object {
        private const val ARG_PLACE_TOP = "place_top"
        private const val ARG_PLACE_BOTTOM = "place_bottom"
        fun newInstance(placeTop: String, placeBottom: String): PlaceDetailsScreenBase {
            val fragment = PlaceDetailsScreenBase()
            val args = Bundle()
            args.putString(ARG_PLACE_TOP, placeTop)
            args.putString(ARG_PLACE_BOTTOM, placeBottom)
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
            placeTop = savedInstanceState.getString(ARG_PLACE_TOP, default)
            placeBottom = savedInstanceState.getString(ARG_PLACE_BOTTOM, default)
        } else {
            arguments.let {
                if (it != null) {
                    placeTop = it.getString(ARG_PLACE_TOP)!!
                }
                if (it != null) {
                    placeBottom = it.getString(ARG_PLACE_BOTTOM)!!
                }
            }
            Log.i("PlaceDetailsScreenBase", "placeTop: $placeTop, placeBottom: $placeBottom")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.place_details_screen_base, container, false)
        topButton = view.findViewById(R.id.option_top)
        bottomButton = view.findViewById(R.id.option_bottom)

        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))

        viewModel = ViewModelProvider(this)[PlaceDetailsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topButton.text = placeTop
        bottomButton.text = placeBottom

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
        outState.putString(ARG_PLACE_TOP, placeTop)
        outState.putString(ARG_PLACE_BOTTOM, placeBottom)
    }

    /**
     * Updates the variables if needed. Requires the user to switch the fragment once again for
     * the UI to be updated.
     *
     * TODO: FInd a way to instantly update UI that does not require to enter the fragment again
     */

    override fun onResume() {
        super.onResume()
        (activity as? PlaceDetailsActivity)?.let {
            if (it.findPlaceByName(placeTop) == null) {
                placeTop = default
                viewModel.updateButtonTexts(placeTop, placeBottom)
            }
            if (it.findPlaceByName(placeBottom) == null) {
                placeBottom = default
                viewModel.updateButtonTexts(placeTop, placeBottom)
            }
        }
    }



    /**
     * Implements functionalities on swipe inputs. onSwipeLeft and onSwipeRight are handled by the
     * viewPager and therefore are empty.
     */

    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}

    /**
     * Swiping up opens up a text input allowing to add a place to favorites. It is
     * automatically added to the next open spot in the last fragment. If the last fragments
     * variables are set, a new fragment is created
     *
     * TODO: Add place to the next free slot and not only in the last fragment.
     */
    override fun onSwipeUp() {
        if(placeTop == default){
            (activity as PlaceDetailsActivity).let {
                it.startPlaceSearch()
                it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                    if (isCompleted) {
                        Log.d("AddingPlace", "Observer check")
                        placeTop = it.viewModel.placeDetailsItemList.last().storeName
                        viewModel.updateButtonTexts(placeTop, placeBottom)
                        checkAndCreateNewFragment()
                        it.operationCompleted.removeObservers(viewLifecycleOwner)
                        it.operationCompletedStatus.value = false
                    }
                }
            }
        }
        else{
            launchPlaceOptions(placeTop)
            viewModel.say(placeTop)
        }
    }

    /**
     * Swiping down opens up a text input allowing to add a place to favorites. It is
     * automatically added to the next open spot in the last fragment. If the last fragments
     * variables are set, a new fragment is created
     *
     * TODO: Add place to the next free slot and not only in the last fragment.
     */
    override fun onSwipeDown() {
        if(placeBottom == default){
            (activity as PlaceDetailsActivity).let {
                it.startPlaceSearch()
                it.operationCompleted.observe(viewLifecycleOwner) { isCompleted ->
                    if (isCompleted) {
                        Log.d("AddingPlace", "Observer check")
                        placeBottom = it.viewModel.placeDetailsItemList.last().storeName
                        viewModel.updateButtonTexts(placeTop, placeBottom)
                        checkAndCreateNewFragment()
                        it.operationCompleted.removeObservers(viewLifecycleOwner)
                        it.operationCompletedStatus.value = false
                    }
                }
            }
        }
        else{
            launchPlaceOptions(placeBottom)
            viewModel.say(placeBottom)
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
        return if (placeTop != default  && placeBottom != default) {
            TRUE
        } else {
            FALSE
        }
    }

    fun checkAndCreateNewFragment() {
        if (areVariablesSet()) {
            (activity as PlaceDetailsActivity).addNewFragment(
                default, default
            )
            Log.println(Log.INFO, "PlacesFragment", "New Fragment created")
        }
    }

    private fun launchPlaceOptions(correspondingPlace: String) {
        val placeOptions = PlaceOptionsScreen(correspondingPlace)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, placeOptions)
            .addToBackStack(null)
            .commit()
    }
}
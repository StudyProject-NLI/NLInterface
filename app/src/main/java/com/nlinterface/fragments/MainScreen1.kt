package com.nlinterface.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nlinterface.R
import com.nlinterface.activities.ClassificationActivity
import com.nlinterface.activities.SettingsActivity
import com.nlinterface.activities.VoiceOnlyActivity
import com.nlinterface.utility.SwipeAction
import com.nlinterface.utility.SwipeNavigationListener
import com.nlinterface.viewmodels.MainViewModel

class MainScreen1 : Fragment(), SwipeAction {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_screen1, container, false)
        view.setOnTouchListener(SwipeNavigationListener(requireContext(), this))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the shared ViewModel
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onSwipeLeft() {
        findNavController().navigate(R.id.Main1_to_Main2)
    }

    override fun onSwipeRight() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onSwipeUp() {
        val intent = Intent(activity, ClassificationActivity::class.java)
        startActivity(intent)
    }

    override fun onSwipeDown() {
        val intent = Intent(activity, VoiceOnlyActivity::class.java)
        startActivity(intent)
    }

    override fun onDoubleTap() {

    }

    override fun onLongPress() {
        if (viewModel.isListening.value == false) {
            viewModel.handleSpeechBegin()
        } else {
            viewModel.cancelListening()
        }
    }
}
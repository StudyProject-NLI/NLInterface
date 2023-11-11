package com.nlinterface.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    val ttsInitialized: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}
package com.nlinterface.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.nlinterface.R

class SettingsActivity : AppCompatActivity() {

    // language: English, German
    // colors (e.g. for red-green impairment)
    // degree of blindness (e.g. if fully blind, more accessible and blind/seeing modes)
    // mode of voice control: button or hands-free

    private var languageDropDown : Spinner? = null
    private var dropDownAdapter : ArrayAdapter<CharSequence>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        languageDropDown = findViewById(R.id.language_dropdown)
        dropDownAdapter = ArrayAdapter.createFromResource(this, R.array.language_options_EN, android.R.layout.simple_spinner_item)
        dropDownAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_item)

        languageDropDown!!.setAdapter(dropDownAdapter)

    }


    }
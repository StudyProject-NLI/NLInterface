package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.databinding.ActivityNextActivityExampleBinding
import com.nlinterface.utility.GlobalParameters

class NextActivityExample : AppCompatActivity() {

    private lateinit var binding: ActivityNextActivityExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityNextActivityExampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // process keep screen on settings
        if (GlobalParameters.instance!!.keepScreenOnSwitch) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
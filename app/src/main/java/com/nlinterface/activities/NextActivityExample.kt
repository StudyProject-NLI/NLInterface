package com.nlinterface.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.nlinterface.databinding.ActivityMainBinding
import com.nlinterface.databinding.ActivityNextActivityExampleBinding

class NextActivityExample : AppCompatActivity() {

    private lateinit var binding: ActivityNextActivityExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityNextActivityExampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
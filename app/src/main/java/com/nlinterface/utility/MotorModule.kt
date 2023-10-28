package com.nlinterface.utility

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nlinterface.R

class MotorModule: AppCompatActivity() {
    private lateinit var angleInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var directionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        angleInput = findViewById(R.id.angleInput)
        calculateButton = findViewById(R.id.calculateButton)
        directionText = findViewById(R.id.directionText)

        calculateButton.setOnClickListener {
            val angleString = angleInput.text.toString()
            if (angleString.isNotEmpty()) {
                val angle = angleString.toDouble()
                val direction = calculateDirection(angle)
                directionText.text = direction
            }
        }
    }

    private fun calculateDirection(angle: Double): String {
        return when {
            angle >= 0 && angle < 90 -> "Right"
            angle >= 90 && angle < 180 -> "Left"
            angle >= 180 && angle < 270 -> "Up"
            angle >= 270 && angle < 360 -> "Right"
            else -> "Invalid angle"
        }
    }
}

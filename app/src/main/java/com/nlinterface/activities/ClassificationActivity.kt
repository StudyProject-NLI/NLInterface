package com.nlinterface.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.ContentValues.TAG
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import com.nlinterface.databinding.ActivityClassificationBinding
import com.nlinterface.databinding.FragmentCameraBinding
import com.nlinterface.utility.ObjectDetectorHelper

class ClassificationActivity: AppCompatActivity() {

    private lateinit var viewBinding: ActivityClassificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 0
            )
        }

        viewBinding = ActivityClassificationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED


}
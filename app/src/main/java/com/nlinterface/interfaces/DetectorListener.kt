package com.nlinterface.interfaces

import com.nlinterface.utility.Recognition
import org.tensorflow.lite.task.vision.detector.Detection

interface DetectorListener {
    fun onError(error: String)
    fun onResults(results: ArrayList<Recognition>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int)
}
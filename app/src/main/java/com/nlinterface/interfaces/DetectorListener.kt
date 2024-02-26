package com.nlinterface.interfaces

import org.tensorflow.lite.task.vision.detector.Detection

interface DetectorListener {
    fun onError(error: String)
    fun onResults(results: MutableList<Detection>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int)
}
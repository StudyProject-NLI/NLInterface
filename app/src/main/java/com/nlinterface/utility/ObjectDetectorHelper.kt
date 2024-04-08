package com.nlinterface.utility

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.nlinterface.interfaces.DetectorListener
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

/*
* Main class for performing object detection.
* - Configures and loads the model
* - Processes bitmap images and returns the results
* */

class ObjectDetectorHelper(
    val context: Context,
    val objectDetectorListener: DetectorListener?
) {
    private var objectDetector: Yolov5TFLiteDetector? = null

    init {
        setupObjectDetector()
    }

    fun clearObjectDetector() {
        objectDetector = null
    }

    fun setupObjectDetector() {
        try {
            val yolov5TFLiteDetector = Yolov5TFLiteDetector()
            yolov5TFLiteDetector.modelFile = "hand-fp16.tflite"
            yolov5TFLiteDetector.addGPUDelegate()
            yolov5TFLiteDetector.initialModel(context)
            objectDetector = yolov5TFLiteDetector

        } catch (e: IllegalStateException) {
            Log.e("NLI-Classification", "TFLite failed to load model with error: " + e.message)
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (objectDetector == null) {
            setupObjectDetector()
        }

        var inferenceTime = SystemClock.uptimeMillis()

        val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage.bitmap)

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        objectDetectorListener?.onResults(results, inferenceTime, tensorImage.height, tensorImage.width)
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_MOBILENETV1 = 0
    }

}
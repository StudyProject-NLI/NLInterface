package com.nlinterface.viewmodels


import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nlinterface.utility.GlobalParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Classes and functions managing a background Service, +
 * which is constantly checking for barcodes
 *
 */

/**
 * The Scanner is used to create a BarcodeScanner object,
 * analyze the imageProxy and check for barcodes,
 * and defines how to handle them.
 *
 *  @param viewModel: viewModel allows the use of the say function
 */
class Scanner(
    private val viewModel: MainViewModel,
    private val vibrator: Vibrator,
) : ImageAnalysis.Analyzer {

    /**
     * BarcodeScanner object: Currently only scans EAN 13 barcodes
     */
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13
        )
        .build()
    private val barcodeScanner = BarcodeScanning.getClient(options)


    /**
     * Analysis of the ImageProxy. Converts the camera output into analyzable format.
     * Applies the function handleBarcodeResult to the first barcode scanned.
     * This is embedded into a Thread to avoid to much calculations on the Main thread.
     *
     * @param image: uses the camera image without passing an argument directly
     *
     * todo : make it so only one barcode is scanned, and only after completion the next one
     *  can be scanned
     */
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {

        val mediaImage = image.image
        if (mediaImage != null) {
            val scannerInput =
                InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            barcodeScanner.process(scannerInput)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        Log.println(
                            Log.INFO,
                            "Scanner",
                            "Barcode " + barcode.rawValue + " was detected"
                        )
                        val urlAddOn = barcode.rawValue ?: "default"
                        Thread {
                            handleBarcodeResult(urlAddOn)
                        }.start()

                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }

    /**
     * Function to handle the Barcode Result.
     * Initiates a vibration to give feedback to the user, that a barcode was scanned
     * It creates an object BrowserSearch to use its function searchUrl
     *
     * @param urlAddOn: the Barcode as a String to use for web-search
     */
    private fun handleBarcodeResult(urlAddOn: String) {

        vibrator.vibrate(200)
        val eanSearch = BrowserSearch()
        Log.println(Log.INFO, "Scraping", "Waiting for Scraping result")
        eanSearch.searchUrl(viewModel, urlAddOn)
    }
}

/**
 * The BrowserSearch class embeds the web-search.
 *
 */
class BrowserSearch {
    val globalParameters = GlobalParameters.instance!!

    /**
     * The function to do the web-search and scraping.
     * Url: https://de.openfoodfacts.org/produkt/$barcode
     * searches the website with regards to the Barcodes value
     * Then scrapes the document for the specified elements and TTS the extracted text.
     *
     * @param viewModel: viewModel allows the use of the say function
     * @param barcode: the barcode as String to add on to the Url
     */
    fun searchUrl (viewModel:MainViewModel, barcode: String) {
        try {
            val searchUrl = "https://de.openfoodfacts.org/produkt/$barcode"
            val document: Document = Jsoup.connect(searchUrl).get()
            var allInfo = ""

            if(globalParameters.navState.ordinal == 0) {
                allInfo += document.select("h1.title-3").first()?.text()
            }
            if (globalParameters.labelsState.ordinal == 0) {
                allInfo += document.getElementById("field_labels")?.text()
            }
            if (globalParameters.cooState.ordinal == 0) {
                allInfo += document.getElementById("field_origins")?.text()
            }
            if(globalParameters.iaaState.ordinal == 0) {
                allInfo += document.getElementById("panel_ingredients_content")?.text()
            }
            if(globalParameters.snvState.ordinal == 0) {
                allInfo += document.select("h4.evaluation__title").text()
            }
            if(globalParameters.brandsState.ordinal == 0) {
                allInfo += document.getElementById("field_brands")?.text()
            }

            viewModel.say(allInfo)
        }   catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
/**
 * The Scanning process handles the camera and initiates the imageAnalysis.
 *
 */

class ScanningProcess{

    private lateinit var cameraProvider: ProcessCameraProvider

    /**
     * Function starting the scanning process.
     * Selects the camera to be used, initializes an ImageAnalysis object,
     * from which to use the scanner method.
     * Also binds the camera to a Lifecycle
     *
     * @param viewModel: passed on to allow using the say function
     * @param context: context for the cameraProvider and the imageAnalyzes
     *
     *
     */
    fun activateScanning(viewModel: MainViewModel, vibrator: Vibrator, context: Context) {

        val selector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            Scanner(viewModel, vibrator)
        )
        cameraProvider = ProcessCameraProvider.getInstance(context).get()
        try {
            cameraProvider.bindToLifecycle(
                ProcessLifecycleOwner.get(),
                selector,
                imageAnalysis
            )
            Log.println(Log.INFO, "Camera", "Camera binding successful")

        } catch (e: Exception) {
            e.printStackTrace().toString()
        }
    }

    fun cleanup(){
        cameraProvider.unbindAll()
    }


}

/**
 * ConstantScanning is a Service,
 * meaning it is allowed to run in the background parallel to the other activity.
 * Also it runs without an UI, which is wanted in the case of the Scanner
 */

class ConstantScanning: Service() {

    lateinit var viewModel: MainViewModel
    /**
     * Configuring a BroadcastReceiver, that allows the user to use the STT utility
     * of the current activity to stop the speech about the information of a product.
     *
     * todo: MAYBE make it that tts really shuts down and is not just overwrite
     *  with empty text
     */
    private val cameraBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if ("BarcodeInfo_Stop" == intent.action) {
                val stopSpeech = intent.getBooleanExtra("stop_speech", false)
                if (stopSpeech) {
                    viewModel.say("")
                    Log.println(Log.INFO, "TTS", "Stop TTS")
                }
            }
        }
    }
    private val filter = IntentFilter("BarcodeInfo_Stop")

    private var scanner = ScanningProcess()

    /**
     * This method is required by the Service architecture,
     * but not needed because the scanning should be done constantly.
     * Therefore it just return null
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Function that manages the Service, once initialized.
     * It creates a viewModel object to be passed on, so the say method can be used later on.
     * It also creates a Vibrator and Scanner object and starts the Scanning process.
     * Returns Start-Sticky to ensure it will try to start again,
     * if it is destroyed for whatever reason
     *
     * @param intent
     * @param flags
     * @param startId
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val application = application
        viewModel = MainViewModel(application)

        viewModel.initTTS()
        registerReceiver(cameraBroadcastReceiver, filter)

        val vibrator =  getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        scanner.activateScanning(viewModel, vibrator, this)
        Log.println(Log.INFO, "Scanner","Barcode Scanning Service is Active")

        return START_NOT_STICKY
    }

    /**
     * When service is terminated camera binds are removed and resources are cleared.
     */

    override fun onDestroy() {

        scanner.cleanup()
        Log.i("ConstantScanning", "Barcode Scanning Service destroyed")
        unregisterReceiver(cameraBroadcastReceiver)
        super.onDestroy()
    }
}

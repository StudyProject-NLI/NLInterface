package com.nlinterface.viewmodels


import android.app.Service
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ProcessLifecycleOwner
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Classes and functions managing a background Service, +
 * which is constantly checking for barcodes
 *
 * TODO: Add voice command allowing to stop TTS
 */

/**
 * The Scanner is used to create a BarcodeScanner object,
 * analyze the imageProxy and check for barcodes,
 * and defines how to handle them.
 *
 *  @param viewModel: viewModel allows the use of the say function
 */
class Scanner(
    private val viewModel: MainViewModel
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
     * TODO: potentially add sound feedback on scanned barcode,
     * TODO: hence the scraping and verbal feedback is a bit delayed
     *
     */
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val scannerInput =
                InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            barcodeScanner.process(scannerInput)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val urlAddOn = barcode.rawValue ?: "default"
                        Thread {
                            handleBarcodeResult(urlAddOn)
                        }.start()
                        break
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
                .addOnCompleteListener { image.close() }
        }
    }

    /**
     * Function to handle the Barcode Result.
     * It creates an object BrowserSearch to use its function searchUrl
     *
     * @param urlAddOn: the Barcode as a String to use for web-search
     */
    private fun handleBarcodeResult(urlAddOn: String) {
        // Handle the barcode result logic here
        val eanSearch = BrowserSearch()
        eanSearch.searchUrl(viewModel, urlAddOn)

    }
}

/**
 * The BrowserSearch class embeds the web-search.
 *
 */
class BrowserSearch{

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
            val name = document.select("h1.title-3").first()?.text()
            val ingredients =
                document.getElementById("panel_ingredients_content")?.text()
            val allInfo = name.toString() + ingredients.toString()
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

    /**
     * Function starting the scanning process.
     * Selects the camera to be used, initializes an ImageAnalysis object,
     * from which to use the scanner method.
     * Also binds the camera to a Lifecycle
     *
     * @param viewModel: passed on to allow using the say function
     * @param context: context for the cameraProvider and the imageAnalyzes
     *
     *  TODO: Change selector to other camera
     */
    fun activateScanning(viewModel: MainViewModel, context: Context) {

        val selector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            Scanner(viewModel)
        )
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        try {
            cameraProvider.bindToLifecycle(
                ProcessLifecycleOwner.get(),
                selector,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * ConstantScanning is a Service,
 * meaning it is allowed to run in the background parallel to the other activity.
 * Also it runs without an UI, which is wanted in the case of the Scanner
 */

class ConstantScanning: Service() {

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
     * It also creates a Scanner object and starts the Scanning process.
     * Returns Start-Sticky to ensure it will try to start again,
     * if it is destroyed for whatever reason
     *
     * @param intent
     * @param flags
     * @param startId
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val application = application
        val viewModel = MainViewModel(application)
        viewModel.initTTS()
        val scanner = ScanningProcess()
        scanner.activateScanning(viewModel, this)

        return START_STICKY
    }

}

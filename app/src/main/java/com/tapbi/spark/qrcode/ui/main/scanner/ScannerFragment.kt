package com.tapbi.spark.qrcode.ui.main.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.FragmentScannerBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.MainActivity
import com.tapbi.spark.qrcode.utils.*
import timber.log.Timber
import java.util.concurrent.Executors

class ScannerFragment : BaseBindingFragment<FragmentScannerBinding, ScannerViewModel>() {
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var toneGenerator: ToneGenerator? = null
    private lateinit var barcodeScanner: BarcodeScanner

    //    private var isScanImageFromGallery = false
    private var isScanSuccess = false

    //    private var uri: Uri? = null
    private var isFlashOn = false

    override fun getViewModel(): Class<ScannerViewModel> {
        return ScannerViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_scanner

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().isDarkThemeOn()) {
            requireActivity().window.statusBarColor = Color.WHITE
        }
        if ((activity as MainActivity).isCameraPermissionGranted) {
            bindCameraUseCases()
        } else if ((activity as MainActivity).isFirstResume && !(activity as MainActivity).isCameraPermissionGranted) {
            requestPermissionCameraLauncher.launch(Manifest.permission.CAMERA)
        } else if ((activity as MainActivity).isFirstResume && !(activity as MainActivity).isCameraPermissionGranted && shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            )
        ) {
            (activity as MainActivity).showDialogRequestPermissionAgain()
        } else if ((activity as MainActivity).isFirstResume && !(activity as MainActivity).isCameraPermissionGranted && !shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            )
        ) {
            (activity as MainActivity).showDialogGoToSetting()
        }
    }

    override fun onPermissionGranted() {

    }

    override fun initView() {
        toneGenerator = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            Timber.e("giangledinh ToneGenerator ${e.printStackTrace()}")
            e.printStackTrace()
            null
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProvider = try {
            ProcessCameraProvider.getInstance(requireContext()).get()
        } catch (e: Exception) {
            null
        }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()

        barcodeScanner = BarcodeScanning.getClient(options)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toneGenerator?.release()
        toneGenerator = null
    }

    override fun evenClick() {
        binding.ivGallery.setOnClickListener {
            if (checkTime(it))
                openGallery()
        }

        binding.ivFlash.setOnClickListener {
            if (checkTime(it))
                getCamera()?.let { camera ->
                    if (camera.cameraInfo.hasFlashUnit()) {
                        if (isFlashOn) {
                            turnOnCamera(
                                isFlashOn = false,
                                imageResource = R.drawable.flash_off
                            )
                        } else {
                            turnOnCamera(
                                isFlashOn = true,
                                imageResource = R.drawable.flash_on
                            )
                        }
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        turnOnCamera(isFlashOn = false, imageResource = R.drawable.flash_off)
        cameraProvider?.unbindAll()
        (activity as MainActivity).isFirstResume = false
    }

    private fun turnOnCamera(isFlashOn: Boolean, imageResource: Int) {
        getCamera()?.cameraControl?.enableTorch(isFlashOn)
        this.isFlashOn = isFlashOn
        binding.ivFlash.setImageResource(imageResource)
    }

    override fun observerData() {
        mainViewModel.isInsertScanResultDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val bundle = Bundle()
                bundle.putInt(Constant.SCAN_RESULT_ID, it.toInt())
                navigationToFragmentWithAnimation(R.id.scanResultFragment, bundle)
                if (sharedPreferenceHelper.getBoolean(Constant.IS_SOUND_0N, false))
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)

            }
        }

        mainViewModel.imageUriFromGallery.observe(viewLifecycleOwner) {
            it?.let {
                (activity as MainActivity).isScanImageFromGallery = true
                Timber.e("giangledinh isScanImageFromGallery")
                processImageProxy(barcodeScanner, null, it)
                mainViewModel.imageUriFromGallery.value = null
            }
        }
    }

    override fun initData() {

    }

    private fun openGallery() {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        mActivityResultLauncher.launch(
//            Intent.createChooser(
//                intent,
//                getString(R.string.select_picture)
//            )
//        )
        mainViewModel.isOpenGallery.postValue(true)
    }

//    private val mActivityResultLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            Timber.e("giangledinh mActivityResultLauncher")
//            if (it.resultCode == Activity.RESULT_OK) {
//                uri = it?.data?.data
//                isScanImageFromGallery = true
//                safeDelay(500L) {
//                    processImageProxy(barcodeScanner, null, uri)
//                }
//            } else {
//                showToast(requireContext(), getString(R.string.action_failed))
//            }
//        }

    private val requestPermissionCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                bindCameraUseCases()
            } else if (!result && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                (activity as MainActivity).showDialogRequestPermissionAgain()
            } else if (!result && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                (activity as MainActivity).showDialogGoToSetting()
            }
        }

    private fun bindCameraUseCases() {
        bindPreviewUseCase()
        bindAnalyseUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider?.unbind(previewUseCase)
        }

        binding.cameraPreview.let { previewView ->
            previewView.display?.let {
                previewUseCase = Preview.Builder()
                    .setTargetRotation(it.rotation)
                    .build()
            }
            previewUseCase?.setSurfaceProvider(previewView.surfaceProvider)
        }
        try {
            previewUseCase?.let { previewUseCase ->
                cameraSelector?.let {
                    cameraProvider?.bindToLifecycle(
                        viewLifecycleOwner,
                        it,
                        previewUseCase
                    )
                }
            }

        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.printStackTrace()
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
        }
    }

    private fun getCamera(): Camera? {
        return try {
            previewUseCase?.let {
                cameraProvider!!.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector!!,
                    it
                )
            }
        } catch (illegalStateException: IllegalStateException) {
            null
        } catch (illegalArgumentException: IllegalArgumentException) {
            null
        }
    }

    private fun bindAnalyseUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        binding.cameraPreview.display?.let {
            analysisUseCase = ImageAnalysis.Builder()
                .setTargetRotation(it.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        }

        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(
            cameraExecutor
        ) { imageProxy ->
            safeDelay(1000L) {
                processImageProxy(barcodeScanner, imageProxy, null)
            }
        }
        try {
            cameraSelector?.let { cameraSelector ->
                analysisUseCase?.let { analysisUseCase ->
                    cameraProvider?.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        analysisUseCase
                    )
                }
            }

        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.printStackTrace()
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy?,
        imageUri: Uri?
    ) {
        val inputImage = if (imageProxy != null)
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        else {
            if (imageUri != null) {
                InputImage.fromFilePath(requireContext(), imageUri)
            } else null
        }
        if (inputImage != null) {
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.forEach { barcode ->
                        if (!(activity as MainActivity).isScanned) {
                            when (barcode.valueType) {
                                Barcode.TYPE_WIFI -> {
                                    val ssid = barcode.wifi!!.ssid
                                    val password = barcode.wifi!!.password
                                    val type = barcode.wifi!!.encryptionType
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = ssid,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.wifi),
                                        title1 = getString(R.string.ssid),
                                        content1 = ssid,
                                        title2 = if (password.isNullOrEmpty()) null else getString(R.string.password),
                                        content2 = password,
                                        title3 = getString(R.string.encryption),
                                        content3 = when (type) {
                                            1 -> getString(R.string.none)
                                            2 -> getString(
                                                R.string.wpa_wpa2
                                            )
                                            else -> getString(R.string.wep)
                                        },
                                        functionName2 = getString(R.string.connect_to_wifi),
                                        functionName4 = getString(R.string.copy_password)
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }
                                Barcode.TYPE_URL -> {
                                    val url = barcode.url!!.url
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = barcode.displayValue,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.web),
                                        title1 = getString(R.string.url),
                                        content1 = url,
                                        functionName4 = getString(R.string.open_in_website)
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }

                                Barcode.TYPE_EMAIL -> {
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = barcode.displayValue,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.email, ""),
                                        title1 = getString(R.string.to),
                                        content1 = barcode.email!!.address,
                                        title2 = if (barcode.email!!.subject.isNullOrEmpty()) null else getString(
                                            R.string.subject
                                        ),
                                        content2 = barcode.email!!.subject,
                                        title3 = if (barcode.email!!.body.isNullOrEmpty()) null else getString(
                                            R.string.message,
                                            ":"
                                        ),
                                        content3 = barcode.email!!.body,
                                        functionName4 = getString(R.string.send_email),
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }

                                Barcode.TYPE_TEXT -> {
                                    if (barcode.rawValue!!.contains("geo:") && barcode.rawValue!!.contains(
                                            "?q"
                                        ) && barcode.rawValue!!.contains(
                                            ","
                                        )
                                    ) {
                                        val scanResult = ScanResult(
                                            barcodeFormat = barcode.format,
                                            rawValue = barcode.rawValue!!,
                                            displayValue = barcode.rawValue!!.substring(
                                                barcode.rawValue!!.indexOf(
                                                    "geo:"
                                                ) + 4, barcode.rawValue!!.indexOf(";")
                                            ),
                                            time = System.currentTimeMillis(),
                                            category = getString(R.string.location, ""),
                                            title1 = getString(R.string.latitude),
                                            content1 = barcode.rawValue!!.substring(
                                                barcode.rawValue!!.indexOf(
                                                    "geo:"
                                                ) + 4, barcode.rawValue!!.indexOf(",")
                                            ),
                                            title2 = getString(R.string.longitude),
                                            content2 = barcode.rawValue!!.substring(
                                                barcode.rawValue!!.indexOf(
                                                    ","
                                                ) + 1, barcode.rawValue!!.indexOf(";")
                                            ),
                                            functionName4 = getString(R.string.show_location_in_map)
                                        )
                                        navigateToFragmentScanResult(scanResult)
                                    } else {
                                        Timber.d("barcode_type_text")
                                        val scanResult = ScanResult(
                                            barcodeFormat = barcode.format,
                                            displayValue = barcode.displayValue,
                                            rawValue = barcode.rawValue!!,
                                            time = System.currentTimeMillis(),
                                            category = getString(R.string.text, ""),
                                            title1 = getString(R.string.text, ":"),
                                            content1 = barcode.rawValue,
                                            functionName4 = getString(R.string.copy_text)
                                        )
                                        navigateToFragmentScanResult(scanResult)
                                    }
                                }

                                Barcode.TYPE_PHONE -> {
                                    val phoneNumber = barcode.rawValue!!.replace("tel:", "")
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = barcode.displayValue,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.phone),
                                        title1 = getString(R.string.phone_number, ":"),
                                        content1 = phoneNumber,
                                        functionName2 = getString(R.string.call, phoneNumber),
                                        functionName3 = getString(R.string.send_sms),
                                        functionName4 = getString(R.string.add_contact),
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }

                                Barcode.TYPE_SMS -> {
                                    val phoneNumber = barcode.sms!!.phoneNumber!!
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = phoneNumber,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.message, ""),
                                        title1 = getString(R.string.phone_number, ":"),
                                        content1 = phoneNumber,
                                        title2 = getString(R.string.message, ":"),
                                        content2 = barcode.sms!!.message,
                                        functionName3 = getString(R.string.call, phoneNumber),
                                        functionName2 = getString(R.string.send_sms),
                                        functionName4 = getString(R.string.add_contact),
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }

                                Barcode.TYPE_CONTACT_INFO -> {
                                    val contactName = barcode.contactInfo!!.name!!.formattedName!!
                                    val phoneNumber =
                                        if (barcode.contactInfo!!.phones.isNotEmpty()) barcode.contactInfo!!.phones[0].number else ""
                                    val addresses = barcode.contactInfo!!.addresses
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = barcode.displayValue,
                                        rawValue = barcode.rawValue,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.contact),
                                        title1 = getString(R.string.name, ":"),
                                        content1 = contactName,
                                        title2 = if (barcode.contactInfo!!.emails.isNotEmpty()) getString(
                                            R.string.email,
                                            ":"
                                        ) else null,
                                        content2 = if (barcode.contactInfo!!.emails.isNotEmpty()) barcode.contactInfo!!.emails[0].address else "",
                                        title3 = if (addresses.isNotEmpty()) getString(R.string.address) else null,
                                        content3 = if (addresses.isNotEmpty()) addresses[0].addressLines[0] else "",
                                        title4 = getString(R.string.telephone),
                                        content4 = phoneNumber,
                                        functionName3 = getString(R.string.add_contact),
                                        functionName4 = getString(R.string.call, phoneNumber)
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }

                                Barcode.TYPE_CALENDAR_EVENT -> {
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        displayValue = barcode.displayValue,
                                        rawValue = barcode.rawValue!!,
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.event),
                                        title1 = getString(R.string.title),
                                        content1 = barcode.calendarEvent!!.summary,
                                        title2 = if (barcode.calendarEvent!!.location.isNullOrEmpty()) null else getString(
                                            R.string.location,
                                            ":"
                                        ),
                                        content2 = barcode.calendarEvent!!.location,
                                        title3 = if (barcode.calendarEvent!!.description.isNullOrEmpty()) null else getString(
                                            R.string.description
                                        ),
                                        content3 = barcode.calendarEvent!!.description,
                                        title4 = getString(R.string.start),
                                        content4 = formatCalendarDateTime(barcode.calendarEvent!!.start!!),
                                        title5 = getString(R.string.end),
                                        content5 = formatCalendarDateTime(barcode.calendarEvent!!.end!!),
                                        functionName4 = getString(R.string.add_event_to_calendar)
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }
                                Barcode.TYPE_PRODUCT -> {
                                    insertResult(barcode)
                                }
                                Barcode.TYPE_GEO -> {
                                    val scanResult = ScanResult(
                                        barcodeFormat = barcode.format,
                                        rawValue = barcode.rawValue!!,
                                        displayValue = barcode.rawValue!!.substring(
                                            barcode.rawValue!!.indexOf("geo:") + 4
                                        ),
                                        time = System.currentTimeMillis(),
                                        category = getString(R.string.location, ""),
                                        title1 = getString(R.string.latitude),
                                        content1 = barcode.rawValue!!.substring(
                                            barcode.rawValue!!.indexOf(
                                                "geo:"
                                            ) + 4, barcode.rawValue!!.indexOf(",")
                                        ),
                                        title2 = getString(R.string.longitude),
                                        content2 = barcode.rawValue!!.substring(
                                            barcode.rawValue!!.indexOf(",") + 1
                                        ),
                                        functionName4 = getString(R.string.show_location_in_map)
                                    )
                                    navigateToFragmentScanResult(scanResult)
                                }
                                Barcode.TYPE_ISBN -> {
                                    insertResult(barcode)
                                }
                            }
                            (activity as MainActivity).isScanned = true
                            isScanSuccess = true
                            (activity as MainActivity).isScanImageFromGallery = false
                        }
                    }
                }
                .addOnFailureListener {
                }
                .addOnCompleteListener {
                    // When the image is from CameraX analysis use case, must call image.close() on received
                    // images when finished using them. Otherwise, new images may not be received or the camera
                    // may stall.

                    if (!isScanSuccess && (activity as? MainActivity)?.isScanImageFromGallery == true) {
                        showToast(requireContext(), getString(R.string.no_barcode_detect))
                        (activity as MainActivity).isScanImageFromGallery = false
                    }
                    imageProxy?.close()
                    return@addOnCompleteListener
                }
        }
    }

    private fun insertResult(barcode: Barcode) {
        val scanResult = ScanResult(
            barcodeFormat = barcode.format,
            displayValue = barcode.displayValue,
            rawValue = barcode.rawValue!!,
            time = System.currentTimeMillis(),
            category = getString(R.string.product),
            title1 = getString(R.string.barcode),
            content1 = barcode.rawValue!!,
            functionName4 = getString(R.string.search)
        )
        navigateToFragmentScanResult(scanResult)
    }

    private fun navigateToFragmentScanResult(scanResult: ScanResult) {
        mainViewModel.insertScanResult(scanResult)
    }

}
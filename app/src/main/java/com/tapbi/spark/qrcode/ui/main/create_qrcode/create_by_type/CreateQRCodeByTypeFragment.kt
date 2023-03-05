package com.tapbi.spark.qrcode.ui.main.create_qrcode.create_by_type

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.*
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.MainActivity
import com.tapbi.spark.qrcode.ui.main.MainViewModel
import com.tapbi.spark.qrcode.ui.main.scan_result.ScanResultFragment.Companion.scanResultId
import com.tapbi.spark.qrcode.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.*


class CreateQRCodeByTypeFragment :
    BaseBindingFragment<FragmentCreateQrCodeByTypeBinding, MainViewModel>() {

    //region init variable
    private val qrCodeType get() = requireArguments().getString(Constant.QRC0DE_CREATE_TYPE, null)
    private val isFromScanResultFragment
        get() = requireArguments().getBoolean(
            Constant.FROM_SCAN_RESULT_FRAGMENT,
            false
        )
    private var wifiType: String = Constant.WPA
    private lateinit var dateStart: Calendar
    private lateinit var dateEnd: Calendar
    private var supportMapFragment: SupportMapFragment? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var map: GoogleMap? = null
    private val isFavorite get() = requireArguments().getBoolean(Constant.IS_FAVORITE, false)
    private var isFavorite2: Boolean? = null

    private var fragmentCreateContactBinding: FragmentCreateContactBinding? = null
    private var fragmentCreateEmailBinding: FragmentCreateEmailBinding? = null
    private var fragmentCreateEventBinding: FragmentCreateEventBinding? = null
    private var fragmentCreateLocationBinding: FragmentCreateLocationBinding? = null
    private var fragmentCreateMessageBinding: FragmentCreateMessageBinding? = null
    private var fragmentCreatePhoneBinding: FragmentCreatePhoneBinding? = null
    private var fragmentCreateTextBinding: FragmentCreateTextBinding? = null
    private var fragmentCreateWebsiteBinding: FragmentCreateWebsiteBinding? = null
    private var fragmentCreateWifiBinding: FragmentCreateWifiBinding? = null


    //endregion

    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_create_qr_code_by_type

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
        savedInstanceState?.let {
            scanResultId = it.getInt(Constant.SCAN_RESULT_ID)
        }
    }

    override fun onPermissionGranted() {

    }

    override fun onPause() {
        super.onPause()
        fragmentCreateLocationBinding?.searchView?.clearFocus()
    }

    override fun initView() {
        qrCodeType?.let { it ->
            when (it) {
                Constant.CONTACT -> {
                    fragmentCreateContactBinding =
                        FragmentCreateContactBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateContactBinding!!.root)
                    disableNestedScrollView(fragmentCreateContactBinding!!.edtAddress)
                    disableNestedScrollView(fragmentCreateContactBinding!!.edtName)
                    disableNestedScrollView(fragmentCreateContactBinding!!.edtPhoneNumber)
                    disableNestedScrollView(fragmentCreateContactBinding!!.edtEmail)
                }
                Constant.EMAIL -> {
                    fragmentCreateEmailBinding = FragmentCreateEmailBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateEmailBinding!!.root)
                    disableNestedScrollView(fragmentCreateEmailBinding!!.edtMessage)
                    disableNestedScrollView(fragmentCreateEmailBinding!!.edtSubject)
                    disableNestedScrollView(fragmentCreateEmailBinding!!.edtTo)
                }
                Constant.EVENT -> {
                    fragmentCreateEventBinding = FragmentCreateEventBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateEventBinding!!.root)
                    disableNestedScrollView(fragmentCreateEventBinding!!.edtDescription)
                    disableNestedScrollView(fragmentCreateEventBinding!!.edtLocation)
                    disableNestedScrollView(fragmentCreateEventBinding!!.edtTitle)
                }
                Constant.MESSAGE -> {
                    fragmentCreateMessageBinding =
                        FragmentCreateMessageBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateMessageBinding!!.root)
                }
                Constant.PHONE -> {
                    fragmentCreatePhoneBinding = FragmentCreatePhoneBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreatePhoneBinding!!.root)
                }
                Constant.TEXT -> {
                    fragmentCreateTextBinding = FragmentCreateTextBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateTextBinding!!.root)
                }
                Constant.WEBSITE -> {
                    fragmentCreateWebsiteBinding =
                        FragmentCreateWebsiteBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateWebsiteBinding!!.root)
                }
                Constant.WIFI -> {
                    fragmentCreateWifiBinding = FragmentCreateWifiBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateWifiBinding!!.root)
                }
                else -> {
                    fragmentCreateLocationBinding =
                        FragmentCreateLocationBinding.inflate(layoutInflater)
                    loadFragmentContent(fragmentCreateLocationBinding!!.root)
                    fragmentCreateLocationBinding!!.searchView.clearFocus()
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    supportMapFragment =
                        childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
                    supportMapFragment?.getMapAsync { googleMap ->
                        map = googleMap
                        googleMap.setOnMapClickListener { latLng ->
                            if (!(activity as MainActivity).isNetworkAvailable) {
                                showToast(
                                    requireContext(),
                                    getString(R.string.network_not_available)
                                )
                            } else {
                                val markerOptions = MarkerOptions()
                                markerOptions.position(latLng)
                                markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
                                googleMap.clear()
                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        latLng,
                                        10F
                                    )
                                )
                                googleMap.addMarker(markerOptions)
                                fragmentCreateLocationBinding!!.edtLatitude.setText(latLng.latitude.toString())
                                fragmentCreateLocationBinding!!.edtLongitude.setText(latLng.longitude.toString())
                                getAddress(latLng.latitude, latLng.longitude)?.let {
                                    fragmentCreateLocationBinding!!.searchView.setText(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAddress(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.isNotEmpty() && addresses[0].getAddressLine(0).isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else
                null
        } catch (e: IOException) {
            null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun evenClick() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (isKeyboardVisible(binding.root)) {
                binding.flContent.setPadding(0, 0, 0, getKeyboardHeight(binding.root) + 16)
            } else {
                binding.flContent.setPadding(0)
            }
        }

        binding.ivBack.setOnClickListener {
            if (checkTime(it)) {
                findNavController().popBackStack()
                requireActivity().hideKeyboard()
            }
        }

        binding.ivDone.setOnClickListener { view ->
            if (checkTime(view))
                qrCodeType?.let {
                    when (it) {
                        Constant.CONTACT -> createQrCodeContact()
                        Constant.EMAIL -> createQrCodeEmail()
                        Constant.EVENT -> createQrCodeEvent()
                        Constant.MESSAGE -> createQrCodeMessage()
                        Constant.PHONE -> createQrCodePhone()
                        Constant.TEXT -> createQrCodeText()
                        Constant.WEBSITE -> createQrCodeWebsite()
                        Constant.WIFI -> createQrCodeWifi()
                        else -> createQrCodeLocation()
                    }
                    requireActivity().hideKeyboard()
                }
        }

        chooseWifiType()

        fragmentCreateEventBinding?.let { binding ->
            binding.edtStartTime.setOnClickListener {
                if (checkTime(it)) {
                    dateStart = Calendar.getInstance()
                    if (binding.edtStartTime.text.toString().isNotEmpty()) {
                        showDateTimePicker(
                            binding.edtStartTime,
                            dateStart,
                            getDateFromString(binding.edtStartTime.text.toString())
                        )
                    } else {
                        showDateTimePicker(binding.edtStartTime, dateStart, dateStart)
                    }

                }
            }

            binding.edtEndTime.setOnClickListener {
                if (checkTime(it)) {
                    dateEnd = Calendar.getInstance()
                    if (binding.edtEndTime.text.toString().isNotEmpty()) {
                        showDateTimePicker(
                            binding.edtEndTime,
                            dateEnd,
                            getDateFromString(binding.edtEndTime.text.toString())
                        )
                    } else {
                        showDateTimePicker(binding.edtEndTime, dateEnd, dateEnd)
                    }
                }
            }
        }

        fragmentCreateLocationBinding?.let { binding ->
//            binding.searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    binding.cardViewMap.gone()
//                    binding.tvCurrentLocation.show()
//                } else {
//                    binding.tvCurrentLocation.gone()
//                    safeDelay(500L) {
//                        binding.cardViewMap.show()
//                    }
//                }
//            }

//            binding.tvCurrentLocation.setOnClickListener {
//                if (!(activity as MainActivity).isNetworkAvailable && !isGPSOn()) {
//                    showToast(requireContext(), getString(R.string.please_turn_on_network_and_gps))
//                } else if (!isGPSOn()) {
//                    showToast(requireContext(), getString(R.string.please_turn_on_gps))
//                } else if (!(activity as MainActivity).isNetworkAvailable) {
//                    showToast(requireContext(), getString(R.string.network_not_available))
//                } else if (checkTime(it))
//                    fetchLastLocation(binding)
//            }

            binding.edtLatitude.addTextChangedListener {
                it?.let {
                    binding.cancelLatidue.show(it.isNotEmpty())
                }
            }

            binding.edtLongitude.addTextChangedListener {
                it?.let {
                    binding.cancelLongitude.show(it.isNotEmpty())
                }
            }

            binding.searchView.addTextChangedListener {
                it?.let {
                    binding.cancelSearch.show(it.isNotEmpty())
                }
            }

            binding.cancelLatidue.setOnClickListener {
                binding.edtLatitude.setText("")
            }

            binding.cancelLongitude.setOnClickListener {
                binding.edtLongitude.setText("")
            }

            binding.cancelSearch.setOnClickListener {
                binding.searchView.setText("")
            }

            binding.edtLongitude.setOnEditorActionListener(onEditorActionListener(binding))
            binding.edtLatitude.setOnEditorActionListener(onEditorActionListener(binding))
            binding.searchView.setOnEditorActionListener { p0, p1, p2 ->
                if (p1 == EditorInfo.IME_ACTION_DONE) {
                    val location = binding.searchView.text.toString()
                    if(location.isBlank() or location.isEmpty()){
                        showToast(requireContext(),getString(R.string.invalid_location))
                    }
                    else{
                        try {
                            lifecycleScope.launch(Dispatchers.IO){
                                var addresses: List<Address>
                                Timber.e("giangledinh ${location.isBlank()}")
                                val geocoder = Geocoder(requireContext())
                                val temp = geocoder.getFromLocationName(location, 1)
                                withContext(Dispatchers.Main){
                                    addresses = temp
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        address.let {
                                            val latLng = LatLng(it.latitude, it.longitude)
                                            val markerOptions = MarkerOptions().position(latLng)
                                            markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
                                            map?.clear()
                                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                                            map?.addMarker(markerOptions)
                                            binding.edtLatitude.setText(it.latitude.toString())
                                            binding.edtLongitude.setText(it.longitude.toString())
                                            requireActivity().hideKeyboard()
                                            binding.searchView.clearFocus()
                                        }
                                    } else {
                                        showToast(requireContext(), getString(R.string.location_not_found))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }

        }


        fragmentCreatePhoneBinding?.let { binding ->
            binding.btnImport.setOnClickListener {
                if (checkTime(it))
                    checkPermissionReadContact()
            }
        }

        fragmentCreateEmailBinding?.let { binding ->
            binding.ivAddContact.setOnClickListener {
                if (checkTime(it))
                    checkPermissionReadContact()
            }
        }

        fragmentCreateMessageBinding?.let { binding ->
            binding.ivAddContact.setOnClickListener {
                if (checkTime(it))
                    checkPermissionReadContact()
            }
        }

        fragmentCreateContactBinding?.let { binding ->
            binding.btnImport.setOnClickListener {
                if (checkTime(it))
                    checkPermissionReadContact()
            }
        }

    }


    //region create qr code by type
    private fun createQrCodeWifi() {
        val ssid = fragmentCreateWifiBinding!!.edtSsid.text.toString().trim()
        val tempPassword =
            if (this.wifiType == Constant.NONE) "" else fragmentCreateWifiBinding!!.edtPassword.text.toString()
                .trim()
        val wifiType = if (this.wifiType == Constant.NONE) "" else ";T:${this.wifiType}"
        val password =
            if (tempPassword.isBlank() || tempPassword.isEmpty()) "" else ";P:$tempPassword"

        val rawValue =
            if (this.wifiType != Constant.NONE) "WIFI:S:$ssid$wifiType$password;;" else "WIFI:S:$ssid$wifiType;;"
        val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
        if (ssid.isNotEmpty() && ssid.isNotBlank()) {
            if (this.wifiType != Constant.NONE) {
                if (fragmentCreateWifiBinding!!.edtPassword.text.toString().trim().isEmpty()) {
                    showToast(requireContext(), getString(R.string.this_wifi_has_no_password))
                }
            }
            if (bitmap != null) {
                if (scanResultId == -1) {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = ssid,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.wifi),
                            title1 = getString(R.string.ssid),
                            content1 = ssid,
                            title2 = if (tempPassword.isEmpty() || tempPassword.isBlank()) null else getString(
                                R.string.password
                            ),
                            content2 = tempPassword,
                            title3 = getString(R.string.encryption),
                            content3 = if (this.wifiType == Constant.WPA) Constant.WPA_WPA2 else this.wifiType,
                            functionName2 = getString(R.string.connect_to_wifi),
                            functionName4 = getString(R.string.copy_password),
                            isQrCodeCreatedByUser = true
                        )
                    )
                } else {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            id = scanResultId,
                            isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = ssid,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.wifi),
                            title1 = getString(R.string.ssid),
                            content1 = ssid,
                            title2 = if (tempPassword.isEmpty() || tempPassword.isBlank()) null else getString(
                                R.string.password
                            ),
                            content2 = tempPassword,
                            title3 = getString(R.string.encryption),
                            content3 = if (this.wifiType == Constant.WPA) Constant.WPA_WPA2 else this.wifiType,
                            functionName2 = getString(R.string.connect_to_wifi),
                            functionName4 = getString(R.string.copy_password),
                            isQrCodeCreatedByUser = true
                        )
                    )
                }
            } else {
                showToast(requireContext(), getString(R.string.cannot_create_qrcode))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_ssid))
        }
    }

    private fun createQrCodeWebsite() {
        val rawValue = fragmentCreateWebsiteBinding!!.edtWebsite.text.toString().trim()
        val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
        if (Patterns.WEB_URL.matcher(rawValue).matches()) {
            if (bitmap != null) {
                if (scanResultId == -1) {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = rawValue,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.web),
                            title1 = getString(R.string.url),
                            content1 = rawValue,
                            functionName4 = getString(R.string.open_in_website),
                            isQrCodeCreatedByUser = true
                        )
                    )
                } else {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            id = scanResultId,
                            isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = rawValue,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.web),
                            title1 = getString(R.string.url),
                            content1 = rawValue,
                            functionName4 = getString(R.string.open_in_website),
                            isQrCodeCreatedByUser = true
                        )
                    )
                }
            } else {
                showToast(requireContext(), getString(R.string.cannot_create_qrcode))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_website))
        }
    }

    private fun createQrCodeText() {
        val rawValue = fragmentCreateTextBinding!!.edtText.text.toString().trim()
        val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
        if (rawValue.isNotBlank() && rawValue.isNotEmpty()) {
            if (bitmap != null) {
                if (scanResultId == -1) {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = rawValue,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.text, ""),
                            title1 = getString(R.string.text, ":"),
                            content1 = rawValue,
                            functionName4 = getString(R.string.copy_text),
                            isQrCodeCreatedByUser = true
                        )
                    )
                } else {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            id = scanResultId,
                            isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = rawValue,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.text, ""),
                            title1 = getString(R.string.text, ":"),
                            content1 = rawValue,
                            functionName4 = getString(R.string.copy_text),
                            isQrCodeCreatedByUser = true
                        )
                    )
                }
            } else {
                showToast(requireContext(), getString(R.string.cannot_create_qrcode))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_text))
        }

    }

    private fun createQrCodePhone() {
        val phoneNumber = fragmentCreatePhoneBinding!!.edtPhoneNumber.text.toString().trim()
        val rawValue = "tel:$phoneNumber"
        val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
        if (checkPhoneNumber(phoneNumber)) {
            if (bitmap != null) {
                if (scanResultId == -1) {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = phoneNumber,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.phone),
                            title1 = getString(R.string.phone_number, ":"),
                            content1 = phoneNumber,
                            functionName2 = getString(R.string.call, phoneNumber),
                            functionName3 = getString(R.string.send_sms),
                            functionName4 = getString(R.string.add_contact),
                            isQrCodeCreatedByUser = true
                        )
                    )
                } else {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            id = scanResultId,
                            isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = phoneNumber,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.phone),
                            title1 = getString(R.string.phone_number, ":"),
                            content1 = phoneNumber,
                            functionName2 = getString(R.string.call, phoneNumber),
                            functionName3 = getString(R.string.send_sms),
                            functionName4 = getString(R.string.add_contact),
                            isQrCodeCreatedByUser = true
                        )
                    )
                }
            } else {
                showToast(requireContext(), getString(R.string.cannot_create_qrcode))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_phone_number))
        }
    }

    private fun createQrCodeMessage() {
        val address = fragmentCreateMessageBinding!!.edtTo.text.toString().trim()
        val message = fragmentCreateMessageBinding!!.edtMessage.text.toString().trim()
        val rawValue = "SMSTO:$address:$message"
        val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
        if (checkPhoneNumber(address)) {
            if (message.isNotBlank() && message.isNotEmpty()) {
                if (bitmap != null) {
                    if (scanResultId == -1) {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = address,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.message, ""),
                                title1 = getString(R.string.phone_number, ":"),
                                content1 = address,
                                title2 = getString(R.string.message, ":"),
                                content2 = message,
                                functionName3 = getString(R.string.call, address),
                                functionName2 = getString(R.string.send_sms),
                                functionName4 = getString(R.string.add_contact),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    } else {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                id = scanResultId,
                                isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = address,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.message, ""),
                                title1 = getString(R.string.phone_number, ":"),
                                content1 = address,
                                title2 = getString(R.string.message, ":"),
                                content2 = message,
                                functionName3 = getString(R.string.call, address),
                                functionName2 = getString(R.string.send_sms),
                                functionName4 = getString(R.string.add_contact),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    }
                } else {
                    showToast(requireContext(), getString(R.string.cannot_create_qrcode))
                }
            } else {
                showToast(requireContext(), getString(R.string.invalid_text))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_phone_number))
        }
    }

    private fun createQrCodeEvent() {
        val title = fragmentCreateEventBinding!!.edtTitle.text.toString().trim()
        val location = fragmentCreateEventBinding!!.edtLocation.text.toString().trim()
        val description = fragmentCreateEventBinding!!.edtDescription.text.toString().trim()
        val starTime = fragmentCreateEventBinding!!.edtStartTime.text.toString().trim()
        val endTime = fragmentCreateEventBinding!!.edtEndTime.text.toString().trim()

        if (title.isEmpty() || starTime.isEmpty() || endTime.isEmpty()) {
            showToast(requireContext(), getString(R.string.filed_required))
        } else {
            dateStart = getDateFromString(starTime)
            dateEnd = getDateFromString(endTime)
            if (dateEnd.timeInMillis < dateStart.timeInMillis) {
                showToast(
                    requireContext(),
                    getString(R.string.the_end_time_must_be_greater_than_the_start_time)
                )
            } else {
                val rawValue = "BEGIN:VEVENT\n" +
                        "LOCATION:$location\n" +
                        "DTSTART:${getCalendarDateTimeFormat(this.dateStart)}\n" +
                        "DTSTART:${getCalendarDateTimeFormat(this.dateStart)}\n" +
                        "DTEND:${getCalendarDateTimeFormat(this.dateEnd)}\n" +
                        "SUMMARY:$title\n" +
                        "DESCRIPTION:$description\n" +
                        "END:VEVENT"
                val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
                if (bitmap != null) {
                    if (scanResultId == -1) {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = title,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.event),
                                title1 = getString(R.string.title),
                                content1 = title,
                                title2 = if (location.isEmpty() || location.isBlank()) null else getString(
                                    R.string.location,
                                    ":"
                                ),
                                content2 = location,
                                title3 = if (description.isEmpty() || location.isBlank()) null else getString(
                                    R.string.description
                                ),
                                content3 = description,
                                title4 = getString(R.string.start),
                                content4 = starTime,
                                title5 = getString(R.string.end),
                                content5 = endTime,
                                functionName4 = getString(R.string.add_event_to_calendar),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    } else {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                id = scanResultId,
                                isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = title,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.event),
                                title1 = getString(R.string.title),
                                content1 = title,
                                title2 = if (location.isEmpty() || location.isBlank()) null else getString(
                                    R.string.location,
                                    ":"
                                ),
                                content2 = location,
                                title3 = if (description.isEmpty() || location.isBlank()) null else getString(
                                    R.string.description
                                ),
                                content3 = description,
                                title4 = getString(R.string.start),
                                content4 = starTime,
                                title5 = getString(R.string.end),
                                content5 = endTime,
                                functionName4 = getString(R.string.add_event_to_calendar),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    }
                } else {
                    showToast(requireContext(), getString(R.string.cannot_create_qrcode))
                }
            }
        }
    }

    private fun createQrCodeEmail() {
        val email = fragmentCreateEmailBinding!!.edtTo.text.toString().trim()
        val subject = fragmentCreateEmailBinding!!.edtSubject.text.toString().trim()
        val message = fragmentCreateEmailBinding!!.edtMessage.text.toString().trim()
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            val rawValue = "MATMSG:TO:$email;SUB:$subject;BODY:$message;;"
            val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
            if (bitmap != null) {
                if (scanResultId == -1) {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = email,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.email, ""),
                            title1 = getString(R.string.to),
                            content1 = email,
                            title2 = if (subject.isBlank() || subject.isEmpty()) null else getString(
                                R.string.subject
                            ),
                            content2 = subject,
                            title3 = if (message.isEmpty() || message.isBlank()) null else getString(
                                R.string.message,
                                ":"
                            ),
                            content3 = message,
                            functionName4 = getString(R.string.send_email),
                            isQrCodeCreatedByUser = true
                        )
                    )
                } else {
                    mainViewModel.insertScanResult(
                        ScanResult(
                            id = scanResultId,
                            isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                            barcodeFormat = Barcode.FORMAT_QR_CODE,
                            displayValue = email,
                            rawValue = rawValue,
                            time = System.currentTimeMillis(),
                            category = getString(R.string.email, ""),
                            title1 = getString(R.string.to),
                            content1 = email,
                            title2 = if (subject.isBlank() || subject.isEmpty()) null else getString(
                                R.string.subject
                            ),
                            content2 = subject,
                            title3 = if (message.isEmpty() || message.isBlank()) null else getString(
                                R.string.message,
                                ":"
                            ),
                            content3 = message,
                            functionName4 = getString(R.string.send_email),
                            isQrCodeCreatedByUser = true
                        )
                    )
                }
            } else {
                showToast(requireContext(), getString(R.string.cannot_create_qrcode))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_email))
        }
    }

    private fun createQrCodeContact() {
        val contactName = fragmentCreateContactBinding!!.edtName.text.toString().trim()
        val email = fragmentCreateContactBinding!!.edtEmail.text.toString().trim()
        val address = fragmentCreateContactBinding!!.edtAddress.text.toString().trim()
        val phoneNumber = fragmentCreateContactBinding!!.edtPhoneNumber.text.toString().trim()
        if (contactName.isNotEmpty() && contactName.isNotBlank()) {
            if (checkPhoneNumber(phoneNumber)) {
                val rawValue = "BEGIN:VCARD\n" +
                        "FN:$contactName \n" +
                        "ADR:;;;$address;;;\n" +
                        "TEL;CELL:$phoneNumber\n" +
                        "EMAIL;WORK;INTERNET:$email\n" +
                        "END:VCARD"
                val bitmap = getImageBitmap(rawValue, Barcode.FORMAT_QR_CODE)
                if (bitmap != null) {
                    if (scanResultId == -1) {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = contactName,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.contact),
                                title1 = getString(R.string.name, ":"),
                                content1 = contactName,
                                title2 = if (email.isEmpty() || email.isBlank()) null else getString(
                                    R.string.email,
                                    ":"
                                ),
                                content2 = email,
                                title3 = if (address.isEmpty() || address.isBlank()) null else getString(
                                    R.string.address
                                ),
                                content3 = address,
                                title4 = getString(R.string.telephone),
                                content4 = phoneNumber,
                                functionName3 = getString(R.string.add_contact),
                                functionName4 = getString(R.string.call, phoneNumber),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    } else {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                id = scanResultId,
                                isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                displayValue = contactName,
                                rawValue = rawValue,
                                time = System.currentTimeMillis(),
                                category = getString(R.string.contact),
                                title1 = getString(R.string.name, ":"),
                                content1 = contactName,
                                title2 = if (email.isEmpty() || email.isBlank()) null else getString(
                                    R.string.email,
                                    ":"
                                ),
                                content2 = email,
                                title3 = if (address.isEmpty() || address.isBlank()) null else getString(
                                    R.string.address
                                ),
                                content3 = address,
                                title4 = getString(R.string.telephone),
                                content4 = phoneNumber,
                                functionName3 = getString(R.string.add_contact),
                                functionName4 = getString(R.string.call, phoneNumber),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    }
                } else {
                    showToast(requireContext(), getString(R.string.cannot_create_qrcode))
                }
            } else {
                showToast(requireContext(), getString(R.string.invalid_phone_number))
            }
        } else {
            showToast(requireContext(), getString(R.string.enter_contact_name))
        }
    }

    private fun createQrCodeLocation() {
        val latitude = try {
            fragmentCreateLocationBinding!!.edtLatitude.text.toString().trim().toDouble()
        } catch (e: Exception) {
            null
        }
        val longitude = try {
            fragmentCreateLocationBinding!!.edtLongitude.text.toString().trim().toDouble()
        } catch (e: Exception) {
            null
        }

        if (latitude != null && longitude != null) {
            if (-90.0 <= latitude.toDouble() && latitude.toDouble() <= 90.0) {
                if (-180.0 <= longitude.toDouble() && longitude.toDouble() <= 180.0) {
                    val rawValue = "geo:$latitude,$longitude"
                    if (scanResultId == -1) {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                rawValue = rawValue,
                                displayValue = "$latitude,$longitude",
                                time = System.currentTimeMillis(),
                                category = getString(R.string.location, ""),
                                title1 = getString(R.string.latitude),
                                content1 = latitude.toString(),
                                title2 = getString(R.string.longitude),
                                content2 = longitude.toString(),
                                functionName4 = getString(R.string.show_location_in_map),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    } else {
                        mainViewModel.insertScanResult(
                            ScanResult(
                                id = scanResultId,
                                isFavorite = if (isFavorite2 != null) isFavorite2!! else isFavorite,
                                barcodeFormat = Barcode.FORMAT_QR_CODE,
                                rawValue = rawValue,
                                displayValue = "$latitude,$longitude",
                                time = System.currentTimeMillis(),
                                category = getString(R.string.location, ""),
                                title1 = getString(R.string.latitude),
                                content1 = latitude.toString(),
                                title2 = getString(R.string.longitude),
                                content2 = longitude.toString(),
                                functionName4 = getString(R.string.show_location_in_map),
                                isQrCodeCreatedByUser = true
                            )
                        )
                    }
                } else {
                    showToast(requireContext(), getString(R.string.invalid_longitude))
                }
            } else {
                showToast(requireContext(), getString(R.string.invalid_latitude))
            }
        } else {
            showToast(requireContext(), getString(R.string.invalid_location))
        }
    }
    //endregion


    override fun observerData() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(Constant.IS_FAVORITE)
            ?.observe(viewLifecycleOwner) {
                isFavorite2 = it
            }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(Constant.LATITUDE_LONGITUDE)
            ?.observe(viewLifecycleOwner) {
                it?.let { latLng ->
                    fragmentCreateLocationBinding?.let { binding ->
                        showLocationOnMap(
                            binding,
                            latLng.substring(0, latLng.indexOf(",")).toDouble(),
                            latLng.substring(latLng.indexOf(",") + 1).toDouble()
                        )

                    }
                }
            }

        mainViewModel.isInsertScanResultDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val bundle = Bundle()
                if (isFromScanResultFragment) {
                    bundle.putBoolean(Constant.BACK_TO_CREATE_HISTORY_FRAGMENT, true)
                } else {
                    bundle.putBoolean(Constant.FROM_CREATE_QR_CODE_BY_TYPE_FRAGMENT, true)
                }
                bundle.putBoolean(Constant.IS_EDITABLE, true)
                bundle.putInt(Constant.SCAN_RESULT_ID, it.toInt())
                navigationToFragmentWithAnimation(R.id.scanResultFragment, bundle)
            }
        }

        mainViewModel.contact.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { contact ->
                qrCodeType?.let { qrType ->
                    when (qrType) {
                        Constant.PHONE -> {
                            fragmentCreatePhoneBinding!!.edtPhoneNumber.setText(contact.phoneNumber)
                            fragmentCreatePhoneBinding!!.edtPhoneNumber.setSelection(contact.phoneNumber.length)
                        }

                        Constant.EMAIL -> {
                            if (contact.email.isNotEmpty()) {
                                fragmentCreateEmailBinding!!.edtTo.setText(contact.email)
                                fragmentCreateEmailBinding!!.edtTo.setSelection(contact.email.length)
                            } else {
                                showToast(
                                    requireContext(),
                                    getString(R.string.the_contact_has_no_email)
                                )
                            }
                        }

                        Constant.CONTACT -> {
                            fragmentCreateContactBinding!!.edtName.setText(contact.name)
                            fragmentCreateContactBinding!!.edtEmail.setText(contact.email)
                            fragmentCreateContactBinding!!.edtPhoneNumber.setText(contact.phoneNumber)
                            fragmentCreateContactBinding!!.edtName.setSelection(contact.name.length)
                            fragmentCreateContactBinding!!.edtEmail.setSelection(contact.email.length)
                            fragmentCreateContactBinding!!.edtPhoneNumber.setSelection(contact.phoneNumber.length)
                        }

                        Constant.MESSAGE -> {
                            fragmentCreateMessageBinding!!.edtTo.setText(contact.phoneNumber)
                            fragmentCreateMessageBinding!!.edtTo.setSelection(contact.phoneNumber.length)
                        }
                    }
                }
            }
        }

        mainViewModel.getScanResultById(scanResultId).observe(viewLifecycleOwner) {
            it?.let {
                if (fragmentCreateWifiBinding != null) {
                    initWifiData(it)
                }
                if (isFromScanResultFragment) {
                    qrCodeType?.let { qrCodeType ->
                        when (qrCodeType) {
                            Constant.CONTACT -> initContactData(it)
                            Constant.EMAIL -> initEmailData(it)
                            Constant.EVENT -> initEventData(it)
                            Constant.LOCATION -> initLocationData(it)
                            Constant.MESSAGE -> initMessageData(it)
                            Constant.PHONE -> initPhoneData(it)
                            Constant.TEXT -> initTextData(it)
                            Constant.WEBSITE -> initWebsiteData(it)
                            Constant.WIFI -> initWifiData(it)
                        }
                    }
                }
            }
        }
    }


    override fun initData() {
//        mainViewModel.getScanResultById(scanResultId)
    }

    private fun setText(string: String?, edt: EditText) {
        edt.setText(string)
        string?.let {
            edt.setSelection(it.length)
        }
        edt.clearFocus()
    }

    private fun initContactData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateContactBinding!!.edtName)
        setText(scanResult.content2, fragmentCreateContactBinding!!.edtEmail)
        setText(scanResult.content3, fragmentCreateContactBinding!!.edtAddress)
        setText(scanResult.content4, fragmentCreateContactBinding!!.edtPhoneNumber)
    }

    private fun initEmailData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateEmailBinding!!.edtTo)
        setText(scanResult.content2, fragmentCreateEmailBinding!!.edtSubject)
        setText(scanResult.content3, fragmentCreateEmailBinding!!.edtMessage)
    }

    private fun initEventData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateEventBinding!!.edtTitle)
        setText(scanResult.content2, fragmentCreateEventBinding!!.edtLocation)
        setText(scanResult.content3, fragmentCreateEventBinding!!.edtDescription)
        setText(scanResult.content4, fragmentCreateEventBinding!!.edtStartTime)
        setText(scanResult.content5, fragmentCreateEventBinding!!.edtEndTime)
    }

    private fun initLocationData(scanResult: ScanResult) {
        showLocationOnMap(
            fragmentCreateLocationBinding!!,
            scanResult.content1!!.toDouble(),
            scanResult.content2!!.toDouble()
        )
    }

    private fun initMessageData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateMessageBinding!!.edtTo)
        setText(scanResult.content2, fragmentCreateMessageBinding!!.edtMessage)
    }

    private fun initPhoneData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreatePhoneBinding!!.edtPhoneNumber)
    }

    private fun initTextData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateTextBinding!!.edtText)
    }

    private fun initWebsiteData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateWebsiteBinding!!.edtWebsite)
    }

    private fun initWifiData(scanResult: ScanResult) {
        setText(scanResult.content1, fragmentCreateWifiBinding!!.edtSsid)
        setText(scanResult.content2, fragmentCreateWifiBinding!!.edtPassword)
        var tvSelected: TextView? = null
        var tvNotSelected1: TextView? = null
        var tvNotSelected2: TextView? = null
        var wifiType = scanResult.content3
        when (wifiType) {
            Constant.WPA_WPA2 -> {
                Timber.e("giangledinhinitWifiData ${scanResult}")
                wifiType = Constant.WPA
                tvSelected = fragmentCreateWifiBinding!!.tvWpaWpa2
                tvNotSelected1 = fragmentCreateWifiBinding!!.tvNone
                tvNotSelected2 = fragmentCreateWifiBinding!!.tvWep
            }
            Constant.WEP -> {
                wifiType = Constant.WEP
                tvSelected = fragmentCreateWifiBinding!!.tvWep
                tvNotSelected1 = fragmentCreateWifiBinding!!.tvNone
                tvNotSelected2 = fragmentCreateWifiBinding!!.tvWpaWpa2
            }
            Constant.NONE -> {
                wifiType = Constant.NONE
                tvSelected = fragmentCreateWifiBinding!!.tvNone
                tvNotSelected1 = fragmentCreateWifiBinding!!.tvWpaWpa2
                tvNotSelected2 = fragmentCreateWifiBinding!!.tvWep
            }
        }
        if (tvSelected != null) {
            if (tvNotSelected1 != null) {
                if (tvNotSelected2 != null) {
                    if (wifiType != null) {
                        getWifiType(wifiType, tvSelected, tvNotSelected1, tvNotSelected2)
                    }
                }
            }
        }
    }

    private fun chooseWifiType() {
        fragmentCreateWifiBinding?.let { binding ->
            binding.tvNone.setOnClickListener {
                if (checkTime(it)) {
                    requireActivity().hideKeyboard()
                    getWifiType(
                        type = "NONE",
                        tvSelected = binding.tvNone,
                        tvNotSelected1 = binding.tvWep,
                        tvNotSelected2 = binding.tvWpaWpa2,
                    )
                }
            }

            binding.tvWep.setOnClickListener {
                if (checkTime(it)) {
                    requireActivity().hideKeyboard()
                    getWifiType(
                        type = "WEP",
                        tvSelected = binding.tvWep,
                        tvNotSelected1 = binding.tvNone,
                        tvNotSelected2 = binding.tvWpaWpa2,
                    )
                }
            }

            binding.tvWpaWpa2.setOnClickListener {
                if (checkTime(it)) {
                    requireActivity().hideKeyboard()
                    getWifiType(
                        type = Constant.WPA,
                        tvSelected = binding.tvWpaWpa2,
                        tvNotSelected1 = binding.tvNone,
                        tvNotSelected2 = binding.tvWep,
                    )
                }
            }
        }
    }

    private fun getWifiType(
        type: String,
        tvSelected: TextView,
        tvNotSelected1: TextView,
        tvNotSelected2: TextView
    ) {
        wifiType = type
        when (type) {
            Constant.NONE -> {
                fragmentCreateWifiBinding!!.edtPassword.gone()
            }
            else -> fragmentCreateWifiBinding!!.edtPassword.show()
        }
        tvSelected.background = ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.bg_blue_ripple_radius_26,
            null
        )
        tvSelected.setTextColor(Color.parseColor("#FBFBFB"))
        tvNotSelected1.background = null
        tvNotSelected1.setTextColor(Color.parseColor("#A3A3A3"))
        tvNotSelected2.background = null
        tvNotSelected2.setTextColor(Color.parseColor("#A3A3A3"))
    }

    @SuppressLint("SetTextI18n")
    private fun showDateTimePicker(edt: EditText, date: Calendar, datePicked: Calendar) {
        DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        date.set(year, monthOfYear, dayOfMonth, hourOfDay, minute)
                        edt.setText(
                            formatCalendarDateTime(
                                year, monthOfYear + 1, dayOfMonth, hourOfDay, minute
                            )
                        )
                    },
                    datePicked.get(Calendar.HOUR_OF_DAY),
                    datePicked.get(Calendar.MINUTE),
                    true
                ).show()
            },
            datePicked.get(Calendar.YEAR),
            datePicked.get(Calendar.MONTH),
            datePicked.get(Calendar.DATE)
        ).show()
    }


    //region check permission location and get last location
    private fun fetchLastLocation(binding: FragmentCreateLocationBinding) {
        if (isPermissionLocationGranted) {
            getLastLocation(binding)
        } else {
            requestPermissionLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val isPermissionLocationGranted
        get() = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLocationLauncher =
        registerForActivityResult(RequestPermission()) { result ->
            if (!result && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showDialogRequestPermissionLocation()
            } else if (!result && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showToast(requireContext(), getString(R.string.action_failed))
            } else {
                fragmentCreateLocationBinding?.let { getLastLocation(it) }
            }
        }

    private fun showDialogRequestPermissionLocation() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_location_is_required_for_this_function))
            setPositiveButton(R.string.go_to_setting) { _, _ ->
                openSettings(activityLocationResultLauncher)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun getLastLocation(binding: FragmentCreateLocationBinding) {
        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener {
            if (it != null) {
                showLocationOnMap(binding, it.latitude, it.longitude)
            } else {
                showToast(requireContext(), getString(R.string.please_try_again))
            }
        }
    }

    private fun showLocationOnMap(
        binding: FragmentCreateLocationBinding,
        latitude: Double,
        longitude: Double
    ) {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().position(latLng)
        markerOptions.title("${latLng.latitude} : ${latLng.longitude}")
        map?.clear()
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
        map?.addMarker(markerOptions)
        binding.edtLatitude.setText(latitude.toString())
        binding.edtLongitude.setText(longitude.toString())
//        binding.cardViewMap.show()
//        binding.tvCurrentLocation.gone()
        binding.searchView.clearFocus()
        requireActivity().hideKeyboard()
        binding.searchView.setText(getAddress(latitude, longitude) ?: "")
    }

    private val activityLocationResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isPermissionLocationGranted) {
                fragmentCreateLocationBinding?.let { getLastLocation(it) }
            } else {
                showDialogRequestPermissionLocation()
            }
        }

    //endregion

    //region check permission contact and get data
    private fun checkPermissionReadContact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionContactGranted) {
                openContact()
            } else {
                requestPermissionReadContactLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        } else {
            openContact()
        }
    }

    private val isPermissionContactGranted
        get() = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionReadContactLauncher =
        registerForActivityResult(RequestPermission()) { result ->
            if (!result && !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                showDialogRequestPermissionContact()
            } else if (!result && shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                showToast(requireContext(), getString(R.string.action_failed))
            } else {
                openContact()
            }
        }

    private fun showDialogRequestPermissionContact() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_contact_is_required))
            setPositiveButton(R.string.go_to_setting) { _, _ ->
                openSettings(activityContactResultLauncher)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        selectContactResultLauncher.launch(intent)
    }


    private val selectContactResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Timber.e("giangledinh ${it?.data?.data}")
            it?.data?.data?.let { uri ->
                mainViewModel.getContactInfo(requireContext(), uri)
            }
        }

    private val activityContactResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isPermissionContactGranted) {
                openContact()
            } else {
                showDialogRequestPermissionContact()
            }
        }

    //endregion

    private fun loadFragmentContent(view: View) {
        binding.flContent.removeAllViews()
        binding.flContent.addView(view)
        binding.flContent.requestLayout()
    }

    override fun clearFocusWhenClickOutSide(event: MotionEvent) {
    }

    private fun isGPSOn(): Boolean {
        val mLocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun checkPhoneNumber(phoneNumber: String): Boolean {
        var endWith = false
        var check = true
        if (phoneNumber.length > 1) {
            endWith = Character.isDigit(phoneNumber[phoneNumber.length - 1])
            for (i in 0..phoneNumber.length - 2) {
                if ((phoneNumber.startsWith("(") && phoneNumber[i] == ')' && phoneNumber[i + 1] == ' '))
                    continue
                if (!Character.isDigit(phoneNumber[i]) && !Character.isDigit(phoneNumber[i + 1])) {
                    check = false
                    break
                }
            }
        }
        return phoneNumber.length > 1 && endWith && check
    }

    private fun disableNestedScrollView(edt: EditText) {
        edt.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            } else
                view.performClick()
            return@setOnTouchListener false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constant.SCAN_RESULT_ID, scanResultId)
    }

    private fun onEditorActionListener(binding: FragmentCreateLocationBinding) =
        TextView.OnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val latitude = try {
                    binding.edtLatitude.text.toString().trim().toDouble()
                } catch (e: Exception) {
                    null
                }
                val longitude = try {
                    binding.edtLongitude.text.toString().trim().toDouble()
                } catch (e: Exception) {
                    null
                }

                if ((activity as MainActivity).isNetworkAvailable) {
                    if (latitude != null && longitude != null) {
                        if (-90.0 <= latitude.toDouble() && latitude.toDouble() <= 90.0) {
                            if (-180.0 <= longitude.toDouble() && longitude.toDouble() <= 180.0) {
                                showLocationOnMap(binding, latitude, longitude)
                            } else {
                                showToast(requireContext(), getString(R.string.invalid_longitude))
                            }
                        } else {
                            showToast(requireContext(), getString(R.string.invalid_latitude))
                        }
                    } else {
                        showToast(requireContext(), getString(R.string.invalid_location))
                    }
                } else {
                    showToast(requireContext(), getString(R.string.network_not_available))
                }
                binding.edtLatitude.clearFocus()
                binding.edtLongitude.clearFocus()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        }

}
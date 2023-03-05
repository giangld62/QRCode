package com.tapbi.spark.qrcode.ui.main.scan_result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.FragmentScanResultBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.MainActivity
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.getDateFromString
import com.tapbi.spark.qrcode.utils.show
import com.tapbi.spark.qrcode.utils.showToast


class ScanResultFragment : BaseBindingFragment<FragmentScanResultBinding, ScanResultViewModel>() {
    private var scanResult: ScanResult? = null

    private val isFromHistoryFragment
        get() = requireArguments().getBoolean(
            Constant.FROM_CREATE_HISTORY_FRAGMENT,
            false
        )

    companion object {
        var scanResultId = -1
    }

    private val mBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFromHistoryFragment) {
                findNavController().popBackStack()
            } else if (requireArguments().getBoolean(
                    Constant.BACK_TO_CREATE_HISTORY_FRAGMENT,
                    false
                )
            ) {
                findNavController().popBackStack(R.id.createHistoryFragment, false)
            } else {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
            (activity as MainActivity).isScanned = false
        }
    }

    override fun getViewModel(): Class<ScanResultViewModel> {
        return ScanResultViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_scan_result

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
        savedInstanceState?.let {
          val scanResultJson = it.getString(Constant.SCAN_RESULT,"")
            if(scanResultJson != ""){
                binding.scanResult = Gson().fromJson(scanResultJson,ScanResult::class.java)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            mBackPressedCallback
        )
    }

    override fun onPermissionGranted() {
    }

    override fun initView() {
        if (requireArguments().getBoolean(Constant.IS_EDITABLE, false)) {
            binding.ivEdit.show()
        }
    }

    override fun evenClick() {
        binding.ivBack.setOnClickListener {
            if (checkTime(it)) {
                if (isFromHistoryFragment) {
                    findNavController().popBackStack()
                } else if (requireArguments().getBoolean(
                        Constant.BACK_TO_CREATE_HISTORY_FRAGMENT,
                        false
                    )
                ) {
                    findNavController().popBackStack(R.id.createHistoryFragment, false)
                } else {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
                (activity as MainActivity).isScanned = false
            }
        }

        binding.ivFavorite.setOnClickListener {
            if (checkTime(it)) {
                scanResult?.let { scanResult ->
                    val fromFavoriteFragment =
                        requireArguments().getBoolean(Constant.FROM_FAVORITE_FRAGMENT, false)
                    if (!fromFavoriteFragment) {
                        mainViewModel.updateFavorite(scanResult.id, !scanResult.isFavorite)
                    } else {
                        if (scanResult.isFavorite) {
                            mainViewModel.updateUnCheckFavoriteByRawValue(scanResult.rawValue!!)
                        } else {
                            mainViewModel.updateFavorite(scanResult.id, !scanResult.isFavorite)
                        }
                    }
                }
            }
        }

        binding.ivShare.setOnClickListener { view ->
            if (checkTime(view, 1000)) {
                scanResult?.let {
                    mainViewModel.getImageUri(
                        requireContext(),
                        getImageBitmap(it.rawValue!!, it.barcodeFormat)
                    )
                }
            }
        }

        binding.tvFunction1.setOnClickListener { view ->
            if (checkTime(view)) {
                scanResult?.let {
                    mainViewModel.codeImageBitmap.postValue(
                        getImageBitmap(
                            it.rawValue!!,
                            it.barcodeFormat
                        )
                    )
                    navigationToFragmentWithAnimation(R.id.showCodeFragment)
                }
            }
        }

        binding.tvFunction2.setOnClickListener {
            if (checkTime(it)) {
                scanResult?.let { scanResult ->
                    when (scanResult.category) {
                        Constant.PHONE -> {
                            callPhone(scanResult.content1!!)
                        }

                        Constant.MESSAGE -> {
                            sendSMS(scanResult.content1!!, scanResult.content2!!)
                        }

                        Constant.WIFI -> {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

        binding.tvFunction3.setOnClickListener {
            if (checkTime(it)) {
                scanResult?.let { scanResult ->
                    when (scanResult.category) {
                        Constant.MESSAGE -> {
                            callPhone(scanResult.content1!!)
                        }

                        Constant.PHONE -> {
                            sendSMS(scanResult.content1!!, "")
                        }

                        Constant.CONTACT -> {
                            addContact(
                                scanResult.content4!!,
                                scanResult.content1!!,
                                scanResult.content2!!
                            )
                        }
                    }
                }
            }
        }

        binding.tvFunction4.setOnClickListener {
            if (checkTime(it)) {
                scanResult?.let { scanResult ->
                    when (scanResult.category) {
                        Constant.TEXT -> {
                            copyText(scanResult.content1)
                            showToast(requireContext(), getString(R.string.copied))
                        }

                        Constant.WIFI -> {
                            copyText(scanResult.content2)
                            showToast(requireContext(), getString(R.string.copied))
                        }

                        Constant.WEBSITE -> {
                            findNavController().navigate(
                                ScanResultFragmentDirections.actionScanResultFragmentToWebViewFragment(
                                    scanResult.rawValue!!
                                )
                            )
                        }

                        Constant.PRODUCT -> {
                            findNavController().navigate(
                                ScanResultFragmentDirections.actionScanResultFragmentToWebViewFragment(
                                    "https://www.barcodelookup.com/${scanResult.rawValue}"
                                )
                            )
                        }

                        Constant.CONTACT -> {
                            callPhone(scanResult.content4!!)
                        }

                        Constant.EMAIL -> {
                            composeEmail(
                                arrayOf(scanResult.content1),
                                scanResult.content2,
                                scanResult.content3!!
                            )
                        }

                        Constant.LOCATION -> {
                            openMap(scanResult.content1!!, scanResult.content2!!)
                        }

                        Constant.EVENT -> {
                            addEvent(
                                title = scanResult.content1!!,
                                beginTime = scanResult.content4!!,
                                endTime = scanResult.content5!!,
                                location = scanResult.content2!!,
                                description = scanResult.content3!!
                            )
                        }

                        else -> {
                            addContact(scanResult.content1!!)
                        }
                    }
                }
            }
        }

        binding.ivEdit.setOnClickListener { view ->
            scanResult?.let {
                if (checkTime(view)) {
                    scanResultId = it.id
                    if (requireArguments().getBoolean(
                            Constant.FROM_CREATE_QR_CODE_BY_TYPE_FRAGMENT,
                            false
                        ) || requireArguments().getBoolean(
                            Constant.BACK_TO_CREATE_HISTORY_FRAGMENT,
                            false
                        )
                    ) {
                        val navController = findNavController()
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            Constant.IS_FAVORITE,
                            it.isFavorite
                        )
                        if(it.category == Constant.LOCATION){
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                Constant.LATITUDE_LONGITUDE,
                                it.displayValue
                            )
                        }
                        navController.popBackStack()
                    } else if (isFromHistoryFragment) {
                        val bundle = Bundle()
                        bundle.putString(Constant.QRC0DE_CREATE_TYPE, it.category)
                        bundle.putBoolean(Constant.FROM_SCAN_RESULT_FRAGMENT, true)
                        bundle.putBoolean(Constant.IS_FAVORITE, it.isFavorite)
                        navigationToFragmentWithAnimation(R.id.createQRCodeByType, bundle)
                    }
                }
            }
        }
    }

    private fun openMap(latitude: String, longitude: String) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?z=17&q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.resolveActivity(requireActivity().packageManager)?.let {
            startActivity(mapIntent)
        }
    }

    private fun addEvent(
        title: String,
        beginTime: String,
        endTime: String,
        location: String,
        description: String
    ) {
        val begin = getDateFromString(beginTime)
        val end = getDateFromString(endTime)
        val intent = Intent(Intent.ACTION_EDIT)
        intent.apply {
            type = "vnd.android.cursor.item/event"
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.timeInMillis)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )
        }
        startActivity(intent)
    }

    private fun addContact(phoneNumber: String, displayName: String = "", email: String = "") {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.EMAIL, email)
            putExtra(
                ContactsContract.Intents.Insert.EMAIL_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_WORK
            )

            putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)

            putExtra(ContactsContract.Intents.Insert.NAME, displayName)


        }
        startActivity(intent)
    }

    private fun sendSMS(phone: String, message: String) {
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
            smsIntent.putExtra("sms_body", message)
            startActivity(smsIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(
                requireContext(),
                getString(R.string.action_failed)
            )
        }
    }

    private fun callPhone(phone: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel:$phone")
        }
        startActivity(intent)
    }


    private fun copyText(text: String?) {
        val clipboard: ClipboardManager? =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", text)
        clipboard?.setPrimaryClip(clip)
    }

    override fun observerData() {
        mainViewModel.getScanResultById(requireArguments().getInt(Constant.SCAN_RESULT_ID))
            .observe(viewLifecycleOwner) {
                it?.let {
                    scanResult = it
                    binding.scanResult = it
                }
            }

        mainViewModel.imageUriSaved.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                shareImage(it)
            }
        }
    }

    override fun initData() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constant.SCAN_RESULT, Gson().toJson(scanResult))
    }
}
package com.tapbi.spark.qrcode.ui.main.create_qrcode

import android.os.Bundle
import android.view.View
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.FragmentCreateBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.scan_result.ScanResultFragment.Companion.scanResultId
import com.tapbi.spark.qrcode.utils.checkTime

class CreateFragment : BaseBindingFragment<FragmentCreateBinding, CreateViewModel>() {

    private var type: String = ""
    override fun getViewModel(): Class<CreateViewModel> {
        return CreateViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_create

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun onPermissionGranted() {
    }

    override fun initView() {
    }

    override fun evenClick() {
        binding.ivText.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.TEXT
                showAdsFull()
            }
        }
        binding.ivContact.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.CONTACT
                showAdsFull()
            }
        }
        binding.ivEmail.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.EMAIL
                showAdsFull()
            }
        }
        binding.ivEvent.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.EVENT
                showAdsFull()
            }
        }
        binding.ivLocation.setOnClickListener {
            if (checkTime(it)) {
                navigationToCreateQrCodeByType(Constant.LOCATION)
            }
        }
        binding.ivMess.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.MESSAGE
                showAdsFull()
            }
        }
        binding.ivPhone.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.PHONE
                showAdsFull()
            }
        }
        binding.ivWebsite.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.WEBSITE
                showAdsFull()

            }
        }
        binding.ivWifi.setOnClickListener {
            if (checkTime(it)) {
                type = Constant.WIFI
                showAdsFull()

            }
        }

        binding.ivHistory.setOnClickListener {
            if (checkTime(it)) {
                navigationToFragmentWithAnimation(R.id.createHistoryFragment)
            }
        }
    }

    override fun nextAfterFullScreen() {
        super.nextAfterFullScreen()
        viewModel.eventScreen.postValue(true)
    }

    override fun observerData() {
        viewModel.eventScreen.observe(viewLifecycleOwner) {
            if (it) {
                if (type.isNotEmpty()) {
                    navigationToCreateQrCodeByType(type)
                    mainViewModel.eventScreen.postValue(false)
                }

            }
        }
    }

    override fun initData() {
    }

    private fun navigationToCreateQrCodeByType(qrCodeType: String) {
        val bundle = Bundle()
        bundle.putString(Constant.QRC0DE_CREATE_TYPE, qrCodeType)
        navigationToFragmentWithAnimation(R.id.createQRCodeByType, bundle)
        scanResultId = -1
    }
}
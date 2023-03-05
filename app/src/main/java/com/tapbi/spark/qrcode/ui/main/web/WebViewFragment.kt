package com.tapbi.spark.qrcode.ui.main.web

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.FragmentWebviewBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.MainViewModel
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.gone
import com.tapbi.spark.qrcode.utils.show


class WebViewFragment : BaseBindingFragment<FragmentWebviewBinding, MainViewModel>() {
    private val navArgs by navArgs<WebViewFragmentArgs>()


    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_webview

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun onPermissionGranted() {
    }

    override fun initView() {
    }

    override fun evenClick() {
        binding.ivBack.setOnClickListener {
            if (checkTime(it))
                findNavController().popBackStack()
        }
    }

    override fun observerData() {
    }

    override fun initData() {
        val privacyPolicyLink = requireArguments().getString(Constant.PRIVACY_POLICY_LINK, "")
        if (privacyPolicyLink != "") {
            loadLink(privacyPolicyLink)
            binding.tvTitle.show()
            binding.tvUrl.gone()
            binding.tvTitle.text = getString(R.string.privacy_policy)
        }
        if (navArgs.url != "") {
            loadLink(navArgs.url)
            binding.tvTitle.gone()
            binding.tvUrl.show()
            binding.tvUrl.text = navArgs.url
        }
    }

    private fun loadLink(url: String) {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (isAdded) {
                        binding.pbLoad.gone()
                    }
                }
            }
            loadUrl(url)
        }
    }
}
package com.tapbi.spark.qrcode.ui.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.tapbi.spark.qrcode.BuildConfig
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.DialogRateAppBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingDialogFragment
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.showToast


class RateAppDialogFragment :
    BaseBindingDialogFragment<DialogRateAppBinding>() {
    private var isRated = false
    private var listener: Listener? = null

    override val layoutId: Int
        get() = R.layout.dialog_rate_app

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initView()
        evenClick()
    }

    private fun initView() {

    }

    private fun evenClick() {

        binding?.let { binding ->

            binding.simpleRatingBar.setOnRatingChangeListener { _, _ ->
                isRated = true
            }


            binding.btnRateNow.setOnClickListener {
                if (checkTime(it)) {
                    if (isRated && binding.simpleRatingBar.rating > 0) {
                        listener?.onRateNowClick()
                        if (binding.simpleRatingBar.rating > 1)
                            openMarket()
                        else
                            showToast(requireContext(), getString(R.string.thanks_for_your_rating))
                        dismiss()
                    } else {
                        showToast(requireContext(), getString(R.string.please_rate_5_stars))
                    }
                }
            }

            binding.btnCancel.setOnClickListener {
                if (checkTime(it)) {
                    dismiss()
                }
            }
        }

    }

    private fun openMarket() {
        try {
            context?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                )
            )
        } catch (e: ActivityNotFoundException) {
            context?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        Constant.link_share_app + BuildConfig.APPLICATION_ID
                    )
                )
            )
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onRateNowClick()
    }

}
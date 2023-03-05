package com.tapbi.spark.qrcode.ui.main.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.tapbi.spark.qrcode.BuildConfig
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.FragmentSettingBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.dialog.RateAppDialogFragment
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.gone
import com.tapbi.spark.qrcode.utils.sendFeedback
import timber.log.Timber


class SettingFragment : BaseBindingFragment<FragmentSettingBinding, SettingViewModel>(),
    RateAppDialogFragment.Listener {
    private var dialog: RateAppDialogFragment? = null

    override fun getViewModel(): Class<SettingViewModel> {
        return SettingViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_setting

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun onPermissionGranted() {
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.tvVersion.text = getString(R.string.version) + " " + BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferenceHelper.getBoolean(Constant.RATED_THE_APP, false)) {
            hideDialogRateApp()
        }

        binding.switchSound.isChecked =
            sharedPreferenceHelper.getBoolean(Constant.IS_SOUND_0N, false)
    }

    private fun hideDialogRateApp() {
        binding.tvRateApp.gone()
        binding.viewDivider3.gone()
    }

    override fun evenClick() {
        binding.tvFeedBack.setOnClickListener {
            if (checkTime(it)) {
                sendFeedback(
                    requireContext(),
                    getString(R.string.email_feedback),
                    "App Report (${requireContext().packageName})",
                    ""
                )
            }
        }

        binding.tvShareApp.setOnClickListener {
            if (checkTime(it, 1000)) {
                try {

                    Timber.e("//////////")
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                    val shareMessage = "${Constant.link_share_app}${BuildConfig.APPLICATION_ID}\n\n"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_one)))
                } catch (e: Exception) {

                }
            }
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            if (checkTime(it)) {
                val bundle = Bundle()
                bundle.putString(Constant.PRIVACY_POLICY_LINK, getString(R.string.link_gg))
                navigationToFragmentWithAnimation(R.id.webViewFragment, bundle)
            }
        }

        binding.tvRateApp.setOnClickListener {
            if (checkTime(it)) {
                val dialog = RateAppDialogFragment()
                dialog.setListener(this)
                dialog.show(childFragmentManager, null)
            }
        }

        binding.switchSound.setOnCheckedChangeListener { view, isCheck ->
            if (checkTime(view)) {
                sharedPreferenceHelper.storeBoolean(Constant.IS_SOUND_0N, isCheck)
                binding.switchSound.isChecked = isCheck
            }
        }
    }

    override fun observerData() {
    }

    override fun initData() {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialog?.dismissAllowingStateLoss()
    }

    override fun onRateNowClick() {
        sharedPreferenceHelper.storeBoolean(Constant.RATED_THE_APP, true)
        hideDialogRateApp()
    }
}

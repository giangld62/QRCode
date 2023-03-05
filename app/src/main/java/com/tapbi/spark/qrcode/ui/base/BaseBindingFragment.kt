package com.tapbi.spark.qrcode.ui.base

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.ironman.trueads.admob.interstital.InterstitialAdAdmob
import com.ironman.trueads.admob.interstital.ShowInterstitialAdsAdmobListener
import com.ironman.trueads.ironsource.ShowInterstitialIronSourceListener
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.ui.main.MainActivity
import com.tapbi.spark.qrcode.ui.main.MainViewModel
import com.tapbi.spark.qrcode.utils.*
import timber.log.Timber


abstract class BaseBindingFragment<B : ViewDataBinding, T : BaseViewModel> :
    BaseFragment() {
    lateinit var binding: B
    lateinit var viewModel: T
    lateinit var mainViewModel: MainViewModel
    protected abstract fun getViewModel(): Class<T>
    abstract val layoutId: Int

    protected abstract fun onCreatedView(view: View?, savedInstanceState: Bundle?)
    protected abstract fun onPermissionGranted()
    protected abstract fun initView()
    protected abstract fun evenClick()
    protected abstract fun observerData()
    protected abstract fun initData()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        initView()
        (activity as MainActivity).action = this::clearFocusWhenClickOutSide
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setStatusBarColor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(getViewModel())
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreatedView(view, savedInstanceState)
        onPermissionGranted()
        observerData()
        evenClick()
        initData()
    }

    fun shareImage(uri: Uri) {
        Timber.e("//////////")
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, getString(R.string.share_image)))
        } catch (e: Exception) {
            Timber.e(e)
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.photo_sharing_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun composeEmail(addresses: Array<String?>?, subject: String?, body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        Timber.e("giangledinh ${intent.resolveActivity(requireActivity().packageManager) == null}")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
        else{
            addresses?.get(0)?.let { sendFeedback(requireContext(), it,if(subject.isNullOrEmpty()) "" else subject,body) }
        }
    }

    fun navigationToFragmentWithAnimation(fragmentId: Int, bundle: Bundle? = null) {
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out)
        findNavController().navigate(fragmentId, bundle, navBuilder.build())
    }

    fun getImageBitmap(rawValue: String, barcodeFormat: Int): Bitmap? {
        val hints: MutableMap<EncodeHintType, Any> = mutableMapOf()
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        hints[EncodeHintType.MARGIN] = 1
        val widthPixels = Resources.getSystem().displayMetrics.widthPixels

        val matrix = try {
            val writer = MultiFormatWriter()
            writer.encode(
                rawValue,
                getBarcodeFormat(barcodeFormat),
                widthPixels,
                widthPixels, hints
            )
        }
        catch (e: Exception){
            null
        }

        return  if(matrix != null){
            BarcodeEncoder().createBitmap(matrix)
        }
        else{
            null
        }
    }

    fun openSettings(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        activityResultLauncher.launch(intent)
    }

    open fun clearFocusWhenClickOutSide(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isAdded) {
                val v: View? = requireActivity().currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        safeDelay(100L) {
                            v.clearFocus()
                            val imm: InputMethodManager =
                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                        }
                    }
                }
            }
        }
    }

    fun isKeyboardVisible(attachedView: View): Boolean {
        val insets = ViewCompat.getRootWindowInsets(attachedView)
        return insets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
    }

    fun getKeyboardHeight(attachedView: View): Int {
        val insets = ViewCompat.getRootWindowInsets(attachedView)
        return insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
    }

    private fun setStatusBarColor(){
        if(requireActivity().isDarkThemeOn()){
            requireActivity().window.setLightStatusBars(false)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.black_color)
        }
        else{
            requireActivity().window.setLightStatusBars(true)
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.gray_F2F2F2)
        }
    }
    protected open fun showAdsFull() {
        InterstitialAdAdmob.showAdInterstitialAdmobIronSrcInterleaved(
            requireActivity(),
            object : ShowInterstitialAdsAdmobListener {
               override fun onLoadFailInterstitialAdsAdmob() {
                    Timber.e("onLoadFailInterstitialAdsAdmob")
                    nextAfterFullScreen()
                }

                override fun onInterstitialAdsAdmobClose() {
                    Timber.e("onInterstitialAdsAdmobClose")
                    nextAfterFullScreen()
                }

                override fun onInterstitialAdsNotShow() {
                    nextAfterFullScreen()
                    Timber.e("onInterstitialAdsNotShow")
                }
            },
            object : ShowInterstitialIronSourceListener {
                override fun onLoadFailInterstitialAdsIronSource() {
                    Timber.e("onLoadFailInterstitialAdsIronSource")
                    nextAfterFullScreen()
                }

                override fun onInterstitialAdsIronSourceClose() {
                    Timber.e("onInterstitialAdsIronSourceClose")
                    nextAfterFullScreen()
                }

                override fun onInterstitialAdsIronSourceNotShow() {
                    Timber.e("onInterstitialAdsIronSourceNotShow")
                    nextAfterFullScreen()
                }

                override fun onInterstitialAdsIronSourceLoaded() {
                    Timber.e("onInterstitialAdsIronSourceLoaded")
                }

                override fun onInterstitialAdsIronSourceShowFail() {
                    Timber.e("onInterstitialAdsIronSourceShowFail")
                    nextAfterFullScreen()
                }
            })
    }

    protected open fun nextAfterFullScreen() {}

}

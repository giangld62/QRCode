package com.tapbi.spark.qrcode.ui.main

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ironman.trueads.admob.ControlAdsAdmob
import com.ironman.trueads.admob.interstital.InterstitialAdAdmob
import com.ironman.trueads.ironsource.InterstitialAdIronSource
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.ActivityMainBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingActivity
import com.tapbi.spark.qrcode.utils.lastClickTime1
import com.tapbi.spark.qrcode.utils.showToast
import timber.log.Timber


class MainActivity : BaseBindingActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var navController: NavController
    var isScanImageFromGallery = false
    var isScanned = false
    var action: ((event: MotionEvent) -> Unit)? = null
    var isNetworkAvailable = false
    var isFirstResume = true
    private val timeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            viewModel!!.isTimeChange.postValue(Unit)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isFirstResume = it.getBoolean(Constant.IS_FIRST_RESUME, false)
        }
        observerData()
    }

    private fun observerData() {
        viewModel!!.isOpenGallery.observe(this) {
            if (it) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                mActivityResultLauncher.launch(
                    Intent.createChooser(
                        intent,
                        getString(R.string.select_picture)
                    )
                )
                viewModel!!.isOpenGallery.postValue(false)
            }
        }
    }

        private val mActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Timber.e("giangledinh mActivityResultLauncher")
            if (it.resultCode == Activity.RESULT_OK) {
                it?.data?.data?.let { uri ->
                    viewModel!!.imageUriFromGallery.postValue(uri)
                }
            } else {
                showToast(this, getString(R.string.action_failed))
            }
        }

    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        // init ads Admob
        ControlAdsAdmob.initAds(this)
        InterstitialAdIronSource.initInterstitialIronSource(
            this,
            getString(R.string.ironsrc_app_key)
        )
        InterstitialAdAdmob.loadInterstitialAdmob(this, getString(R.string.admob_id_interstitial))
        InterstitialAdIronSource.loadInterstitialIronSource(applicationContext)
        registerReceiver(timeChangeReceiver, IntentFilter("android.intent.action.TIME_SET"))
        registerNetWorkCallback()
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(Constant.IS_FIRST_RESUME, isFirstResume)
        lastClickTime1 = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timeChangeReceiver)
    }

    override fun onResume() {
        super.onResume()
        InterstitialAdIronSource.resumeInterstitialAdIronSource(this)
    }

    override fun onPause() {
        super.onPause()
        InterstitialAdIronSource.onPauseInterstitialAdIronSource(this)
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        action?.let { it(event) }
        return super.dispatchTouchEvent(event)
    }

    override fun setupData() {
    }

    override val layoutId: Int
        get() = R.layout.activity_main

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            isNetworkAvailable = true
        }

        override fun onLost(network: Network) {
            isNetworkAvailable = false
        }
    }

    private fun registerNetWorkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    val isCameraPermissionGranted
        get() =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

    fun showDialogGoToSetting() {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_camera_is_required))
            setPositiveButton(R.string.go_to_setting) { _, _ ->
                openSettings(activityCameraResultLauncher)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private val activityCameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!isCameraPermissionGranted) {
                showDialogGoToSetting()
            }
        }

    private val requestPermissionCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (!result && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showDialogRequestPermissionAgain()
            } else if (!result && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showDialogGoToSetting()
            }
        }


    fun showDialogRequestPermissionAgain() {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_camera_is_required))
            setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                requestPermissionCameraLauncher.launch(Manifest.permission.CAMERA)
                dialog.dismiss()
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openSettings(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        activityResultLauncher.launch(intent)
    }
}
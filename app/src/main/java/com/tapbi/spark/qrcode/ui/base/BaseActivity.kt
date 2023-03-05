package com.tapbi.spark.qrcode.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tapbi.spark.qrcode.data.local.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }
}

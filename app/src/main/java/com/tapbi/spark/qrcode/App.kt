package com.tapbi.spark.qrcode

import androidx.multidex.MultiDexApplication
import com.tapbi.spark.qrcode.utils.MyDebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initLog()
        instance = this
    }


    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(MyDebugTree())
        }
    }

    companion object {
        var instance: App? = null
            private set
    }
}
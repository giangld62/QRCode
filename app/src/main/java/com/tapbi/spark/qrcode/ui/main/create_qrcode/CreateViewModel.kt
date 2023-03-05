package com.tapbi.spark.qrcode.ui.main.create_qrcode

import com.tapbi.spark.qrcode.common.LiveEvent
import com.tapbi.spark.qrcode.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(): BaseViewModel(){
    val eventScreen = LiveEvent<Boolean>()
}
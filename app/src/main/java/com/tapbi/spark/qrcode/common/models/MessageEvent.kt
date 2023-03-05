package com.tapbi.spark.qrcode.common.models

import com.tapbi.spark.qrcode.data.model.ScanResult


data class MessageEvent (
    var typeEvent: Int = 0,
    var stringValue: String = "",
    var scanResult: ScanResult? = null
)


package com.tapbi.spark.qrcode.data.model

data class ScanHistory(
    var category: String? = "",
    var time: Long = 0,
    var scanResults: List<ScanResult>
)
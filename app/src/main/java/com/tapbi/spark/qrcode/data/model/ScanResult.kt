package com.tapbi.spark.qrcode.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant

@Entity
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var barcodeFormat: Int = -1,
    var category: String? = null,
    var displayValue: String? = null,
    var rawValue: String? = null,
    var time: Long = 0,
    var isFavorite: Boolean = false,
    var isQrCodeCreatedByUser: Boolean = false,

    var title1: String? = null,
    var content1: String? = null,
    var title2: String? = null,
    var content2: String? = null,
    var title3: String? = null,
    var content3: String? = null,
    var title4: String? = null,
    var content4: String? = null,
    var title5: String? = null,
    var content5: String? = null,

    var functionName2: String? = null,
    var functionName3: String? = null,
    var functionName4: String? = null,
) {
    fun getIcon(): Int{
        return when(category){
            Constant.WIFI -> R.drawable.wifi_img
            Constant.WEBSITE -> R.drawable.website_icon
            Constant.EMAIL -> R.drawable.email_icon
            Constant.TEXT -> R.drawable.text_icon
            Constant.PHONE -> R.drawable.phone_img
            Constant.MESSAGE -> R.drawable.message_img
            Constant.CONTACT -> R.drawable.contact_img
            Constant.EVENT -> R.drawable.event_icon
            Constant.LOCATION -> R.drawable.location_img
            else -> R.drawable.product_icon
        }
    }

    fun getFunctionIcon2(): Int{
        return when(category){
            Constant.WIFI -> R.drawable.wifi_icon
            Constant.PHONE -> R.drawable.phone_icon
            Constant.MESSAGE -> R.drawable.send_sms_icon
            else -> 0
        }
    }

    fun getFunctionIcon3(): Int{
        return when(category){
            Constant.PHONE -> R.drawable.send_sms_icon
            Constant.MESSAGE -> R.drawable.phone_icon
            Constant.CONTACT -> R.drawable.contact_icon
            else -> 0
        }
    }

    fun getFunctionIcon4(): Int{
        return when(category){
            Constant.WIFI -> R.drawable.copy_password_icon
            Constant.WEBSITE -> R.drawable.search_blue_icon
            Constant.EMAIL -> R.drawable.send_email_icon
            Constant.TEXT -> R.drawable.copy_password_icon
            Constant.PHONE -> R.drawable.contact_icon
            Constant.MESSAGE -> R.drawable.contact_icon
            Constant.CONTACT -> R.drawable.phone_icon
            Constant.EVENT -> R.drawable.event_img
            Constant.LOCATION -> R.drawable.show_location_img
            else -> R.drawable.search_icon
        }
    }

}


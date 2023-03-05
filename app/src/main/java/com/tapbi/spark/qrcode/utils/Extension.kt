package com.tapbi.spark.qrcode.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.text.format.DateUtils
import android.view.Window
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.tapbi.spark.qrcode.R
import timber.log.Timber
import java.util.*


fun formatDate(dateLong: Long, formatType: Int = 3): String {
    val date = Date(dateLong)
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = java.text.SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    )
    val isToday = DateUtils.isToday(dateLong)
    return if (formatType == 3) "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    else if (isToday) "TODAY" else dateFormat.format(date)
}

fun formatCalendarDateTime(time: Barcode.CalendarDateTime): String {
    Timber.d("formatCalendarDateTime ${time.rawValue}")
    val day = if (time.day > 9) "${time.day}" else "0${time.day}"
    val month = if (time.month > 9) "${time.day}" else "0${time.month}"
    val minutes = if (time.minutes > 9) "${time.minutes}" else "0${time.minutes}"
    val barcodeHours = if (time.isUtc) time.hours + 7 else time.hours
    val hours =
        if (barcodeHours in 10..12) "$barcodeHours" else if (barcodeHours in 13..21) "0${barcodeHours - 12}" else if (barcodeHours in 22..24) "${barcodeHours - 12}" else "0$barcodeHours"
    return if (time.hours in 12..23) "$day/$month/${time.year}, ${hours}:${minutes}PM"
    else "$day/$month/${time.year}, ${hours}:${minutes}AM"
}


fun formatCalendarDateTime(y: Int, m: Int, d: Int, h: Int, minute: Int): String {
    val day = if (d > 9) "$d" else "0$d"
    val month = if (m > 9) "$m" else "0$m"
    val minutes = if (minute > 9) "$minute" else "0$minute"
    val hours =
        if (h in 10..12) "$h" else if (h in 13..21) "0${h - 12}" else if (h in 22..23) "${h - 12}" else "0$h"
    return if (h in 12..23) "$day/$month/$y, $hours:${minutes}PM"
    else "$day/$month/$y, $hours:${minutes}AM"
}

fun getDateFromString(dateString: String): Calendar{
    val date = Calendar.getInstance()
    val isAfterNoon = dateString.substring(17, 19) == "PM"
    date.set(
        dateString.substring(6, 10).toInt(),
        dateString.substring(3, 5).toInt() - 1,
        dateString.substring(0, 2).toInt(),
        if (!isAfterNoon) dateString.substring(12, 14)
            .toInt() else (dateString.substring(12, 14).toInt() + 12),
        dateString.substring(15, 17).toInt()
    )
    return date
}

fun getCalendarDateTimeFormat(dateTime: Calendar): String {
    val d = dateTime.get(Calendar.DATE)
    val m = dateTime.get(Calendar.MONTH)+1
    val minute = dateTime.get(Calendar.MINUTE)
    val y = dateTime.get(Calendar.YEAR)
    val h = dateTime.get(Calendar.HOUR_OF_DAY)
    val day = if (d > 9) "$d" else "0$d"
    val month = if (m > 9) "$m" else "0$m"
    val minutes = if (minute > 9) "$minute" else "0$minute"
    val hours = if(h > 9) "$h" else "0$h"
    return "$y$month${day}T${hours}${minutes}00"
}

fun showToast(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}

fun Window.setLightStatusBars(b: Boolean) {
    WindowCompat.getInsetsController(this, decorView)?.isAppearanceLightStatusBars = b
}

fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun getBarcodeFormat(barcodeFormatValue: Int): BarcodeFormat {
    return when (barcodeFormatValue) {
        1 -> BarcodeFormat.CODE_128
        2 -> BarcodeFormat.CODE_39
        4 -> BarcodeFormat.CODE_93
        8 -> BarcodeFormat.CODABAR
        16 -> BarcodeFormat.DATA_MATRIX
        32 -> BarcodeFormat.EAN_13
        64 -> BarcodeFormat.EAN_8
        128 -> BarcodeFormat.ITF
        256 -> BarcodeFormat.QR_CODE
        512 -> BarcodeFormat.UPC_A
        1024 -> BarcodeFormat.UPC_E
        2048 -> BarcodeFormat.PDF_417
        4096 -> BarcodeFormat.AZTEC
        else -> BarcodeFormat.QR_CODE
    }

}

fun sendFeedback(context: Context, supportEmail: String, subject: String,text: String?) {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.type = "text/email"
    emailIntent.setPackage("com.google.android.gm")
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_TEXT, text)

    Timber.e("//////////")
    context.startActivity(
        Intent.createChooser(
            emailIntent,
            context.getString(R.string.send_email_report_app)
        )
    )
}


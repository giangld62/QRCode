package com.tapbi.spark.qrcode.data.local.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.local.db.ScanResultDao
import com.tapbi.spark.qrcode.data.model.ScanHistory
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.utils.formatDate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject

class QRCodeRepository @Inject constructor(private val scanResultDao: ScanResultDao) {

    fun insertScanResult(scanResult: ScanResult): Long {
        return scanResultDao.insertScanResult(scanResult)
    }

    fun getImageUri(context: Context, img: Bitmap?): Uri? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "barcode.png"
            )
            val stream = FileOutputStream(file)
            img?.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.close()
            FileProvider.getUriForFile(
                context,
                "com.tapbi.spark.qrcode.provider",
                file
            )
        } catch (e: IOException) {
            null
        }
    }

    fun getFavoriteList(groupBy: String, queryString: String): List<ScanHistory> {
        return if (groupBy == Constant.DATE) {
            val scanResults = getFavoriteScanResultsOrderByRawValue(queryString)
            Collections.sort(scanResults, Comparator<ScanResult> { obj1, obj2 ->
                return@Comparator Integer.valueOf(obj2.time.toInt())
                    .compareTo(Integer.valueOf(obj1.time.toInt()))
            })
            groupScanResultByDate(scanResults)
        } else {
            val scanResult2 = getFavoriteScanResultsOrderByRawValue(queryString)
            Collections.sort(scanResult2, Comparator<ScanResult> { obj1, obj2 ->
                return@Comparator obj1.category!!.compareTo(obj2.category!!, false)
            })
            groupScanResultByCategory(scanResult2)
        }
    }

    fun updateFavorite(id: Int, isFavorite: Boolean) {
        scanResultDao.updateFavorite(id, isFavorite)
    }

    fun updateUnCheckFavoriteByRawValue(rawValue: String) {
        scanResultDao.updateUnCheckFavoriteByRawValue(rawValue)
    }

    fun getScanResultById(id: Int): LiveData<ScanResult> {
        return scanResultDao.getScanResultById(id)
    }


    fun getScanHistoryList(
        groupBy: String,
        queryString: String?
    ): List<ScanHistory> {
        return if (groupBy == Constant.DATE) {
            groupScanResultByDate(scanResultDao.getAllScanResultsOrderByTime(queryString))
        } else {
            groupScanResultByCategory(scanResultDao.getAllScanResultsOrderByCategory(queryString))
        }
    }

    fun getScanHistoryListOrderByName(queryString: String?): List<ScanResult> {
        return scanResultDao.getAllScanResultsByDisplayValue(queryString)
    }

    fun getFavoriteListOrderByName(queryString: String?): List<ScanResult> {
        val scanResult = getFavoriteScanResultsOrderByRawValue(queryString)
        Collections.sort(scanResult, Comparator<ScanResult> { obj1, obj2 ->
            return@Comparator obj1.displayValue!!.compareTo(obj2.displayValue!!, false)
        })
        return scanResult
    }

    fun deleteAllScanResult(isQrCodeCreatedByUser: Boolean) {
        scanResultDao.deleteAllScanResult(isQrCodeCreatedByUser)
    }

    fun deleteScanResultById(id: Int) {
        scanResultDao.deleteScanResultById(id)
    }

    fun deleteFavoriteScanResultWithSameRawValue(rawValue: String) {
        scanResultDao.deleteFavoriteScanResultWithSameRawValue(rawValue)
    }

    fun getAllQrCode(isQrCodeCreatedByUser: Boolean): List<ScanHistory> {
        return groupScanResultByDate(scanResultDao.getAllQrCode(isQrCodeCreatedByUser))
    }

    private fun groupScanResultByDate(scanResults: List<ScanResult>): List<ScanHistory> {
        val scanHistories = arrayListOf<ScanHistory>()
        if (scanResults.isNotEmpty()) {
            var startPosition = 0
            var currentTime = formatDate(scanResults[startPosition].time, Constant.DATE_TYPE_2)
            for (i in scanResults.indices) {
                if (formatDate(scanResults[i].time, Constant.DATE_TYPE_2) != currentTime) {
                    scanHistories.add(
                        ScanHistory(
                            time = scanResults[i - 1].time,
                            scanResults = scanResults.subList(startPosition, i)
                        )
                    )
                    startPosition = i
                    currentTime = formatDate(scanResults[i].time, Constant.DATE_TYPE_2)
                }
                if (formatDate(scanResults[i].time, Constant.DATE_TYPE_2) == formatDate(
                        scanResults[scanResults.size - 1].time, Constant.DATE_TYPE_2
                    )
                ) {
                    scanHistories.add(
                        ScanHistory(
                            time = scanResults[i].time,
                            scanResults = scanResults.subList(i, scanResults.size)
                        )
                    )
                    break
                }
            }
        }
        return scanHistories
    }

    private fun groupScanResultByCategory(scanResults: List<ScanResult>): List<ScanHistory> {
        val scanHistories = arrayListOf<ScanHistory>()
        if (scanResults.isNotEmpty()) {
            var startPosition = 0
            var currentCategory = scanResults[0].category
            for (i in scanResults.indices) {
                if (scanResults[i].category != currentCategory) {
                    scanHistories.add(
                        ScanHistory(
                            category = scanResults[i - 1].category,
                            scanResults = scanResults.subList(startPosition, i)
                        )
                    )
                    startPosition = i
                    currentCategory = scanResults[i].category
                }
                if (scanResults[i].category == scanResults[scanResults.size - 1].category) {
                    scanHistories.add(
                        ScanHistory(
                            category = scanResults[i].category,
                            scanResults = scanResults.subList(i, scanResults.size)
                        )
                    )
                    break
                }
            }
        }
        return scanHistories
    }

    private fun getFavoriteScanResultsOrderByRawValue(queryString: String?): List<ScanResult> {
        val tempList = scanResultDao.getAllFavoriteScanResults(queryString)
        val resultsList = arrayListOf<ScanResult>()
        if (tempList.isNotEmpty()) {
            var currentRawValue = tempList[0].rawValue
            for (i in tempList.indices) {
                if (tempList[i].rawValue != currentRawValue) {
                    resultsList.add((tempList[i - 1]))
                    currentRawValue = tempList[i].rawValue
                }
                if (tempList[i].rawValue == tempList[tempList.size - 1].rawValue) {
                    resultsList.add((tempList[tempList.size - 1]))
                    break
                }
            }
        }
        return resultsList
    }

}
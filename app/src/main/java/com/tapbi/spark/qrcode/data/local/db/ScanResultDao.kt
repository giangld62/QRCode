package com.tapbi.spark.qrcode.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tapbi.spark.qrcode.data.model.ScanResult

@Dao
interface ScanResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScanResult(scanResult: ScanResult): Long

    @Query("delete from ScanResult where isQrCodeCreatedByUser =:isQrCodeCreatedByUser")
    fun deleteAllScanResult(isQrCodeCreatedByUser: Boolean)

    @Query("delete from ScanResult where id =:id")
    fun deleteScanResultById(id: Int)

    @Query("delete from ScanResult where isFavorite = 1 and rawValue =:rawValue")
    fun deleteFavoriteScanResultWithSameRawValue(rawValue: String)

    @Query("select * from ScanResult where isQrCodeCreatedByUser = 0  and displayValue like :queryString order by category")
    fun getAllScanResultsOrderByCategory(queryString: String?): List<ScanResult>

    @Query("select * from ScanResult where isQrCodeCreatedByUser = 0 and displayValue like:queryString order by time desc")
    fun getAllScanResultsOrderByTime(queryString: String?): List<ScanResult>

    @Query("select * from ScanResult where isQrCodeCreatedByUser = 0 and displayValue like :queryString order by displayValue")
    fun getAllScanResultsByDisplayValue(queryString: String?): List<ScanResult>

    @Query("select * from ScanResult where isFavorite = 1 and displayValue like :queryString order by rawValue")
    fun getAllFavoriteScanResults(queryString: String?): List<ScanResult>

    @Query("select * from ScanResult where isQrCodeCreatedByUser = :isQrCodeCreatedByUser order by time desc")
    fun getAllQrCode(isQrCodeCreatedByUser: Boolean): List<ScanResult>

    @Query("update ScanResult set isFavorite =:isFavorite where id =:id")
    fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("update ScanResult set isFavorite = 0 where rawValue =:rawValue")
    fun updateUnCheckFavoriteByRawValue(rawValue: String)

    @Query("select * from ScanResult where id =:id")
    fun getScanResultById(id: Int): LiveData<ScanResult>

    @Query("select count(*) from ScanResult where isQrCodeCreatedByUser = 0")
    fun getRowCount(): LiveData<Int>

    @Query("select count(*) from ScanResult where isFavorite = 1")
    fun getRowCountFavorite(): LiveData<Int>


    @Query("select count(*) from ScanResult where isQrCodeCreatedByUser = 1")
    fun getRowCountCreateByUser(): LiveData<Int>

}
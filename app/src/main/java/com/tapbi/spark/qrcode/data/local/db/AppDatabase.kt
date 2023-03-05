package com.tapbi.spark.qrcode.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult


@Database(entities = [ScanResult::class], version = Constant.DB_VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val scanResultDao: ScanResultDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }
    }
}
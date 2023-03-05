package com.tapbi.spark.qrcode.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.local.db.AppDatabase
import com.tapbi.spark.qrcode.data.local.db.ScanResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideSharedPreference(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java,
            Constant.DB_NAME
        ).fallbackToDestructiveMigration().addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideScanResult(db: AppDatabase): ScanResultDao {
        return db.scanResultDao
    }

}

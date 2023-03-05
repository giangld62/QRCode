package com.tapbi.spark.qrcode.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.qrcode.common.LiveEvent
import com.tapbi.spark.qrcode.data.local.db.ScanResultDao
import com.tapbi.spark.qrcode.data.local.repository.ContactRepository
import com.tapbi.spark.qrcode.data.local.repository.QRCodeRepository
import com.tapbi.spark.qrcode.data.model.Contact
import com.tapbi.spark.qrcode.data.model.ScanHistory
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.ui.base.BaseViewModel
import com.tapbi.spark.qrcode.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val qrCodeRepository: QRCodeRepository,
    private val contactRepository: ContactRepository,
    private val scanResultDao: ScanResultDao
) :
    BaseViewModel() {
    //    val scanResult = MutableLiveData<ScanResult>()
    val codeImageBitmap = MutableLiveData<Bitmap?>()
    val currentPageLiveData = MutableLiveData<Int?>()
    val imageUriSaved = MutableLiveData<Event<Uri?>>()
    val isInsertScanResultDone = MutableLiveData<Event<Long>>()
    val isUpdateFavoriteDone = MutableLiveData<Event<Unit>>()
    val isUnCheckFavoriteDone = MutableLiveData<Event<Unit>>()
    val getAllQrCodeCreateByUser = MutableLiveData<List<ScanHistory>>()
    var scanResultOrderByName = MutableLiveData<List<ScanResult>>()
    val favoriteListOrderByName = MutableLiveData<List<ScanResult>>()
    val isDeleteAllScanResultDone = MutableLiveData<Unit>()
    val contact = MutableLiveData<Event<Contact?>>()
    val isDeleteScanResultByIdDone = MutableLiveData<Unit>()
    val isTimeChange = MutableLiveData<Unit>()
    val isDeleteFavoriteScanResultWithSameRawValueDone = MutableLiveData<Unit>()
    val rowCountLiveData = scanResultDao.getRowCount()
    val rowCountCreateByUserLiveData = scanResultDao.getRowCountCreateByUser()
    val rowCountFavoriteLiveData = scanResultDao.getRowCountFavorite()
    val isOpenGallery = MutableLiveData<Boolean>()
    val imageUriFromGallery = MutableLiveData<Uri>()
    //    val scanHistoryList = MutableLiveData<Event<List<ScanHistory>>>()
    val scanHistoryList = MutableLiveData<List<ScanHistory>>()
    val favoriteScanResults = MutableLiveData<List<ScanHistory>>()
    val finishLoadAds = MutableLiveData<Boolean>()
    val eventScreen = LiveEvent<Boolean>()
    fun getImageUri(context: Context, imgBitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {
            imageUriSaved.postValue(Event(qrCodeRepository.getImageUri(context, imgBitmap)))
        }

    }

    fun getScanResultOrderByName(queryString: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            scanResultOrderByName.postValue(
                qrCodeRepository.getScanHistoryListOrderByName(
                    queryString
                )
            )
        }
    }

    fun getFavoriteListOrderByName(queryString: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteListOrderByName.postValue(
                qrCodeRepository.getFavoriteListOrderByName(
                    queryString
                )
            )
        }
    }

    fun deleteAllScanResult(isQrCodeCreatedByUser: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            isDeleteAllScanResultDone.postValue(
                qrCodeRepository.deleteAllScanResult(
                    isQrCodeCreatedByUser
                )
            )
        }
    }

    fun getContactInfo(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            contact.postValue(Event(contactRepository.getContact(context, uri)))
        }
    }


    fun getScanHistoryList(groupBy: String, queryString: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            scanHistoryList.postValue(
                qrCodeRepository.getScanHistoryList(groupBy, queryString)
            )
        }
    }

    fun getAllFavoriteScanResults(groupBy: String, queryString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteScanResults.postValue(qrCodeRepository.getFavoriteList(groupBy, queryString))
        }
    }

    fun getAllQrCode(isQrCodeCreatedByUser: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            getAllQrCodeCreateByUser.postValue(qrCodeRepository.getAllQrCode(isQrCodeCreatedByUser))
        }
    }

    fun getScanResultById(id: Int): LiveData<ScanResult> {
        return qrCodeRepository.getScanResultById(id)
    }


    fun insertScanResult(scanResult: ScanResult) {
        viewModelScope.launch(Dispatchers.IO) {
            isInsertScanResultDone.postValue(Event(qrCodeRepository.insertScanResult(scanResult)))
        }
    }

    fun updateUnCheckFavoriteByRawValue(rawValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isUnCheckFavoriteDone.postValue(
                Event(
                    qrCodeRepository.updateUnCheckFavoriteByRawValue(
                        rawValue
                    )
                )
            )
        }
    }


    fun updateFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            isUpdateFavoriteDone.postValue(Event(qrCodeRepository.updateFavorite(id, isFavorite)))
        }
    }

    fun deleteScanResultById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isDeleteScanResultByIdDone.postValue(qrCodeRepository.deleteScanResultById(id))
        }
    }

    fun deleteFavoriteScanResultWithSameRawValue(rawValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isDeleteFavoriteScanResultWithSameRawValueDone.postValue(
                qrCodeRepository.deleteFavoriteScanResultWithSameRawValue(
                    rawValue
                )
            )
        }
    }
}
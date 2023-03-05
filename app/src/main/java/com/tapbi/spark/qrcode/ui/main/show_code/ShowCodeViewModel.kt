package com.tapbi.spark.qrcode.ui.main.show_code

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.qrcode.data.local.repository.ImageRepository
import com.tapbi.spark.qrcode.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowCodeViewModel @Inject constructor(private val imageRepository: ImageRepository) :
    BaseViewModel() {
    val isSaveImageDone = MutableLiveData<Unit>()

    fun saveImage(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            isSaveImageDone.postValue(imageRepository.saveImage(bitmap, context, "qrcode"))
        }
    }
}
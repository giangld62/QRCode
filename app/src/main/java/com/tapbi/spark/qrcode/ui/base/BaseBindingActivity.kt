package com.tapbi.spark.qrcode.ui.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider

abstract class BaseBindingActivity<B : ViewDataBinding, VM : BaseViewModel?> :
    BaseActivity() {
    lateinit var binding: B
    var viewModel: VM? = null
    abstract val layoutId: Int

    abstract fun getViewModel(): Class<VM>
    abstract fun setupView(savedInstanceState: Bundle?)
    abstract fun setupData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        viewModel = ViewModelProvider(this).get(getViewModel())
        setupView(savedInstanceState)
        setupData()
    }

}
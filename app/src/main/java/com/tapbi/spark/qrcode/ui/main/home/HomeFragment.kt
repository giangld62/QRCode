package com.tapbi.spark.qrcode.ui.main.home

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.FragmentHomeBinding
import com.tapbi.spark.qrcode.ui.adapter.ViewPagerAdapter
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.main.MainActivity
import com.tapbi.spark.qrcode.utils.checkTime
import timber.log.Timber

class HomeFragment : BaseBindingFragment<FragmentHomeBinding, HomeViewModel>() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private var currentPage = 2
    override fun getViewModel(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    private val mBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_home

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        requireActivity().window?.decorView?.systemUiVisibility = 0
        savedInstanceState?.let {
            currentPage = it.getInt(Constant.CURRENT_PAGE)
            binding.viewPager.setCurrentItem(currentPage, false)
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            mBackPressedCallback
        )
    }


    override fun onPermissionGranted() {
    }

    override fun initView() {
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.isSaveEnabled = false
        binding.viewPager.adapter = viewPagerAdapter
        binding.bnvMain.selectedItemId = R.id.scannerFragment
        binding.viewPager.setCurrentItem(currentPage, false)
        binding.bnvMain.menu.findItem(R.id.scannerFragment).isEnabled = false
    }

    override fun evenClick() {
        binding.bnvMain.menu.forEach {
            val view = binding.bnvMain.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
        }
        binding.btnScan.setOnClickListener {
            if(checkTime()){
                binding.bnvMain.selectedItemId = R.id.scannerFragment
                if (currentPage != 2) {
                    binding.viewPager.setCurrentItem(2, false)
                    currentPage = 2
                    if (!(activity as MainActivity).isCameraPermissionGranted && shouldShowRequestPermissionRationale(
                            Manifest.permission.CAMERA
                        )
                    ) {
                        (activity as MainActivity).showDialogRequestPermissionAgain()
                    } else if (!(activity as MainActivity).isCameraPermissionGranted && !shouldShowRequestPermissionRationale(
                            Manifest.permission.CAMERA
                        )
                    ) {
                        (activity as MainActivity).showDialogGoToSetting()
                    }
                }
            }
        }
        binding.bnvMain.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.historyFragment -> {
                    if (checkTime()) {
                        binding.viewPager.setCurrentItem(0, false)
                        currentPage = 0
                        return@setOnItemSelectedListener true
                    }
                    return@setOnItemSelectedListener false
                }
                R.id.createFragment -> {
                    if (checkTime()) {
                        binding.viewPager.setCurrentItem(1, false)
                        currentPage = 1
                        return@setOnItemSelectedListener true
                    }
                    return@setOnItemSelectedListener false
                }
                R.id.favoriteFragment -> {
                    if (checkTime()) {
                        binding.viewPager.setCurrentItem(3, false)
                        currentPage = 3
                        return@setOnItemSelectedListener true
                    }
                    return@setOnItemSelectedListener false
                }
                else -> {
                    if (checkTime()) {
                        binding.viewPager.setCurrentItem(4, false)
                        currentPage = 4
                        return@setOnItemSelectedListener true
                    }
                    return@setOnItemSelectedListener false
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.currentPageLiveData.postValue(currentPage)
    }

    override fun observerData() {
        mainViewModel.currentPageLiveData.observe(viewLifecycleOwner) {
            Timber.e("giangledinh currentPage ${it}")
            it?.let {
                binding.viewPager.setCurrentItem(it, false)
                currentPage = it
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constant.CURRENT_PAGE, currentPage)
    }

    override fun initData() {
    }
}
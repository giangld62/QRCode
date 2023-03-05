package com.tapbi.spark.qrcode.ui.main.favorite

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.FragmentFavoriteBinding
import com.tapbi.spark.qrcode.ui.adapter.FavoriteAdapter
import com.tapbi.spark.qrcode.ui.adapter.ScanResultAdapter2
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.utils.gone
import com.tapbi.spark.qrcode.utils.show

class FavoriteFragment : BaseBindingFragment<FragmentFavoriteBinding, FavoriteViewModel>(),
    FavoriteAdapter.OnItemClickListener, ScanResultAdapter2.OnItemClickListener {
    private lateinit var historyAdapter: FavoriteAdapter
    private lateinit var scanResultAdapter: ScanResultAdapter2
    private var scanResultIdIsOpening = -1
    private val sortTypeSaved
        get() = sharedPreferenceHelper.getStringWithDefault(
            Constant.SORT_FAVORITE,
            Constant.DATE
        )
    private var isShowFilter = false
    private var queryString: String? = ""

    override fun getViewModel(): Class<FavoriteViewModel> {
        return FavoriteViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_favorite

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

    override fun onPause() {
        super.onPause()
        if (isShowFilter) {
            goneFilter()
        }
        if (binding.searchView.query.toString().isNotEmpty()) {
            binding.searchView.setQuery("", false)
        }
        closeLayout()
    }

    override fun initView() {
        historyAdapter = FavoriteAdapter(this)
        binding.rvFavorite.adapter = historyAdapter
        val viewBinderHelper = ViewBinderHelper()
        viewBinderHelper.setOpenOnlyOne(true)
        scanResultAdapter = ScanResultAdapter2(viewBinderHelper)
        scanResultAdapter.setListener(this)
        when (sortTypeSaved) {
            Constant.DATE -> getFavoriteList(Constant.DATE, "%%")
            Constant.CATEGORY -> getFavoriteList(Constant.CATEGORY, "%%")
            Constant.NAME -> getFavoriteListOrderByName("%%")
        }
    }

    override fun evenClick() {
        binding.ivFilter.setOnClickListener {
            if (isShowFilter) {
                goneFilter()
            } else {
                showFilter()
            }
        }

        binding.ctlRoot.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isShowFilter) {
                        goneFilter()
                    }
                    closeLayout()
                }
                MotionEvent.ACTION_UP -> view.performClick()
            }
            return@setOnTouchListener false
        }

        binding.tvDate.setOnClickListener {
            if (sortTypeSaved != Constant.DATE) {
                getFavoriteList(Constant.DATE, "%${queryString}%")
            } else {
                goneFilter()
            }
        }
        binding.tvType.setOnClickListener {
            if (sortTypeSaved != Constant.CATEGORY) {
                getFavoriteList(Constant.CATEGORY, "%${queryString}%")
            } else {
                goneFilter()
            }
        }
        binding.tvName.setOnClickListener {
            if (sortTypeSaved != Constant.NAME) {
                getFavoriteListOrderByName("%${queryString}%")
            } else {
                goneFilter()
            }
        }

        binding.searchView.post {
            if (isAdded) {
                binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        binding.searchView.clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        queryString = newText
                        getFavoriteListByType(newText)
                        return true
                    }
                })

                binding.searchView.setOnQueryTextFocusChangeListener { view, b ->
                    if (b && isShowFilter) {
                        goneFilter()
                    }
                    closeLayout()
                }
            }
        }

        binding.rvFavorite.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isShowFilter) {
                    goneFilter()
                }
                closeLayout()
            }
        })

        binding.rvFavorite.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isShowFilter) {
                        goneFilter()
                    }
                }
                MotionEvent.ACTION_UP -> view.performClick()
            }
            return@setOnTouchListener false
        }
    }


    private fun showFilter() {
        isShowFilter = true
        binding.llSortOrder.show()
    }

    private fun goneFilter() {
        isShowFilter = false
        binding.llSortOrder.gone()
    }

    override fun observerData() {
        mainViewModel.isTimeChange.observe(viewLifecycleOwner) {
            if (sortTypeSaved == Constant.DATE) {
                getFavoriteList(Constant.DATE, "%${queryString}%")
            }
        }

        mainViewModel.rowCountFavoriteLiveData.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.tvNoData.show()
                historyAdapter.submitList(null)
            }
        }

        mainViewModel.favoriteScanResults.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    binding.tvNoData.show()
                } else {
                    binding.tvNoData.gone()
                }
                historyAdapter.submitList(it)
                mainViewModel.favoriteScanResults.postValue(null)
            }
        }

        mainViewModel.favoriteListOrderByName.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    binding.tvNoData.show()
                } else {
                    binding.tvNoData.gone()
                }
                scanResultAdapter.submitList(it)
                mainViewModel.favoriteListOrderByName.postValue(null)
            }
        }

        mainViewModel.isUnCheckFavoriteDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                updateHistory()
            }
        }

        mainViewModel.isDeleteFavoriteScanResultWithSameRawValueDone.observe(viewLifecycleOwner) {
            updateHistory()
            mainViewModel.getAllQrCode(true)
        }

    }

    private fun updateHistory() {
        when (sharedPreferenceHelper.getStringWithDefault(
            Constant.SORT_HISTORY,
            Constant.DATE
        )) {
            Constant.DATE -> mainViewModel.getScanHistoryList(Constant.DATE, "%%")
            Constant.CATEGORY -> mainViewModel.getScanHistoryList(Constant.CATEGORY, "%%")
            Constant.NAME -> mainViewModel.getScanResultOrderByName("%%")
        }
    }

    private fun getFavoriteListByType(queryString: String?) {
        when (sortTypeSaved) {
            Constant.DATE -> mainViewModel.getAllFavoriteScanResults(
                Constant.DATE, "%${queryString}%"
            )
            Constant.CATEGORY -> mainViewModel.getAllFavoriteScanResults(
                Constant.CATEGORY,
                "%${queryString}%"
            )
            Constant.NAME -> mainViewModel.getFavoriteListOrderByName("%${queryString}%")
        }
    }

    override fun initData() {
    }

    override fun onItemClick(id: Int) {
        if (isShowFilter)
            goneFilter()
        val bundle = Bundle()
        bundle.putInt(Constant.SCAN_RESULT_ID, id)
        bundle.putBoolean(Constant.FROM_FAVORITE_FRAGMENT, true)
        navigationToFragmentWithAnimation(R.id.scanResultFragment, bundle)
    }

    override fun onFavoriteClick(scanResult: ScanResult,position: Int) {
        if (isShowFilter)
            goneFilter()
        mainViewModel.updateUnCheckFavoriteByRawValue(scanResult.rawValue!!)
    }

    override fun onDeleteClick(scanResult: ScanResult,position: Int) {
        if (isShowFilter)
            goneFilter()
        mainViewModel.deleteFavoriteScanResultWithSameRawValue(scanResult.rawValue!!)
    }

    override fun onSlide(id: Int) {
        if (isShowFilter)
            goneFilter()
    }

    override fun onOpened(id: Int, position: Int) {
        scanResultIdIsOpening = id
    }

    override fun onClosed(id: Int) {
    }


    private fun getFavoriteList(groupBy: String, queryString: String) {
        setUpAdapter()
        mainViewModel.getAllFavoriteScanResults(groupBy, queryString)
        if (groupBy == Constant.DATE) {
            saveState(
                Constant.DATE, ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.tick_icon,
                    null
                ), null, null
            )
        } else if (groupBy == Constant.CATEGORY) {
            saveState(
                Constant.CATEGORY, null, ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.tick_icon,
                    null
                ), null
            )
        }
    }

    private fun setUpAdapter() {
        closeLayout()
        binding.rvFavorite.adapter = historyAdapter
        historyAdapter.submitList(null)
        scanResultAdapter.submitList(null)
    }

    private fun closeLayout() {
        scanResultAdapter.closeLayout(scanResultIdIsOpening.toString())
        historyAdapter.closeLayout(scanResultIdIsOpening.toString())
    }

    private fun getFavoriteListOrderByName(queryString: String) {
        setUpAdapter2()
        mainViewModel.getFavoriteListOrderByName(queryString)
        saveState(
            Constant.NAME, null, null, ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.tick_icon,
                null
            )
        )
    }

    private fun setUpAdapter2() {
        historyAdapter.closeLayout(scanResultIdIsOpening.toString())
        binding.rvFavorite.adapter = scanResultAdapter
        historyAdapter.submitList(null)
        scanResultAdapter.submitList(null)
    }

    private fun saveState(
        sortTypeSaved: String,
        drawableDate: Drawable?,
        drawableType: Drawable?,
        drawableName: Drawable?
    ) {
        sharedPreferenceHelper.storeString(Constant.SORT_FAVORITE, sortTypeSaved)
        binding.tvDate.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableDate, null, null, null
        )
        binding.tvType.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableType, null, null, null
        )
        binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableName, null, null, null
        )
        goneFilter()
    }

}
